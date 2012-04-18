package org.lilian.experiment;

import static org.lilian.util.Series.series;

import java.io.IOException;
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

import freemarker.template.Template;

public class RepeatExperiment extends MultiExperiment
{
	private int repeats;
	
	/**
	 * Repeat a single experiment a given number of times
	 * 
	 * @param exp
	 * @param sameSeed use same seed every time (perhaps to test whether code is 
	 * properly deterministic, would normally be false)
	 * @param repeats
	 */
	public RepeatExperiment(Constructor<? extends Experiment> structor, int repeats, Object... inputs)
	{		
		super(structor.getDeclaringClass(), false);
		this.repeats = repeats;
		
		for(int i : series(repeats))
			try
			{
				experiments.add(structor.newInstance(inputs));
			} catch(Exception e) 
			{
				throw new RuntimeException("Experiment ("+type()+") cannot be instantiated with the given parameters ("+Arrays.toString(inputs)+").", e);
			}
	}
	
	@Result(name = "Repeat results")
	public Results results()
	{
		BasicResults results = new BasicResults();
		
		for(Method method : Tools.allMethods(type(), Result.class))
		{
			Result anno = method.getAnnotation(Result.class);
			CollatedResult cres = new CollatedResult(anno);
			
			try
			{
				for(Experiment experiment : experiments)
					cres.add(method.invoke(experiment));
			} catch(Exception e) {
				throw new RuntimeException("Error invoking.", e); // TODO Make nicer
			}
			
			results.add(cres, anno);

		}
		
		return results;
	}
	
	public int repeats()
	{
		return repeats;
	}
	
	private class CollatedResult implements Reporting
	{
		List<Object> values = new ArrayList<Object>();
		Result annotation;
		
		public CollatedResult(Result annotation)
		{
			this.annotation = annotation;
		}
		
		public void add(Object value)
		{
			values.add(value);
		}
		
		@Override
		public String name()
		{
			return annotation.name() + "(over " + repeats() + " trials)";
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
				return fmConfig.getTemplate("repeat.ftl");
			} catch (IOException e)
			{
				throw new RuntimeException("Could not load repeat.tpl template", e);
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
			data.put("raw", values.toString());
				
			return data;
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
			for(Object value : values)
				sum += ((Number) value).doubleValue();
			
			return sum/values.size();
		}
		
		public double standardDeviation()
		{
			double mean = mean();
		
			double varSum = 0.0;
			for(Object value : values)
			{
				double diff = mean - ((Number) value).doubleValue();
				varSum += diff * diff;
			}

			double variance = varSum/(values.size() - 1);
			return Math.sqrt(variance);
		}
		
		public double median()
		{
			List<Double> v = new ArrayList<Double>(values.size());
			
			for(Object value : values)
				v.add(((Number) value).doubleValue());
			
			Collections.sort(v);
			
			if(v.size() % 2 == 1)
				return v.get(v.size()/2); // element s/2+1, but index s/2
			return (v.get(v.size()/2 - 1) + v.get(v.size()/2)) / 2.0;
		}
		
		public Object mode()
		{
			BasicFrequencyModel<Object> model = new BasicFrequencyModel<Object>(values);
			
			return model.sorted().get(0);
		}
	}
}
