package org.lilian.util.graphs.jung;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lilian.Global;
import org.lilian.util.Series;

import edu.uci.ics.jung.graph.Graph;


public class FloydWarshall<V, E>
{
	private short[][] distances;
	private Graph<V, E> graph;
	
	private Map<V, Integer> indices;
	private List<V> vertices;

	public FloydWarshall(Graph<V, E> graph)
	{
		this.graph = graph;
		int n = graph.getVertexCount();
		
		indices = new HashMap<V, Integer>();
		int c = 0;
		for(V vertex : graph.getVertices())
			indices.put(vertex, c++);
		
		vertices = new ArrayList<V>();
		for(V vertex : graph.getVertices())
			vertices.add(vertex);
		
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
					distances[i][j] = graph.isNeighbor(vertices.get(i), vertices.get(j)) ? 1 : Short.MAX_VALUE;
			
		for(int k : series(n))
			for(int i: series(n))
				for(int j : series(n))
					distances[i][j] = (short)Math.min(distances[i][j], distances[i][k]+distances[k][j]);
		
		Global.log().info("FW finished");
	}
	
	public int distance(V vi, V vj)
	{
		int i = indices.get(vi);
		int j = indices.get(vj);
		
		return distances[i][j];
	}
}
