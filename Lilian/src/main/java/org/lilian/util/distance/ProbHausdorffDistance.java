package org.lilian.util.distance;

import java.util.*;

public class ProbHausdorffDistance<T> implements Distance<List<T>>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2671978167769861893L;
	private Distance<T> distance = new NaturalDistance();
	
	/**
	 * Constructs a hausdorff distance object. When using this constructor,
	 * T is assumed to be metrizable. ( This is not caught by generics, so be 
	 * careful).
	 * 
	 */
	public ProbHausdorffDistance()
	{}

	/**
	 * constructor for situations where T is not Metrizable. 
	 * 
	 */
	public ProbHausdorffDistance(Distance<T> distance)
	{
		this.distance = distance;
	}
	
	public double distance(List<T> a, List<T> b) {
		return Math.max(
				directedHausdorff(a, b, distance), 
				directedHausdorff(b, a, distance));

	}

	public static <T extends Metrizable<T>> double hausdorff(List<T> a, List<T> b)
	{
		return hausdorff(a, b, new NaturalDistance<T>());
	}
	
	
	public static <T> double hausdorff(List<T> a, List<T> b, Distance<T> distance)
	{
		return Math.max( directedHausdorff(a, b, distance), 
		                 directedHausdorff(b, a, distance));
	}

	/**
	 * Calculates the directed hausdorff distance between two point sets.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T extends Metrizable<T>> double directedHausdorff(
			List<T> a, 
			List<T> b)
	{
		return directedHausdorff(a, b, new NaturalDistance<T>());
	}
		
	public static <T> double directedHausdorff(
		List<T> a, List<T> b, Distance<T> dist)
	{		
		double  max = Double.NEGATIVE_INFINITY, 
				min;

		for(T pointA : a)
		{
			min = Double.POSITIVE_INFINITY;
			for(T pointB : b)
				min = Math.min(dist.distance(pointA, pointB), min);
			
			max = Math.max(min, max);
		}
		
		return max;
	}
}
