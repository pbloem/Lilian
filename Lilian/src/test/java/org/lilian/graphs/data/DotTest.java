package org.lilian.graphs.data;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.random.RandomGraphs;

public class DotTest {

	@Test
	public void testRead() 
	{
		Global.random = new Random();
		UTGraph<String, String> graph = RandomGraphs.preferentialAttachment(30, 2);
		
		UTGraph<String, String> out = Dot.read(graph.toString());
		
		assertEquals(graph.size(), out.size());
		assertEquals(graph.numLinks(), out.numLinks());
	}

}
