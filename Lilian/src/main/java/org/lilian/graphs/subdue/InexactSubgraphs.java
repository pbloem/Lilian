package org.lilian.graphs.subdue;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;

import org.lilian.Global;
import org.lilian.graphs.MapUTGraph;
import org.lilian.graphs.TGraph;
import org.lilian.graphs.TNode;
import org.lilian.graphs.UTGraph;
import org.lilian.util.Pair;
import org.lilian.util.Series;

/**
 * Searches for inexact subgraphs of a given template in a larger graph.
 * 
 * @author Peter
 *
 * @param <L>
 * @param <N>
 */
public class InexactSubgraphs<L, T>
{
	private int beamWidth = -1;
	
	private MapUTGraph<L, T> graph1; 
	private TGraph<L, T> template;
	 
	private double threshold;
	private boolean returnBest;
	
	private InexactCost<L> costFunction;
	
	// * The number of links required to connect each substructure to the graph
	public List<Integer> numLinks = new ArrayList<Integer>();
	
	// * The transformation cost connected with each substructure
	public List<Integer> transCosts = new ArrayList<Integer>();


	public InexactSubgraphs(UTGraph<L, T> graph, UTGraph<L, T> template,
			InexactCost<L> cost, double threshold, boolean returnBest)
	{
		this(graph, template, cost, threshold, returnBest, -1);
	}
	
	public InexactSubgraphs(UTGraph<L, T> graph, UTGraph<L, T> template,
			InexactCost<L> cost, double threshold, boolean returnBest, int beamWidth)
	{
		this.beamWidth = beamWidth;
		this.graph1 = MapUTGraph.copy(graph);
		this.template = template;
		
		this.threshold = threshold;
		this.returnBest = returnBest;
		
		this.costFunction = cost;
		
		State best = null;
		
		// * Loop the search until no more substructures are found
		do 
		{
			best = search();
			// System.out.print("."+template.size()+".");
			
			if(best != null && ! this.graph1.nodes().isEmpty())
			{
				// * remove the matched nodes from the graph
				
				// ** Sort the indices from largest to smallest so that we can 
				//    remove them in order without affecting the rest of the
				//    indices to be removed 
				List<Integer> rm = new ArrayList<Integer>(best.nodes1.length);
				for(int i : best.nodes1)
					rm.add(i);
				
				Collections.sort(rm, Collections.reverseOrder());

				// * Deduplicate
				Set<TNode<L, T>> nodes = new HashSet<TNode<L, T>>();
				for(int i : rm)
					if(i >= 0)
						nodes.add(graph1.nodes().get(i));
				
				// * record transformation cost and linking cost
				int links = 0;
				for(TNode<L, T> node : nodes)
					for(TNode<L, T> neighbour : node.neighbors())
						if(! nodes.contains(neighbour))
							links++;
				
				numLinks.add(links);
				
				transCosts.add((int)best.cost());
				
				// * remove the nodes from the graph
				for(TNode<L, T> node : nodes)
					node.remove();
			}

		} while(best != null && ! this.graph1.nodes().isEmpty());
		// System.out.println();
	}

	private State search()
	{
		State best = null;
		double bestCost = Double.POSITIVE_INFINITY;
		
		LinkedList<State> buffer = new LinkedList<State>();
		buffer.add(new State());

		while(! buffer.isEmpty())
		{
//			Global.log().info("buffer size:" + buffer.size());
			State top = buffer.poll();
			
			// * If the lowest state has cost higher than threshold, we 
			//   will never reach a state below it.
			if(top.cost() > threshold)
				return best;
			
			// * Cost estimation is optimistic so if our current cost is worse 
			//   than the best yet observed, it'll never get better.
			if(top.cost() > bestCost)
				continue;
			
			if(top.complete())
			{
				if(returnBest)
				{
					if(top.cost() < bestCost)
					{
						best = top;
						bestCost = top.cost();
					}
				} else
				{
					return top;
				}
			}
			
			for(State child : top)
				buffer.add(child);
			
			Collections.sort(buffer);
			
			if(beamWidth != -1)
				while(buffer.size() > beamWidth)
					buffer.removeLast();
					
		}
		
		return best;
	}
	
	public int numMatches()
	{
		return numLinks.size();
	}
	
	/** 
	 * The graph with all substructures removed
	 * 
	 * @return
	 */
	public UTGraph<L, T> silhouette()
	{
		return graph1;
	}
	
