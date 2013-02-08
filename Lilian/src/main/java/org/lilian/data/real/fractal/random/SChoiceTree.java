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
 * A stochastic variant of a choice tree. Each node stores a probability 
 * distribution over codons.
 * 
 * @author Peter
 *
 */
public class SChoiceTree
{

	// * The number of IFS models
	private int max = -1;
	
	// * The number of components per IFS
	private int branching;
	
	private int depth;
	
	private Node root;
	
	public SChoiceTree(int branching, int max, int depth)
	{
		this.max = max;
		this.branching = branching;
		root = new Node(null, depth);
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
	 * Sets the symbol at the node corresponding to the given code. 
	 * 
	 * @param code
	 * @param symbol
	 */
	public void set(List<Integer> code, int symbol, double weight)
	{
		get(code).set(symbol, weight);
	}
	
	public void count(BasicFrequencyModel<Integer> model)
	{
		root.count(model);
	}
	
	public class Node 
	{
		private BasicFrequencyModel<Integer> codon = new BasicFrequencyModel<Integer>();

		private List<Node> children;
		private Node parent;
		
		public Node(Node parent)
		{
			this.parent = parent;
			children = new ArrayList<Node>(branching);
		}
		
		public void clear()
		{
			codon = new BasicFrequencyModel<Integer>();
		}
		
		protected Node(Node parent, int depth)
		{
			
			for(int i : Series.series(max))
				codon.add(i, 1.0/max);
			
			if(depth > 0)
			{
				children = new ArrayList<Node>(max);
				
				for(int i : Series.series(max))
					children.add(new Node(this, depth - 1));
			} else
				children = new ArrayList<Node>(0);
		}

		
		public void count(BasicFrequencyModel<Integer> model)
		{
			for(Integer token : codon.tokens())
				model.add(token, codon.frequency(token));
			
			for(Node child : children)
				child.count(model);
		}
		
		public boolean leaf()
		{
			return children.isEmpty();
		}
		
		/**
		 * The codon is the choice of IFS component at this node. 
		 */
		public BasicFrequencyModel<Integer> codon()
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
		
		public void set(int symbol, double weight)
		{
			codon.add(symbol, weight);
			max = Math.max(max, symbol);
		}
		
		public SChoiceTree tree()
		{
			return SChoiceTree.this;
		}
	}
	
	public int max()
	{
		return max;
	}
	
	public String toString()
	{
		return root.toString();
	}	
}
