package org.lilian.util.graphs;

import static org.junit.Assert.*;

import org.junit.Test;

public class GraphsTest
{

	@Test
	public void test()
	{
		BaseGraph<String> line = Graphs.line(3);
		
		System.out.println(line);
		assertEquals(3, line.size());
	}

}
