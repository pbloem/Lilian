package org.lilian.util.graphs.jung;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.lilian.Global;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class GreedyBoxerTest
{

	@Test
	public void test()
	{
		assertEquals(7, GreedyBoxer.smallestException(Arrays.asList(0, 1, 2 ,3, 4, 5, 6)));
		assertEquals(0, GreedyBoxer.smallestException(Arrays.asList(1, 2 ,3, 4, 5, 6, 7)));
		assertEquals(4, GreedyBoxer.smallestException(Arrays.asList(0, 1, 2 ,3, 5, 6, 7)));
	}


	@Test
	public void testBox()
	{
		int lb = 2;
		
		Graph<String, String> graph = new UndirectedSparseGraph<String, String>();
		
		graph.addEdge("0", "a", "b");
		graph.addEdge("1", "b", "c");
		graph.addEdge("2", "c", "d");
		graph.addEdge("3", "d", "e");
		graph.addEdge("4", "e", "a");
		
		GreedyBoxer<String, String> boxer = new GreedyBoxer<String, String>(graph, lb, false);
		
		Boxing<String, String> boxing = boxer.box(lb);
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
		
		GreedyBoxer<Integer, Integer> boxer = new GreedyBoxer<Integer, Integer>(graph, lb, false);
		
		Boxing<Integer, Integer> boxing = boxer.box(lb);
		System.out.println(boxing);
		System.out.println(boxing.size());
		
		assertEquals(0, boxing.uncovered().size());
		
		assertEquals(0, boxing.overCovered().size());

	}
	
	
}
