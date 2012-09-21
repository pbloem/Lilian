package org.lilian.util.graphs;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.lilian.util.Pair;
import org.lilian.util.graphs.algorithms.UndirectedVF2;

/**
 * A basic implementation of the {@link Graph} interface
 * 
 * 
 * OPEN QUESTIONS:
 * - Should equals be based on object instance or on isomorphism? I think instance
 * 
 * @author peter
 *
 * @param <L>
 */
public class BaseGraph<L> extends AbstractCollection<BaseGraph<L>.Node> 
	implements Graph<L, BaseGraph<L>.Node>
{
	protected Map<L, Set<Node>> nodes = new LinkedHashMap<L, Set<Node>>();
	protected int size = 0;
	protected int numEdges = 0;
	protected long modCount = 0;
	
	public class Node implements org.lilian.util.graphs.Node<L, Node>
	{
		private Set<Node> neighbours = new LinkedHashSet<Node>();
		private L label;
		
		private Integer labelId = null;
		private Long labelIdMod;

		public Node(L label)
		{
			this.label = label;
		}

		@Override
		public Set<Node> neighbours()
		{
			return Collections.unmodifiableSet(neighbours);
		}

		@Override
		public Node neighbour(L label)
		{
			for(Node node : neighbours)
				if(node.equals(label))
					return node;
			
			return null;			 
		}

		@Override
		public L label()
		{
			return label;
		}

		@Override
		public Set<Node> neighbours(L label)
		{
			Set<Node> result = new HashSet<Node>();
			for(Node node: neighbours)
				if(node.equals(label))
					result.add(node);
			
			return result;
			
		}

		@Override
		public void connect(Node other)
		{
			if(this.graph().hashCode() != other.graph().hashCode())
				throw new IllegalArgumentException("Can only connect nodes in the same graph.");
			
			if(connected(other))
				return;
			
			neighbours.add(other);
			other.neighbours.add(this);
			
			numEdges++;
			modCount++;
		}

		@Override
		public void disconnect(Node other)
		{
			if(!connected(other))
				return;
			
			neighbours.remove(other);
			other.neighbours.remove(this);
			
			numEdges--;
			modCount++;	
		}

		@Override
		public boolean connected(Node other)
		{
			return neighbours.contains(other);
		}

		@Override
		public Graph<L, Node> graph()
		{
			return BaseGraph.this;
		}
		
		public int id()
		{
			return ((Object) this).hashCode();
		}
		
		/** 
		 * An id to identify this node among nodes with the same label.
		 * @return
		 */
		public int labelId()
		{
			if(labelIdMod == null || labelIdMod != modCount)
			{
				Set<Node> others = nodes.get(label);
				
				int i = 0;
				for(Node other : others)
				{
					if(other.equals(this))
					{
						labelId = i;
						break;
					}
					i++;
				}
				labelIdMod = modCount;
			}	
			return labelId;
		
		}
		
		public String toString()
		{
			boolean unique = nodes.get(label).size() <= 1;

			return label + (unique ? "" : "_" + labelId());
		}
	}

	@Override
	public Node node(L label)
	{
		Set<Node> n = nodes.get(label);
		if(n == null)
			return null;
	
		return n.iterator().next();
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public Set<Node> nodes(L label)
	{
		Set<Node> n = nodes.get(label);
		if(n == null)
			return Collections.emptySet();
		
		return Collections.unmodifiableSet(n); 
	}

	@Override
	public Iterator<Node> iterator()
	{
		return new BGIterator();
	}
	
	private class BGIterator implements Iterator<Node>
	{
		private static final int BUFFER_SIZE = 10;
		private Deque<Node> buffer = new LinkedList<Node>();
		private Iterator<L> labelIterator;
		private Iterator<Node> nodeIterator;

		public BGIterator()
		{
			labelIterator = nodes.keySet().iterator();

		}
		
		@Override
		public boolean hasNext()
		{
			buffer();
			return ! buffer.isEmpty();
		}

		@Override
		public Node next()
		{
			buffer();
			return buffer.pop();
		}

		@Override
		public void remove()
		{
			// * TODO support?
			throw new UnsupportedOperationException();
		}
		
		public void buffer()
		{
			while(buffer.size() < BUFFER_SIZE)
			{
				
				while(nodeIterator == null || ! nodeIterator.hasNext())
				{
					if(! labelIterator.hasNext())
						return;
					nodeIterator = nodes.get(labelIterator.next()).iterator();
				}
				
				buffer.add(nodeIterator.next());
			}
		}
		
	}

	/**
	 * Returns true if there exists a mapping from nodes in this graph thi nodes 
	 * in the other graph such that their labels are equal (ie. equal() returns 
	 * true) two nodes are connected in this graph if and only if they are
	 * connected in the other graph.
	 *  
	 * @param other
	 * @return
	 */
	public <N extends org.lilian.util.graphs.Node<L, N>> boolean equals(Graph<L, N> other)
	{
		UndirectedVF2<L, N> vf2 = new UndirectedVF2<L, N>((Graph<L, N>) this, other, true);
		
		return vf2.matches();
	}

	@Override
	public int numEdges()
	{
		return numEdges;
	}

	@Override
	public Node addNode(L label)
	{
		size ++;
		modCount++;
		
		Node node = new Node(label);
		if(!nodes.containsKey(label))
			nodes.put(label, new LinkedHashSet<Node>());
		
		nodes.get(label).add(node);
		return node;
	}
	
	/**
	 * Returns true if each label currently describes a unique node. 
	 * 
	 * 
	 * @return
	 */
	public boolean unique()
	{
		for(L label : nodes.keySet())
			if(nodes.get(label).size() > 1)
				return false;
		
		return true;
	}
	
	/**
	 * Returns a representation of the graph in Dot language format.
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("graph {");
		
		Set<Node> done = new HashSet<Node>();
		
		boolean first = true;
		for(L label : nodes.keySet())
			for(Node node : nodes.get(label))
			{
				for(Node neighbour : node.neighbours())
					if(! done.contains(neighbour))
					{
						if(first)
							first = false;
						else
							sb.append("; ");
					
						sb.append(node.toString() + " -- " + neighbour.toString());
					}
				
				done.add(node);
			}
		
		sb.append("}");
		
		return sb.toString();
	}

	@Override
	public boolean connected(L first, L second)
	{
		for(Node f : nodes.get(first))
			for(Node s : nodes.get(second))
				if(f.connected(s))
					return true;
		return false;
	}
	

}
