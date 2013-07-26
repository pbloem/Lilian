package org.lilian.graphs.motifs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


import org.lilian.graphs.DGraph;
import org.lilian.graphs.DNode;
import org.lilian.graphs.Subgraph;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.graphs.algorithms.DVF2;

public class DCensus<L>
{
	private DGraph<L> graph;
	private int n;
	
	private BasicFrequencyModel<DGraph<L>> 
		counts = new BasicFrequencyModel<DGraph<L>>(); 
		
	public DCensus(DGraph<L> graph, int n)
	{
		this.graph = graph;
		this.n = n;
		
		run(new ArrayList<DNode<L>>(n), n);
	}
	
	private void run(List<DNode<L>> nodes, int depth)
	{
		if(depth == 0)
		{
			counts.add(Subgraph.dSubgraph(graph, nodes));
			return;
		}
		
		Collection<DNode<L>> neighbors; 
		if(nodes.isEmpty())
		{
			neighbors = (Collection<DNode<L>>) graph.nodes();
		} else {
			neighbors = new HashSet<DNode<L>>();
			for(DNode<L> node : nodes)
				neighbors.addAll(node.neighbors());
			neighbors.removeAll(nodes);
		}
		
		for(DNode<L> node : neighbors)
		{
			nodes.add(node);
			run(nodes, depth - 1);
			nodes.remove(nodes.size() - 1);
		}
	}
	
	public BasicFrequencyModel<DGraph<L>> model()
	{
		return counts;
	}
	
	public BasicFrequencyModel<DGraph<L>> compact()
	{
		BasicFrequencyModel<DGraph<L>> compact = new BasicFrequencyModel<DGraph<L>>();
		
		for(DGraph<L> token : counts.tokens())
		{
			boolean added = false;
			for(DGraph<L> existing : compact.tokens())
			{
				DVF2<L> vf2 = new DVF2<L>(token, existing, false);
				if(vf2.matches())
				{
					compact.add(existing, counts.frequency(token));
					added = true;
					break;
				}
			}
			
			if(! added)
				compact.add(token, counts.frequency(token));
		}
		
		return compact;
	}
	
	
}
