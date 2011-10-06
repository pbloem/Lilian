package org.lilian.util.graphs;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
	public static <L, N extends Node<L, N>> Walk<L, N> find(Iterable<L> track, Graph<L, ?> graph)
	{
		// TODO
		return null;
	}	
	
	
	
}
