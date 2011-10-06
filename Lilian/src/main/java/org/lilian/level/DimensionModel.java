package org.lilian.level;

import static java.lang.Math.log;

import java.io.IOException;
import java.util.*;

import org.lilian.Global;
import org.lilian.corpora.Corpus;
import org.lilian.corpora.SequenceIterator;
import org.lilian.util.*;

import JSci.maths.statistics.NormalDistribution;

/**
 * Experimental variation on the LevelStatisticsModel.
 * 
 * @param <T>
 */

public class DimensionModel<T> extends LevelStatisticsModel<T> {
	
	private double maxDistance;
	
	private Map<T, List<Integer>> indices = new HashMap<T, List<Integer>>();
	
	private Map<T, Double> dimCache = new HashMap<T, Double>();
	private Map<T, Double> meanCache = new HashMap<T, Double>();	
	private Map<T, Double> varCache = new HashMap<T, Double>();	
	private Map<T, Double> numCache = new HashMap<T, Double>();	
	 
	public DimensionModel(double maxDistance)
	{
	}	
	
	public DimensionModel(double maxDistance, Corpus<T> corpus)
		throws IOException
	{
		this.maxDistance = maxDistance;		
		add(corpus);
	}
			
	public void add(T token)
	{
		super.add(token);
		
		if(!indices.containsKey(token))
			indices.put(token, new ArrayList<Integer>());
		
		indices.get(token).add((int)total);
		
		dimCache.remove(token);
	}
	
	public List<Integer> indices(T token)
	{
		return indices.get(token);
	}

	public double dimension(T token)
	{
		if(dimCache.containsKey(token))
			return dimCache.get(token);
		
		// * Takens estimator
		List<Integer> data = indices.get(token);
		int n = data.size();
		double[] distances = new double[(n*n - n)/2]; 
		
		int c = 0;
		for(int i = 0; i < data.size(); i++)
			for(int j = i; j < data.size(); j++)
			{
				if(i != j)
				{
					int a = data.get(i),
					    b = data.get(j);

					distances[c] = Math.abs(a - b);
					c++;
				}
			}
		
		NormalDistribution dist = new NormalDistribution(distances);
		
		meanCache.put(token, dist.getMean());
		varCache.put(token, dist.getVariance());

		double sum = 0.0;
		int num = 0;
		
		double max = dist.getMean() + dist.getVariance() * maxDistance;
		
		for(double d : distances)		
			if(d < max)
			{
				sum += log(d/max);
				num++;
			}
		
		numCache.put(token, (double)num);
		
		double value = -num/(double)sum;		
		
		dimCache.put(token, value);
		
		return value;
	}
	
	public double mean(T token)
	{
		dimension(token);
		
		return meanCache.get(token);
	}
	
	public double variance(T token)
	{
		dimension(token);
		
		return varCache.get(token);
	}
	
	public double num(T token)
	{
		dimension(token);
		
		return numCache.get(token);
	}	

	
	public class CompressionComparator implements Comparator<T>
	{
		@Override
		public int compare(T first, T second) {
			return Double.compare(dimension(first), dimension(second));
		}
	}

	public String out(T token)
	{
		return out(token, true);
	}
	
	public String out(T token, boolean printToken)
	{
		return super.out(token, printToken) + ", "
			+ dimension(token) + ", "
			+ mean(token) + ", "
			+ variance(token) + ","
			+ num(token);
	}
}
