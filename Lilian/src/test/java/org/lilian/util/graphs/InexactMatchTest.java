package org.lilian.util.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.util.graphs.old.BaseGraph;
import org.lilian.util.graphs.old.Graphs;
import org.lilian.util.graphs.old.algorithms.CostFunctions;
import org.lilian.util.graphs.old.algorithms.InexactCost;
import org.lilian.util.graphs.old.algorithms.InexactMatch;

public class InexactMatchTest
{

	@Test
	public void testMatches()
	{
		BaseGraph<String> k2 = Graphs.k2();
		BaseGraph<String> k3 = Graphs.k3();
		
		InexactCost<String> cost = CostFunctions.uniform();
		InexactMatch<String, BaseGraph<String>.Node> im = 
			new InexactMatch<String, BaseGraph<String>.Node>(
					k2, k3, cost, 4.0);

		
		System.out.println(im.matches());
		System.out.println(im.distance());
		System.out.println(im.bestMatch());
	}

}
