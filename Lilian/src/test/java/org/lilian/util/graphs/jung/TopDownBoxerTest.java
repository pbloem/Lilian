package org.lilian.util.graphs.jung;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class TopDownBoxerTest
{

	@Test
	public void testBox()
	{
		int l = 2;
		Graph<String, String> graph = new UndirectedSparseGraph<String, String>();
		
		graph.addEdge("0", "a", "b");
		graph.addEdge("1", "b", "c");
		graph.addEdge("2", "c", "d");
		graph.addEdge("3", "d", "e");
		graph.addEdge("4", "e", "a");
		
		TopDownBoxer<String, String> boxer = new TopDownBoxer<String, String>(graph);
		
		Boxing<String, String> boxing = boxer.box(l);
		System.out.println(boxing);
		System.out.println(boxing.size());
		
		assertEquals(0, boxing.uncovered().size());
		
		assertEquals(0, boxing.overCovered().size());
	}
	
	//@Test
	public void testBox2()
	{
		int l = 3;

		Graph<Integer, Integer> graph = Graphs.abRandom(70, 5, 3);
		
		TopDownBoxer<Integer, Integer> boxer = new TopDownBoxer<Integer, Integer>(graph);
		
		Boxing<Integer, Integer> boxing = boxer.box(l);
		System.out.println(boxing);
		System.out.println(boxing.size());
		
		assertEquals(0, boxing.uncovered().size());
		
		assertEquals(0, boxing.overCovered().size());
		
		System.out.println(boxing.maxSize());
		assertTrue(boxing.maxDistance() <= l);

	}

}
