package org.lilian.util.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.util.graphs.algorithms.CostFunctions;
import org.lilian.util.graphs.algorithms.InexactCost;
import org.lilian.util.graphs.algorithms.InexactSubgraphs;

public class InexactSubgraphsTest
{

	@Test
	public void test()
	{
		BaseGraph<String> line = Graphs.line(1);
		BaseGraph<String> ladder = Graphs.ladder(6);
		
		System.out.println(line);
		System.out.println(ladder);
		
		InexactCost<String> cost = CostFunctions.transformationCost(1, ladder.size(), ladder.numEdges());
		InexactSubgraphs<String, BaseGraph<String>.Node> is =
				new InexactSubgraphs<String, BaseGraph<String>.Node>(
						ladder, line, cost, 10.0);
		
		System.out.println(is.numMatches());
		System.out.println(is.numLinks());
		System.out.println(is.silhouette());
	}

}
