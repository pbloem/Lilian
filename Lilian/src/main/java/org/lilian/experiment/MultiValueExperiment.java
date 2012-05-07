package org.lilian.experiment;

import static org.lilian.util.Series.series;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
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
	
	private Constructor<Experiment> structor;

	// * The parameters for which multiple values where defined
	private List<Integer> multiParameterIndices = new ArrayList<Integer>();
	private List<Parameter> multiParameter = new ArrayList<Parameter>();
	private List<List<Object>> paramTuples = new ArrayList<List<Object>>();
	
	public @State int repeats = 1;

	/**
	 * Create different experiments for different inputs
	 * 
	 * @param ctr
	 * @param sameSeed Whether to start each experiment with the same seed, or to give each a new seed
	 * @param inputs
	 */
	public MultiValueExperiment(Constructor<Experiment> ctr, boolean sameSeed, int repeats, Object... inputs)
	{
		
		super(ctr.getDeclaringClass(), sameSeed);
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
		
		createExperiments(inputs, new Object[inputs.length], 0, 0, repeats, new ArrayList<Object>());
		
		this.repeats = repeats;
		this.sameSeed = sameSeed;
	}
	
	private void createExperiments(Object[] master, Object[] current, int i, int multis, int repeats, List<Object> tuple)
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
			for(Object value : ((Run.Multi<?>) master[i]))
			{
				Object[] newCurrent = Arrays.copyOf(current, current.length);
				newCurrent[i] = value;
				
				List<Object> newTuple = new ArrayList<Object>(tuple);
				newTuple.add(value);
				
				createExperiments(master, newCurrent, i+1, multis + 1, repeats, newTuple);
			}
		}
	}

	
	@Result(name = "Multivalue results")
	public Results results()
	{
		BasicResults results = new BasicResults();
		
		for(Method method : Tools.allMethods(type(), Result.class))
		{
			Result anno = method.getAnnotation(Result.class);
			CollatedResult cres = new CollatedResult(anno);
			
			try
			{
				for(int i : series(experiments.size()))
				{
					Experiment experiment = experiments.get(i);
					List<Object> tuple = paramTuples.get(i);
					cres.add(tuple, method.invoke(experiment));
				}
			} catch(Exception e) {
				throw new RuntimeException("Error invoking.", e); // TODO Make nicer
			}
			
			results.add(cres, anno);

		}
		
		return results;
	}
	
	/**
	 * For every @Result of the base experiment, we create one CollatedResult 
	 * containing the results for all the different parameters and various 
	 * methods for handling them
	 * 
	 * 
	 * TODO: remove code replication between this class and the analogous in 
	 * RepeatExperiment
	 *
	 * @author Peter
	 *
	 */
	private class CollatedResult implements Reporting
	{
		List<Object> values = new ArrayList<Object>();
		List<List<Object>> parameters = new ArrayList<List<Object>>();
		Result annotation;
		
		public CollatedResult(Result annotation)
		{
			this.annotation = annotation;
		}
		
		public void add(List<Object> params, Object value)
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
			data.put("names", names);
			
			List<Map<String, Object>> pairs = new ArrayList<Map<String, Object>>();
			for(int i : series(values.size()))
			{
				Map<String, Object> pair = new HashMap<String, Object>();
				pair.put("parameters", stringList(parameters.get(i)));
				pair.put("value", values.get(i).toString());
				pairs.add(pair);
			}
			
			data.put("pairs", pairs);
			
			return data;
		}
		
		public List<String> stringList(List<Object> in)
		{
			List<String> out = new ArrayList<String>(in.size());
			for(Object i : in)
			{
				out.add(i.toString());
			}
			return out;
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
		
		public Object mode()
		{
			BasicFrequencyModel<Object> model = new BasicFrequencyModel<Object>(values);
			
			return model.sorted().get(0);
		}
	}	
	
}
