package org.lilian.level;

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
 * 
 *
 * @param <T>
 */

public class CompressionModel<T> extends LevelStatisticsModel<T> {
	
	private int trials = 50;
	private double res = 0.0005;
	private double exp = 0.25;	
	
	private Map<T, List<Integer>> indices = new HashMap<T, List<Integer>>();
	
	private Map<T, Double> compCache = new HashMap<T, Double>();
	private Map<T, Double> normCompCache = new HashMap<T, Double>();
	
	public NullModel nullModel = null; // lazy initialization 
	 
	public CompressionModel() {
		super();
	}	
	
	public CompressionModel(Corpus<T> corpus)
		throws IOException
	{
		super();
		add(corpus);
	}
			
	public void add(T token)
	{
		super.add(token);
		
		if(!indices.containsKey(token))
			indices.put(token, new ArrayList<Integer>());
		
		indices.get(token).add((int)total);
		
		compCache.remove(token);
		normCompCache.remove(token);
		
		nullModel = null;
	}
	
	public List<Integer> indices(T token)
	{
		return indices.get(token);
	}

	public double compression(T token)
	{
		if(compCache.containsKey(token))
			return compCache.get(token);
		
		BitString bs = BitString.zeros(total);
		
		if(indices.containsKey(token))
			for(int i : indices.get(token))	
				bs.set(i-1, true);
		
		double ratio = GZIPCompressor.ratio(bs.byteArray()); 
		
		compCache.put(token, ratio);
		
		return ratio;
	}
	
	public double compressionNormalized(T token)
	{
		if(normCompCache.containsKey(token))
			return normCompCache.get(token);
		
		if(nullModel == null)
			nullModel = new NullModel(trials, res, total, exp);
		
		double p = probability(token);
		
		double mean = nullModel.getMean(p),
		       std  = Math.sqrt(nullModel.getVariance(p));		
		

		double val = (compression(token) - mean)/std;

		normCompCache.put(token, val);
		
		return val;		
	}	
	
	public class CompressionComparator implements Comparator<T>
	{
		@Override
		public int compare(T first, T second) {
			return Double.compare(compression(first), compression(second));
		}
	}
	
	public class CompNormComparator implements Comparator<T>
	{
		@Override
		public int compare(T first, T second) {
			return Double.compare(compressionNormalized(first), compressionNormalized(second));
		}
	}

	public String out(T token)
	{
		return out(token, true);
	}
	
	public String out(T token, boolean printToken)
	{
		return super.out(token, printToken) + ", "
			+ compression(token) + ", "
			+ compressionNormalized(token);
	}
	
	public class NullModel
	{
		private double exp;
		private int samples;
		private int length;
		private double resolution;
		
		private Map<Integer, NormalDistribution> map = new HashMap<Integer, NormalDistribution>();
		
		public NullModel(int samples, double resolution, int length, double exp) 
		{
			this.samples = samples;
			this.resolution = resolution;
			this.length = length;
			this.exp = exp;
		}

		public double getMean(double probability) 
		{
			int i = index(probability);
			
			if(!map.containsKey(i))
				add(probability);
			
			return map.get(i).getMean();
		}
		
		public double getVariance(double probability) 
		{
			int i = index(probability);
			
			if(!map.containsKey(i))
				add(probability);
			
			return map.get(i).getMean();
		}
		
		public void add(double probability)
		{
			int i = index(probability);			
			
			if(map.containsKey(i))
				return;
			
			double[] vals = new double[trials];
			
			for(int j = 0; j < trials; j++)
			{
				BitString array = new BitString(length);
				for(int k = 0; k < length; k++)
					array.add(Global.random.nextDouble() < probability);
				
				vals[j] = GZIPCompressor.ratio(array.byteArray());
			}
			
			NormalDistribution dist = new NormalDistribution(vals);
			map.put(i, dist);
		}
		
		public int size()
		{
			return map.size();
		}
		
		private int index(double probability)
		{
			double pPrime = Math.pow(probability, exp);
			int index = (int)Math.ceil(pPrime/resolution);
			
			return index;
		}		
	}
}
