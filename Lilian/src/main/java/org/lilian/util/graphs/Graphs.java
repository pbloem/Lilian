package org.lilian.util.graphs;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lilian.Global;
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
	
		
	public static BaseGraph<String> k2()
	{
		BaseGraph<String> graph = new BaseGraph<String>();
		
		BaseGraph<String>.Node n1 = graph.addNode("a");
		BaseGraph<String>.Node n2 = graph.addNode("b");
		
		n1.connect(n2);
		
		return graph;
	}
	
	public static BaseGraph<String> k3()
	{
		BaseGraph<String> graph = new BaseGraph<String>();
		
		BaseGraph<String>.Node n1 = graph.addNode("a");
		BaseGraph<String>.Node n2 = graph.addNode("b");
		BaseGraph<String>.Node n3 = graph.addNode("c");
		
		n1.connect(n2);
		n2.connect(n3);
		n3.connect(n1);
		
		return graph;
	}	
	
	public static BaseGraph<String> line(int n)
	{
		BaseGraph<String> graph = new BaseGraph<String>();

		if(n == 0)
			return graph;
			
		BaseGraph<String>.Node last = graph.addNode("."), next;
		for(int i : series(n-1))
		{
			next = graph.addNode(".");
			last.connect(next);
			last = next;
		}
		
		return graph;
	}
	
	public static BaseGraph<String> star(int n)
	{
		BaseGraph<String> graph = new BaseGraph<String>();
			
		BaseGraph<String>.Node center = graph.addNode(".");
		for(int i : Series.series(n))
			center.connect(graph.addNode("."));
		
		return graph;
	}
	
	public static BaseGraph<String> ladder(int n)
	{
		BaseGraph<String> graph = new BaseGraph<String>();

		if(n == 0)
			return graph;
			
		BaseGraph<String>.Node lastLeft = graph.addNode("."),
		                       lastRight = graph.addNode("."),				
		                       nextLeft, nextRight;
		lastLeft.connect(lastRight);
		
		for(int i : series(n-1))
		{
			nextRight = graph.addNode(".");
			nextLeft  = graph.addNode(".");
			
			nextLeft.connect(nextRight);
			
			nextRight.connect(lastRight);
			nextLeft.connect(lastLeft);
			
			lastLeft = nextLeft;
			lastRight = nextRight;
		}
		
		return graph;
	}
	
	public static BaseGraph<String> random(int n, double prob)
	{
		BaseGraph<String> graph = new BaseGraph<String>();
		List<BaseGraph<String>.Node> nodes = new ArrayList<BaseGraph<String>.Node>(n);

		for(int i : series(n))
			nodes.add(graph.addNode("."));
		
		for(int i : series(n))
			for(int j : series(i+1, n))
				if(Global.random.nextDouble() < prob)
					nodes.get(i).connect(nodes.get(j));
		
		return graph;
	}
	
	
}
