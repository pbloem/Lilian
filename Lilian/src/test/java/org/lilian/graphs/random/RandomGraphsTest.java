package org.lilian.graphs.random;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.graphs.Graphs;
import org.lilian.graphs.UTGraph;

public class RandomGraphsTest {

	@Test
	public void testRandomSelfLoops() {
		Global.random = new Random();
		
		UTGraph<String, String> graph = RandomGraphs.random(30, 16);
		assertFalse(Graphs.hasSelfLoops(graph));
		
		System.out.println(graph);
		System.out.println(Graphs.shuffle(graph));		
	}

	@Test
	public void testBA() {
		UTGraph<String, String> graph = RandomGraphs.preferentialAttachment(30, 2);
		System.out.println(graph);
		System.out.println(Graphs.shuffle(graph));
	}	
	
}
