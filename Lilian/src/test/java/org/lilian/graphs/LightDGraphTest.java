package org.lilian.graphs;

import static org.junit.Assert.*;

import org.junit.Test;

public class LightDGraphTest
{


	@Test
	public void testMapDTGraph()
	{
		DGraph<String> graph = new LightDGraph<String>();
	}

	@Test
	public void testToString()
	{
		DGraph<String> graph = new LightDGraph<String>();
		
		DNode<String> a = graph.add("a"),
		              b = graph.add("b");
		graph.add("c");
	
		a.connect(b);
		
		System.out.println(graph);
	}

	@Test
	public void starTest()
	{
		DGraph<String> graph = new LightDGraph<String>();
		
		DNode<String> a = graph.add(null),
		              b = graph.add(null),
		              c = graph.add(null),
		              d = graph.add(null),
		              e = graph.add(null);
	
		b.connect(a);
		c.connect(a);
		d.connect(a);
		e.connect(a);
		
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
		
		DTNode<String, Double> a = graph.add(null),
		                       b = graph.add(null),
		                       c = graph.add(null),
		                       d = graph.add(null),
		                       e = graph.add(null);
	
		b.connect(a);
		c.connect(a);
		d.connect(a);
		e.connect(a);
		
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
		DGraph<String> graph = new LightDGraph<String>();
		
		DNode<String> a = graph.add(null),
		              b = graph.add(null),
		              c = graph.add(null);

	
		a.connect(b);
		
		assertTrue(a.connected(b));
		assertFalse(a.connected(a));
		assertFalse(b.connected(a));
		assertFalse(a.connected(c));
		assertFalse(c.connected(a));
		assertFalse(b.connected(c));
		assertFalse(c.connected(b));
	}
}
