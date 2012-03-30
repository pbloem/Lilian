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
	private Class<Experiment> experiment;
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
	
	
	/**
	 * 
	 * @param ctr
	 * @param inputs
	 */
	public MultiExperiment(Constructor<Experiment> ctr, Object... inputs)
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
		
		for(Method method : experiment.getDeclaredMethods())
		{
			for(Annotation annotation : method.getDeclaredAnnotations())
				if(annotation instanceof Result)
				{
					resultAnnotations.add((Result) annotation);
					resultMethods.add(method);
				}
		}
		
		createExperiments(inputs, new Object[inputs.length], 0);
	}
	
	private void createExperiments(Object[] master, Object[] current, int i)
	{
		if(master.length == i)
		{
			try
			{
				experiments.add(structor.newInstance(current));
			} catch (Exception e)
			{
				throw new RuntimeException("Failed to create experiment", e);
			}
		} else if(! multiParameterIndices.contains(i))
		{
			current[i] = master[i];
			createExperiments(master, current, i + 1);
		} else 
		{
			for(Object value : ((Run.Multi<?>) master[i]))
			{
				Object[] newCurrent = Arrays.copyOf(current, current.length);
				newCurrent[i] = value;
				createExperiments(master, newCurrent, i+1);
			}
		}
		
	}

	@Override
	protected void body()
	{
		Environment main = Environment.current();
		while(lastFinished < experiments.size())
		{
			int i = lastFinished + 1;
			
			// * set up environment
			File subdir = new File(dir, i + "/");
			subdir.mkdirs();
			Environment sub = new Environment(subdir, out);
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
