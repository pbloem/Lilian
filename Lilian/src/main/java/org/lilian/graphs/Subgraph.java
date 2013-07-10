package org.lilian.graphs;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO: This is a polymorphism nightmare. It's too easy to call the wrong method.
 * @author Peter
 *
 */
public class Subgraph
{
	/**
	 * Generates a subgraph containing the given nodes, 
	 * and any links existing between them in the original graph.
	 * 
	 * @param graph
	 * @param nodes
	 * @return
	 */
	public static <L, T> UTGraph<L, T> subgraph(UTGraph<L, T> graph, Collection<UTNode<L, T>> nodes)
	{
		List<UTNode<L, T>> list = new ArrayList<UTNode<L,T>>(nodes);
		
		UTGraph<L, T> out = new MapUTGraph<L, T>();
		for(UTNode<L, T> node : list)
			out.add(node.label());
		
		for(int i : series(list.size()))
			for(int j : series(i, list.size()))
				for(T tag : graph.tags())
					if(list.get(i).connected(list.get(j), tag))
						out.nodes().get(i).connect(out.nodes().get(j), tag);
		
		return out;
	}
	
	/**
	 * Generates a subgraph containing the nodes at the provided indices, 
	 * and any links existing between them in the original graph.
	 * 
	 * @param graph
	 * @param nodes
	 * @return
	 */
	public static <L, T> UTGraph<L, T> subgraphIndices(UTGraph<L, T> graph, Collection<Integer> nodes)
	{
		List<UTNode<L, T>> list = new ArrayList<UTNode<L,T>>();
		for(int i : nodes)
			list.add(graph.nodes().get(i));
		
		return subgraph(graph, list);
	}
	
	/**
	 * Generates a subgraph containing the given nodes, 
	 * and any links existing between them in the original graph.
	 * 
	 * @param graph
	 * @param nodes
	 * @return
	 */
	public static <L, T> DTGraph<L, T> subgraph(DTGraph<L, T> graph, Collection<DTNode<L, T>> nodes)
	{
		List<DTNode<L, T>> list = new ArrayList<DTNode<L,T>>(nodes);
		
		DTGraph<L, T> out = new MapDTGraph<L, T>();
		for(DTNode<L, T> node : list)
			out.add(node.label());
		
		for(int i : series(list.size()))
			for(int j : series(list.size()))
				for(T tag : graph.tags())
					if(list.get(i).connected(list.get(j), tag))
						out.nodes().get(i).connect(out.nodes().get(j), tag);
		
		return out;
	}
	
	/**
	 * Generates a subgraph containing the nodes at the provided indices, 
	 * and any links existing between them in the original graph.
	 * 
	 * @param graph
	 * @param nodes
	 * @return
	 */
	public static <L, T> DTGraph<L, T> subgraphIndices(DTGraph<L, T> graph, Collection<Integer> nodes)
	{
		List<DTNode<L, T>> list = new ArrayList<DTNode<L,T>>();
		for(int i : nodes)
			list.add(graph.nodes().get(i));
		
		return subgraph(graph, list);
	}
	
	public static <L> DGraph<L> subgraph(DGraph<L> graph, Collection<DNode<L>> nodes)
	{
		List<DNode<L>> list = new ArrayList<DNode<L>>(nodes);
		
		DGraph<L> out = new MapDTGraph<L, String>();
		for(Node<L> node : list)
			out.add(node.label());
		
		for(int i : series(list.size()))
			for(int j : series(list.size()))
				if(list.get(i).connected(list.get(j)))
					out.nodes().get(i).connect(out.nodes().get(j));
		
		return out;
	}
	
	public static <L> DGraph<L> subgraphIndices(DGraph<L> graph, Collection<Integer> nodes)
	{
		List<DNode<L>> list = new ArrayList<DNode<L>>();
		for(int i : nodes)
			list.add(graph.nodes().get(i));
		
		return subgraph(graph, list);
	}
	
	public static <L> Graph<L> subgraph(Graph<L> graph, Collection<Node<L>> nodes)
	{
		List<Node<L>> list = new ArrayList<Node<L>>(nodes);
		
		Graph<L> out = new MapUTGraph<L, String>();
		for(Node<L> node : list)
			out.add(node.label());
		
		for(int i : series(list.size()))
			for(int j : series(list.size()))
				if(list.get(i).connected(list.get(j)))
					out.nodes().get(i).connect(out.nodes().get(j));
		
		return out;
	}
	
	public static <L> Graph<L> subgraphIndices(Graph<L> graph, Collection<Integer> nodes)
	{
		List<Node<L>> list = new ArrayList<Node<L>>();
		for(int i : nodes)
			list.add(graph.nodes().get(i));
		
		return subgraph(graph, list);
	}
}
