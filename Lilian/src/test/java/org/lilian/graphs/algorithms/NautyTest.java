package org.lilian.graphs.algorithms;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.lilian.util.Functions.asSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Graphs;
import org.lilian.graphs.MapUTGraph;
import org.lilian.graphs.Node;
import org.lilian.util.Functions;
import org.lilian.util.Order;
import org.lilian.util.Series;

public class NautyTest
{
	private Graph<String> legs()
	{
		Graph<String> graph = new MapUTGraph<String, String>();
		
		graph.add("a");
		graph.add("b");
		graph.add("c");
		
		graph.node("a").connect(graph.node("b"));
		graph.node("b").connect(graph.node("c"));
		
		return graph;
	}
	
	private Graph<String> graph()
	{
		Graph<String> graph = new MapUTGraph<String, String>();
		
		graph.add("a");
		graph.add("b");
		graph.add("c");
		graph.add("d");
		graph.add("e");
		graph.add("f");
		graph.add("g");
		graph.add("h");
		graph.add("i");

		graph.node("a").connect(graph.node("b"));
		graph.node("b").connect(graph.node("c"));
		graph.node("a").connect(graph.node("d"));
		graph.node("b").connect(graph.node("e"));
		graph.node("c").connect(graph.node("f"));
		graph.node("d").connect(graph.node("e"));
		graph.node("e").connect(graph.node("f"));
		graph.node("d").connect(graph.node("g"));
		graph.node("e").connect(graph.node("h"));
		graph.node("f").connect(graph.node("i"));
		graph.node("g").connect(graph.node("h"));
		graph.node("h").connect(graph.node("i"));
		
		return graph;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRefine1()
	{
		Graph<String> graph = graph();
		
		Nauty nauty = new Nauty();
		
		List<List<Node<String>>> unitPartition = new ArrayList<List<Node<String>>>();
		unitPartition.add(new ArrayList<Node<String>>(graph.nodes()));

		List<List<Node<String>>> expected = new ArrayList<List<Node<String>>>();
		expected.add(asList(graph.node("a"), graph.node("c"), graph.node("g"), graph.node("i")));
		expected.add(asList(graph.node("b"), graph.node("d"), graph.node("f"), graph.node("h")));
		expected.add(asList(graph.node("e")));
		
		assertEquals(expected, nauty.refine(unitPartition));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRefine2()
	{
		Graph<String> graph = graph();
		
		Nauty nauty = new Nauty();
		
		List<List<Node<String>>> partition = new ArrayList<List<Node<String>>>();
		partition.add(asList(graph.node("a")));
		partition.add(asList(graph.node("c"), graph.node("g"), graph.node("i")));
		partition.add(asList(graph.node("b"), graph.node("d"), graph.node("f"), graph.node("h")));
		partition.add(asList(graph.node("e")));
		
		List<List<Node<String>>> expected = new ArrayList<List<Node<String>>>();
		expected.add(asList(graph.node("a")));
		expected.add(asList(graph.node("c"), graph.node("g")));
		expected.add(asList(graph.node("i")));
		expected.add(asList(graph.node("f"), graph.node("h")));
		expected.add(asList(graph.node("b"), graph.node("d")));
		expected.add(asList(graph.node("e")));
		
		assertEquals(expected, nauty.refine(partition));
	}

	@Test
	public void testDegree()
	{
		Graph<String> graph = graph();
		
		Nauty nauty = new Nauty();
		
		List<List<Node<String>>> unitPartition = new ArrayList<List<Node<String>>>();
		unitPartition.add(new ArrayList<Node<String>>(graph.nodes()));
		
		assertEquals(2, nauty.degree(graph.node("a"), unitPartition.get(0)));
		assertEquals(4, nauty.degree(graph.node("e"), unitPartition.get(0)));

		
	}
	
	@Test
	public void testSearch()
	{
		Graph<String> graph = Graphs.blank(graph(), "x"), orderedA, orderedB;
		Order order;
		
		Nauty nauty = new Nauty();

		// * Find the canonical order for the graph
		order = nauty.order(graph);
		
		orderedA = Graphs.reorder(graph, order);

		// * re-order graph and test 
		graph = Graphs.reorder(graph, Order.random(graph.size()));
		order = nauty.order(graph);		
				
		orderedB = Graphs.reorder(graph, order);
		
		assertTrue(orderedA.equals(orderedB));
		assertTrue(orderedB.equals(orderedA));
	}
	
	@Test
	public void testSearchLegs()
	{
		Graph<String> graph = Graphs.blank(legs(), "x"), orderedA, orderedB;
		Order order;
		
		Nauty nauty = new Nauty();

		// * Find the canonical order for the graph
		order = nauty.order(graph);
		
		orderedA = Graphs.reorder(graph, order);

		// * re-order graph and test 
		graph = Graphs.reorder(graph, Order.random(graph.size()));
		order = nauty.order(graph);		
				
		orderedB = Graphs.reorder(graph, order);
		
		assertTrue(orderedA.equals(orderedB));
		assertTrue(orderedB.equals(orderedA));
	}
}
