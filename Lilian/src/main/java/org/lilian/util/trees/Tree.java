package org.lilian.util.trees;

import java.util.*;

import org.lilian.corpora.OVISCorpus;

/**
 * This class represents a tree of Objects.
 *
 * The class is created by passing an initial object, which becomes the root
 * node. After creation, getRoot can be called to retrieve the root node.
 * Children can then be added as well.
 *
 * Example use:<pre>
 *      Tree&lt;String&gt; t = new Tree&lt;String&gt;("r");
 *      Tree&lt;String&gt;.Node node, n0, n1, n2;
 *
 *      node = t.getRoot();
 *
 *      n0 = node.addChild("r0");
 *      n1 = node.addChild("r1");
 *
 *      n0.addChild("r00");
 * </pre>
 * 
 * TODO
 * <ul>
 *  <li /> This class needs a lot of unit tests
 *  <li /> Implement {@link Collection}
 *  <li /> Consider the possibility of turning this into an interface with 
 *         various implementations (a la {@link Collection}), focusing on different performance in 
 *         different taks. See also {@link org.lilian.util.trees.DOPTree DOPTree} 
 * 	<li /> Provide list views for the nodes (<tt>List&lt;Tree&lt;T&gt;.Node&gt;</tt>) and 
 * 	       the types (<code>List&lt;T&gt;</code>). We
 *         can pass the type of walk as a parameter. If this is done, the 
 * 	       nodeIterator method can go.
 *  <li /> Maintain a separate modcount for each node, so not all cached values 
 *  	   need to be recalculated after an update.
 *  <li /> There's a theoretical possibility that the modcount wraps around, 
 *         becomes -1 and cached values won't be updated.
 * </ul>
 */

public class Tree<T> implements Iterable<Tree<T>.Node>
{
	private Node root;
	private int numNodes;
	
	protected int modCount = 0;

	public Tree(T root)
	{
		this.root = new Node(root, 0);
		numNodes = 1;
	}

	public Node getRoot()
	{
		return root;
	}
	
	/**
	 * Returns an iterator over the nodes of this tree in pre-order traversal
	 * (from root to leaves and from left to right)
	 */
	public Iterator<Node> iterator()
	{
		return new NodeIterator();
	}

	/**
	 * Returns an iterator over the values of this tree in pre-order traversal
	 * (from root to leaves and from left to right)
	 */	
	public Iterator<T> valueIterator()
	{
		return new ValueIterator();
	}

	/**
	 * Returns the number of nodes in this tree
	 */
	public int size()
	{
		return numNodes;
	}
	
	/**
	 * Returns a list of leaves in order from left to right.
	 *
	 * The order is equal to the order of a preorder traversal,
	 * minus the internal nodes and the root. It this is a parse
	 * tree, this method returns the sentence.
	 *
	 * @return The leaves of this tree, from left to right
	 */
	public List<T> getLeaves()
	{
		List<T> list = new Vector<T>();
		getLeaves(root, list);
		return list;
	}

	private void getLeaves(Node node, List<T> list)
	{
		if(node.getChildren().size() == 0)
		{
			list.add(node.getValue());
		}else
		{
			Iterator<Node> it = node.getChildren().iterator();

			while(it.hasNext())
				getLeaves(it.next(), list);
		}
	}

	public String toString()
	{
		StringBuilder s = new StringBuilder();
		toString(root, s);

		return s.toString();
	}

	private void toString(Node n, StringBuilder s)
	{
		s.append('(');
		s.append(n.getValue());
		s.append(", [");;

		// add children
		boolean first = true;
		for(Node child : n.getChildren())
		{
			if(first)
				first = false;
			else
				s.append(", ");
			
			toString(child, s);
		}

		s.append("])");
	}

	public class Node{
		
		private int index; 
		private Node parent = null;
		private Vector<Node> children;
		private T value;
		
		/* cached value for the number of subtrees */
		private int subtrees;
		
		/* the modCount the last time the number of subtrees was calculated */
		private int subtreesModCount = -1;
		/* the modCount the last time the hashCode was calculated */		
		private int hashCodeModCount = -1;
		private int hashCode;

		/**
		 * Creates a new node.
		 * 
		 * @param value The value that this node holds
		 * @param index The index of this node among its siblings.
		 */
		private Node(T value, int index)
		{
			this.value = value;
			this.index = index;
			children = new Vector<Node>();
		}

		private void setParent(Node parent)
		{
			this.parent = parent;
		}

		public T getValue()
		{
			return value;
		}
		
		public void setValue(T value)
		{
			this.value = value;
			modCount++;
		}		

		public Node getParent()
		{
			return parent;
		}

		public List<Node> getChildren()
		{
			return Collections.unmodifiableList(children);
		}
		
		public boolean isRoot()
		{
			return (parent == null);
		}

		public boolean isLeaf()
		{
			return (children.size() == 0);
		}

