package org.lilian.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.graphs.subdue.CostFunctions;
import org.lilian.graphs.subdue.InexactCost;
import org.lilian.graphs.subdue.InexactSubgraphs;


public class InexactSubgraphsTest
{

	@Test
	public void test()
	{
		UTGraph<String, String> line = Graphs.line(3, "");
		UTGraph<String, String> ladder = Graphs.ladder(6, "");
		
		System.out.println(line);
		System.out.println(ladder);
		
		InexactCost<String> cost = 
				CostFunctions.transformationCost(1, ladder.size(), ladder.numLinks());
		InexactSubgraphs<String, String> is =
				new InexactSubgraphs<String, String>(
						ladder, line, cost, 10.0, false);
		
		System.out.println(is.numMatches());
		System.out.println(is.numLinks());
		System.out.println(is.silhouette());
	}

}