	/**
	 * The number of links required to add each substructure to the graph
	 * @return
	 */
	public List<Integer> numLinks()
	{
		return numLinks;
	}
	
	/**
	 * The number of links required to add each substructure to the graph
	 * @return
	 */
	public List<Integer> transCosts()
	{
		return transCosts;
	}
	
	private class State implements Iterable<State>, Comparable<State>
	{
		// * representation of the pairs
		// - non-negative values represent mappings between nodes. Negative 
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
		 * Creates a child state of the given state with the given pair added.
		 * 
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
			cost = expectedCost() + currentCost();
		}
		
		private double expectedCost()
		{
			double cost = 0.0;
			
			// * The nodes that are currently paired
			Set<TNode<L, T>> a1 = new HashSet<TNode<L, T>>();
			Set<TNode<L, T>> a2 = new HashSet<TNode<L, T>>();
			
			for(int i : series(nodes1.length))
				if(node1(i) != null)
					a1.add(node1(i));
			for(int i : series(nodes2.length))
				if(node2(i) != null)
					a2.add(node2(i));
			
			// * The shells around a1 and a2
			Set<TNode<L, T>> b1 = new LinkedHashSet<TNode<L, T>>();
			Set<TNode<L, T>> b2 = new LinkedHashSet<TNode<L, T>>();
			
			for(TNode<L, T> node : a1)
				for(TNode<L, T> neighbour : node.neighbors())
					if(!a1.contains(neighbour))
						b1.add(neighbour);
			
			for(TNode<L, T> node : a2)
				for(TNode<L, T> neighbour : node.neighbors())
					if(!a2.contains(neighbour))
						b2.add(neighbour);
			if(b2.size() > b1.size())
				cost += Math.abs(b1.size() - b2.size());
			
			// * NOTE: The 1983 paper is not absolutely clear on this step
			//   this may not be right
			int b1Links = 0, b2Links = 0;
			for(TNode<L, T> node : b1)
				b1Links += node.neighbors().size();
			for(TNode<L, T> node : b2)
				b2Links += node.neighbors().size();
						
			// * We need to be optimistic so we assume that every link addition 
			//   fixes two of the missing neighbors counted above.
			if(b2Links > b1Links)
				cost += Math.abs(b1Links - b2Links) / 2.0;
			
			return cost;
		}
		
		private double currentCost()
		{
			// * if this is a complete match, then at least some of the pairs 
			//   must be definite (ie. not represent the removal for addition of
			//   a node.) 
			if(complete())
			{
				int connected = 0;
				for(int i : series(size()))
					if(nodes1[i] >= 0 && nodes2[i] >= 0)
						connected++;
				
				if(connected == 0)
					return Double.POSITIVE_INFINITY;
			}
					
			
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
					TNode<L, T> n1i = node1(i), n1j = node1(j); 
					TNode<L, T> n2i = node2(i), n2j = node2(j);
					
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
		
		public TNode<L, T> node1(int i)
		{
			if(nodes1[i] < 0)
				return null;
				
			return graph1.nodes().get(nodes1[i]);
		}
		
		public TNode<L, T> node2(int i)
		{
			if(nodes2[i] < 0)
				return null;
			
			return template.nodes().get(nodes2[i]);
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
				remaining2 = new ArrayList<Integer>(template.size() + 1);
				
				remaining1.addAll(Series.series(graph1.size()));
				remaining2.addAll(Series.series(template.size()));

				remaining1.add(-1);
				remaining2.add(-1);
				
				for(int n1 : nodes1)
					if(n1 >= 0)
						remaining1.remove((Integer)n1);
				for(int n2 : nodes2)
					if(n2 >= 0)
						remaining2.remove((Integer)n2);
				
//				System.out.println(State.this + " remaining: " + remaining1 + " " + remaining2);
				
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
				
				boolean complete2 = (n2 < 0 && remaining2.size() == 1) || (n2 >= 0 && remaining2.size() <= 2 );				
	
				State result = new State(State.this, n1, n2, complete2);
				
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
					(nodes1[i] == -1 ? "λ" : graph1.nodes().get(nodes1[i]))
					+ "-" + 
					(nodes2[i] == -1 ? "λ" : template.nodes().get(nodes2[i]))
				);
			}
			
			sb.append("] ").append(cost() + " ("+expectedCost()+", "+currentCost()+") ").append(complete() ? "c" : "i");
				
			return sb.toString();
		}
		
	}
}
