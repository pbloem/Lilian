package org.lilian.util.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.util.Series;
import org.lilian.util.graphs.old.BaseGraph;
import org.lilian.util.graphs.old.Graphs;
import org.lilian.util.graphs.old.algorithms.GraphMDL;

public class GraphMDLTest
{

	@Test
	public void test()
	{
		BaseGraph<String> graph = Graphs.ba(30, 3, 2);
		BaseGraph<String> edge = Graphs.line(2);
		BaseGraph<String> star = Graphs.star(3);

		
		// System.out.println(GraphMDL.mdl(graph));
		// for(int threshold : Series.series(20))
		System.out.println(GraphMDL.mdl(graph, edge, 0.0, true));
		System.out.println(GraphMDL.mdl(graph, star, 0.0, true));
	}

	@Test
	public void test2()
	{
		BaseGraph<String> graph = Graphs.random(20, 0.5);
		BaseGraph<String> substructure = Graphs.line(1);
		
		System.out.println(GraphMDL.mdl(graph));
		System.out.println(GraphMDL.mdl(graph, substructure, 7, false));
	}
	
	@Test
	public void small()
	{	
		System.out.println(GraphMDL.mdl(Graphs.line(0)));
		System.out.println(GraphMDL.mdl(Graphs.line(1)));
		System.out.println(GraphMDL.mdl(Graphs.line(2)));
	}
	
	@Test
	public void jbc()
	{	
		
		BaseGraph<String> graph = Graphs.jbc();
		
		BaseGraph<String> substructure = new BaseGraph<String>();
		// * square 1
		BaseGraph<String>.Node s1x = graph.addNode("x"),
                               s1y = graph.addNode("y"),
                               s1z = graph.addNode("z"),
                               s1q = graph.addNode("q");
		
		System.out.println("--" + GraphMDL.mdl(Graphs.jbc(), Graphs.single("x"), 20.0, false));
		System.out.println("--" + GraphMDL.mdl(Graphs.jbc(), substructure, 20.0, false));

	}
	
}
