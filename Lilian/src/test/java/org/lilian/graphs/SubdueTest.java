package org.lilian.graphs;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;
import org.lilian.graphs.random.RandomGraphs;
import org.lilian.graphs.subdue.CostFunctions;
import org.lilian.graphs.subdue.GraphMDL;
import org.lilian.graphs.subdue.InexactCost;
import org.lilian.graphs.subdue.Subdue;

public class SubdueTest
{

	@Test
	public void test()
	{
		UTGraph<String, String> in = 
				Graphs.jbc();
				// RandomGraphs.random(30, 0.5);
				// RandomGraphs.preferentialAttachment(1000, 1);
		
		
		InexactCost<String> costFunction = CostFunctions.uniform();
		Subdue<String, String> sub = 
				new Subdue<String, String>(
						in, costFunction, 0.0, true);
		
		Collection<Subdue<String, String>.Substructure> subs =
				sub.search(5, 300, 20, -1);
		
		System.out.println(GraphMDL.mdl(in));
		for(Subdue<String, String>.Substructure structure : subs)
			System.out.println(structure);
	}

}
