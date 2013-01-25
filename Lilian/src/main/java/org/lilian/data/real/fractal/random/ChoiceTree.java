package org.lilian.data.real.fractal.random;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.List;

import org.lilian.Global;
import org.lilian.data.real.Map;
import org.lilian.data.real.Similitude;
import org.lilian.models.BasicFrequencyModel;
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
	private int max = -1;
	// * The number of components per IFS
	private int branching;
	
	private int depth;
	
	private Node root;
	
//	public ChoiceTree(int firstSymbol)
//	{
//		root = new Node(firstSymbol, null);
//	}
	
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
	
	public int depth()
	{
		return depth;
	}
	
	public int branching()
	{
		return branching;
	}
	
	/**
	 * Returns the node denoted by this code. NOTE: The last element in this code 
	 * refers to one of the components of the _parent_ of the Node returned.
	 * 
	 * @param code
	 * @return
	 */
	public Node get(List<Integer> code)
	{
		return root.get(code);
	}
	
	/**
	 * Sets the symbol at the node corresponding to the given code. If any nodes
	 * don't exist, they are created with symbol -1  
	 * 
	 * @param code
	 * @param symbol
	 */
	public void set(List<Integer> code, int symbol)
	{
		root.set(code, symbol);
	}
	
	public void count(BasicFrequencyModel<Integer> model)
	{
		root.count(model);
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
			
			max = max(max, codon);
			
			children = new ArrayList<Node>(branching);
		}
		
		public void count(BasicFrequencyModel<Integer> model)
		{
			model.add(codon);
			
			for(Node child : children)
				child.count(model);
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
			Node child = new Node(codon, this);
			children.add(child);
			
			return child;
		}
		
		public boolean leaf()
		{
			return children.isEmpty();
		}
		
		/**
		 * The codon is the choice of IFS component at this node. If it is -1, 
		 * then no choice information exists at this node.
		 * 
		 * @return
		 */
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
		
		public Node get(List<Integer> code)
		{
			if(code.isEmpty())
				return this;
			
			return get(code.subList(1, code.size()));
		}
		
		public void set(List<Integer> code, int symbol)
		{
			if(code.isEmpty())
			{
				codon = symbol;
				max = max(max, codon);
				return;
			}
			
			int c = code.get(0);
			
			while(children.size() <= c)
				child(-1);
			
			children.get(c).set(code.subList(1, code.size()), symbol);
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
