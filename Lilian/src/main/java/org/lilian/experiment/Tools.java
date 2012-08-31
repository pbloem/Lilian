package org.lilian.experiment;

import static org.lilian.experiment.Tools.isNumeric;
import static org.lilian.util.Series.series;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Series;

/**
 * Utility functions, mostly for reflection
 * @author Peter
 *
 */
public class Tools
{
	public static <T> List<String> stringList(List<T> in)
	{
		List<String> out = new ArrayList<String>(in.size());
		for(Object i : in)
		{
			out.add(i.toString());
		}
		return out;
	}
	
	/**
	 * Returns all declared methods, including inherited.
	 */
	public static List<Method> allMethods(Class<?> clss)
	{
		List<Method> list =  new ArrayList<Method>();
		allMethods(clss, list, null);
		return list;
	}
	
	/**
	 * Returns all declared methods, including inherited, with the given annotation.
	 * 
	 * @param clss
	 * @param annotationType
	 * @return
	 */
	public static List<Method> allMethods(Class<?> clss, Class<? extends Annotation> annotationType)
	{
		List<Method> list =  new ArrayList<Method>();
		allMethods(clss, list, annotationType);
		return list;
	}
	
	private static void allMethods(Class<?> clss, List<Method> list, Class<? extends Annotation> annotationType)
	{
		if(clss == null)
			return;
		
		for(Method method : clss.getDeclaredMethods())
			if(annotationType == null)
				list.add(method);
			else
				for(Annotation ann : method.getAnnotations())
					if(annotationType.isAssignableFrom(ann.getClass()))
					{
						list.add(method);
						break;
					}	
					
		allMethods(clss.getSuperclass(), list, annotationType);
	}
	
	/**
	 * Whether all result values represent numbers
	 * @return
	 */
	public static boolean isNumeric(List<?> values)
	{
		for(Object value : values)
			if(! (value instanceof Number))
				return false;
		return true;
	}

	public static double mean(List<? extends Number> values)
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
	
	public static double standardDeviation(List<? extends Number> values)
	{
		double mean = mean(values);
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
	
	public static double median(List<? extends Number> values)
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
		
		if(vs.isEmpty())
			return -1.0;
		
		Collections.sort(vs);
		
		if(vs.size() % 2 == 1)
			return vs.get(vs.size()/2); // element s/2+1, but index s/2
		return (vs.get(vs.size()/2 - 1) + vs.get(vs.size()/2)) / 2.0;
	}
	
	public static <T> T mode(List<T> values)
	{
		BasicFrequencyModel<T> model = new BasicFrequencyModel<T>(values);
		
		return model.sorted().get(0);
	}
	
	public static String cssSafe(String in) {
		String out = in;
		
		out = out.toLowerCase().trim(); 
	    out = out.replaceAll("\\s+", "-");
	    out = out.replaceAll("[^a-z0-9\\-]", ""); // remove weird characters
	    
	    return out;
	}

	public static boolean tabular(List<?> values)
	{	
		for(Object value : values)
			if(! (value instanceof Collection<?>))
				return false;
		return true;
	}
	
	public static int tableWidth(List<?> table)
	{
		int width = 0; 
		for(Object row : table)
			width = Math.max( width, ((Collection<?>)row).size() );
	
		return width;
		
	}

	public static double min(List<? extends Number> values)
	{
	
		double min = Double.POSITIVE_INFINITY;
		
		for(Object value : values)
		{
			double v = ((Number) value).doubleValue();
			if(!(Double.isNaN(v) || Double.isNaN(v)))
				min = Math.min(min, v);
		}
		
		return min;
	}
	

	public static double max(List<? extends Number> values)
	{
	
		double max = Double.NEGATIVE_INFINITY;
		
		for(Object value : values)
		{
			double v = ((Number) value).doubleValue();
			if(!(Double.isNaN(v) || Double.isNaN(v)))
				max = Math.max(max, v);
		}
		
		return max;
	}	
	
	public static <T> List<List<T>> combine(List<T>... lists)
	{
		List<List<T>> table = new ArrayList<List<T>>();
		for(int i : series(lists[0].size()))
		{
			ArrayList<T> row = new ArrayList<T>(lists.length);
			for(List<T> list : lists)
				row.add(list.get(i));
			table.add(row);
		}
		
		return table;
	}
}
