package org.lilian.util.graphs.algorithms;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.lilian.Global;
import org.lilian.util.Functions;
import org.lilian.util.Series;
import org.lilian.util.graphs.BaseGraph;

public class BAGenerator
{

	private static final String LABEL = "x";
	private int initial;
	private int attach;
	
	private BaseGraph<String> graph;
	private List<BaseGraph<String>.Node> nodes = new ArrayList<BaseGraph<String>.Node>();
	
	private List<Double> probabilities = new ArrayList<Double>();
	private double sum = 0.0;
	
	public BAGenerator(int initial, int attach)
	{
		this.initial = initial;
		this.attach = attach;
		
		graph = new BaseGraph<String>();
		
		for(int i : series(initial))
		{
			nodes.add(graph.addNode(LABEL));
			probabilities.add(0.0);
		}
	}
	
	public int draw()
	{
		if(sum == 0.0)
			return Global.random.nextInt(nodes.size());
		
		return Functions.draw(probabilities, sum);
	}
	
	public BaseGraph<String>.Node newNode()
	{
		BaseGraph<String>.Node node = graph.addNode(LABEL);
		
		List<Integer> neighbours = new ArrayList<Integer>(attach);
		for(int i : series(attach))
			neighbours.add(draw());
		
		// System.out.println(neighbours);
		for(int draw : neighbours)
		{
			node.connect(nodes.get(draw));
			probabilities.set(draw, probabilities.get(draw) + 1.0);
			sum++;
		}
		
		nodes.add(node);	
		probabilities.add((double)node.neighbours().size());
		sum += (double)node.neighbours().size();
		
		return node;
	}
	
	public void iterate(int n)
	{
		for(int i : series(n))
			newNode();
	}

	public BaseGraph<String> graph() 
	{
		return graph;
	}
}
