package org.lilian.util.graphs.jung;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class MeasuresTest
{

	@Test
	public void testAssortativity()
	{
		UndirectedGraph<String, String> graph = new UndirectedSparseGraph<String, String>();
	
//		graph.addVertex("a");
//		graph.addVertex("b");
//		graph.addVertex("c");
//		
//		graph.addEdge("1", "a", "b");
//		graph.addEdge("2", "b", "c");
//		graph.addEdge("3", "c", "a");
//		
//		System.out.println(Measures.assortativity(graph));
//		
		graph = new UndirectedSparseGraph<String, String>();
		
		graph.addEdge("1", "a", "b");
		graph.addEdge("2", "a", "c");
		graph.addEdge("3", "a", "d");
		graph.addEdge("4", "a", "e");
		
//		assertEquals(-1, Measures.assortativity(graph), 0.0);
		
		graph = new UndirectedSparseGraph<String, String>();
				
		graph.addEdge("1", "a", "b");
		graph.addEdge("2", "a", "c");
		graph.addEdge("3", "a", "d");
		graph.addEdge("4", "a", "e");
		graph.addEdge("5", "e", "a");
		graph.addEdge("6", "e", "f");
		graph.addEdge("7", "e", "g");
		graph.addEdge("8", "e", "h");
		
		assertEquals(-0.75, Measures.assortativity(graph), 0.0);		
		
	}

}
