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
 * Runs multiple copies of the same experiment (possibly with different parameters)
 * @author Peter
 *
 */
public abstract class MultiExperiment extends AbstractExperiment
{	
	protected Class<? extends Experiment> type;
	
	// * The experiments to perform
	protected List<Experiment> experiments = new ArrayList<Experiment>();

	
	// * The methods for getting results
	private List<Method> resultMethods = new ArrayList<Method>();
	private List<Result> resultAnnotations = new ArrayList<Result>();
	
	/**
	 * State information
	 */
	public @State int lastFinished;
	public @State List<List<Object>> results;
	public @State boolean sameSeed;
	
	public MultiExperiment(Class<? extends Experiment> type, boolean sameSeed)
	{
		this.type = type;
		this.sameSeed = sameSeed;
	}
	
//	public MultiExperiment(Class<? extends Experiment> experiment, boolean sameSeed)
//	{
//		this.experiment = experiment;
//		this.sameSeed = sameSeed;
//		
//		findResultMethods();
//	}
	
	public void add(Experiment exp)
	{
		
	}
	
	/**
	 * Returns the type of experiment
	 * 
	 * @return
	 */
	public Class<? extends Experiment> type()
	{
		return type;
	}
	
	private void findResultMethods()
	{
		for(Method method : type.getDeclaredMethods())
		{
			for(Annotation annotation : method.getDeclaredAnnotations())
				if(annotation instanceof Result)
				{
					resultAnnotations.add((Result) annotation);
					resultMethods.add(method);
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
	
	public void setDescription(String description)
	{
		for(Experiment experiment: experiments)
			experiment.setDescription(description);
		
		super.setDescription(description); 
	}
}
