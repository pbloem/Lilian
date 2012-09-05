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
import org.lilian.data.real.Datasets;
import org.lilian.util.Functions;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.EuclideanDistance;

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
		List<Double> tail = new ArrayList<Double>(n);
		
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
		
		return 
			(dimension / distance) * 
			pow(distance/maxDistance, dimension);
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
		return significance(distances, n, -1, -1);
	}
	
	public double significance(List<Double> distances, int n, int numCandidates, int samplesPerCandidate)
	{
		double threshold = ksTest(distances, false);
	
		int above = 0;
		for(int i : Series.series(n))
		{
			if(i % 1 == 0)
				Global.log().info("* Finished " + i + " trials of "+n+".");
			
			System.out.print(".");
			List<Double> generated = generate(distances, distances.size());
			Collections.sort(generated);
			System.out.print(".");
		
			Takens generatedPL = 
					numCandidates == -1 ?
					Takens.fit(generated, true).fit() :
					Takens.fit(generated, true).fit(numCandidates, samplesPerCandidate);
			System.out.print(".");
			
			if(generatedPL.ksTest(generated, false) >= threshold)
				above ++;
			System.out.print(".");

		}
		
		return above / (double) n;
	}

	public double significance(List<Double> distances, double epsilon)
	{
		return significance(distances, epsilon, -1, -1);
	}	
	
	public double significance(List<Double> distances, double epsilon, int numCandidates, int samplesPerCandidate)
	{
		return significance(distances, (int)(0.25 * Math.pow(epsilon, -2.0)), numCandidates, samplesPerCandidate);
	}	

	public static <P> Fit fit(List<P> data, Distance<? super P> metric)
	{
		return new Fit(distances(data, metric));
	}	
	
	
	/**
	 * This method does not store a copy of all distances, but instead stores
	 * the data and calculates all distances on the fly. The drawback is that we 
	 * cannot perform the ksTest on all distances but we must sample.
	 * 
	 * @param data
	 * @param metric
	 * @return
	 */
	public static <P> BigFit<P> bigFit(List<P> data, Distance<? super P> metric)
	{
		return new BigFit<P>(data, metric);
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
	
	public static class BigFit<P>
	{
		List<P> data;
		Distance<? super P> metric;
		
		public BigFit(List<P> data, Distance<? super P> metric)
		{
			super();
			this.data = data;
			this.metric = metric;
		}
		
		public Takens fit(double maxDistance)
		{
			double sum = 0.0, n = 0.0;
			
			for(int i : series(data.size()))
				for(int j : series(i+1, data.size()))
				{
					double distance = metric.distance(data.get(i), data.get(j));
					if(distance < maxDistance)
					{ 
						n++;
						sum += log(distance);
					}
				}
			
			double dimension = n / (- sum +  n * log(maxDistance));
			
			return new Takens(dimension, maxDistance);
		}	
		
		public Takens fit(List<Double> candidates, int ksSamples)
		{
			// * Find the distance that minimizes the KS value
			Takens best = null;
			double bestKS = Double.POSITIVE_INFINITY;		
			
			Functions.tic();
			
			List<Double> distanceSample = sample(ksSamples);
			
			for(int i : series(candidates.size()))
			{
				Takens current = fit(candidates.get(i));
				double ksValue = current.ksTest(distanceSample, true);
				
				if(ksValue < bestKS)
				{
					bestKS = ksValue;
					best = current;
				}
				
				if(i%50000 == 0 && candidates.size() > 100000)
					Global.log().info("Iteration " + i + " of " + candidates.size() + " finished. ("+((100 * i)/candidates.size()) +"% after "+Functions.toc()+" seconds)");
			}

			return best;
		}	
		
		public List<Double> sample(int size)
		{
			List<Double> distanceSample = new ArrayList<Double>(size);
			for(int i : series(size))
			{
				int a = -1, b = -1; 
				while(a == b)
				{
					a = Global.random.nextInt(data.size());
					b = Global.random.nextInt(data.size());
				}
				
				distanceSample.add(metric.distance(data.get(a), data.get(b)));
			}
			Collections.sort(distanceSample);	
			return distanceSample;
		}
		
		/**
		 * Fir with all distances, but use a sample to estimate KS distance
		 * @param ksSamples
		 * @return
		 */
		public Takens fit(int ksSamples)
		{
			// * Find the distance that minimizes the KS value
			Takens best = null;
			double bestKS = Double.POSITIVE_INFINITY;		
			
			Functions.tic();
			
			List<Double> distanceSample = new ArrayList<Double>(ksSamples);
			for(int i : series(ksSamples))
			{
				int a = -1, b = -1; 
				while(a == b)
				{
					a = Global.random.nextInt(data.size());
					b = Global.random.nextInt(data.size());
				}
				
				distanceSample.add(metric.distance(data.get(a), data.get(b)));
			}
			
			Collections.sort(distanceSample);
			
			for(int i : series(data.size()))
				for(int j : series(i+1, data.size()))
				{
					double distance = metric.distance(data.get(i), data.get(j));
					
					Takens current = fit(distance);
					double ksValue = current.ksTest(distanceSample, true);
					
					if(ksValue < bestKS)
					{
						bestKS = ksValue;
						best = current;
					}
					
				}
			

			return best;
		}	
		
		/**
		 * This method uses all available data to calculate the MLE dimension, 
		 * and uses a list of C candidates for the maxDistance parameter that was 
		 * generated by C full fits to a small subsample of the data. 
		 * 
		 * @param samplesPerCandidate
		 * @param numCandidates
		 * @return
		 */
		public Takens fit(int numCandidates, int samplesPerCandidate, int ksSamples)
		{
			List<Double> candidates = candidates(numCandidates, samplesPerCandidate);
			
			Collections.sort(candidates);
			
			Global.log().info("candidates: " + candidates);
			
			return fit(candidates, ksSamples);
		}	
		
		/**
		 * Generates a list of candidates for the maxDistance parameter by 
		 * fitting to a small subsample of the data multiple times. For each 
		 * fit the maxDistance is returned as a candidate. 
		 * 
		 * @param numCandidates
		 * @param samplesPerCandidate
		 * @return
		 */
		public List<Double> candidates(int numCandidates, int samplesPerCandidate)
		{
			List<Double> candidates = new ArrayList<Double>(numCandidates);
			for(int i : Series.series(numCandidates))
			{
				List<Double> distanceSample = new ArrayList<Double>(numCandidates);
				for(int j : series(samplesPerCandidate))
				{
					int a = -1, b = -1; 
					while(a == b)
					{
						a = Global.random.nextInt(data.size());
						b = Global.random.nextInt(data.size());
					}
					
					distanceSample.add(metric.distance(data.get(a), data.get(b)));
				}
				Collections.sort(distanceSample);
				
				Takens dist = Takens.fit(distanceSample, true).fit();
				candidates.add(dist.maxDistance());
			}			
			
			return candidates;
		}
		
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
					sum += log(distance);
				}
			
			double dimension = n / (- sum +  n * log(maxDistance));
			
			return new Takens(dimension, maxDistance);
		}	
		
		
		public Takens fit(double maxDistance, int samples)
		{
			double sum = 0.0, n = 0.0;
			for(int i : series(samples))
			{
				double distance = Global.random.nextInt(distances.size());
				if(distance < maxDistance)
				{ 
					n++;
					sum += log(distance);
				}
			}
			
			double dimension = n / (- sum +  n * log(maxDistance));
			return new Takens(dimension, maxDistance);
		}	
				
		public Takens fitSampled(int samples)
		{
			// * Find the distance that minimizes the KS value
			Takens best = null;
			double bestKS = Double.POSITIVE_INFINITY;	
			
			Functions.tic();
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
				
				if(i%200 == 0)
					Global.log().info("Iteration " + i + " of " + samples + " finished. ("+((100 * i)/samples) +"% after "+Functions.toc()+" seconds)");
				
			}
			
			return best;
		}
		
		/**
		 * This method uses all available data to calculate the MLE dimension, 
		 * and uses a list of C candidates for the maxDistance parameter that was 
		 * generated by C full fits to a small subsample of the data. 
		 * 
		 * @param samplesPerCandidate
		 * @param numCandidates
		 * @return
		 */
		public Takens fit(int numCandidates, int samplesPerCandidate)
		{
			List<Double> candidates = new ArrayList<Double>(numCandidates);
			for(int i : Series.series(numCandidates))
			{
				List<Double> sample = Datasets.sample(distances, samplesPerCandidate);
				
				Takens dist = Takens.fit(sample, false).fit();
				candidates.add(dist.maxDistance());
			}
			
			Collections.sort(candidates);
			
			return fit(candidates);
		}
			
		public Takens fit(List<Double> candidates)
		{
			// * Find the distance that minimizes the KS value
			Takens best = null;
			double bestKS = Double.POSITIVE_INFINITY;		
			
			Functions.tic();
			
			for(int i : series(candidates.size()))
			{
				Takens current = fit(candidates.get(i));
				double ksValue = current.ksTest(this.distances, true);
				
				if(ksValue < bestKS)
				{
					bestKS = ksValue;
					best = current;
				}
				
				if(i%50000 == 0 && candidates.size() > 100000)
					Global.log().info("Iteration " + i + " of " + candidates.size() + " finished. ("+((100 * i)/candidates.size()) +"% after "+Functions.toc()+" seconds)");
			}

			return best;
		}
		
		
		/**
		 * Fits to a golden standard. The parameter d_max is chosen so that the 
		 * difference of the dimension estimate with the given target is 
		 * minimized. 
		 * 
		 * @param target
		 * @return
		 */
		public Takens fitError(double target)
		{
			// * Find the distance that minimizes the KS value
			Takens best = null;
			double lowestError = Double.POSITIVE_INFINITY;		
			
			Functions.tic();
			
			for(int i : series(distances.size()))
			{
				Takens current = fit(distances.get(i));
				double error = Math.abs(current.dimension() - target);
				
				if(error < lowestError)
				{
					lowestError = error;
					best = current;
				}
			}

			return best;
		}	
		
		public Takens fit()
		{
			return fit(distances);
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
	

}
