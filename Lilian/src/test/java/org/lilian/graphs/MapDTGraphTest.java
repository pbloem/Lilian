package org.lilian.graphs;

import static org.junit.Assert.*;

import org.junit.Test;

public class MapDTGraphTest
{

	@Test
	public void testMapDTGraph()
	{
		DTGraph<String, Double> graph = new MapDTGraph<String, Double>();
	}

	@Test
	public void testToString()
	{
		DTGraph<String, Double> graph = new MapDTGraph<String, Double>();
		
		DTNode<String, Double> a = graph.add("a"),
		                       b = graph.add("b"),
		                       c = graph.add("c");
	
		a.connect(b, 0.5);
		
		System.out.println(graph);
	}

	@Test
	public void starTest()
	{
		DTGraph<String, Double> graph = new MapDTGraph<String, Double>();
		
		DTNode<String, Double> a = graph.add("a"),
		                       b = graph.add("b"),
		                       c = graph.add("c"),
		                       d = graph.add("d"),
		                       e = graph.add("e");
	
		b.connect(a, 0.5);
		c.connect(a, 0.5);
		d.connect(a, 0.5);
		e.connect(a, 0.5);
		
		System.out.println(graph);
		
		e.disconnect(a);
		
		System.out.println(graph);
		
		a.remove();
		
		System.out.println(graph);	
	}
	
	@Test
	public void testRemove()
	{
		DTGraph<String, Double> graph = new MapDTGraph<String, Double>();
		
		DTNode<String, Double> a = graph.add("a"),
		                       b = graph.add("b"),
		                       c = graph.add("c"),
		                       d = graph.add("d"),
		                       e = graph.add("e");
	
		b.connect(a, 0.5);
		c.connect(a, 0.5);
		d.connect(a, 0.5);
		e.connect(a, 0.5);
		
		System.out.println(graph.numLinks() + " " + graph.size());
		
		assertEquals(4, graph.numLinks());
		assertEquals(5, graph.size());
		
		a.remove();
		
		assertEquals(0, graph.numLinks());
		assertEquals(4, graph.size());
	}
	
	@Test
	public void testConnected()
	{
		DTGraph<String, Double> graph = new MapDTGraph<String, Double>();
		
		DTNode<String, Double> a = graph.add("a"),
		                       b = graph.add("b"),
		                       c = graph.add("c");

	
		a.connect(b, 0.5);
		
		assertTrue(a.connected(b));
		assertFalse(a.connected(a));
		assertFalse(b.connected(a));
		assertFalse(a.connected(c));
		assertFalse(c.connected(a));
		assertFalse(b.connected(c));
		assertFalse(c.connected(b));
	}
	
	@Test
	public void testEquals()
	{
		UTGraph<String, String> g1 = new MapUTGraph<String, String>();
		g1.add("a");
		g1.add("b");
		g1.add("c");
		g1.add("d");

		
		g1.node("a").connect(g1.node("b"), "1");
		g1.node("a").connect(g1.node("b"), "2");
		g1.node("a").connect(g1.node("b"), "2");

		g1.node("b").connect(g1.node("c"), "1");
		
		g1.node("c").connect(g1.node("d"), "1");
		g1.node("c").connect(g1.node("d"), "2");
		g1.node("d").connect(g1.node("c"), "2");
		
		UTGraph<String, String> g2 = new MapUTGraph<String, String>();
		g2.add("a");
		g2.add("b");
		g2.add("c");
		g2.add("d");
		 
		g2.node("a").connect(g2.node("b"), "2");
		g2.node("a").connect(g2.node("b"), "1");
		g2.node("a").connect(g2.node("b"), "2");
                             
		g2.node("b").connect(g2.node("c"), "1");		
         
		g2.node("c").connect(g2.node("d"), "2");
		g2.node("c").connect(g2.node("d"), "1");
		g2.node("d").connect(g2.node("c"), "2");
		
		assertEquals(g1, g2);
		
		g2.node("a").connect(g2.node("b"), "2");
	
		assertFalse(g1.equals(g2));
	}
	
}
