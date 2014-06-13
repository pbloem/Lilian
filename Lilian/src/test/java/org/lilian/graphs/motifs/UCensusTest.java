package org.lilian.graphs.motifs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nodes.DGraph;
import org.nodes.UGraph;
import org.nodes.random.RandomGraphs;

public class UCensusTest
{

	@Test
	public void testModel()
	{
		UGraph<String> graph = RandomGraphs.random(100, 500);
		
		UCensus<String> c = new UCensus<String>(graph, 3);
		
		System.out.println(c.model());
		System.out.println(c.compact());

	}
	
	@Test
	public void testModelDirected()
	{
		DGraph<String> graph = RandomGraphs.randomDirected(10, 0.3);
		
		System.out.println(graph);
		
		DCensus<String> c = new DCensus<String>(graph, 3);
		
		System.out.println(c.model());
		System.out.println(c.compact());

	}

}
