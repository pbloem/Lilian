package org.lilian.data.dimension;

import static java.lang.Math.log;
import static java.util.Collections.reverseOrder;

import java.util.*;
import java.io.*;

import org.lilian.util.distance.Distance;

public class CorrelationIntegral implements Serializable {

	private static final long serialVersionUID = -6751262871445740509L;
	
	// * Total number of pairs observed
	private int total = 0;
	// * The radii for which to compute the integral
	private List<Double> distances  = new ArrayList<Double>();
	// * The counts for each radius
	private List<Double> counts = new ArrayList<Double>();
	
	private CorrelationIntegral(List<Double> distances)
	{
		this.distances.addAll(distances);
		Collections.sort(this.distances, reverseOrder());
		
		for(int i = 0; i < distances.size(); i++)
			counts.add(0.0);
	}
	
	/**
	 * Increase the count at the given depth
	 * 
	 * @param depth
	 */
	public <Q> void observe(Q a, Q b, Distance<Q> distance)
	{
		total ++;
		double d = distance.distance(a, b);
		
		observe(d);
	}
	
	public void observe(double distance)
	{
		for(int i = 0; i < distances.size(); i++)
			if(distance < distances.get(i))
				counts.set(i, counts.get(i) + 1.0);
			else
				break;
	}
	
	public List<Double> distances()
	{
		return Collections.unmodifiableList(distances);
	}
	
	public List<Double> counts()
	{
		return Collections.unmodifiableList(counts);
	}
	
	public int total()
	{
		return total;
	}
	
	public static <T> CorrelationIntegral fromDataset(
			List<T> data, double step, double max, Distance<T> distance)
	{
		List<Double> distances = Takens.distances(data, distance);
		
		return fromDistances(distances, step, max);
	}
	
	public static CorrelationIntegral fromDistances( 
			List<Double> distances, double step, double max)
	{
		List<Double> radii = new ArrayList<Double>();
		
		for(double r = Math.log10(step); r <= Math.log10(max); r += step)
			radii.add(Math.pow(10.0, r));
				
		return fromDistances(distances, radii);
	}	
	
	public static CorrelationIntegral fromDistances(
			List<Double> distances, List<Double> radii)
	{
		CorrelationIntegral cint = new CorrelationIntegral(radii);
		
		for(double distance : distances)
			cint.observe(distance);

		return cint;
	}
	
	/**
	 * The old way of calculating the correlation integral that doesn't account 
	 * for bias in estimating p(B_e(x)) from data.
	 * 
	 * @param <T>
	 * @param data
	 * @param radii
	 * @param distance
	 * @return
	 */
	public static <T> CorrelationIntegral fromDatasetOld(
			List<T> data, double step, double max, Distance<T> distance)
	{
		return fromDatasetOld(data, step, 1.0, max, distance);
	}
	
	public static <T> CorrelationIntegral fromDatasetOld(
			List<T> data, double step, double weight, double max, Distance<T> distance)
	{
		List<Double> radii = new ArrayList<Double>();
		
		for(double r = step; r <= 1.0; r += step)
			radii.add(Math.pow(r, weight) * max);
		
		return fromDatasetOld(data, radii, distance);
	}	
	
	public static <T> CorrelationIntegral fromDatasetOld(
			List<T> data, List<Double> radii, Distance<T> distance)
	{
		CorrelationIntegral cint = new CorrelationIntegral(radii);
		
		for(int i = 0; i < data.size(); i++)
			for(int j = 0; j < data.size(); j++)
					cint.observe(data.get(i), data.get(j), distance);
		
		return cint;
	}	

}
