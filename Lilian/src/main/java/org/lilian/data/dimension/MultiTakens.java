package org.lilian.data.dimension;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lilian.Global;
import org.lilian.data.real.AbstractGenerator;
import org.lilian.data.real.Datasets;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Functions;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;

/**
 * Variant of the Takens estimator to find multiple characteristic scales in 
 * data. This estimator by itself finds a range in the data over which it best 
 * follows the assumed power law (by the KS test). Repeated application of the 
 * estimator on the left over ranges will find relevant ranges in the data.
 * 
 * @author Peter
 *
 */
public class MultiTakens extends AbstractGenerator<Double>
{
	
	private double maxDistance;
	private double minDistance;
	private double dimension;
	
	
	public MultiTakens(double dimension, double minDistance, double maxDistance)
	{
		super();
		this.maxDistance = maxDistance;
		this.minDistance = minDistance;
		this.dimension = dimension;
	}

	public double minDistance()
	{
		return minDistance;
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
		List<Double> tail = new ArrayList<Double>(n);
		
		for(double distance : distances)
		{
			if(distance >= minDistance && distance < maxDistance)
				tail.add(distance);
			if(sorted && distance >= maxDistance)
				break;
		}
		
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
		if(distance < minDistance || distance >= maxDistance)
			return 0.0;
		
		if(minDistance == 0)
			return (dimension / distance) * pow(distance/maxDistance, dimension);
		
		return dimension * pow(distance/maxDistance, dimension - 1.0);
	}
	
	/**
	 * How many of the distances in the list are contained within this 
	 * distribution's range.
	 */
	public int captures(List<Double> distances)
	{
		int n = 0;
		for(double distance : distances)
			if(contains(distance))
				n++;
		
		return n;
	}
	
	public boolean contains(double d)
	{
		return minDistance <= d && d < maxDistance;
	}
	
	/**
	 * The probability of encountering a distance below the given value.
	 * 
	 * @param distance
	 * @return
	 */
	public double cdf(double distance)
	{
		if(minDistance == 0.0)
			return pow(distance/maxDistance, dimension);
		
		double a = pow(distance/minDistance, dimension) - 1.0;
		double b = pow(maxDistance/minDistance, dimension) - 1.0;
		
		return a / b;
	
	}
	
	public double cdfInv(double probability)
	{
		double ri = pow(maxDistance, dimension),
		       rh = pow(minDistance, dimension);
		
		double base = (ri - rh) * probability + rh;
		return pow(base, 1.0/dimension);
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
			if(datum.doubleValue() < minDistance || datum.doubleValue() >= maxDistance)
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
		return significance(distances, n, -1);
	}
	
	public double significance(List<Double> distances, int n, int samples)
	{
		double threshold = ksTest(distances, false);
	
		int above = 0;
		for(int i : Series.series(n))
		{
			if(i % 1 == 0)
				Global.log().info("* Finished " + i + " trials of "+n+".");
			
			List<Double> generated = generate(distances, distances.size());
			Collections.sort(generated);
		
//			MultiTakens generatedPL = 
//					samples == -1 ?
//					MultiTakens.fit(generated, true).fit() :
//					MultiTakens.fit(generated, true).fitSampled(samples);
			
			MultiTakens generatedPL = 
					MultiTakens.fit(generated, true).fitForwardBackward();
			
			if(generatedPL.ksTest(generated, false) >= threshold)
				above ++;
		}
		
		return above / (double) n;
	}

	public double significance(List<Double> distances, double epsilon)
	{
		return significance(distances, epsilon, -1);
	}	
	
	public double significance(List<Double> distances, double epsilon, int samples)
	{
		return significance(distances, (int)(0.25 * Math.pow(epsilon, -2.0)), samples);
	}	

	public static <P> Fit fit(List<P> data, Distance<? super P> metric)
	{
		return new Fit(distances(data, metric));
	}

