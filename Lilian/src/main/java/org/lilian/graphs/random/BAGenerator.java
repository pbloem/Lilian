package org.lilian.graphs.random;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lilian.Global;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Graphs;
import org.lilian.graphs.MapUTGraph;
import org.lilian.graphs.Node;
import org.lilian.graphs.UTGraph;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class BAGenerator
{

	private static final String LABEL = "x";
	private int initial;
	private int attach;
	
	private UTGraph<String, String> graph;
	
	private List<Node<String>> probabilities = new ArrayList<Node<String>>();
	private double sum = 0.0;
	
	public BAGenerator(int initial, int attach)
	{
		this.initial = initial;
		this.attach = attach;
		
		graph = Graphs.k(initial, LABEL);
		
		for(int i : series(initial))
			for(int j : series(initial - 1))
				probabilities.add(graph.nodes().get(i));
	}

	/**
	 * Add a node to the graph.
	 * 
	 * Each node is added to m distinct, pre-drawn other nodes, where the 
	 * probability of a node being drawn is proportional to its number of links.
	 * 
	 * @return The new node added to the graph.
	 */
	public Node<String> newNode()
	{
		Node<String> node = graph.add(LABEL);
		
		for(Node<String> neighbor : sample(probabilities, attach))
		{	
			node.connect(neighbor);

			probabilities.add(neighbor);
			probabilities.add(node);
		}
		
		return node;
	}
	
	/**
	 * Returns k distinct random samples from 'values'
	 * 
	 * @param values
	 */
	private static <P> Set<P> sample(List<P> values, int k)
	{
		Set<P> set = new HashSet<P>();
		
		while (set.size() < k)
			set.add(values.get(Global.random.nextInt(values.size())));
		
		return set;
	}
	
	public void iterate(int n)
	{
		for(int i : series(n))
			newNode();
	}

	public UTGraph<String, String> graph() 
	{
		return graph;
	}
}
