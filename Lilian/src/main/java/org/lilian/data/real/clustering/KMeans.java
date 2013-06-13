package org.lilian.data.real.clustering;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.Global;
import org.lilian.data.real.Datasets;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.classification.Classification;
import org.lilian.data.real.classification.Classified;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Functions;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.EuclideanDistance;

public class KMeans
{
	private Distance<Point> distance = new EuclideanDistance();
	private Classified<Point> data;
	private int numClusters;
	
	private int dim;
	
	// * The cluster means
	private List<Point> means;
	
	public KMeans(List<Point> data, int numClusters)
	{
		int n = data.size();
		dim = data.get(0).dimensionality();
		
		this.numClusters = numClusters;
		
		means = Datasets.sampleWithoutReplacement(data, numClusters);
		
		Global.log().info("Calculating distances");
				
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
			Point p = data.get(i);
			
			int bestCluster = -1;
			double bestDistance = Double.POSITIVE_INFINITY; 
			
			for(int cluster : series(numClusters))
			{
				double dist = distance.distance(p, means.get(cluster));
				if(dist < bestDistance)
				{
					bestDistance = dist;
					bestCluster = cluster;
				}
			}
			
			data.setClass(i, bestCluster);
		}		
		
		System.out.println(data.classes());
	}
	
	public Classified<Point> clustered()
	{
		return data;
	}
	
	/**
	 * Means
	 */
	public void update()
	{
		System.out.println(means);

		
		List<RealVector> means = new ArrayList<RealVector>(numClusters);
		BasicFrequencyModel<Integer> fm = new BasicFrequencyModel<Integer>();
		
		for(int i : series(numClusters))
			means.add(new ArrayRealVector(dim));
		
		for(int i : series(data.size()))
		{
			int cluster = data.cls(i);
			fm.add(cluster);
			
			means.set(cluster, means.get(cluster).add(data.get(i).getBackingData()));
		}
		
		for(int i : series(numClusters)) 
					means.get(i).mapMultiplyToSelf(1.0/fm.frequency(i));
	
		for(int i : series(numClusters))
			this.means.set(i, new Point(means.get(i)));
		
	}
	
	public void iterate(int n)
	{
		for(int i : series(n))
		{
			assign();
			update();
		}
	}



}
