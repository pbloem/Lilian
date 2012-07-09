package org.lilian.util.graphs.jung;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lilian.Global;
import org.lilian.util.Series;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;

public class CBBBoxer<V, E> implements BoxingAlgorithm<V, E>
{
	private Graph<V, E> graph;
	
	private Distance<V> usp;
	
	public CBBBoxer(Graph<V, E> graph)
	{
		this.graph = graph;
		
		usp = new DijkstraDistance<V, E>(graph);
	}

	@Override
	public Boxing<V, E> box(int l)
	{
		List<Set<V>> result = new ArrayList<Set<V>>();
				
		Set<V> uncovered = new LinkedHashSet<V>();
		uncovered.addAll(graph.getVertices());
				
		while(! uncovered.isEmpty())
		{
// 			Global.log().info("uncovered size: " +  uncovered.size());
			List<V> candidates = new ArrayList<V>(uncovered);
			Set<V> box = new HashSet<V>();
			while(! candidates.isEmpty())
			{
				int draw = Global.random.nextInt(candidates.size());
				V center = candidates.remove(draw);
				
				box.add(center);
				uncovered.remove(center);
				
				// Remove the candidates that are too far away
				Set<V> neighbourhood = neighbourhood(center, l);
				candidates.retainAll(neighbourhood);
				
//				Iterator<V> it = candidates.iterator();
//				while(it.hasNext())
//				{
//					V other = it.next();
//					if(distance(center, other) >= l)
//						it.remove();
//				}
			}
			
			result.add(box);
		}
		
		return new Boxing<V, E>(result, graph);
	}
	
	/**
	 * Return all nodes with distance less than d to center.
	 * @param center
	 * @param d
	 * @return
	 */
	public Set<V> neighbourhood(V center, int d)
	{
		Set<V> neighbourhood = new LinkedHashSet<V>();
		Set<V> shell0 = new LinkedHashSet<V>(),
		       shell1 = new LinkedHashSet<V>();
				
		neighbourhood.add(center);
		shell0.add(center);
		
		int c = 1;
		while(c < d)
		{
			for(V vertex : shell0)
				shell1.addAll(graph.getNeighbors(vertex));
			 
			neighbourhood.addAll(shell1);
			
			shell0 = shell1;
			shell1 = new LinkedHashSet<V>();
			c++;
		}
		
		return neighbourhood;
	}

	private int distance(V first, V second)
	{
				
		Number dNum = usp.getDistance(first, second);
		int distance = (dNum != null) ? (int)dNum.doubleValue() : Integer.MAX_VALUE;
		
		return distance;
	}

}
