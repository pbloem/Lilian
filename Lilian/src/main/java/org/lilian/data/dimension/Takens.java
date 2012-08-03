package org.lilian.data.dimension;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lilian.Global;
import org.lilian.data.real.AbstractGenerator;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;

public class Takens extends AbstractGenerator<Double>
{
	
	private double maxDistance;
	private double dimension;
	
	
	public Takens(double dimension, double maxDistance)
	{
		super();
		this.maxDistance = maxDistance;
		this.dimension = dimension;
	}

	public double maxDistance()
	{
		return maxDistance;
	}

	public double dimension()
	{
		return dimension;
	}
	
	/**
	 * Performs the KS test with this distribution and the distances over all 
	 * pairs of points in the given dataset.
	 * 
	 * @param points
	 * @return
	 */
	public double ksTest(List<Double> distances, boolean sorted)
	{
		int n = distances.size();
		List<Double> tail = new ArrayList<Double>((n*n-n)/2);
		
		for(double distance : distances)
			if(distance < maxDistance)
				tail.add(distance);
			else // * if the list is sorted, we can break here
				if(sorted)
					break;
		
		if(tail.size() == 0)
			return 1.0;
		
		if(! sorted)
			Collections.sort(tail);
		
		double max = Double.NEGATIVE_INFINITY;
		
		for(int i : series(tail.size()))
		{
			Double x = tail.get(i);
			
			double dataCDF = (i + 1) / (double) tail.size();
			double plCDF = cdf(x);
			
			double diff = Math.abs(dataCDF - plCDF);
			max = Math.max(diff, max);
		}
		
		return max;
	}
	
	/**
	 * The probability of encountering the given distance
	 * 
	 * @param distance
	 * @return
	 */
	public double p(double distance)
	{
		if(distance > maxDistance)
			return 0.0;
		
		return dimension * pow(distance/maxDistance, dimension - 1.0);
	}
	
	/**
	 * The probability of encountering a distance below the given value.
	 * 
	 * @param distance
	 * @return
	 */
	public double cdf(double distance)
	{
		return pow(distance/maxDistance, dimension);
	}
	
	public double cdfInv(double probability)
	{
		return maxDistance * pow(probability, 1.0 / dimension); 
	}
	
	@Override
	public Double generate()
	{
		double u = Global.random.nextDouble();
		
		return cdfInv(u);
	}
	
	public List<Double> generate(Collection<Double> observed, int number)
	{
		int n = observed.size();
		List<Double> head = new ArrayList<Double>(observed.size());
		
		for(Double datum : observed)
			if(datum.doubleValue() > maxDistance)
				head.add(datum);
		
		List<Double> result = new ArrayList<Double>(number);
		for(int i : Series.series(number))
			if(Global.random.nextDouble() < head.size() / (double)n)
				result.add(head.get(Global.random.nextInt(head.size())));
			else 
				result.add(generate());
		
		return result;
	}	
	
	
	public double significance(List<Double> distances, int n)
	{
		double threshold = ksTest(distances, false);
	
		int above = 0;
		for(int i : Series.series(n))
		{
			if(i % 500 == 0 && i != 0)
				Global.log().info("* finished " + i + " trials of "+n+".");
			
			List<Double> generated = generate(distances, distances.size());
		
			Takens generatedPL = Takens.fit(generated).fit();
			
			if(generatedPL.ksTest(generated, false) >= threshold)
				above ++;
		}
		
		return above / (double) n;
	}

	public double significance(List<Double> distances, double epsilon)
	{
		return significance(distances, (int)(0.25 * Math.pow(epsilon, -2.0)));
	}	

	public static <P> Fit fit(List<P> data, Distance<? super P> metric)
	{
		return new Fit(distances(data, metric));
	}

	public static <P> Fit fit(List<Double> distances)
	{
		return new Fit(distances);
	}
	
	public static class Fit
	{
		private List<Double> distances;
		
		public Fit(List<Double> distances)
		{
			this.distances = distances;
		}
		
		public Takens fit(double maxDistance)
		{
			double sum = 0.0, n = 0.0;
			for(double distance : distances)
				if(distance < maxDistance)
				{ 
					n++;
					sum += log( distance / maxDistance);
				}
			
			double dimension = - n / sum;
			return new Takens(dimension, maxDistance);
		}
		
		
		public Takens fitSampled(int samples)
		{
			// * Find the distance that minimizes the KS value
			Takens best = null;
			double bestKS = Double.POSITIVE_INFINITY;	
			
			for(int i : series(samples))
			{
				double distance = distances.get(Global.random.nextInt(distances.size()));
				
				Takens current = fit(distance);
				double ksValue = current.ksTest(distances, true);
				
				if(ksValue < bestKS)
				{
					bestKS = ksValue;
					best = current;
				}
			}
			
			return best;
		}
		
		public Takens fit()
		{
//			// * find a set of all unique distances
//			Collection<Double> unique = unique(false);
//				
//			Global.log().info(unique.size()  + " unique distances.");

			// * Find the distance that minimizes the KS value
			Takens best = null;
			double bestKS = Double.POSITIVE_INFINITY;		
			
			int i = 0;
			for(double distance : distances)
			{
				i++;
				
				Takens current = fit(distance);
				double ksValue = current.ksTest(this.distances, true);
				
				if(ksValue < bestKS)
				{
					bestKS = ksValue;
					best = current;
				}
				
				if(i%200 == 0)
					Global.log().info("Iteration " + i + " of " + distances.size() + " finished.");
			}

			return best;
		}
		
		public Collection<Double> unique( boolean sort)
		{
			Set<Double> unique = new LinkedHashSet<Double>();
					
			for(double distance : distances)
				unique.add(distance);
			
			if(!sort)
				return unique;
			
			List<Double> distancesSorted = new ArrayList<Double>(unique);
			Collections.sort(distancesSorted);
			
			return distancesSorted;
		}
	}
	
	public static <P> List<Double> distances(List<P> points, Distance<? super P> metric)
	{
		List<Double> distances = new ArrayList<Double>();
		
		for(int i : series(points.size()))
			for(int j : series(i+1, points.size()))
			{
				double distance = metric.distance(points.get(i), points.get(j));
				distances.add(distance);
			}	
		
		Collections.sort(distances);
		return distances;
	}
	

}
