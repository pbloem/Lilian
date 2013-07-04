package org.lilian.graphs;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.lilian.util.Pair;
import org.lilian.util.Series;

/**
 * A light-weight (ie. low memory) implementation of a directed graph.
 * 
 * This graph uses non-persistent objects for nodes and links. This means that 
 * these objects become invalid if the graph is edited after their creation, 
 * unless the edit operation is performed on the object itself.  
 * 
 * @author Peter
 *
 * @param <L>
 */
public class LightDGraph<L> implements DGraph<L>
{
	// * the initial capacity reserved for neighbors
	public static final int NEIGHBOR_CAPACITY = 5;
	
	private List<L> labels;
	
	private List<List<Integer>> out;
	private List<List<Integer>> in;	
	
	private int numLinks = 0;
	private long modCount = 0;
	
	public LightDGraph()
	{
		this(16);
	}
	
	public LightDGraph(int capacity)
	{
		out = new ArrayList<List<Integer>>(capacity);
		in = new ArrayList<List<Integer>>(capacity); 
	}
	
	@Override
	public int size()
	{
		return labels.size();
	}

	@Override
	public int numLinks()
	{
		return numLinks;
	}

	@Override
	public DNode<L> node(L label)
	{
		int i = labels.indexOf(label);
		if(i == -1)
			throw new NoSuchElementException("Graph does not contain node with label "+label+"");
		
		return new LightDNode(i);
	}
	
	private class LightDNode implements DNode<L>
	{
		private Integer index;
		// The modCount of the graph for which this node is safe to use
		private long graphState = modCount;
		private boolean dead = false;

		public LightDNode(int index)
		{
			this.index = index;
		}
		
		@Override
		public L label()
		{
			return labels.get(index);
		}

		@Override
		public void remove()
		{
			check();
			
			for(List<Integer> neighbours : out)
			{
				neighbours.remove(index);
				numLinks --;
			}
			for(List<Integer> neighbours : in)
			{
				neighbours.remove(index);
				numLinks --;
			}

			
			out.remove((int)index);
			out.remove((int)index);

			dead = true;
		}

		private void check()
		{
			if(dead)
				throw new IllegalStateException("Node is dead (index was "+index+")");
			
			if(modCount != graphState)
				throw new IllegalStateException("Graph was modified since node creation.");
		}

		@Override
		public boolean dead()
		{
			return dead;
		}

		@Override
		public int degree()
		{
			return inDegree() + outDegree();
		}

		@Override
		public Collection<? extends DNode<L>> neighbors()
		{
			List<Integer> indices = new ArrayList<Integer>(degree());
			
			for(int i : in.get(this.index))
				indices.add(i);
			for(int i : out.get(this.index))
				indices.add(i);
			
			return new NodeList(indices);
		}

		@Override
		public DNode<L> neighbor(L label)
		{
			for(int i : in.get(this.index))
				if(eq(labels.get(i), label))
					return new LightDNode(index);
			for(int i : out.get(this.index))
				if(eq(labels.get(i), label))
					return new LightDNode(index);
			
			return null;
		}

		@Override
		public Collection<? extends DNode<L>> neighbors(L label)
		{
			List<Integer> indices = new ArrayList<Integer>(degree());
	
			for(int i : in.get(this.index))
				if(eq(labels.get(i), label))
					indices.add(i);
			for(int i : out.get(this.index))
				if(eq(labels.get(i), label))
					indices.add(i);
			
			return new NodeList(indices);
		}

		@Override
		public Collection<? extends DNode<L>> out()
		{
			List<Integer> indices = new ArrayList<Integer>(outDegree());
			
			for(int i : in.get(this.index))
					indices.add(i);
			
			return new NodeList(indices);
		}

		@Override
		public Collection<? extends DNode<L>> out(L label)
		{
			List<Integer> indices = new ArrayList<Integer>(outDegree());
			
			for(int i : out.get(this.index))
				if(eq(labels.get(i), label))
					indices.add(i);
			
			return new NodeList(indices);
		}

		@Override
		public Collection<? extends DNode<L>> in()
		{
			List<Integer> indices = new ArrayList<Integer>(inDegree());
			
			for(int index : in.get(this.index))
				indices.add(index);
			
			return new NodeList(indices);
		}

		@Override
		public Collection<? extends DNode<L>> in(L label)
		{
			List<Integer> indices = new ArrayList<Integer>(inDegree());
			
			for(int i : in.get(this.index))
				if(eq(labels.get(i), label))
					indices.add(i);
			
			return new NodeList(indices);
		}

		@Override
		public void connect(Node<L> to)
		{

			int fromIndex = index, toIndex = to.index();
			
			out.get(fromIndex).add(toIndex);
			in.get(toIndex).add(fromIndex);
			
			Collections.sort(out.get(fromIndex));
			Collections.sort(out.get(toIndex));
			
			modCount++;
			
			// This node object is still safe to use
			graphState = modCount;
			
			numLinks ++;
		}

		@Override
		public void disconnect(Node<L> other)
		{
			int mine = index, his = other.index();
			
			int links = 0;
			
			while(out.get(mine).remove((Integer)his))
				links++;
			while(out.get(his).remove((Integer)mine))
				links++;
			
			while(in.get(mine).remove((Integer)his));
			while(in.get(his).remove((Integer)mine));

			numLinks -= numLinks;			
			modCount++;
		}