	public static <P> Fit fit(List<Double> distances, boolean sorted)
	{
		if(!sorted)
		{
			distances = new ArrayList<Double>(distances);
			Collections.sort(distances);
		}
		
		return new Fit(distances);
	}
	
	public static class Fit
	{
		private static final double EPSILON = 1E-6;
		private static final int MID = 10;
		private List<Double> distances;
		
		public Fit(List<Double> distances)
		{
			// * Distances is sorted
			this.distances = distances;
		}
		
		public MultiTakens fit(double min, double max)
		{			
			double a = max / min;
			
			
			double sum = 0.0, n = 0.0;
			for(double distance : distances)
				if(min <= distance && distance < max)
				{
					n++;
					sum += log(distance);
				}
			
			double dimension;
			
			if(min == 0.0) // * If min = 0 use the single scale MLE
			{
				dimension = n / (n * log(max) - sum);
			} else
			{
				 
				double c = - (n * log(min) - sum) / n;
				
				// * Find a starting range for x
				double top = 1;
				while(f(top, a, c) >= 0)
					top *= 2.0;
				
	//			System.out.println(f(top, a, c));
				
				dimension = minimize(0.0, top, a, c);
				
	//			System.out.println("!!!!!!! " +min + " " + max + " " + dimension + " " + n + " " +a + " "+ c+ " " + f(dimension, a, c) + " " +sum);
				
			}
			
			if(Double.isNaN(dimension))
				return null;
			
			return new MultiTakens(dimension, min, max);
		}	
			
		private double minimize(double rh, double ri, double a, double c)
		{
			double mid = (rh + ri) / 2.0;
			if(ri - rh < EPSILON)
				return mid; 
			
			double f = f(mid, a, c);
			
			// System.out.println("--------" + rh +"_" +f(rh, a, c) + " - " + mid + "_" + f(mid,a,c) + " - " +ri+"_"+ f(ri,a,c));
			
			if(f < 0)
				return minimize(rh, mid, a, c);
			return minimize(mid, ri, a, c);
		}

		private double f(double x, double a, double c)
		{
			return 1.0 / x - (pow(a, x) * log(a))/(pow(a, x) - 1.0) + c;
		}
		
//		public MultiTakens fitSampled(int samples)
//		{
//			// * Find the distance that minimizes the KS value
//			MultiTakens best = null;
//			double bestKS = Double.POSITIVE_INFINITY;	
//			
//			Functions.tic();
//			for(int i : series(samples))
//			{
//				double distance = distances.get(Global.random.nextInt(distances.size()));
//				
//				MultiTakens current = fit(distance);
//				double ksValue = current.ksTest(distances, true);
//				
//				if(ksValue < bestKS)
//				{
//					bestKS = ksValue;
//					best = current;
//				}
//				
//				if(i%200 == 0)
//					Global.log().info("Iteration " + i + " of " + samples + " finished. ("+((100 * i)/samples) +"% after "+Functions.toc()+" seconds)");
//				
//			}
//			
//			return best;
//		}
//
//		public MultiTakens fitSampled(int samplesMaxDepth, int samplesMLE)
//		{
//			// * Find the distance that minimizes the KS value
//			MultiTakens best = null;
//			double bestKS = Double.POSITIVE_INFINITY;	
//			
//			List<Double> dSample = Datasets.sample(distances, samplesMLE);
//			
//			Functions.tic();
//			for(int i : series(samplesMaxDepth))
//			{
//				double distance = distances.get(Global.random.nextInt(distances.size()));
//				
//				MultiTakens current = fit(distance);
//				double ksValue = current.ksTest(dSample, true);
//				
//				if(ksValue < bestKS)
//				{
//					bestKS = ksValue;
//					best = current;
//				}			
//				
//				System.out.print(".");
//			}
//			
//			System.out.println();
//			
//			return best;
//		}
//			
//		public MultiTakens fit(List<Double> candidates)
//		{
//			// * Find the distance that minimizes the KS value
//			MultiTakens best = null;
//			double bestKS = Double.POSITIVE_INFINITY;		
//			
//			Functions.tic();
//			
//			// * We walk through the distances backwards because the 
//			//   last values will take the longest.
//			for(int i : series(candidates.size()-1, -1))
//			{
//				MultiTakens current = fit(candidates.get(i));
//				double ksValue = current.ksTest(this.distances, true);
//				
//				if(ksValue < bestKS)
//				{
//					bestKS = ksValue;
//					best = current;
//				}
//				
//				if(i%50 == 0)
//					Global.log().info("Iteration " + i + " of " + candidates.size() + " finished. ("+((100 * i)/candidates.size()) +"% after "+Functions.toc()+" seconds)");
//			}
//
//			return best;
//		}
		
