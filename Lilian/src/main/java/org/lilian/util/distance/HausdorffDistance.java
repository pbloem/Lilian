package org.lilian.util.distance;

import java.util.*;

public class HausdorffDistance<T> implements Distance<List<T>> 
{
	
	private static final long serialVersionUID = -2445971333646667214L;
	private double[][] distMatrix;
	private int aSize =-1;
	private int bSize = -1;
	
	private Distance<T> distance = new NaturalDistance();
	
	/**
	 * Constructs a hausdorff distance object. When using this constructor,
	 * T is assumed to be metrizable. ( This is not caught by generics, so be 
	 * careful).
	 * 
	 */
	public HausdorffDistance()
	{}

	/**
	 * constructor for situations where T is not Metrizable. 
	 * 
	 */
	public HausdorffDistance(Distance<T> distance)
	{
		this.distance = distance;
	}
	
	public double distance(List<T> a, List<T> b) {
		
		createDistMatrix(a.size(), b.size());
		
		for(int i = 0; i < a.size(); i++)
			for(int j = 0; j < b.size(); j++)
				distMatrix[i][j] = distance.distance(a.get(i), b.get(j));

		double abMax = Double.NEGATIVE_INFINITY, abMin; 
		for(int i = 0; i < a.size(); i++)
		{
			abMin = Double.POSITIVE_INFINITY;
			for(int j = 0; j < b.size(); j++)
				abMin = Math.min(abMin, distMatrix[i][j]);
			
			abMax = Math.max(abMax, abMin);
		}
		
		double baMax = Double.NEGATIVE_INFINITY, baMin; 
		for(int i = 0; i < b.size(); i++)
		{
			baMin = Double.POSITIVE_INFINITY;
			for(int j = 0; j < a.size(); j++)
				baMin = Math.min(baMin, distMatrix[j][i]);
			
			baMax = Math.max(baMax, baMin);
		}
		
		return Math.max(abMax, baMax);
	}

	private void createDistMatrix(int newASize, int newBSize) {
		if(newASize == aSize && newBSize == bSize)
			return;
			
		distMatrix = new double[newASize][];
		
		for(int i = 0; i < newASize; i++)
			distMatrix[i] = new double[newBSize];
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
