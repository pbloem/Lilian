package org.lilian.util.graphs.jung;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class CPPBoxerTest
{

	@Test
	public void testBox()
	{
		Graph<String, String> graph = new UndirectedSparseGraph<String, String>();
		
		graph.addEdge("0", "a", "b");
		graph.addEdge("1", "b", "c");
		graph.addEdge("2", "c", "d");
		graph.addEdge("3", "d", "e");
		graph.addEdge("4", "e", "a");
		
		CPPBoxer<String, String> boxer = new CPPBoxer<String, String>(graph);
		
		System.out.println(boxer.box(1));
	}

}
