package org.lilian.data.real.ds;

import static org.lilian.util.Functions.choose;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.lilian.Global;
import org.lilian.data.real.Datasets;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.neural.Activations;
import org.lilian.neural.ThreeLayer;
import org.lilian.util.Functions;
import org.lilian.util.Series;

/**
 * An (experimental) EM algorithm for the induction of discrete dynamical 
 * systems and other ergodic distributions.
 * 
 * @author Peter
 *
 */
public class EM
{
	public static final double VAR = 0.5;
	
	protected int dim;
	
	protected List<Point> data;
	protected double sigma;
	protected int numSources;
	
	// * These lists represent point pairs so that each point in 'from' 
	//   represents a possible source for the given point in 'to'
	private List<Point> from;
	private List<Point> to;
	
	private List<Double> weights;
	
	protected ThreeLayer map;
	
	public EM(List<Point> data, int hidden, double sigma, int numSources)
	{
		this(data, sigma, numSources, 
				ThreeLayer.random(
						data.get(0).dimensionality(), 
						hidden, VAR, Activations.sigmoid()));
	}
	
	public EM(List<Point> data, double sigma, int numSources, ThreeLayer map)
	{
		this.data = data;
		dim = data.get(0).dimensionality();
		
		this.sigma = sigma;
		this.numSources = numSources;
		
		from = new ArrayList<Point>();
		to = new ArrayList<Point>();
		weights = new ArrayList<Double>();
		
		this.map = map;
	}

	public void iterate(int sampleSize, int epochs, double learningRate, boolean reset)
	{
		expectation(sampleSize);
		maximization(epochs, learningRate, reset);
	}
	
	/**
	 * Estimate the latent variables
	 */
	public void expectation(int sampleSize)
	{
		List<Point> sample = 
				sampleSize == -1 ? data : Datasets.sample(data, sampleSize);
		
		from.clear();
		to.clear();
		weights.clear();
				
		for(int to : series(sample.size()))
		{
			List<Index> indices = new ArrayList<Index>(sample.size());
			
			for(int from : series(sample.size()))
				if(from != to)
				{
					Point mean = map.map(sample.get(from));
					double density = 
							new MVN(mean, sigma).density(sample.get(to));
					
					indices.add(new Index(from, density));
				}
						
			// * Remove all but top {numSources}
			Collections.sort(indices);
			while(indices.size() > numSources)
				indices.remove(indices.size() - 1);
			
			// Global.log().info(sample.get(to) + " " + map.map(sample.get(to)) + " " + to + " " + indices);
			
			// * add point pairs with normalized weights
			double sum = 0.0;
			for(Index index : indices)
				sum += index.weight();
			
			Point toPoint = sample.get(to);
			for(Index index : indices)
			{
				Point fromPoint = sample.get(index.index());
				
				this.to.add(toPoint);
				this.from.add(fromPoint);
				this.weights.add(index.weight() / sum);
			}
		}
	}
	
	/**
	 * Estimate the parameters of the map from the induced responsibilities
	 */
	public void maximization(int epochs, double learningRate, boolean reset)
	{
		if(reset)
			map = ThreeLayer.random(
				map.inputSize(), map.hiddenSize(), VAR, Activations.sigmoid());
		
		for(int epoch : series(epochs))
			for(int i : series(to.size()))
				map.train(from.get(i), to.get(i), weights.get(i) * learningRate);
	}
	
	public ThreeLayer map()
	{
		return map;
	}
	
	public int dimension()
	{
		return dim;
	}
	
	private class Index implements Comparable<Index>
	{
		private int index;
		private double weight;
		
		public Index(int index, double weight)
		{
			this.index = index;
			this.weight = weight;
		}
		
		public int index()
		{
			return index;
		}
		public double weight()
		{
			return weight;
		}

		@Override
		public int compareTo(Index o)
		{
			return - Double.compare(weight, o.weight);
		}

		@Override
		public String toString()
		{
			return "[" + index + ", w=" + weight + "]";
		}
	}
	
	public static ThreeLayer initial(List<Point> data, int hidden, double learningRate, int iterations)
	{
		ThreeLayer map = 
				ThreeLayer.random(data.get(0).dimensionality(), 
						hidden, VAR, Activations.sigmoid());
		
		for(int i : Series.series(iterations))
			map.train(choose(data), choose(data), learningRate);
		
		return map;
	}
}