		@Override
		public boolean connected(Node<L> to)
		{
			int mine = index, his = to.index();
			if(out.get(mine).contains(his))
				return true;
			if(out.get(his).contains(mine))
				return true;
			
			return false;
		}

		@Override
		public DGraph<L> graph()
		{
			return LightDGraph.this;
		}

		@Override
		public int index()
		{
			return index;
		}

		@Override
		public int inDegree()
		{
			return in.get(index).size();
		}

		@Override
		public int outDegree()
		{
			return out.get(index).size();
		}
	}
	
	private class LightDLink implements DLink<L>
	{
		private DNode<L> from, to;
		
		private long graphState = modCount;
		
		private boolean dead = false;
		
		public LightDLink(int from, int to)
		{
			this.from = new LightDNode(from);
			this.to = new LightDNode(to);
		}
		
		@Override
		public Collection<? extends Node<L>> nodes()
		{
			return Arrays.asList(from, to);
		}

		@Override
		public Graph<L> graph()
		{
			return LightDGraph.this;
		}

		@Override
		public void remove()
		{
			in.get(to.index()).remove((Integer)from.index());
			out.get(from.index()).remove((Integer)to.index());
			
			modCount++;
			dead = true;
		}

		@Override
		public boolean dead()
		{
			return dead;
		}

		@Override
		public DNode<L> first()
		{
			return from;
		}

		@Override
		public DNode<L> second()
		{
			return to;
		}
		
	}

	private class NodeList extends AbstractList<DNode<L>>
	{
		private List<Integer> indices;

		public NodeList(List<Integer> indices)
		{
			this.indices = indices;
		}

		@Override
		public LightDNode get(int index)
		{
			return new LightDNode(indices.get(index));
		}

		@Override
		public int size()
		{
			return indices.size();
		}
	}
	
	@Override
	public Collection<? extends DNode<L>> nodes(L label)
	{
		// * count the occurrences so that we can set the ArrayList's capacity 
		//   accurately
		int frq = 0;
		for(L l : labels)
			if(eq(l, label))
				frq++;
			
		List<Integer> indices = new ArrayList<Integer>(frq);
		
		for(int i : Series.series(size()))
			if(eq(labels.get(i), label))
				indices.add(i);
		
		return new NodeList(indices);
	}

	@Override
	public List<? extends DNode<L>> nodes()
	{
		return new NodeList(Series.series(size()));
	}
	
	@Override
	public Collection<? extends DLink<L>> links()
	{
		return new LinkList();
	}
	
	private class LinkList extends AbstractCollection<DLink<L>>
	{
		@Override
		public Iterator<DLink<L>> iterator()
		{
			return new LLIterator();
		}

		@Override
		public int size()
		{
			return numLinks;
		}
		
		private class LLIterator implements Iterator<DLink<L>>
		{
			private static final int BUFFER_LIMIT = 5;
			private long graphState = state();
			private Deque<Pair<Integer, Integer>> buffer = new LinkedList<Pair<Integer,Integer>>();
			int next = 0;
			
			private void check()
			{
				if(graphState != state())
					throw new ConcurrentModificationException("Graph has been modified.");
			}

			@Override
			public boolean hasNext()
			{
				check();
				read();
				
				return ! buffer.isEmpty();
			}

			@Override
			public DLink<L> next()
			{
				check();
				read();
				
				if(buffer.isEmpty())
					throw new NoSuchElementException();
				
				Pair<Integer, Integer> pair = buffer.pop();
				
				return new LightDLink(pair.first(), pair.second());
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException("Method not supported");
			}
			
			private void read()
			{
				if(next == LightDGraph.this.size())
					return;
				
				while(buffer.size() < BUFFER_LIMIT && next != LightDGraph.this.size())
				{
					int from = next;
					next++;
					
					List<Integer> tos = out.get(from);
					for(int to : tos)
						buffer.add(new Pair<Integer, Integer>(from, to));
				}
					
			}
		}
		
	}

	@Override
	public DNode<L> add(L label)
	{
		labels.add(label);
		
		in.add(new ArrayList<Integer>(NEIGHBOR_CAPACITY));
		out.add(new ArrayList<Integer>(NEIGHBOR_CAPACITY));
		
		return new LightDNode(in.size() - 1);
	}

	@Override
	public Set<L> labels()
	{
		return new HashSet<L>(labels);
	}

	@Override
	public boolean connected(L from, L to)
	{
		for(DNode<L> a : nodes(from))
			for(DNode<L> b : nodes(to))
				if(a.connected(b))
					return true;
		
		return false;
	}

	@Override
	public long state()
	{
		return modCount;
	}
	
	private boolean eq(Object a, Object b)
	{
		if(a == null && b == null)
			return true;
		
		if(a == null || b == null)
			return false;
		
		return a.equals(b);
	}
	
	/**
	 * Resets all neighbour list to their current capacity, plus the 
	 * given margin. 
	 * 
	 * @param margin
	 */
	public void compact(int margin)
	{
		for(int i : Series.series(in.size()))
		{
			List<Integer> old = in.get(i);
			List<Integer> nw = new ArrayList<Integer>(old.size() + margin);
			nw.addAll(old);
			
			in.set(i, nw);
		}
		
		for(int i : Series.series(out.size()))
		{
			List<Integer> old = out.get(i);
			List<Integer> nw = new ArrayList<Integer>(old.size() + margin);
			nw.addAll(old);
			
			out.set(i, nw);
		}
	}
}