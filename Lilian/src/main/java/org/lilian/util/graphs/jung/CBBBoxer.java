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

public class CBBBoxer<V, E> implements BoxingAlgorithm<V, E>
{
	private Graph<V, E> graph;
	
	private UnweightedShortestPath<V, ?> usp;
	
	public CBBBoxer(Graph<V, E> graph)
	{
		this.graph = graph;
		
		usp = new UnweightedShortestPath<V, E>(graph);
	}

	@Override
	public Boxing<V, E> box(int l)
	{
		List<Set<V>> result = new ArrayList<Set<V>>();
				
		Set<V> uncovered = new HashSet<V>();
		uncovered.addAll(graph.getVertices());
				
		while(! uncovered.isEmpty())
		{
			List<V> candidates = new ArrayList<V>(uncovered);
			Set<V> box = new HashSet<V>();
			while(! candidates.isEmpty())
			{
				int draw = Global.random.nextInt(candidates.size());
				V center = candidates.remove(draw);
				
				box.add(center);
				uncovered.remove(center);
				
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
		
		return new Boxing<V, E>(result, graph);
	}
	
	private int distance(V first, V second)
	{
				
		Number dNum = usp.getDistance(first, second);
		int distance = (dNum != null) ? (int)dNum.doubleValue() : Integer.MAX_VALUE;
		
		return distance;
	}

}
