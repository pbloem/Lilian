package org.lilian.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.graphs.random.RandomGraphs;
import org.lilian.graphs.subdue.GraphMDL;
import org.lilian.util.Series;

public class GraphMDLTest
{

	@Test
	public void test()
	{
		UTGraph<String, String> graph = RandomGraphs.preferentialAttachment(100, 2);
		UTGraph<String, String> edge  = Graphs.line(2, "x");
		UTGraph<String, String> star  = Graphs.star(3, "x");

		
		// System.out.println(GraphMDL.mdl(graph));
		// for(int threshold : Series.series(20))
		System.out.println(GraphMDL.mdl(graph, edge, 0.0, true));
		System.out.println(GraphMDL.mdl(graph, star, 0.0, true));
	}

	@Test
	public void test2()
	{
		UTGraph<String, String> graph = RandomGraphs.random(20, 0.5);
		UTGraph<String, String> substructure = Graphs.line(1, "");
		
		System.out.println(GraphMDL.mdl(graph));
		System.out.println(GraphMDL.mdl(graph, substructure, 7, false));
	}
	
	@Test
	public void small()
	{	
		System.out.println(GraphMDL.mdl(Graphs.line(0, "x")));
		System.out.println(GraphMDL.mdl(Graphs.line(1, "x")));
		System.out.println(GraphMDL.mdl(Graphs.line(2, "x")));
	}
	
	@Test
	public void jbc()
	{	
		boolean sparse = true;
		
		
		UTGraph<String, String> substructure = new MapUTGraph<String, String>();
		// * square 1
		UTNode<String, String> s1x = substructure.add("x"),
                               s1y = substructure.add("y"),
                               s1z = substructure.add("z"),
                               s1q = substructure.add("q");
		
		UTGraph<String, String> substructure2 = new MapUTGraph<String, String>();
		// * square 1
		UTNode<String, String> c0 = substructure2.add("c"),
                               c1 = substructure2.add("c");
		
		System.out.println("--" + GraphMDL.mdl(Graphs.jbc(), Graphs.single("x"), 4.0, sparse));
		System.out.println("--" + GraphMDL.mdl(Graphs.jbc(), substructure, 4.0, sparse));
		System.out.println("--" + GraphMDL.mdl(Graphs.jbc(), substructure2, 4.0, sparse));

	}
	
}
