package org.lilian.util.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.util.graphs.algorithms.CostFunctions;
import org.lilian.util.graphs.algorithms.InexactCost;
import org.lilian.util.graphs.algorithms.Subdue;

public class SubdueTest
{

	@Test
	public void test()
	{
		BaseGraph<String> in = Graphs.line(10);
		
		InexactCost<String> costFunction = CostFunctions.uniform();
		Subdue<String, BaseGraph<String>.Node> sub = 
				new Subdue<String, BaseGraph<String>.Node>(
						in, costFunction, 4.0);
	}

}
