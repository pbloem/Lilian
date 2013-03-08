package org.lilian.data.real.clustering;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.lilian.Global;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.classification.Classification;
import org.lilian.data.real.classification.Classified;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;

public class KMedioids<P>
{
	private Distance<P> distance;
	private Classified<P> data;
	private int numClusters;
	
	// * Indices of the medioids
	private List<Integer> medioids;
	
	// * A cache for the distances
	private RealMatrix distances;
	
	public KMedioids(List<P> data, Distance<P> distance, int numClusters)
	{
		int n = data.size();
		
		this.distance = distance;
		this.numClusters = numClusters;
		
		medioids = sample(numClusters, n);
		
		Global.log().info("Calculating distances");
		
		int total = (n * n + n) / 2;
		int t = 0;
				
		distances = new Array2DRowRealMatrix(n, n);
		for(int i : series(n))
			for(int j : series(i, n))
			{
				distances.setEntry(i, j, 
						distance.distance(data.get(i), data.get(j)));
				Global.log().info("Calculating distance " + t + " out of " + total);
				t++;
			}
		
		List<Integer> classes = new ArrayList<Integer>(n);
		for(int i : series(n))
			classes.add(-1);
		
		this.data = Classification.combine(data, classes);
	}
	
	/**
	 * Assign each point to its closest medioid.
	 */
	public void assign()
	{
		for(int i : series(data.size()))
		{
			int bestCluster = -1;
			double bestDistance = Double.POSITIVE_INFINITY; 
			
			for(int cluster : series(numClusters))
			{
				double distance = distance(i, medioids.get(cluster));
				
				if(distance < bestDistance)
				{
					bestDistance = distance;
					bestCluster = cluster;
				}
			}
			
			data.setClass(i, bestCluster);
		}		
		
		System.out.println(data.classes());
	}
	
	public Classified<P> clustered()
	{
		return data;
	}
	
	/**
	 * Choose the optimal medioids.
	 */
	public void update()
	{
		for(int cluster : series(numClusters))
		{
			int bestMedioid = -1;
			double bestMedioidScore = Double.POSITIVE_INFINITY;
			
			for(int medioid : series(data.size()))
			{
				double score = 0.0;
				for(int other : series(data.size()))
					if(data.cls(other) == cluster)
					{
						double d = distance(medioid, other);
						score += d * d;
					}
				
				if(score < bestMedioidScore)
				{
					bestMedioidScore = score;
					bestMedioid = medioid;
				}
			}
			
			medioids.set(cluster, bestMedioid);
		}
	}
	
	public void iterate(int n)
	{
		for(int i : series(n))
		{
			assign();
			update();
		}
	}
	
	private double distance(int i, int j)
	{
		int small, big;
		if(i < j)
		{
			small = i; 
			big = j;
		} else
		{
			small = j; 
			big = i;
		}
		
		return distances.getEntry(small, big);
	}
	
	/**
	 * Samples k distinct values from the first n integers
	 * @param k
	 * @param size
	 * @return
	 */
	public static List<Integer> sample(int k, int size)
	{
		// * The algorithm we use basically simulates having an array with the 
		//   values of o to n-1 at their own indices, and for each i, choosing a 
		//   random index above it and swapping the two entries.
		//
		//   Since we expect low k, most entries in this array will stay at 
		//   their original index and we only stores the values that deviate.
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
				
		for(int i : series(k))
		{
			// Sample a random integer above or equal to i and below 'size'
			int draw = Global.random.nextInt(size - i) + i;
			
			int drawValue = map.containsKey(draw) ? map.get(draw) : draw;
			int iValue = map.containsKey(i) ? map.get(i) : i; 
			
			// swap the values
			map.put(i, drawValue);
			map.put(draw, iValue);
		}
		
		List<Integer> result = new ArrayList<Integer>(k);
		for(int i : series(k))
			result.add(map.get(i));
		
		return result;
	}


}
