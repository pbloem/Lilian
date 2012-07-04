package org.lilian.experiment;

import static org.lilian.util.Series.series;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Series;

import freemarker.template.Template;

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
public class MultiValueExperiment extends MultiExperiment
{
	
	private static final int MAX_LENGTH = 30;

	private Run.Builder structor;

	// * The parameters for which multiple values where defined
	private List<Integer> multiParameterIndices = new ArrayList<Integer>();
	private List<Parameter> multiParameter = new ArrayList<Parameter>();
	private List<List<String>> paramTuples = new ArrayList<List<String>>();
	
	public @State int repeats = 1;

	/**
	 * Create different experiments for different inputs
	 * 
	 * @param ctr
	 * @param sameSeed Whether to start each experiment with the same seed, or to give each a new seed
	 * @param inputs
	 */
	public MultiValueExperiment(Run.Builder ctr, boolean sameSeed, int repeats, Object[] inputs)
	{
		
		super(repeats == 1 ? (Class<? extends Experiment>)ctr.getDeclaringClass() : RepeatExperiment.class, sameSeed);
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
		
		createExperiments(inputs, new Object[inputs.length], 0, 0, repeats, new ArrayList<String>());
		
		this.repeats = repeats;
		this.sameSeed = sameSeed;
	}
	
	private String name(Object input)
	{
		if(input instanceof Map<?, ?>)
			return "" + ((Map<String, ?>)input).get("name");
			
		if(input.toString().length() > MAX_LENGTH)
			return input.toString().substring(0, MAX_LENGTH - 4) + "...";
		
		return input.toString();
	}
	
	private void createExperiments(Object[] master, Object[] current, int i, int multis, int repeats, List<String> tuple)
	{
		if(master.length == i)
		{
			try
			{
				if(repeats == 1)
					experiments.add(structor.newInstance(current));
				else 
					experiments.add(new RepeatExperiment(structor, repeats, current));
				
				paramTuples.add(tuple);
					
			} catch (Exception e)
			{
				throw new RuntimeException("Failed to create experiment", e);
			}
		} else if(! multiParameterIndices.contains(i))
		{
			current[i] = master[i];
			createExperiments(master, current, i + 1, multis, repeats, tuple);
		} else 
		{
			int j = 0;
			for(Object value : ((Run.Multi<?>) master[i]))
			{
				Object[] newCurrent = Arrays.copyOf(current, current.length);
				newCurrent[i] = value;
				
				List<String> newTuple = new ArrayList<String>(tuple);
				newTuple.add(((Run.Multi<?>)master[i]).name(j));
				
				createExperiments(master, newCurrent, i+1, multis + 1, repeats, newTuple);
				j++;
			}
		}
	}

	
	@Result(name = "Multivalue results")
	public Results results() throws IllegalAccessException, InvocationTargetException
	{
		BasicResults results = new BasicResults();
		
		for(Method method : Tools.allMethods(type(), Result.class))
			addResult(method, results);
			
		return results;
	}
	
	private void addResult(Method method, BasicResults results) throws IllegalAccessException, InvocationTargetException
	{
		// If method returns a Results object
		if(Results.class.isAssignableFrom(method.getReturnType()))
		{
			Map<Result, CollatedResult> collatedResults = new HashMap<Result, CollatedResult>();
			for(int j : series(experiments.size()))
			{	
				
				Experiment experiment = experiments.get(j);
				List<String> tuple = paramTuples.get(j);
				
				Results res = (Results) method.invoke(experiment);

				for(int i : Series.series(res.size()))
				{
					Result anno = res.annotation(i);
					Object value = res.value(i);
					
					if(! collatedResults.containsKey(anno))
						collatedResults.put(anno, new CollatedResult(anno));
					
					collatedResults.get(anno).add(tuple, res.value(i));
				}
			}
			
			for(Result anno : collatedResults.keySet())
				results.add(collatedResults.get(anno), anno);
	
			
		} else // If method returns a regular value
		{
				
			Result anno = method.getAnnotation(Result.class);
			CollatedResult cres = new CollatedResult(anno);
			
			for(int i : series(experiments.size()))
			{
				Experiment experiment = experiments.get(i);
				List<String> tuple = paramTuples.get(i);
				cres.add(tuple, method.invoke(experiment));
			}

			results.add(cres, anno);
		}
	}
	
	/**
	 * For every @Result of the base experiment, we create one CollatedResult 
	 * containing the results for all the different parameters and various 
	 * methods for handling them
	 * 
	 * TODO: remove code replication between this class and the analogous in 
	 * RepeatExperiment
	 *
	 * @author Peter
	 *
	 */
	private class CollatedResult implements Reporting, HasResults
	{
		List<Object> values = new ArrayList<Object>();
		List<List<String>> parameters = new ArrayList<List<String>>();
		Result annotation;
		
		public CollatedResult(Result annotation)
		{
			this.annotation = annotation;
		}
		
		public void add(List<String> params, Object value)
		{
			parameters.add(params);
			
			values.add(value);
		}
		
		@Override
		public String name()
		{
			return annotation.name() + "(for various parameters)";
		}