		public MultiTakens fit(int samples)
		{
			List<Double> candidates = Datasets.sample(distances, samples);
			Set<Double> u = new HashSet<Double>(candidates);
			candidates = new ArrayList<Double>(u);
			
			Collections.sort(candidates);
			
			return fit(candidates);
		}		
		
		public MultiTakens fit()
		{
			return fit(distances);
		}
		
		public MultiTakens fitForwardBackward()
		{
			return fitForwardBackward(distances, 0.0, Double.POSITIVE_INFINITY);
		}
		
		public MultiTakens fitForwardBackward(double lower, double upper)
		{
			return fitForwardBackward(distances, lower, upper);
		}
			
		
		public MultiTakens fit(List<Double> candidates)
		{
//			// * find a set of all unique distances
//			Collection<Double> unique = unique(false);
//				
//			Global.log().info(unique.size()  + " unique distances.");

			// * Find the distance that minimizes the KS value
			MultiTakens best = null;
			double bestKS = Double.POSITIVE_INFINITY;		
			
			Functions.tic();
			
			int n = candidates.size(), c = 0, tot = (n*n - n)/2 - n + 1;
			for(int i : series(candidates.size() - MID))
				for(int j : series(i+1 + MID, candidates.size()))
				{
					MultiTakens current = fit(candidates.get(i), candidates.get(j));
					double ksValue = current.ksTest(this.distances, true);
					
					if(ksValue < bestKS)
					{
						bestKS = ksValue;
						best = current;
					}
					
//					if(c%2000 == 0)
//						Global.log().info("Iteration " + c + " of " + tot + " finished. ("+((100 * c)/tot) +"% after "+Functions.toc()+" seconds)");
					c++;
				}

			return best;
		}
		
		public MultiTakens fitForwardBackward(List<Double> candidates, double lower, double upper)
		{
//			// * find a set of all unique distances
//			Collection<Double> unique = unique(false);
//				
//			Global.log().info(unique.size()  + " unique distances.");

			// * Find the distance that minimizes the KS value
			MultiTakens best = null;
			double bestKS = Double.POSITIVE_INFINITY;		
			
			Functions.tic();
			
			int n = candidates.size(), c = 0;

			for(int i : series(candidates.size()))
			{	
				if(candidates.get(i) > upper)
					break;
				
				if(candidates.get(i) < lower)
					continue;
					
				MultiTakens current = fit(lower, candidates.get(i));
				if(current != null)
				{
					double ksValue = current.ksTest(this.distances, true);
						
					if(ksValue < bestKS)
					{
						bestKS = ksValue;
						best = current;
					}
				}
					
//				if(c % 2000 == 0)
//					Global.log().info("Finding max: Iteration " + c + " of " + candidates.size() + " finished. ("+((100 * c)/candidates.size()) +"% after "+Functions.toc()+" seconds)");
				c++;
			}
			
			if(best == null)
				return null;
			
			double max = best.maxDistance();
			c = 0;
			for(int i : series(candidates.size()))
			{	
				if(distances.get(i) >= max)
					break;
				
				if(candidates.get(i) < lower)
					continue;	
				
				MultiTakens current = fit(candidates.get(i), max);
				if(current != null)
				{
					double ksValue = current.ksTest(this.distances, true);
						
					if(ksValue < bestKS)
					{
						bestKS = ksValue;
						best = current;
					}
				}
					
//				if(c%2000 == 0)
//					Global.log().info("Finding min: Iteration " + c + " of " + candidates.size() + " finished. ("+((100 * c)/candidates.size()) +"% after "+Functions.toc()+" seconds)");
				c++;
			}

			return best;
		}	
		
