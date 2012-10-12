package org.lilian.util.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.util.graphs.old.BaseGraph;
import org.lilian.util.graphs.old.Graphs;

public class GraphsTest
{

	@Test
	public void test()
	{
		BaseGraph<String> line = Graphs.line(3);
		
		System.out.println(line);
		assertEquals(3, line.size());
	}
	
	@Test
	public void testLadder()
	{
		int n = 3;
		BaseGraph<String> ladder = Graphs.ladder(2);
		
		System.out.println(ladder);
		assertEquals(n + 2 * (n - 1), ladder.numEdges());
		assertEquals(n*2, ladder.size());
	}

	@Test
	public void testJBC()
	{
		BaseGraph<String> jbc = Graphs.jbc();
		
		System.out.println(jbc);
	}

}
