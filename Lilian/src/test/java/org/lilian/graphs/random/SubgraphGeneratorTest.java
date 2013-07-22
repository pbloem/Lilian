package org.lilian.graphs.random;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import org.junit.Test;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Graphs;
import org.lilian.graphs.Node;
import org.lilian.graphs.Subgraph;
import org.lilian.util.Series;

public class SubgraphGeneratorTest
{

	@Test
	public void testGenerate()
	{
		Graph<String> graph = Graphs.star(20, "x");
		System.out.println(graph);
		
		SubgraphGenerator<String> gen = new SubgraphGenerator<String>(graph, 3);
		
		for(int i : series(10))
		{
			Graph<String> sub = Subgraph.subgraph(graph, gen.generate().nodes());
			
			assertEquals(3, sub.size());
			assertEquals(2, sub.numLinks());
			for(Node<String> node : sub.nodes())
				assertEquals(1, node.degree());
		}
	}

}