		public Collection<Double> unique(boolean sort)
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
		
		public List<Double> distances()
		{
			return Collections.unmodifiableList(distances);
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
	

	public static List<MultiTakens> multifit(List<Double> distances, boolean sorted, int maxDepth, double epsilon, double sigThreshold)
	{
		if(! sorted)
		{
			distances = new ArrayList<Double>(distances);
			Collections.sort(distances);
		}
		
		List<MultiTakens> m = multifit(0.0, Double.POSITIVE_INFINITY, distances, maxDepth, epsilon, sigThreshold);
		
		ArrayList<MultiTakens> multi = new ArrayList<MultiTakens>(m.size());
		
		// Retain only significant ranges
		for(MultiTakens dist : m)
			if(dist.significance(distances, epsilon) >= sigThreshold)
				multi.add(dist);
		
		return multi;
	}
	
	private static List<MultiTakens> multifit(double min, double max, List<Double> distances, int depth, double epsilon, double sigThreshold)
	{
		if(depth == 0)
			return new ArrayList<MultiTakens>();
		
		MultiTakens best = fit(distances, true).fitForwardBackward(min, max);
		
		if(best == null)
			return new ArrayList<MultiTakens>();
		
		double sig = best.significance(distances, epsilon);
		System.out.println("Significance for range ["+best.minDistance()+", "+best.maxDistance()+"):"+sig);
//		if(sig < sigThreshold)
//		{
//			List<MultiTakens> res = new ArrayList<MultiTakens>();
//			// res.add(best);
//			return res;
//		}
		
		double maxBelow = best.minDistance(), 
		       minAbove = best.maxDistance();
		
		List<MultiTakens> below = multifit(min, maxBelow, distances, depth - 1, epsilon, sigThreshold);
		List<MultiTakens> above = multifit(minAbove, max, distances, depth - 1, epsilon, sigThreshold);
		
		below.add(best);
		below.addAll(above);
		
		return below;
	}
	
	/**
	 * Generates a dataset that is 'like' the given observed datasets, except 
	 * certain ranges are modeled by MultiTakens distributions. Outside these 
	 * ranges data is bootstrapped from the observed data. The observed data is 
	 * also used to assign prior probabilities to the distributions. 
	 * 
	 * @param distributions
	 * @param observed
	 * @param size
	 * @return
	 */
	public static List<Double> generate(List<MultiTakens> distributions, List<Double> observed, int size)
	{
		// * Weighting of the distributions by points captured by each distribution
		BasicFrequencyModel<Integer> model = new BasicFrequencyModel<Integer>();

		// * Will hold the points that fall outside of any of the modeled ranges 
		List<Double> outside = new ArrayList<Double>(observed.size());
		
		for(double distance : observed)
		{
			boolean added = false;
			for(int i : Series.series(distributions.size()) )
				if(distributions.get(i).contains(distance))
				{
					model.add(i);
					added = true;
				}
			
			if(! added)
				outside.add(distance);
		}
		
		List<Double> result = new ArrayList<Double>(size);
		for(int i : Series.series(size))
		{
			// * Draw a point outside the distribution with probability outside/total
			//   Otherwise draw a point from one of the distributions.
			
			if(Global.random.nextDouble() < outside.size()/(double)observed.size())
				result.add(outside.get(Global.random.nextInt(outside.size())));
			else
				result.add(distributions.get(model.choose()).generate());
		}
		
		Collections.sort(result);
		return result;
	}
}
