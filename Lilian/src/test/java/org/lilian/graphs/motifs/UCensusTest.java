package org.lilian.graphs.motifs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.graphs.DGraph;
import org.lilian.graphs.UGraph;
import org.lilian.graphs.random.RandomGraphs;

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
		DGraph<String> graph = RandomGraphs.randomDirected(200, 0.3);
		
		System.out.println(graph);
		
		DCensus<String> c = new DCensus<String>(graph, 3);
		
		System.out.println(c.model());
		System.out.println(c.compact());

	}

}
