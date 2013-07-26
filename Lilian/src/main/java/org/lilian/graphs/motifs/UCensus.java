package org.lilian.graphs.motifs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lilian.graphs.Graph;
import org.lilian.graphs.Node;
import org.lilian.graphs.Subgraph;
import org.lilian.graphs.UGraph;
import org.lilian.graphs.UNode;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Series;
import org.lilian.graphs.algorithms.UTVF2;
import org.lilian.graphs.algorithms.UVF2;

public class UCensus<L>
{
	private UGraph<L> graph;
	private int n;
	
	private BasicFrequencyModel<UGraph<L>> 
		counts = new BasicFrequencyModel<UGraph<L>>(); 
		
	public UCensus(UGraph<L> graph, int n)
	{
		this.graph = graph;
		this.n = n;
		
		run(new ArrayList<UNode<L>>(n), n);
	}
	
	private void run(List<UNode<L>> nodes, int depth)
	{
		if(depth == 0)
		{
			counts.add(Subgraph.uSubgraph(graph, nodes));
			return;
		}
		
		Collection<UNode<L>> neighbors; 
		if(nodes.isEmpty())
		{
			neighbors = (Collection<UNode<L>>) graph.nodes();
		} else {
			neighbors = new HashSet<UNode<L>>();
			for(UNode<L> node : nodes)
				neighbors.addAll(node.neighbors());
			neighbors.removeAll(nodes);
		}
		
		for(UNode<L> node : neighbors)
		{
			nodes.add(node);
			run(nodes, depth - 1);
			nodes.remove(nodes.size() - 1);
		}
	}
	
	public BasicFrequencyModel<UGraph<L>> model()
	{
		return counts;
	}
	
	public BasicFrequencyModel<UGraph<L>> compact()
	{
		BasicFrequencyModel<UGraph<L>> compact = new BasicFrequencyModel<UGraph<L>>();
		
		for(UGraph<L> token : counts.tokens())
		{
			boolean added = false;
			for(UGraph<L> existing : compact.tokens())
			{
				UVF2<L> vf2 = new UVF2<L>(token, existing, false);
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
