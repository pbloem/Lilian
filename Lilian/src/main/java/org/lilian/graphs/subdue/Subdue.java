package org.lilian.graphs.subdue;

import static java.util.Collections.sort;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.lilian.Global;
import org.lilian.graphs.MapUTGraph;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.UTNode;
import org.lilian.util.Series;
import org.lilian.util.distance.Metrizable;

/**
 * Subdue (Jonyer, Cook, Holder, 2003) finds relevant substructures in a graph 
 * based on an MDL criterion
 *  
 * @author Peter
 *
 */
public class Subdue<L, T>
{
	private int matcherBeamWidth = -1;
	private double costThreshold = 0.0;
	private InexactCost<Token> costFunction = null;
	
	private UTGraph<L, T> graph;
	private MapUTGraph<Token, TagToken> tGraph;
	
	private Set<L> labels;
	private Set<T> tags;
	private boolean sparse = false;
	
	public Subdue(UTGraph<L, T> graph, boolean sparse)
	{
		labels = graph.labels();
		tags = graph.tags();
		
		this.graph = graph;
		this.sparse = sparse;
		tGraph = wrap(graph);
	}

	public Subdue(UTGraph<L, T> graph, InexactCost<L> costFunction, double costThreshold, boolean sparse)
	{
		this(graph, costFunction, costThreshold, sparse, -1);
	}
	
	public Subdue(UTGraph<L, T> graph, InexactCost<L> costFunction, double costThreshold, boolean sparse, int matcherBeamWidth)
	{
		this.matcherBeamWidth = matcherBeamWidth;
		
		labels = graph.labels();
		tags = graph.tags();
		
		this.costThreshold = costThreshold;
		this.costFunction = new CostWrapper(costFunction);
		this.sparse = sparse;
		
		this.graph = graph;
		this.tGraph = wrap(graph);
	}
	
	/**
	 * 
	 * @param iterations The number of times to extend the current collections of substructures
	 * @param beamWidth The number of substructures to keep
	 * @param maxBest The number of best substructures to return
	 * @param maxSubSize The maximum size of a substructure (-1 for no limit)
	 * @return
	 */
	public Collection<Substructure> search(int iterations, int beamWidth, int maxBest, int maxSubSize)
	{
		// Global.log().info("...");
		
		LinkedList<Substructure> parents = new LinkedList<Substructure>(),
				                 children = new LinkedList<Substructure>(),
		                         bestList = new LinkedList<Substructure>();
		
		for(L label : labels)
			parents.add(new Substructure(substructure(label)));
		
		sort(parents);
		
		for(int i : series(iterations))
		{
//			Global.log().info("Starting iteration " + i);
//			Global.log().info("* there are " + parents.size() + " parents");
//			
			// * generate all extensions of the parents
			List<MapUTGraph<Token, TagToken>> extensions = new LinkedList<MapUTGraph<Token, TagToken>>();
			for(Substructure parent : parents)
				for(MapUTGraph<Token, TagToken> child : substructures(parent.subgraph()))
					if(child.size() <= maxSubSize || maxSubSize == -1)
						extensions.add(child);
			
//			Global.log().info("Generated " + extensions.size() +  " children. Checking for isomorphisms.");
//			
			// * check for isomorphic children
			while(! extensions.isEmpty())
			{
				MapUTGraph<Token, TagToken> next = extensions.remove(0);
				
//				if(extensions.size() % 100 == 0)
//					Global.log().info(extensions.size() + " left.");
				
				Iterator<MapUTGraph<Token, TagToken>> iterator = extensions.iterator();
				while(iterator.hasNext())
				{
					MapUTGraph<Token, TagToken> other = iterator.next();
					
//					InexactMatch<Token, BaseGraph<Token>.Node> im = 
//							new InexactMatch<Token, BaseGraph<Token>.Node>(
//									next, other, costFunction, costThreshold);
//					
//					if(im.matches())
//						iterator.remove();
					
					UndirectedVF2<Token, TagToken> vf2 = 
							new UndirectedVF2<Token, TagToken>(next, other, true);
					
					if(vf2.matches())
						iterator.remove();
				}
				
//				Global.log().info("children: "+children.size());
				children.add(new Substructure(next));
			}
			
// 			Global.log().info("Reduced to "+children.size()+" non-isomorphic children.");
	
			sort(children);
			
//			for(Substructure child : children)
//				System.out.println("__ " + child);
			
			while(children.size() > beamWidth)
				children.pollLast();
			
			bestList.addAll(children);
			sort(bestList);
			while(bestList.size() > maxBest)
				bestList.pollLast();
			
			parents = children;
			children = new LinkedList<Substructure>();			
		}

		return bestList; 
	}

