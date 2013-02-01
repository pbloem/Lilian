package org.lilian.graphs.subdue;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.lilian.Global;
import org.lilian.graphs.Graph;
import org.lilian.graphs.MapUTGraph;
import org.lilian.graphs.Node;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class BAGenerator
{

	private static final String LABEL = "x";
	private int initial;
	private int attach;
	
	private Graph<String> graph;
	
	private List<Node<String>> probabilities = new ArrayList<Node<String>>();
	private double sum = 0.0;
	
	public BAGenerator(int initial, int attach)
	{
		this.initial = initial;
		this.attach = attach;
		
		graph = new MapUTGraph<String, String>();
		
		for(int i : series(initial))
			probabilities.add(graph.add(LABEL));
	}

	public Node<String> newNode()
	{
		Node<String> node = graph.add(LABEL);
		
		// System.out.println(neighbours);
		for(int i : series(attach))
		{
			Node<String> neighbor =
					probabilities.get(Global.random.nextInt(probabilities.size()));
			
			node.connect(neighbor);
			
			probabilities.add(neighbor);
			probabilities.add(node);
		}
		
		return node;
	}
	
	public void iterate(int n)
	{
		for(int i : series(n))
			newNode();
	}

	public Graph<String> graph() 
	{
		return graph;
	}
}
