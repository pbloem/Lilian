package org.lilian.util.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lilian.util.Series;

public class Graphs
{
	
	public <L, N extends Node<L, N>> List<L> label(List<N> nodes)
	{
		// TODO
		return null;
	}

	public <L, N extends Node<L, N>> Collection<L> labelCollection(Collection<N> nodes)
	{
		// TODO
		return null;
	}
	
	public <L, N extends Node<L, N>> Set<L> labelSet(Set<N> nodes)
	{
		// TODO
		return null;
	}
	
	public <L, N extends Node<L, N>> Iterable<L> labelIterable(Iterable<N> nodes)
	{
		// TODO
		return null;
	}
	
	/**
	 * Finds the given path of labels in the graph. 
	 * 
	 * Example use:
	 * <code>
	 * Walks.find(graph, Arrays.asList("a", "b", "c", "d", "e"));
	 * </code>
	 * 
	 * @param <T>
	 * @return
	 */
	public static <L, N extends Node<L, N>> Set<Walk<L, N>> find(Iterable<L> track, Graph<L, ?> graph)
	{
		// TODO
		return null;
	}	
	
	public static <L, N extends Node<L, N>> Set<L> labels(Graph<L, N> graph)
	{
		Set<L> labels = new LinkedHashSet<L>();
		
		for(N node : graph)
			labels.add(node.label());
		
		return labels;
	}
	
	/**
	 * Returns a graph with the same structure and labels as that in the 
	 * argument, but with the nodes in a different order. Ie. this method 
	 * returns a random isomorphism of the argument.
	 * 
	 * @param graph
	 * @return
	 */
	public static <L, N extends Node<L, N>> BaseGraph<L> shuffle(Graph<L, N> graph)
	{
		List<Integer> shuffle = Series.series(graph.size());
		Collections.shuffle(shuffle);
		
		List<N> nodes = new ArrayList<N>(graph);
		List<BaseGraph<L>.Node> outNodes = new ArrayList<BaseGraph<L>.Node>(graph.size());
		
		BaseGraph<L> out = new BaseGraph<L>(); 
		for(int i : shuffle)
		{
			BaseGraph<L>.Node newNode = out.addNode(nodes.get(i).label());
			outNodes.add(newNode);
		}
		
		for(int i : Series.series(graph.size()))
			for(int j : Series.series(i, graph.size()))
			{
				if(nodes.get(shuffle.get(i)).connected(nodes.get(shuffle.get(j))))
					outNodes.get(i).connect(outNodes.get(j));
			}
		
		return out;
	}
}
