package org.lilian.util.graphs.jung;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lilian.Global;
import org.lilian.util.Series;

import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;

public class CPPBoxer<V, E> implements BoxingAlgorithm<V, E>
{
	private Graph<V, E> graph;
	private List<V> vertices;
	private Map<V, Integer> indices;
	private int n;
	
	private UnweightedShortestPath<V, ?> usp;
	
	// * half matrix  for caching the distances between nodes
	private int[][] distances;
	
	public CPPBoxer(Graph<V, E> graph)
	{
		this.graph = graph;
		n = graph.getVertexCount();
		
		// * Store the vertices in data structures that allows us to move between 
		//   V and integer representation easily
		vertices = new ArrayList<V>(graph.getVertices());
		
		indices = new HashMap<V, Integer>();
		for(int i : series(n))
			indices.put(vertices.get(i), i);
		
		for(int i : series(n))
		{
			distances[i] = new int[n-i];
			for(int j : Series.series(n-i))
				distances[i][j] = -1;
		}
		
		usp = new UnweightedShortestPath<V, E>(graph);
	}

	@Override
	public List<Set<V>> box(int l)
	{
		List<Set<V>> result = new ArrayList<Set<V>>();
				
		Set<V> uncovered = new HashSet<V>();
		
		uncovered.addAll(uncovered);
				
		while(! uncovered.isEmpty())
		{
			List<V> candidates = new ArrayList<V>(uncovered);
			Set<V> box = new HashSet<V>();
			while(! candidates.isEmpty())
			{
				int draw = Global.random.nextInt(candidates.size());
				V center = candidates.remove(draw);
				box.add(center);
				
				Iterator<V> it = candidates.iterator();
				while(it.hasNext())
				{
					V other = it.next();
					if(distance(center, other) >= l)
						it.remove();
				}
			}
			
			result.add(box);
		}
		
		return result;
	}
	
	private int distance(V first, V second)
	{
		int fIndex = indices.get(first), sIndex = indices.get(second);
		
		int max = Math.max(fIndex, sIndex),
		    min = Math.min(fIndex, sIndex);
		
		if(distances[max][min] != -1)
			return distances[max][min];
		
		int distance = (int)usp.getDistance(first, second).doubleValue();
		distances[max][min] = distance;
		return distance;
	}

}
