package org.lilian.util.graphs.jung;

import static org.lilian.util.Series.series;

import java.util.AbstractList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lilian.util.Series;

import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * Static utility methods related to boxing algorithms
 * 
 * @author Peter
 *
 */
public class Boxing<V, E> extends AbstractList<Set<V>>
{
	private List<Set<V>> base;
	private Graph<V, E> graph;
	
	private UnweightedShortestPath<V, E> usp;
	
	public Boxing(List<Set<V>> base, Graph<V, E> graph)
	{
		this.base = base;
		this.graph = graph;
		
		usp = new UnweightedShortestPath<V, E>(graph);
	}

	/**
	 * Returns all vertices that have not been assigned some box.
	 * @param boxing
	 * @param graph
	 * @return
	 */
	public Set<V> uncovered()
	{
		Set<V> vertices = new HashSet<V>();
		vertices.addAll(graph.getVertices());
		
		Set<V> covered = new HashSet<V>();
		for(Set<V> box : this)
			covered.addAll(box);
		
		vertices.removeAll(covered);
		return vertices;
	}
	
	/**
	 * Returns all vertices that have been assigned multiple boxes.
	 * @param boxing
	 * @param graph
	 * @return
	 */
	public Set<V> overCovered()
	{
		Set<V> vertices = new HashSet<V>();
		
		int n = size();
		for(int i : series(n))
			for(int j : series(i + 1, n))
			{
				Set<V> copy = new HashSet<V>();
				copy.addAll(get(i));
				
				copy.retainAll(get(j));
				vertices.addAll(copy);
			}
				
		return vertices;
	}
	
	public int maxSize()
	{
		int max = Integer.MIN_VALUE;
		for(Set<V> box : base)
			max = Math.max(max,  box.size());
		
		return max;
	}
	
	public int maxDistance()
	{
		int max = Integer.MIN_VALUE;
		for(Set<V> box : base)
		{
			int boxMax = Integer.MIN_VALUE;
			for(V a : box)
				for(V b : box)
					boxMax = Math.max(max, (int)usp.getDistance(a, b).doubleValue());
			max = Math.max(max, boxMax);
		}
		
		return max;
	}


	@Override
	public Set<V> get(int index)
	{
		return base.get(index);
	}

	@Override
	public int size()
	{	
		return base.size();
	}
	
	/**
	 * <p>
	 * Constructs the graph with boxes as vertices, and an edge between two 
	 * vertices if there is at least one edge between member from both boxes in
	 * the original graph.  
	 * </p><p>
	 * The integer labels of the vertices of the new graph match the indices of 
	 * the boxes in this boxing. The edge labels have no particular meaning.
	 * </p> 
	 * @return
	 */
	public Graph<Integer, Integer> postGraph()
	{
		Graph<Integer, Integer> post = new UndirectedSparseGraph<Integer, Integer>();
		
		for(int i : series(size()))
			post.addVertex(i);
		
		int edges = 0;
		for(int i : series(size()))
			for(int j : series(i+1, size()))
				if(connected(i, j))
					post.addEdge(edges ++, i, j);
		
		return post;
	}
	
	/**
	 * Whether box i and box j are connected. Ie. there is at least one link 
	 * between a member of i and a member of j.
	 *  
	 * @param i
	 * @param j
	 * @return
	 */
	public boolean connected(int i, int j)
	{
		for(V vi : get(i))
			for(V vj : get(j))
				if(graph.isNeighbor(vi, vj))
					return true;
		
		return false;
	}
	
}
