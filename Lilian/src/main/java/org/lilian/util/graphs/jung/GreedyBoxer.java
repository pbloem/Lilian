package org.lilian.util.graphs.jung;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lilian.Global;
import org.lilian.util.Pair;
import org.lilian.util.Series;

import cern.colt.Arrays;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class GreedyBoxer<V, E> implements BoxingAlgorithm<V, E>
{
	private int[][] colors;
	private int lMax; // The maximal (horizontal) index to the colors array
	private short[][] distanceCache = null;
	
	private Graph<V, E> graph;
	private List<V> vertices;
	private Distance<V> dist;
	
	public GreedyBoxer(Graph<V, E> graph, int lm, boolean distanceCache)
	{
		super();
		this.graph = graph;
		dist = new DijkstraDistance<V, E>(graph);
		vertices = new ArrayList<V>(graph.getVertices());

		if(distanceCache)
			makeCache();
		
		setMatrix(lm-1);
	}	

	private void makeCache()
	{
		Global.log().info("Starting cache");
		
		int n = vertices.size();
		distanceCache = new short[n][];
		for(int i = 0; i < n; i++)
		{
			System.out.print('.');
			if(i%200==0) System.out.println();
			
			distanceCache[i] = new short[i];
			for(int j = 0; j < i; j++)
			{
				V first = vertices.get(i), second = vertices.get(j);
				Number dNum = dist.getDistance(first, second);
				int distance = (dNum != null) ? (int)dNum.doubleValue() : Integer.MAX_VALUE;
			
				short v = (distance < Short.MAX_VALUE) ? (short)distance : Short.MAX_VALUE;
				distanceCache[i][j] = v;
			}
		}	
		
		dist = null;
		Global.log().info("Finished cache");
	}

	@Override
	public Boxing<V, E> box(int l)
	{
		int lIndex = l-1;
		if(lIndex > lMax)
			setMatrix(lIndex);
		
		Map<Integer, Set<V>> boxes = new HashMap<Integer, Set<V>>();
		for(int i : series(graph.getVertexCount()))
		{
			V vertex = vertices.get(i);
			int color = colors[i][lIndex];
			
			if(! boxes.containsKey(color))
				boxes.put(color, new HashSet<V>());
			
			boxes.get(color).add(vertex);
		}
		
		return new Boxing<V, E>(new ArrayList<Set<V>>(boxes.values()), graph);
		
	}
	
	private int distance(int i, int j)
	{
		if(i == j) 
			return 0;		
		
		if(distanceCache != null)
		{
			int max = Math.max(i,  j), min = Math.max(i,  j);
			return distanceCache[max][min];
		}
		
		V first = vertices.get(i), second = vertices.get(j);
		Number dNum = dist.getDistance(first, second);
		int distance = (dNum != null) ? (int)dNum.doubleValue() : Integer.MAX_VALUE;
		
		return distance;
	}

	private void setMatrix(int lMax)
	{
		// * NB the l index represent the distance - 1. So the colors at 
		//   colors[.][0] are those for box size 1
		
		this.lMax = lMax;
		int n = graph.getVertexCount();
		
		colors = new int[n][];
		
		// * Set the color of node 0 to 0 for all l's
		colors[0] = new int[lMax+1];
		for (int l : series(lMax))
			colors[0][l] = 0;
		
		List<Integer> nColors = new ArrayList<Integer>();
		List<Pair<Integer, Integer>> neighbours = new ArrayList<Pair<Integer,Integer>>(n);
		
		for(int i : series(1, n))
		{
			if(i%1000 == 0)
				Global.log().info("At node "+i);
			colors[i] = new int[lMax+1];

			// * this node's neighbours in the dual graph (nodes that are farther than l away)
			//   we will shrink this as we increment l
			neighbours.clear();
			for(int j : series(i))
				neighbours.add(new Pair<Integer, Integer>(j, distance(i, j)));
			
			for(int l : series(lMax+1))
			{
				Iterator<Pair<Integer, Integer>> it = neighbours.iterator();
				while(it.hasNext())
					if(it.next().second() < l + 1)
						it.remove();
								
				nColors.clear();
				for(Pair<Integer, Integer> pair : neighbours)
					nColors.add(colors[pair.first()][l]);
				
				colors[i][l] = smallestException(nColors);
			}
		}

	}
	
	/**
	 * Returns the smallest positive integer not contained in a given list of
	 * illegal integers.
	 * 
	 * This method modifies the list it is passed.
	 * 
	 * @param illegal
	 * @return
	 */
	public static int smallestException(List<Integer> illegal)
	{
		Collections.sort(illegal);
		int res = 0;
		for(int i : illegal)
			if(i > res)
				return res;
			else
				res = i+1;
		return res;
	}
}
