package org.lilian.graphs.subdue;

import java.util.Collection;
import java.util.List;

import org.lilian.graphs.UTGraph;
import org.lilian.graphs.subdue.Wrapping.TagToken;
import org.lilian.graphs.subdue.Wrapping.Token;
import org.lilian.util.Pair;

public class FindRules
{

	public static <L, T> void findRules(UTGraph<L, T> graph, int depth)
	{
		if(depth == 0)
			return;
		InexactCost<L> costFunction = CostFunctions.uniform();
		Subdue<L, T> subdue = new Subdue<L, T>(graph, costFunction, 0.0, 25);
		Collection<Subdue<L, T>.Substructure> c = subdue.search(4, 100, 10, 5);
		
		Subdue<L, T>.Substructure sub = c.iterator().next();
		
		UTGraph<L, T> template = sub.subgraph();
		UTGraph<Token, TagToken> sil =sub.silhouette();
		
		System.out.println(template + ", " + sil);
		
		findRules(sil, depth- 1);
	}
}
