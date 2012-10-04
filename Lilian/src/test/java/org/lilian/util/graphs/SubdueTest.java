package org.lilian.util.graphs;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;
import org.lilian.util.graphs.algorithms.CostFunctions;
import org.lilian.util.graphs.algorithms.InexactCost;
import org.lilian.util.graphs.algorithms.Subdue;

public class SubdueTest
{

	@Test
	public void test()
	{
		BaseGraph<String> in = Graphs.ladder(6);
		
		InexactCost<String> costFunction = CostFunctions.uniform();
		Subdue<String, BaseGraph<String>.Node> sub = 
				new Subdue<String, BaseGraph<String>.Node>(
						in, costFunction, 4.0);
		
		Collection<Subdue<String, BaseGraph<String>.Node>.Substructure> subs =
				sub.search(5, 200, 200, 3);
	}

}