		/**
		 * Return the number of subtrees the 
		 */
		public int getSubtreeCount()
		{
			if(modCount != subtreesModCount)
				recalculateSubtrees();
			
			return subtrees;
		}
		
		private void recalculateSubtrees()
		{
			if( isLeaf() )
			{
				subtrees = 0;
				subtreesModCount = modCount;
				return;
			}
				
			int result = 1;
			for(Node child : children)
				result *= child.getSubtreeCount() + 1;				
			
			subtrees = result;
			subtreesModCount = modCount;
		}
		
		public String toString()
		{
			return value.toString();
		}

		/**
		 * Adds a value to this node as a child. The method returns
		 * the node that was created for the value.
		 *
		 * @param value The value to add as a child
		 * @return The Node that was created for this value
		 */
		public Node addChild(T value)
		{
			modCount++;

			int index = children.size();
			
			Node node = new Node(value, index);
			node.setParent(this);

			children.add(node);

			//update the nodecount in Tree
			numNodes++;

			return node;
		}
		
		/** 
		 * Remove this node (and its underlying subtree) from this tree
		 * 
		 * @returns This nodes value
		 * @throws UnsupportedOperationException If this method is called on a 
		 * 		root node.
		 */
		public T remove()
		{
			if(isRoot()) // Can't remove root
				throw new UnsupportedOperationException("Can't remove root node");
			
			children = null; // (help the gc)
			
			parent.children.remove(index);
			modCount++;
			
			// reset the indices of the other nodes
			for(int i = 0; i < parent.children.size(); i++)
				parent.children.get(i).index = i;
			
			return value;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj)
		{
			/* Check if the classes match */
			if(! (obj instanceof Tree.Node))
				return false;
			
			/* Check if the hashcodes match */
			if(this.hashCode != obj.hashCode())
				return false;
			
			Tree.Node other = (Tree.Node)obj;		

			/* Check if the values match */
			if(this.value == null && other.value != null || 
						this.value != null && other.value == null )
				return false;
			
			if(this.value != null && ! this.value.equals(other.value))
				return false;
		
			/* Checl if the number of children match */
			if(this.children.size() != other.children.size())
				return false;

			/* Check if the children all match */ 
			for(int i = 0; i < children.size(); i++)
				if(! this.children.get(i).equals(other.children.get(i)))
					return false;
				
			/* All checks passed, subtrees are equal */
			return true;
		}

		@Override
		public int hashCode()
		{
			/* Recalculate the hashCode if necessary		*/
			if(hashCodeModCount != modCount)
			{
				hashCode = 0;
				
				for(Node child : children)
					hashCode += 31 * child.hashCode();
				
				hashCode += 31 * (value == null ? 0 : value.hashCode());
				hashCodeModCount = modCount;
			}
			
			return hashCode;
		}
	}

	/**
	 * FIXME: check for modifications of the tree and throw 
	 * ConcurrentModificationException
	 */
	private class NodeIterator implements Iterator<Node>
	{
		LinkedList<Node> buffer = new LinkedList<Node>();
		
		public NodeIterator()
		{
			buffer.add(root);			
		}

		@Override
		public boolean hasNext()
		{
			return ! buffer.isEmpty();
		}

		@Override
		public Node next()
		{
			if(buffer.isEmpty())
				throw new NoSuchElementException();
			
			
			
			Node result = buffer.remove(0);

			int i = 0;
			for(Node child : result.children)
				buffer.add(i++, child);
			
			return result;			
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();			
		}
	}

	/**
	 * 
	 */
	private class ValueIterator implements Iterator<T>
	{
		NodeIterator backIt = new NodeIterator();

		@Override
		public boolean hasNext()
		{
			return backIt.hasNext();
		}

		@Override
		public T next()
		{
			return backIt.next().getValue(); 
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();			
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if(! (obj instanceof Tree))
			return false;

		Tree other = (Tree)obj;
		
		/* If the hashcodes aren't equal, we know the objects aren't either.
		 * We use this to eliminate most of the cases quickly (since the 
		 * hashcode is cached) 
		 */
		if(other.hashCode() != this.hashCode())
			return false;
		
		/* if the hashcodes are equal, we need to check the whole tree 
		 * (recursively) */
		return root.equals(other.root);
	}

	@Override
	public int hashCode()
	{
		return 31 + root.hashCode(); 
	}
	
	public static void main(String[] args)
	{
		Tree<String> tree = OVISCorpus.parseTree("(S, [(NP-SBJ, [(DT, [(the, [])]), (NNS, [(computers, [])])]), (VP, [(VBD, [(were, [])]), (ADJP-PRD, [(JJ, [(crude, [])])]), (PP, [(IN, [(by, [])]), (NP, [(NP, [(NN, [(today, [])]), (POS, [('s, [])])]), (NNS, [(standards, [])])])])])])", false);

		Iterator<Tree<String>.Node> it = tree.iterator();
		while(it.hasNext())
		{
			Tree<String>.Node node = it.next();
			if(node.isLeaf()) System.out.println(node.getValue());
		}
	}

}
