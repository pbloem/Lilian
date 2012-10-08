package org.lilian.util.graphs.algorithms;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;

import org.lilian.util.Pair;
import org.lilian.util.Series;
import org.lilian.util.graphs.Graph;
import org.lilian.util.graphs.Node;

public class InexactMatch<L, N extends Node<L, N>>
{
	
	private Graph<L, N> graph1, graph2;
	
	private State best;
	private double bestCost = Double.POSITIVE_INFINITY;
	
	private double threshold;
	
	private List<N> nodeList1, nodeList2; 
	private InexactCost<L> costFunction;
	
	private PriorityQueue<State> buffer = new PriorityQueue<State>();
	
	public InexactMatch(Graph<L, N> graph1, Graph<L, N> graph2,
			InexactCost<L> cost, double threshold)
	{
		this.graph1 = graph1;
		this.graph2 = graph2;
		
		this.threshold = threshold;
		
		nodeList1 = new ArrayList<N>(graph1);
		nodeList2 = new ArrayList<N>(graph2);
		
		this.costFunction = cost;
		
		buffer.add(new State());
		search();
	}

	private void search()
	{
		while(! buffer.isEmpty())
		{
//			for(State state : buffer)
//				System.out.println(state);
//			System.out.println();
//			
			State top = buffer.poll();
			
			// * If the lowest state has cost higher than threshold, we 
			//   will never reach a state below it.
			if(top.cost() > threshold)
				return;
			
			// * Cost estimation is optimistic so if our current cost is worse 
			//   than the best yet observed, it'll never get better.
			if(top.cost() > bestCost)
				continue;
			
			if(top.complete())
			{
				if(top.cost() < bestCost)
				{
					best = top;
					bestCost = top.cost();
				}
				
				if(top.cost() <= threshold)
					return;
			}
			
			for(State child : top)
				buffer.add(child);
		}
	}
	
	/**
	 * Returns true if the graphs match to within the given edit distance 
	 * 
	 * @return
	 */
	public boolean matches()
	{
		if(best == null)
			return false;
		
		return distance() <= threshold;
	}
	
	/**
	 * The found distance. Note that if matches is true is is the first match
	 * found below the given threshold. If not, this is the absolute best 
	 * distance.
	 * @return
	 */
	public double distance()
	{
		if(best == null)
			return Double.POSITIVE_INFINITY;
		
		return best.cost();
	}
	
	/**
	 * Best match found. If matches() is true, this is the first complete match 
	 * with cost at or below the threshold. If not, it is the match with absolute 
	 * lowest cost.
	 *  
	 * @return
	 */
	public Pair<List<N>, List<N>> bestMatch()
	{
		if(best == null)
			return null;
			
		List<N> l1 = new ArrayList<N>();
		List<N> l2 = new ArrayList<N>();
		
		for(int i : Series.series(best.size()))
		{
			l1.add(best.node1(i));
			l2.add(best.node2(i));
		}
			
		return new Pair<List<N>, List<N>>(l1, l2);			
	}

	private class State implements Iterable<State>, Comparable<State>
	{
		// * representation of the pairs
		// - non-negative values represent mappings between nodes. negative 
		//   values map to new nodes. 
		private int[] nodes1;
		private int[] nodes2;
		
		private double cost;
		private boolean complete;
		
		/**
		 * Creates a root state
		 */
		public State()
		{
			nodes1 = new int[0];
			nodes2 = new int[0];
			cost = 0.0;
			complete = false;
		}
		
		/**
		 * creates a child state of the given state with the givebn pair added.
		 * @param parent
		 * @param g1Node
		 * @param g2Node
		 */
		public State(State parent, int g1Node, int g2Node, boolean complete)
		{
			int n = parent.size();
			nodes1 = new int[n+1];
			nodes2 = new int[n+1];
			
			System.arraycopy(parent.nodes1, 0, nodes1, 0, n);
			System.arraycopy(parent.nodes2, 0, nodes2, 0, n);

			nodes1[n] = g1Node;
			nodes2[n] = g2Node;
			
			this.complete = complete;
						
			// * Calculate score
			cost = currentCost() + expectedCost();
		}
		
		private double currentCost()
		{
			double cost = 0.0;
			
			// * The nodes that are currently paired
			Set<N> a1 = new HashSet<N>();
			Set<N> a2 = new HashSet<N>();
			
			for(int i : series(nodes1.length))
				if(node1(i) != null)
					a1.add(node1(i));
			for(int i : series(nodes2.length))
				if(node2(i) != null)
					a2.add(node2(i));
			
			// * The shells around a1 and a2
			Set<N> b1 = new LinkedHashSet<N>();
			Set<N> b2 = new LinkedHashSet<N>();
			
			for(N node : a1)
				for(N neighbour : node.neighbours())
					if(!a1.contains(neighbour))
						b1.add(neighbour);
			
			for(N node : a2)
				for(N neighbour : node.neighbours())
					if(!a2.contains(neighbour))
						b2.add(neighbour);
			
			cost += Math.abs(b1.size() - b2.size());
			
			// * NOTE: The 1983 paper is not absolutely clear on this step
			//   this may not be right
			int b1Links = 0, b2Links = 0;
			for(N node : b1)
				b1Links += node.neighbours().size();
			for(N node : b2)
				b2Links += node.neighbours().size();
			
			// * We need to be optimistic so we assume that every link addition 
			//   fixes two of the missing neighbours counted above.
			cost += Math.abs(b1Links - b2Links) / 2.0;
			
			return cost;
		}
		
