package org.lilian.data.real.fractal.random;

import java.util.ArrayList;
import java.util.List;

import org.lilian.Global;
import org.lilian.data.real.Map;
import org.lilian.data.real.Similitude;
import org.lilian.search.Parametrizable;
import org.lilian.util.Series;

/**
 * This class represents a choice for a single instance of a random iterated 
 * function system. 
 * 
 * @author Peter
 *
 */
public class ChoiceTree
{

	// * The number of IFS models
	private int max;
	// * The number of components per IFS
	private int branching;
	
	private int depth;
	
	private Node root;
	
	private <M extends Map & Parametrizable> ChoiceTree(DiscreteRIFS<M> model, int depth)
	{
		branching = -1;
		root = new Node(model, null, depth);
	}
	
	public ChoiceTree(List<Integer> choices, int max, int branching, int depth)
	{
		root = new Node(choices.get(0), null);
		this.branching = branching;
		
		fill(choices, 1, depth, root);
	}
	
	private void fill(List<Integer> choices, int i, int d, Node parent) 
	{
		if(d == 0)
			return;
		
		while(parent.children().size() < branching)
			parent.child(choices.get(i++));
		
		for(Node child : parent.children())
			fill(choices, i, d - 1, child);
			
	}
	
	public Node root()
	{
		return root;
	}
	
	public int branching()
	{
		return branching;
	}
	
	public class Node 
	{
		private int codon;
		private List<Node> children;
		private Node parent;
		
		public Node(int codon, Node parent)
		{
			this.parent = parent;
			this.codon = codon;
			
			children = new ArrayList<Node>(branching);
		}
		
		protected <M extends Map & Parametrizable>
			Node(DiscreteRIFS<M> model, Node parent, int depth)
		{
			codon = model.draw();
			
			if(depth > 0)
			{
				int max = model.models().get(codon).size();
				children = new ArrayList<Node>(max);
				
				for(int i : Series.series(max))
					children.add(new Node(model, this, depth - 1));
			} else
				children = new ArrayList<Node>(0);
			
			
		}
		
		public Node child(int codon)
		{
			if(children.size() >= branching)
				throw new RuntimeException("Node is full. No further children allowed.");
			
			Node child = new Node(codon, this);
			children.add(child);
			
			return child;
		}
		
		public boolean leaf()
		{
			return children.isEmpty();
		}
		
		public int codon()
		{
			return codon;
		}
		
		public List<Node> children()
		{
			return children;
		}
		
		public String toString()
		{
			
			return codon + " _ " + children.toString();
		}	
	}
	
	public String toString()
	{
		return root.toString();
	}	
	
	public static ChoiceTree random(int max, int components, int depth)
	{
		ArrayList<Integer> choices = new ArrayList<Integer>();
		for(int i : Series.series(0, depth))
			for(int j : Series.series(0, (int)Math.pow(components, i)))
				choices.add(Global.random.nextInt(max));
		
		return new ChoiceTree(choices, max, components, depth);
	}
	
	public static <M extends Map & Parametrizable> ChoiceTree 
							random(DiscreteRIFS<M> model, int depth)
	{
		return new ChoiceTree(model, depth);
		
	}
}
