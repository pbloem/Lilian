package org.lilian.util.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.util.Series;
import org.lilian.util.graphs.algorithms.GraphMDL;

public class GraphMDLTest
{

	@Test
	public void test()
	{
		BaseGraph<String> graph = Graphs.star(3);
		BaseGraph<String> substructure = Graphs.line(1);
		
		System.out.println(GraphMDL.mdl(graph));
		//for(int threshold : Series.series(20))
		System.out.println(GraphMDL.mdl(graph, substructure, 7));
	}

	@Test
	public void test2()
	{
		BaseGraph<String> graph = Graphs.random(20, 0.5);
		BaseGraph<String> substructure = Graphs.line(1);
		
		System.out.println(GraphMDL.mdl(graph));
		System.out.println(GraphMDL.mdl(graph, substructure, 7));
	}
	
	@Test
	public void small()
	{	
		System.out.println(GraphMDL.mdl(Graphs.line(0)));
		System.out.println(GraphMDL.mdl(Graphs.line(1)));
		System.out.println(GraphMDL.mdl(Graphs.line(2)));
	}
	
}
