package org.lilian.util.graphs.jung;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class GraphsTest
{

	@Test
	public void toBitsTest()
	{
		Graph<String, String> graph = new UndirectedSparseGraph<String, String>();
		
		graph.addEdge("0", "a", "b");
		graph.addEdge("1", "b", "c");
		graph.addEdge("2", "c", "d");
		graph.addEdge("3", "d", "a");
		graph.addEdge("4", "a", "c");
		

		System.out.println(Graphs.toBits(graph, Arrays.asList("a", "b", "c", "d")));
		System.out.println(graph.getVertices());
		
	}

}