	/**
	 * Returns a single node substructure with the given label
	 * @param label
	 * @return
	 */
	public MapUTGraph<Token, TagToken> substructure(L label)
	{
		MapUTGraph<Token, TagToken> graph = new MapUTGraph<Token, TagToken>();
		graph.add(new LabelToken(label));
		
		return graph;
	}
	
	/**
	 * Returns all substructures that can be derived by adding a link to the 
	 * given parent (possibly by also adding a new node)
	 */
	public List<MapUTGraph<Token, TagToken>> substructures(MapUTGraph<Token, TagToken> parent)
	{
		List<MapUTGraph<Token, TagToken>> substructures = new ArrayList<MapUTGraph<Token, TagToken>>();
		
		// * Try all additional connections between existing nodes
		for(int i : series(parent.nodes().size()))
			for(int j : series(i+1, parent.nodes().size()))
			{
				UTNode<Token, TagToken> 
						n1 = parent.nodes().get(i), 
						n2 = parent.nodes().get(j);
				
				for(T tag : tags)
				{	
					TagToken tt = new LabelTagToken(tag); 
					if(! n1.connected(n2, tt))
					{
						MapUTGraph<Token, TagToken> sub = copy(parent, n1, n2, tt);
						substructures.add(sub);
					}
				}
			}
		
		// * Try all connections to a new node
		for(int i : series(parent.nodes().size()))
			for(L label : labels)
				for(T tag : tags)
				{
					UTNode<Token, TagToken> n1 = parent.nodes().get(i);
					MapUTGraph<Token, TagToken> sub = copy(parent, n1, label);
					substructures.add(sub);
				}
		
		return substructures;
	}
	
	/**
	 * Creates a copy of the given node with a single extra node (of the given label)
	 * connected to the graph by the given existing node n1.
	 * 
	 * @param parent
	 * @param n1
	 * @param label
	 * @return
	 */
	private MapUTGraph<Token, TagToken> copy(
			MapUTGraph<Token, TagToken> parent, 
			UTNode<Token, TagToken> n1, 
			L label)
	{
		MapUTGraph<Token, TagToken> child = new MapUTGraph<Token, TagToken>();
		
		// * Note : these intermediate lists are no longer necessary.
		List<UTNode<Token, TagToken>> nodesIn = new ArrayList<UTNode<Token, TagToken>>(parent.nodes());
		List<UTNode<Token, TagToken>> nodesOut = new ArrayList<UTNode<Token, TagToken>>(parent.size());

		
		for(UTNode<Token, TagToken> nodeIn : nodesIn)
			nodesOut.add(child.add(nodeIn.label()));
		UTNode<Token, TagToken> newNode = child.add(new LabelToken(label));
		
		for(int i : series(nodesIn.size()))
		{
			UTNode<Token, TagToken> ni = nodesIn.get(i);
			for(int j : series(i, nodesIn.size()))
			{
				UTNode<Token, TagToken> nj = nodesIn.get(j);
				if(ni.connected(nj))
					nodesOut.get(i).connect(nodesOut.get(j));
			
			}
			
			if(ni.equals(n1))
				nodesOut.get(i).connect(newNode);
		}
		
		return child;
	}

	/**
	 * Creates a copy of the given parent graph with an extra edge between the
	 * two given nodes
	 * 
	 * @param parent
	 * @param n1
	 * @param n2
	 * @return
	 */
	private MapUTGraph<Token, TagToken> copy(
			MapUTGraph<Token, TagToken> parent,
			UTNode<Token, TagToken> n1,
			UTNode<Token, TagToken> n2,
			TagToken newTag)
	{
		MapUTGraph<Token, TagToken> child = new MapUTGraph<Token, TagToken>();
		List<UTNode<Token, TagToken>> nodesIn = new ArrayList<UTNode<Token, TagToken>>(parent.nodes());
		List<UTNode<Token, TagToken>> nodesOut = new ArrayList<UTNode<Token, TagToken>>(parent.size());
		
		for(UTNode<Token, TagToken> nodeIn : nodesIn)
			nodesOut.add(child.add(nodeIn.label()));
		
		for(int i : series(nodesIn.size()))
			for(int j : series(i, nodesIn.size()))
			{
				UTNode<Token, TagToken> ni = nodesIn.get(i), nj = nodesIn.get(j);
				
				if(ni.connected(nj))
					nodesOut.get(i).connect(nodesOut.get(j), ni.link(nj).tag());
				if(ni.equals(n1) && nj.equals(n2))
					nodesOut.get(i).connect(nodesOut.get(j), newTag);
			}
					
			
		return child;
	}
	
