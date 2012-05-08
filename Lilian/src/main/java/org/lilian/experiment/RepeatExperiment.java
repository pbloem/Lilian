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
	
	private class CollatedResult implements Reporting, HasResults
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
			data.put("infs", num ? infs() : "Data is not numeric");
			data.put("nans", num ? nans() : "Data is not numeric");
				
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
		
		@Result(name = "mean")
		public double mean()
		{
			if(! isNumeric())
				return Double.NaN;
			
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
		
		@Result(name = "std dev")
		public double standardDeviation()
		{
			if(! isNumeric())
				return Double.NaN;
			
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
		
		@Result(name = "median")
		public double median()
		{
			if(! isNumeric())
				return Double.NaN;
			
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
		
		@Result(name = "mode")
		public Object mode()
		{
			BasicFrequencyModel<Object> model = new BasicFrequencyModel<Object>(values);
			
			return model.sorted().get(0);
		}
		
		
		@Result(name = "nans")
		public int nans()
		{
			if(! isNumeric())
				return 0;
			
			int num = 0;
			
			for(Object value : values)
			{
				double v = ((Number) value).doubleValue();
				if(Double.isNaN(v))
					num ++;
			}
			
			return num;	
		}
		
		@Result(name = "infs")
		public int infs()
		{
			if(! isNumeric())
				return 0;

			int num = 0;
			
			for(Object value : values)
			{
				double v = ((Number) value).doubleValue();
				if(Double.isInfinite(v))
					num ++;
			}
			
			return num;	
		}		
	}
}
