package org.lilian.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.graphs.subdue.CostFunctions;
import org.lilian.graphs.subdue.InexactCost;
import org.lilian.graphs.subdue.InexactMatch;

public class InexactMatchTest
{

	@Test
	public void testMatches()
	{
		UTGraph<String, String> k2 = Graphs.k(2, "x");
		UTGraph<String, String> k3 = Graphs.k(3, "x");
		
		System.out.println(k2);
		System.out.println(k3);
		
		InexactCost<String> cost = CostFunctions.uniform();
		InexactMatch<String, String> im = 
			new InexactMatch<String, String>(
					k2, k3, cost, 4.0);

		
		System.out.println(im.matches());
		System.out.println(im.distance());
		System.out.println(im.bestMatch());
	}

}
