package org.lilian.util.graphs.jung;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.lilian.Global;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class CBBBoxerTest
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
		
		CBBBoxer<String, String> boxer = new CBBBoxer<String, String>(graph);
		
		Boxing<String, String> boxing = boxer.box(2);
		System.out.println(boxing);
		System.out.println(boxing.size());
		
		assertEquals(0, boxing.uncovered().size());
		
		assertEquals(0, boxing.overCovered().size());
	}
	
	@Test
	public void testBox2()
	{
		Global.random = new Random();
		int lb = 2;
		
		Graph<Integer, Integer> graph = Graphs.abRandom(700, 5, 3);
		
		CBBBoxer<Integer, Integer> boxer = new CBBBoxer<Integer, Integer>(graph);
		
		Boxing<Integer, Integer> boxing = boxer.box(lb);
		System.out.println(boxing);
		System.out.println(boxing.size());
		
		assertEquals(0, boxing.uncovered().size());
		
		assertEquals(0, boxing.overCovered().size());

	}

}