		@Override
		public String description()
		{
			return annotation.description();
		}

		@Override
		public Template template()
		{
			try
			{
				return fmConfig.getTemplate("multi.ftl");
			} catch (IOException e)
			{
				throw new RuntimeException("Could not load multi.tpl template", e);
			}
		}

		@Override
		public Object data()
		{
			Map<String, Object> data = new HashMap<String, Object>();
			
			
			boolean num = isNumeric();
			boolean res = hasResults();
			
			if(res)
			{
				Class<?> resClass = values.get(0).getClass();
				
				List<String> names = new ArrayList<String>();
				for(Parameter p : multiParameter)
					names.add(p.name());
				for(Method method : Tools.allMethods(resClass, Result.class))
					names.add(method.getAnnotation(Result.class).name());
					
				data.put("row_headers", names);
				
				List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
				for(int i : series(values.size()))
				{
					Map<String, Object> row = new HashMap<String, Object>();
					row.put("parameters", parameters.get(i));
										
					// * Find all @Result methods in the HasResults object
					List<Object> outputs = new ArrayList<Object>();
					for(Method method : Tools.allMethods(resClass, Result.class))
						try
						{
							outputs.add(method.invoke(values.get(i)));
						} catch (Exception e)
						{
							throw new RuntimeException(e);
						} 
					row.put("values", outputs);
					rows.add(row);
				}
				
				data.put("rows", rows);				
				
			} else
			{
				data.put("mean", num ? mean() : "Data is not numeric");		
				data.put("std_dev", num ? standardDeviation() : "Data is not numeric");
				data.put("median", num ? median() : "Data is not numeric");
				data.put("mode", mode().toString());
				data.put("infs", num ? infs() : "Data is not numeric");
				data.put("nans", num ? nans() : "Data is not numeric");
				
				List<String> names = new ArrayList<String>();
				for(Parameter p : multiParameter)
					names.add(p.name());
				names.add(annotation.name());
				data.put("row_headers", names);
				
				List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
				for(int i : series(values.size()))
				{
					Map<String, Object> row = new HashMap<String, Object>();
					row.put("parameters", parameters.get(i));

					String vStr = values.get(i).toString();
					row.put("values", Arrays.asList(vStr.length() > MAX_LENGTH ? vStr.substring(0, MAX_LENGTH-4) + "..." : vStr));
					rows.add(row);
				}
				
				data.put("rows", rows);
				
				data.put("id", Tools.cssSafe(name()));
			}
			
			return data;
		}
	
		
		/**
		 * Whether all result values represent a HasResults objects
		 * @return
		 */
		public boolean hasResults()
		{
			for(Object value : values)
				if(! (value instanceof HasResults))
					return false;
			return true;
		}
		
		/**
		 * Whether all result values represent numbers
		 * @return
		 */
		public boolean isNumeric()
		{
			for(Object value : values)
				if(! (value instanceof Number))
					return false;
			return true;
		}
		
		@Result(name="mean")
		public double mean()
		{
			double sum = 0.0;
			double num = 0.0;
			
			for(Object value : values)
			{
				double v = ((Number) value).doubleValue();
				if(!(Double.isNaN(v) || Double.isNaN(v)))
				{
					sum += v; 
					num ++;
				}
			}
			
			return sum/num;
		}
		
		@Result(name="nans")
		public int nans()
		{
			int num = 0;
			
			for(Object value : values)
			{
				double v = ((Number) value).doubleValue();
				if(Double.isNaN(v))
					num ++;
			}
			
			return num;	
		}
		
		@Result(name="infs")
		public int infs()
		{
			int num = 0;
			
			for(Object value : values)
			{
				double v = ((Number) value).doubleValue();
				if(Double.isInfinite(v))
					num ++;
			}
			
			return num;	
		}
		
		@Result(name="std dev")
		public double standardDeviation()
		{
			double mean = mean();
			double num = 0.0;
			
			double varSum = 0.0;
			for(Object value : values)
			{
				double v = ((Number) value).doubleValue();
				
				if(!(Double.isNaN(v) || Double.isNaN(v)))
				{
					double diff = mean - v;
					varSum += diff * diff;
					num++;
				}
			}

			double variance = varSum/(num - 1);
			return Math.sqrt(variance);
		}
		
		@Result(name="median")
		public double median()
		{
			List<Double> vs = new ArrayList<Double>(values.size());
			
			for(Object value : values)
			{
				double v = ((Number) value).doubleValue();

				if(!(Double.isNaN(v) || Double.isNaN(v)))
				{
					vs.add(v);
				}
			}
			
			Collections.sort(vs);
			
			if(vs.size() % 2 == 1)
				return vs.get(vs.size()/2); // element s/2+1, but index s/2
			return (vs.get(vs.size()/2 - 1) + vs.get(vs.size()/2)) / 2.0;
		}
		
		@Result(name="mode")
		public Object mode()
		{
			BasicFrequencyModel<Object> model = new BasicFrequencyModel<Object>(values);
			
			return model.sorted().get(0);
		}
	}	
	
}
