package org.lilian.graphs.algorithms;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lilian.Global;
import org.lilian.graphs.Graph;
import org.lilian.util.Series;

public class FloydWarshall<L>
{
	private short[][] distances;
	private int n;

	public FloydWarshall(Graph<L> graph)
	{
		n = graph.size();
		
		distances = new short[n][];
		for(int i : series(n))
			distances[i] = new short[n];
		
		Global.log().info("FW Starting");
		
		// * Init the matrix
		for(int i: series(n))
			for(int j : series(n))
				if(i == j)
					distances[i][j] = 0;
				else
					distances[i][j] = 
					graph.nodes().get(i).connected(graph.nodes().get(j)) 
					? 1 : Short.MAX_VALUE;
			
		for(int k : series(n))
			for(int i: series(n))
				for(int j : series(n))
					distances[i][j] = (short)Math.min(distances[i][j], distances[i][k]+distances[k][j]);
		
		Global.log().info("FW finished");
	}
	
	public int distance(int i, int j)
	{
		return distances[i][j];
	}
	
	public double meanDistance()
	{
		double meanDistance = 0.0;
		int num = 0;
		for(int i : series(n))
			for(int j : series(n))
			{
				meanDistance += distance(i, j);
				num++;
			}
		
		meanDistance /= (double) num;
		return meanDistance;
	}
	
	public double diameter()
	{
		double diameter = Double.NEGATIVE_INFINITY;
		for(int i : series(n))
			for(int j : series(n))
				diameter = Math.max(diameter, distance(i, j));

		return diameter;
	}
}
