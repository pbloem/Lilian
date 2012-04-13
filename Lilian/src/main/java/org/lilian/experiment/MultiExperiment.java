package org.lilian.experiment;

import static org.lilian.util.Series.series;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lilian.Global;
import org.lilian.util.Series;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Runs several independent experiments sequentially, each in its own 
 * environment and directory inside the directory of the MultiExperiment.
 * 
 * The MultiExperiment will detect which experiments inherit from the same class
 * and collate their results if they do so.
 * 
 * For numerical results, it will compute the average, median, mode, variance, etc
 * 
 * For each parameter varying between experiments, the results will be plotted
 * as appropriate.
 * 
 * 
 * @author Peter
 *
 */
public class MultiExperiment extends AbstractExperiment
{
	private Class<? extends Experiment> experiment;
	private Constructor<Experiment> structor;
	
	// * The experiments to perform
	private List<Experiment> experiments = new ArrayList<Experiment>();
	// * The parameters for which multiple values where defined
	private List<Integer> multiParameterIndices = new ArrayList<Integer>();
	private List<Parameter> multiParameter = new ArrayList<Parameter>();
	
	// * The methods for getting results
	private List<Method> resultMethods = new ArrayList<Method>();
	private List<Result> resultAnnotations = new ArrayList<Result>();
	
	/**
	 * State information
	 */
	public @State int lastFinished;
	public @State List<List<Object>> results;
	public @State boolean sameSeed = true;

	/**
	 * Repeat a single experiment a given number of times
	 * 
	 * @param exp
	 * @param sameSeed use same seed every time (perhaps to test whether code is 
	 * properly deterministic, would normally be false)
	 * @param repeats
	 */
	public MultiExperiment(Experiment exp, boolean sameSeed, int repeats)
	{
		experiment = exp.getClass();
		
		findResultMethods();
		
		for(int i : series(repeats))
			experiments.add(exp.clone());
		
		this.sameSeed = sameSeed;
	}
	
	/**
	 * Create different experiments for different inputs
	 * 
	 * @param ctr
	 * @param sameSeed Whether to start each experiment with the same seed, or to give ach a new seed
	 * @param inputs
	 */
	public MultiExperiment(Constructor<Experiment> ctr, boolean sameSeed, int repeats, Object... inputs)
	{
		experiment = ctr.getDeclaringClass();
		structor = ctr;
		
		for(int i : series(inputs.length))
		{
			if(inputs[i] instanceof Run.Multi<?>)
			{
				multiParameterIndices.add(i);
				for(Annotation annotation : ctr.getParameterAnnotations()[i])
					if(annotation instanceof Parameter)
						multiParameter.add((Parameter) annotation);
			}
		}
		
		findResultMethods();
		
		createExperiments(inputs, new Object[inputs.length], 0, repeats);
		
		this.sameSeed = sameSeed;
	}
	
	private void findResultMethods()
	{
		for(Method method : experiment.getDeclaredMethods())
		{
			for(Annotation annotation : method.getDeclaredAnnotations())
				if(annotation instanceof Result)
				{
					resultAnnotations.add((Result) annotation);
					resultMethods.add(method);
				}
		}
	}

	private void createExperiments(Object[] master, Object[] current, int i, int repeats)
	{
		if(master.length == i)
		{
			try
			{
				if(repeats == 1)
					experiments.add(structor.newInstance(current));
				else 
					experiments.add(new MultiExperiment(structor.newInstance(current), false, repeats));
			} catch (Exception e)
			{
				throw new RuntimeException("Failed to create experiment", e);
			}
		} else if(! multiParameterIndices.contains(i))
		{
			current[i] = master[i];
			createExperiments(master, current, i + 1, repeats);
		} else 
		{
			for(Object value : ((Run.Multi<?>) master[i]))
			{
				Object[] newCurrent = Arrays.copyOf(current, current.length);
				newCurrent[i] = value;
				createExperiments(master, newCurrent, i+1, repeats);
			}
		}
	}

	@Override
	protected void body()
	{
		Environment main = Environment.current();
		while(lastFinished < experiments.size() - 1)
		{
			int i = lastFinished + 1;
			
			// * set up environment
			File subdir = new File(dir, i + "/");
			subdir.mkdirs();
			
			long subSeed = Environment.current().seed();
			Environment sub = new Environment(subdir, sameSeed ? subSeed : Global.random.nextLong());
			Environment.current = sub;
			
			// * Run experiment
			experiments.get(i).run();
			
			// * Record results
			results.add(new ArrayList<Object>());
			for(int m : series(resultMethods.size()))
			{
				try
				{
					results.get(i).add(resultMethods.get(m).invoke(experiments.get(i)));
				} catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}
			
			try
			{
				writeResults();
			} catch (IOException e)
			{
				throw new RuntimeException("Problem writing results", e);
			}
			
			// * Save the state
			lastFinished ++;
			save();
		}
		Environment.current = main;
	}

	private void writeResults() throws IOException
	{
		File outFile = new File(dir, "results.all.csv");
		
		CSVWriter out = new CSVWriter(new BufferedWriter(new FileWriter(outFile)));
		
		for(List<Object> line : results)
		{
			String[] strings = new String[line.size()];
			for(int i : Series.series(line.size()))
					strings[i] = line.get(i).toString();
			out.writeNext(strings);
		}
		
		out.flush();
		out.close();
	}

	@Override
	protected void setup()
	{
		lastFinished = -1;
		results = new ArrayList<List<Object>>();
	}

	public int size()
	{
		return experiments.size();
	}
	
	
}