	/**
	 * Produces a graph with the same structure as the input, but with wrapper 
	 * objects as labels/tags that refer back to the original labels/tags. 
	 * (But not nodes) 
	 * 
	 * @param graph
	 * @return
	 */
	private MapUTGraph<Token, TagToken> wrap(UTGraph<L, T> graph)
	{
		MapUTGraph<Token, TagToken> wrapped = new MapUTGraph<Token, TagToken>();
		
		for(UTNode<L, T> nodeIn : graph.nodes())
			wrapped.add(new LabelToken(nodeIn.label()));
		
		for(int i : series(graph.size()))
			for(int j : series(i, graph.size()))
			{
				UTNode<L, T> ni = graph.nodes().get(i), nj = graph.nodes().get(j);
				for(T tag : tags)
				{
					if(ni.connected(nj, tag))
						wrapped.nodes().get(i).connect(wrapped.nodes().get(j), 
								new LabelTagToken(tag));
				}
			}
		

		
		return wrapped;
	}


	/**
	 * A token is a node label in a substructure. It can represent a labeled 
	 * node in the graph or a variable node.
	 * 
	 * @author Peter
	 *
	 */
	public interface Token {
		
	}
	
	public interface TagToken {
		
	}
	
	private class LabelToken implements Token 
	{
		L label;

		public LabelToken(L label)
		{
			this.label = label;
		} 
		
		public L label()
		{
			return label;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LabelToken other = (LabelToken) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (label == null)
			{
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			return true;
		}

		private Subdue getOuterType()
		{
			return Subdue.this;
		}
		
		public String toString()
		{
			return label+"";
		}
	}
	
	private class LabelTagToken implements TagToken 
	{
		T label;

		public LabelTagToken(T label)
		{
			this.label = label;
		} 
		
		public T label()
		{
			return label;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LabelTagToken other = (LabelTagToken) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (label == null)
			{
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			return true;
		}

		private Subdue getOuterType()
		{
			return Subdue.this;
		}
		
		public String toString()
		{
			return label+"";
		}
	}
	
	
	/**
	 * A subgraph with an associated MDL score
	 * 
	 * @author Peter
	 *
	 */
	public  class Substructure 
		implements Comparable<Substructure>
	{
		private MapUTGraph<Token, TagToken> subGraph;
		private double score;
		
		public Substructure(MapUTGraph<Token, TagToken> subGraph)
		{
			this.subGraph = subGraph;
			calculateScore();
		}
		
		public MapUTGraph<Token, TagToken> subgraph()
		{
			return subGraph;
		}

		private void calculateScore()
		{
			score = GraphMDL.mdl(tGraph, subGraph, costThreshold, sparse, matcherBeamWidth);
		}
		
		public double score()
		{
			return score;
		}

		public boolean matches(Substructure other)
		{
			InexactMatch<Token, TagToken> im = 
					new InexactMatch<Subdue.Token, Subdue.TagToken>(
							this.subGraph, other.subGraph, costFunction, costThreshold);
			
			return im.matches();
		}

		@Override
		public int compareTo(Substructure other)
		{
			return Double.compare(this.score, other.score);
		}
		
		public String toString()
		{
			return subGraph.toString() + " " + score + " " + GraphMDL.mdl(subGraph);
		}
	}
	
	private class CostWrapper implements InexactCost<Token>
	{
		private InexactCost<L> master;

		public CostWrapper(InexactCost<L> master)
		{
			this.master = master;
		}

		@Override
		public double relabel(Token in, Token out)
		{
			if(in instanceof Subdue.LabelToken && out instanceof Subdue.LabelToken)
				return master.relabel(((LabelToken)in).label(), ((LabelToken)out).label );
			return 1.0;
		}

		@Override
		public double removeNode(Token label)
		{
			if(label instanceof Subdue.LabelToken)
				return master.removeNode(((LabelToken)label).label());
			return 1.0;
		}

		@Override
		public double addNode(Token label)
		{
			if(label instanceof Subdue.LabelToken)
				return master.addNode(((LabelToken)label).label());
			return 1.0;
		}

		@Override
		public double removeLink()
		{
			return master.removeLink();
		}

		@Override
		public double addLink()
		{
			return master.removeLink();
		}
		
		
	}
}
