//package org.lilian.graphs.subdue;
//
//import static org.lilian.util.Series.series;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Deque;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedHashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.NoSuchElementException;
//import java.util.PriorityQueue;
//import java.util.Set;
//
//import org.lilian.Global;
//import org.lilian.graphs.MapUTGraph;
//import org.lilian.graphs.TGraph;
//import org.lilian.graphs.TLink;
//import org.lilian.graphs.TNode;
//import org.lilian.graphs.UTGraph;
//import org.lilian.util.Pair;
//import org.lilian.util.Series;
//
//import static org.lilian.graphs.subdue.Wrapping.*;
//
///**
// * Takes a subgraph (template) and a larger graph and finds the occurence of the 
// * template in the graph. This class returns the result as a tokenized graph
// * (ie all the nodes replaced with nodes with token objects wrapped around the 
// * labels) with the occurrences of the template replaced by symbol tokens. 
// * 
// * TODO: Decouple the graph matching code from the code that extracts it from 
// * the main graph, and repeats the process. 
// * 
// * @author Peter
// *
// * @param <L>
// * @param <N>
// */
//public class InexactSubgraphs<L, T>
//{
//	private Wrapping<L, T> wrapping = new Wrapping<L, T>();
//	private Token symbolToken = wrapping.symbol();
//	
//	private int beamWidth = -1;
//	
//	private MapUTGraph<Token, TagToken> silhouette; 
//	private TGraph<L, T> template;
//	 
//	private double threshold;
//	private boolean returnBest;
//	
//	private InexactCost<L> costFunction;
//	
//	// * The number of links required to connect each substructure to the graph
//	private List<Integer> numLinks = new ArrayList<Integer>();
//	
//	// * The transformation cost connected with each substructure
//	private List<Integer> transCosts = new ArrayList<Integer>(); 
//	
//	private Set<Integer> symbolNodes = new HashSet<Integer>();
//	private long lastMod = -1;
//
//	public InexactSubgraphs(UTGraph<L, T> graph, UTGraph<L, T> template,
//			InexactCost<L> cost, double threshold, boolean returnBest)
//	{
//		this(graph, template, cost, threshold, returnBest, -1);
//	}
//	
//	public InexactSubgraphs(UTGraph<L, T> graph, UTGraph<L, T> template,
//			InexactCost<L> cost, double threshold, boolean returnBest, int beamWidth)
//	{
//		this.beamWidth = beamWidth;
//		this.silhouette = wrapping.wrap(graph);
//		this.template = template;
//		
//		this.threshold = threshold;
//		this.returnBest = returnBest;
//		
//		this.costFunction = cost;
//		
//		State best = null;
//		
//		// * Loop the search until no more substructures are found
//		do 
//		{
//			best = search();
//			
//			if (best != null && ! silhouette.nodes().isEmpty())
//			{
//				// * Add a symbol node
//				TNode<Token, TagToken> symbol = silhouette.add(symbolToken);
//				
//				// * remove the matched nodes from the graph
//				
//				// ** Sort the indices from largest to smallest so that we can 
//				//    remove them in order without affecting the rest of the
//				//    indices to be removed 
//				//   (if we remove from low to high, the indices of the higher 
//				//    nodes will change in the graph)
//				List<Integer> rm = new ArrayList<Integer>(best.silhouetteNodes.length);
//				for (int i : best.silhouetteNodes)
//					rm.add(i);
//				
//				Collections.sort(rm, Collections.reverseOrder());
//
//				// * Deduplicate
//				Set<TNode<Token, TagToken>> nodes = new HashSet<TNode<Token, TagToken>>();
//				for (int i : rm)
//					if (i >= 0)
//						nodes.add(silhouette.nodes().get(i));
//				
//				// * record transformation cost and linking cost
//				int links = 0;
//				for (TNode<Token, TagToken> node : nodes)
//					for (TNode<Token, TagToken> neighbour : node.neighbors())
//						if (! nodes.contains(neighbour))
//							links++;
//				
//				numLinks.add(links);
//				
//				transCosts.add((int)best.cost());
//				
//				// * Remove the nodes from the graph
//				int i = 0; // index of the node to be removed in the template
//				for(TNode<Token, TagToken> node : nodes)
//				{
//					
//					
//					// * Check the neighbours. If they are not to be removed, 
//					//   they should be connected to the symbol node
//					for(TNode<Token, TagToken> neighbor : node.neighbors())
//						if(! nodes.contains(neighbor))
//							for(TLink<Token, TagToken> link : node.links(neighbor))
//							{
//								if(! (link.tag() instanceof Wrapping<?, ?>.LabelTagToken) )
//									throw new IllegalStateException("Illegal state. All tagtokens should be of type LabelTagToken");
//								
//								Wrapping<L, T>.LabelTagToken tag = (Wrapping<L, T>.LabelTagToken) link.tag(); 
//								TagToken newTT;
//								if(tag.firstAnnotation() == null)
//									newTT = wrapping.tag(tag.tag(), i);
//								else
//									newTT = wrapping.tag(tag.tag(), tag.firstAnnotation(), i);
//								
//								symbol.connect(neighbor, newTT);
//							}
//							
//					node.remove();
//					i++;
//				}
//			}
//
//		} while (best != null && ! silhouette.nodes().isEmpty());
//		
//	}
//	
//	/**
//	 * Returns a list of the indices of the nodes in sihouette which are 
//	 * symbolnodes
//	 * 
//	 * @return
//	 */
//	public Set<Integer> symbolNodes()
//	{
//		if(lastMod != silhouette.state())
//		{
//			symbolNodes.clear();
//			for(int i : Series.series(silhouette.size()))
//				if(silhouette.nodes().get(i).label() instanceof Wrapping<?, ?>.SymbolToken)
//					symbolNodes.add(i);
//					
//				
//			lastMod = silhouette.state();
//		}
//		
//		return symbolNodes;
//	}
//	
//	private State search()
//	{
//		State best = null;
//		double bestCost = Double.POSITIVE_INFINITY;
//		
//		LinkedList<State> buffer = new LinkedList<State>();
//		buffer.add(new State());
//
//		while(! buffer.isEmpty())
//		{
////			Global.log().info("buffer size:" + buffer.size());
//			State top = buffer.poll();
//			
//			// * If the lowest state has cost higher than threshold, we 
//			//   will never reach a state below it.
//			if(top.cost() > threshold)
//				return best;
//			
//			// * Cost estimation is optimistic so if our current cost is worse 
//			//   than the best yet observed, it'll never get better.
//			if(top.cost() > bestCost)
//				continue;
//			
//			if(top.complete())
//			{
//				if(returnBest)
//				{
//					if(top.cost() < bestCost)
//					{
//						best = top;
//						bestCost = top.cost();
//					}
//				} else
//				{
//					return top;
//				}
//			}
//			
//			for(State child : top)
//				buffer.add(child);
//			
//			Collections.sort(buffer);
//			
//			if(beamWidth != -1)
//				while(buffer.size() > beamWidth)
//					buffer.removeLast();
//					
//		}
//		
//		return best;
//	}
//	
//	public int numMatches()
//	{
//		return numLinks.size();
//	}
//	
//	/** 
//	 * The graph with all substructures removed
//	 * 
//	 * @return
//	 */
//	public UTGraph<Token, TagToken> silhouette()
//	{
//		return silhouette;
//	}
//	
//	/**
//	 * The number of links required to add each substructure to the graph
//	 * @return
//	 */
//	public List<Integer> numLinks()
//	{
//		return numLinks;
//	}
//	
//	/**
//	 * The number of links required to add each substructure to the graph
//	 * @return
//	 */
//	public List<Integer> transCosts()
//	{
//		return transCosts;
//	}
//	
//	public Wrapping<L, T> wrapping()
//	{
//		return wrapping;
//	}
//	
//	private class State implements Iterable<State>, Comparable<State>
//	{
//		// * Each state is defined primarily by a list of node pairs.
//		//   These two array represent these node pairs. 
//		// 
//		// - silhouetteNodes[0] is the index in 'silhouette' of the node which 
//		//   is part of the first pair. templateNodes[0] is the index of the 
//		//   node in template that is part of the first pair.
//		//
//		// - non-negative values represent mappings between nodes. Negative 
//		//   values map to new nodes. 
//		private int[] silhouetteNodes;
//		private int[] templateNodes;
//		
//		private double cost;
//		private boolean complete;
//		
//		/**
//		 * Creates a root state
//		 */
//		public State()
//		{
//			silhouetteNodes = new int[0];
//			templateNodes = new int[0];
//			cost = 0.0;
//			complete = false;
//		}
//		
//		/**
//		 * Creates a child state of the given state with the given pair added.
//		 * 
//		 * @param parent
//		 * @param g1Node
//		 * @param g2Node
//		 */
//		public State(State parent, int silhouetteNode, int templateNode, boolean complete)
//		{
//			int n = parent.size();
//			silhouetteNodes = new int[n+1];
//			templateNodes = new int[n+1];
//			
//			System.arraycopy(parent.silhouetteNodes, 0, silhouetteNodes, 0, n);
//			System.arraycopy(parent.templateNodes, 0, templateNodes, 0, n);
//
//			silhouetteNodes[n] = silhouetteNode;
//			templateNodes[n] = templateNode;
//			
//			this.complete = complete;
//						
//			// * Calculate score
//			cost = expectedCost() + currentCost();
//		}
//		
//		private double expectedCost()
//		{
//			double cost = 0.0;
//			
//			// * The nodes that are currently paired
//			Set<TNode<Token, TagToken>> aSilhouette = new HashSet<TNode<Token, TagToken>>();
//			Set<TNode<L, T>> aTemplate = new HashSet<TNode<L, T>>();
//			
//			for(int i : series(silhouetteNodes.length))
//				if(nodeSilhouette(i) != null)
//					aSilhouette.add(nodeSilhouette(i));
//			for(int i : series(templateNodes.length))
//				if(nodeTemplate(i) != null)
//					aTemplate.add(nodeTemplate(i));
//			
//			// * The shells around a1 and a2
//			Set<TNode<Token, TagToken>> bSilhouette = new LinkedHashSet<TNode<Token, TagToken>>();
//			Set<TNode<L, T>> bTemplate = new LinkedHashSet<TNode<L, T>>();
//			
//			for(TNode<Token, TagToken> node : aSilhouette)
//				for(TNode<Token, TagToken> neighbour : node.neighbors())
//					if(!aSilhouette.contains(neighbour))
//						bSilhouette.add(neighbour);
//			
//			for(TNode<L, T> node : aTemplate)
//				for(TNode<L, T> neighbour : node.neighbors())
//					if(!aTemplate.contains(neighbour))
//						bTemplate.add(neighbour);
//			if(bTemplate.size() > bSilhouette.size())
//				cost += Math.abs(bSilhouette.size() - bTemplate.size());
//			
//			// * NOTE: The 1983 paper is not absolutely clear on this step
//			//   this may not be right
//			int b1Links = 0, b2Links = 0;
//			for(TNode<Token, TagToken> node : bSilhouette)
//				b1Links += node.neighbors().size();
//			for(TNode<L, T> node : bTemplate)
//				b2Links += node.neighbors().size();
//						
//			// * We need to be optimistic so we assume that every link addition 
//			//   fixes two of the missing neighbors counted above.
//			if(b2Links > b1Links)
//				cost += Math.abs(b1Links - b2Links) / 2.0;
//			
//			return cost;
//		}
//		
//		private double currentCost()
//		{
//			// * if this is a complete match, then at least some of the pairs 
//			//   must be definite (ie. not represent the removal or addition of
//			//   a node.) 
//			if(complete())
//			{
//				int connected = 0;
//				for(int i : series(size()))
//					if(silhouetteNodes[i] >= 0 && templateNodes[i] >= 0)
//						connected++;
//				
//				if(connected == 0)
//					return Double.POSITIVE_INFINITY;
//			}
//					
//			// * If any of the template nodes map to a symbol node, this is an 
//			//   illegal state
//			for(int i : Series.series(size()))
//				if(nodeSilhouette(i) != null)
//					if(! (nodeSilhouette(i).label() instanceof Wrapping<?, ?>.LabelToken)) 
//						return Double.POSITIVE_INFINITY;
//				
//			
//			double cost = 0.0;
//			
//			// * relabeling penalty
//			for(int i : Series.series(size()))
//			{
//				if(nodeSilhouette(i) != null && nodeTemplate(i) != null)
//				{
//					// * unwrap the labeltoken
//					L silhouetteLabel = 
//							((Wrapping<L, T>.LabelToken) (nodeSilhouette(i).label())).label();
//					L templateLabel =  
//							nodeTemplate(i).label();
//					
//					if(! silhouetteLabel.equals(templateLabel))
//						cost += costFunction.relabel(
//							silhouetteLabel, 
//							templateLabel);
//				}
//			}
//			
//			// * Node addition penalty
//			for(int i : series(size()))
//				if(nodeSilhouette(i) == null)
//					cost += costFunction.addNode(nodeTemplate(i).label());
//
//			// * Node deletion penalty
//			for(int i : series(size()))
//				if(nodeTemplate(i) == null)
//				{
//					L silhouetteLabel =
//							((Wrapping<L, T>.LabelToken) (nodeSilhouette(i).label())).label();
//					cost += costFunction.removeNode(silhouetteLabel);
//				}
//			
//			// * Link penalties
//			for(int i : series(size()))
//				for(int j : series(i+1, size()))
//				{
//					TNode<Token, TagToken> n1i = nodeSilhouette(i), n1j = nodeSilhouette(j); 
//					TNode<L, T> n2i = nodeTemplate(i), n2j = nodeTemplate(j);
//					
//					boolean n1connected;
//					if(n1i == null || n1j == null)
//						n1connected = false;
//					else
//						n1connected = n1i.connected(n1j);
//					
//					boolean n2connected;
//					if(n2i == null || n2j == null)
//						n2connected = false;
//					else
//						n2connected = n2i.connected(n2j); 
//										
//					if(n1connected && !n2connected)
//						cost += costFunction.addLink();
//					
//					if(!n1connected && n2connected)
//						cost += costFunction.removeLink();
//
//				}
//			
//			// * Maybe this can be optimized by iterating over edges (once
//			//   we have that implemented).
//			
//			return cost;
//		}
//		
//		/**
//		 * Returns the node in the silhouette  
//		 * @param i
//		 * @return
//		 */
//		public TNode<Token, TagToken> nodeSilhouette(int i)
//		{
//			if(silhouetteNodes[i] < 0)
//				return null;
//				
//			return silhouette.nodes().get(silhouetteNodes[i]);
//		}
//		
//		public TNode<L, T> nodeTemplate(int i)
//		{
//			if(templateNodes[i] < 0)
//				return null;
//			
//			return template.nodes().get(templateNodes[i]);
//		}
//		
//		@Override
//		public Iterator<State> iterator()
//		{
//			return new StateIterator();
//		}
//		
//		private class StateIterator implements Iterator<State>
//		{
//			// * Lists of the nodes that have not yet been pairs
//			private List<Integer> remainingS, remainingT;
//			// * indices of the nodes to add to the next state to return.
//			private int i, j;
//			
//			public StateIterator()
//			{
//				remainingS = new ArrayList<Integer>(silhouette.size() + 1);
//				remainingT = new ArrayList<Integer>(template.size() + 1);
//				
//				remainingS.addAll(Series.series(silhouette.size()));
//				remainingT.addAll(Series.series(template.size()));
//
//				remainingS.add(-1);
//				remainingT.add(-1);
//				
//				for(int n1 : silhouetteNodes)
//					if(n1 >= 0)
//						remainingS.remove((Integer)n1);
//				
//				for(int n2 : templateNodes)
//					if(n2 >= 0)
//						remainingT.remove((Integer)n2);
//				
//				// * Also remove the symbol nodes from remainingS as these are 
//				//   not allowed to be part of a match
//				remainingS.removeAll(symbolNodes());
//				
////				System.out.println(State.this + " remaining: " + remaining1 + " " + remaining2);
//				
//				i = 0;
//				j = 0;
//			}
//			
//			@Override
//			public boolean hasNext()
//			{
//				if(i >= remainingS.size() - 1 && j >= remainingT.size() - 1)
//					return false;
//				
//				return true;
//			}
//
//			@Override
//			public State next()
//			{
//				if(! hasNext())
//					throw new NoSuchElementException();
//				
//				int n1 = remainingS.get(i),
//				    n2 = remainingT.get(j);
//				
//				boolean complete2 = (n2 < 0 && remainingT.size() == 1) || (n2 >= 0 && remainingT.size() <= 2 );				
//	
//				State result = new State(State.this, n1, n2, complete2);
//				
//				j++;
//				if(j > remainingT.size() - 1)
//				{
//					j = 0;
//					i ++;
//				}
//				
//				return result;
//			}
//
//			@Override
//			public void remove()
//			{
//				throw new UnsupportedOperationException();
//			}
//		}
//
//		public double cost()
//		{
//			return cost;
//		}
//		
//		/**
//		 * Whether this state represents a complete mapping from g1 nodes to g2 
//		 * nodes.
//		 * @return
//		 */
//		public boolean complete()
//		{
//			return complete;
//		}
//		
//		/**
//		 * Number of pairs (not the number of elements returned by iteration).
//		 * @return
//		 */
//		public int size()
//		{
//			return silhouetteNodes.length;
//		}
//
//		@Override
//		public int compareTo(State other)
//		{
//			return Double.compare(this.cost, other.cost);
//		}
//		
//		public String toString()
//		{
//			StringBuffer sb = new StringBuffer();
//			
//			sb.append("[");
//			boolean first = true;
//			for(int i : Series.series(size()))
//			{
//				if(first)
//					first = false;
//				else
//					sb.append(", ");
//					
//				sb.append(
//					(silhouetteNodes[i] == -1 ? "λ" : silhouette.nodes().get(silhouetteNodes[i]))
//					+ "-" + 
//					(templateNodes[i] == -1 ? "λ" : template.nodes().get(templateNodes[i]))
//				);
//			}
//			
//			sb.append("] ").append(cost() + " ("+expectedCost()+", "+currentCost()+") ").append(complete() ? "c" : "i");
//				
//			return sb.toString();
//		}
//		
//	}
//}