		private double expectedCost()
		{
			double cost = 0.0;
			
			// * relabeling penalty
			for(int i : Series.series(size()))
			{
				if(node1(i) != null && node2(i) != null)
					if(! node1(i).label().equals(node2(i).label()) )
						cost += costFunction.relabel(
							node1(i).label(), 
							node2(i).label());
			}
			
			// * Node addition penalty
			for(int i : series(size()))
				if(node1(i) == null)
					cost += costFunction.addNode(node2(i).label());

			// * Node deletion penalty
			for(int i : series(size()))
				if(node2(i) == null)
					cost += costFunction.addNode(node1(i).label());
			
			// * Link penalties
			for(int i : series(size()))
				for(int j : series(i+1, size()))
				{
					N n1i = node1(i), n1j = node1(j), 
					  n2i = node2(i), n2j = node2(j);
					
					boolean n1connected;
					if(n1i == null || n1j == null)
						n1connected = false;
					else
						n1connected = n1i.connected(n1j);
					
					boolean n2connected;
					if(n2i == null || n2j == null)
						n2connected = false;
					else
						n2connected = n2i.connected(n2j); 
										
					if(n1connected && !n2connected)
						cost += costFunction.addLink();
					
					if(!n1connected && n2connected)
						cost += costFunction.removeLink();

				}
			// Maybe this can be optimized by iterating over edges (once
			// we have that implemented).
			
			
			return cost;
		}
		
		public N node1(int i)
		{
			if(nodes1[i] < 0)
				return null;
				
			return nodeList1.get(nodes1[i]);
		}
		
		public N node2(int i)
		{
			if(nodes2[i] < 0)
				return null;
			return nodeList2.get(nodes2[i]);
		}
		
		@Override
		public Iterator<State> iterator()
		{
			return new StateIterator();
		}
		
		private class StateIterator implements Iterator<State>
		{
			// * Lists of the nodes that have not yet been pairs
			private List<Integer> remaining1, remaining2;
			// * indices of the nodes to add to the next state to return.
			private int i, j;
			
			public StateIterator()
			{
				remaining1 = new ArrayList<Integer>(graph1.size() + 1);
				remaining2 = new ArrayList<Integer>(graph2.size() + 1);
				
				remaining1.addAll(Series.series(graph1.size()));
				remaining2.addAll(Series.series(graph2.size()));

				remaining1.add(-1);
				remaining2.add(-1);
				
				for(int n1 : nodes1)
					if(n1 >= 0)
						remaining1.remove((Integer)n1);
				for(int n2 : nodes2)
					if(n2 >= 0)
						remaining2.remove((Integer)n2);
				
				// System.out.println(State.this + " remaining: " + remaining1 + " " + remaining2);
				
				i = 0;
				j = 0;
			}
			
			@Override
			public boolean hasNext()
			{
				if(i >= remaining1.size() - 1 && j >= remaining2.size() - 1)
					return false;
				
				return true;
			}

			@Override
			public State next()
			{
				if(! hasNext())
					throw new NoSuchElementException();
				
				int n1 = remaining1.get(i),
				    n2 = remaining2.get(j);
				
				boolean complete1 = (n1 < 0 && remaining1.size() == 1) || (n1 >= 0 && remaining1.size() <= 2 );
				boolean complete2 = (n2 < 0 && remaining2.size() == 1) || (n2 >= 0 && remaining2.size() <= 2 );				
	
				State result = new State(State.this, n1, n2, complete1 && complete2);
				
				j++;
				if(j > remaining2.size() - 1)
				{
					j = 0;
					i ++;
				}
				
				return result;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		}

		public double cost()
		{
			return cost;
		}
		
		/**
		 * Whether this state represents a complete mapping from g1 nodes to g2 
		 * nodes.
		 * @return
		 */
		public boolean complete()
		{
			return complete;
		}
		
		/**
		 * Number of pairs (not the number of elements returned by iteration).
		 * @return
		 */
		public int size()
		{
			return nodes1.length;
		}

		@Override
		public int compareTo(State other)
		{
			return Double.compare(this.cost, other.cost);
		}
		
		public String toString()
		{
			StringBuffer sb = new StringBuffer();
			
			sb.append("[");
			boolean first = true;
			for(int i : Series.series(size()))
			{
				if(first)
					first = false;
				else
					sb.append(", ");
					
				sb.append(
					(nodes1[i] == -1 ? "λ" : nodeList1.get(nodes1[i]))
					+ "-" + 
					(nodes2[i] == -1 ? "λ" :nodeList2.get(nodes2[i]))
				);
			}
			
			sb.append("] ").append(cost() + " ").append(complete() ? "c" : "i");
				
			return sb.toString();
		}
		
	}
}
