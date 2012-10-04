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
		BaseGraph<String> graph = Graphs.ladder(6);
		BaseGraph<String> substructure = Graphs.star(3);
		
		System.out.println(GraphMDL.mdl(graph));
		//for(int threshold : Series.series(20))
		System.out.println(GraphMDL.mdl(graph, substructure, 7));
	}

}
