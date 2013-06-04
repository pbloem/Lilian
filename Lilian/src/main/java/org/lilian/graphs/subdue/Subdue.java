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
import org.lilian.graphs.subdue.Wrapping.TagToken;
import org.lilian.graphs.subdue.Wrapping.Token;
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
	private InexactCost<L> costFunction = null;
	
	private UTGraph<L, T> data;
	
	private Set<L> labels;
	private Set<T> tags;
	
	public Subdue(UTGraph<L, T> graph)
	{
		labels = graph.labels();
		tags = graph.tags();
		
		data = graph;
	}

	public Subdue(UTGraph<L, T> graph, InexactCost<L> costFunction, double costThreshold)
	{
		this(graph, costFunction, costThreshold, -1);
	}
	
	public Subdue(UTGraph<L, T> graph, InexactCost<L> costFunction, double costThreshold, int matcherBeamWidth)
	{
		this.matcherBeamWidth = matcherBeamWidth;
		
		labels = graph.labels();
		tags = graph.tags();
		
		this.costThreshold = costThreshold;
		this.costFunction = costFunction;
		
		data = graph;
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
		LinkedList<Substructure> parents = new LinkedList<Substructure>(),
				                 children = new LinkedList<Substructure>(),
		                         bestList = new LinkedList<Substructure>();
		
		for(L label : labels)
			parents.add(new Substructure(substructure(label)));
		
		sort(parents);
		
		for(int i : series(iterations))
		{	
//			Global.log().info("iteration " + i);
//			for(Substructure sub : bestList)
//				System.out.println(sub);
			
			// * generate all extensions of the parents
			List<MapUTGraph<L, T>> extensions = new LinkedList<MapUTGraph<L, T>>();
			for(Substructure parent : parents)
				for(MapUTGraph<L, T> child : substructures(parent.subgraph()))
					if(child.size() <= maxSubSize || maxSubSize == -1)
						extensions.add(child);
			
			// * check for isomorphic children
			while(! extensions.isEmpty())
			{
				MapUTGraph<L, T> next = extensions.remove(0);

				Iterator<MapUTGraph<L, T>> iterator = extensions.iterator();
				while(iterator.hasNext())
				{
					MapUTGraph<L, T> other = iterator.next();
					
					UndirectedVF2<L, T> vf2 = 
							new UndirectedVF2<L, T>(next, other, true);
					
					if(vf2.matches())
						iterator.remove();
				}
				
				children.add(new Substructure(next));
			}

			sort(children);
						
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
	public MapUTGraph<L, T> substructure(L label)
	{
		MapUTGraph<L, T> graph = new MapUTGraph<L, T>();
		graph.add(label);
		
		return graph;
	}
	
	/**
	 * Returns all substructures that can be derived by adding a link to the 
	 * given parent (possibly by also adding a new node)
	 */
	public List<MapUTGraph<L, T>> substructures(MapUTGraph<L, T> parent)
	{
		List<MapUTGraph<L, T>> substructures = new ArrayList<MapUTGraph<L, T>>();
		
		// * Try all additional connections between existing nodes
		for(int i : series(parent.nodes().size()))
			for(int j : series(i+1, parent.nodes().size()))
			{
				UTNode<L, T> 
						n1 = parent.nodes().get(i), 
						n2 = parent.nodes().get(j);
				
				for(T tag : tags)
				{	
					if(! n1.connected(n2, tag))
					{
						MapUTGraph<L, T> sub = copy(parent, n1, n2, tag);
						substructures.add(sub);
					}
				}
			}
		
		// * Try all connections to a new node
		for(int i : series(parent.nodes().size()))
			for(L label : labels)
				for(T tag : tags)
				{
					UTNode<L, T> n1 = parent.nodes().get(i);
					MapUTGraph<L, T> sub = copy(parent, n1, label);
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
	private MapUTGraph<L, T> copy(
			MapUTGraph<L, T> parent, 
			UTNode<L, T> n1, 
			L label)
	{
		MapUTGraph<L, T> child = new MapUTGraph<L, T>();
		
		// * Note : these intermediate lists are no longer necessary.
		List<UTNode<L, T>> nodesIn = new ArrayList<UTNode<L, T>>(parent.nodes());
		List<UTNode<L, T>> nodesOut = new ArrayList<UTNode<L, T>>(parent.size());

		
		for(UTNode<L, T> nodeIn : nodesIn)
			nodesOut.add(child.add(nodeIn.label()));
		UTNode<L, T> newNode = child.add(label);
		
		for(int i : series(nodesIn.size()))
		{
			UTNode<L, T> ni = nodesIn.get(i);
			for(int j : series(i, nodesIn.size()))
			{
				UTNode<L, T> nj = nodesIn.get(j);
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
	private MapUTGraph<L, T> copy(
				MapUTGraph<L, T> parent,
				UTNode<L, T> n1,
				UTNode<L, T> n2,
				T newTag)
	{
		MapUTGraph<L, T> child = new MapUTGraph<L, T>();
		List<UTNode<L, T>> nodesIn = new ArrayList<UTNode<L, T>>(parent.nodes());
		List<UTNode<L, T>> nodesOut = new ArrayList<UTNode<L, T>>(parent.size());
		
		for(UTNode<L, T> nodeIn : nodesIn)
			nodesOut.add(child.add(nodeIn.label()));
		
		for(int i : series(nodesIn.size()))
			for(int j : series(i, nodesIn.size()))
			{
				UTNode<L, T> ni = nodesIn.get(i), nj = nodesIn.get(j);
				
				if(ni.connected(nj))
					nodesOut.get(i).connect(nodesOut.get(j), ni.link(nj).tag());
				if(ni.equals(n1) && nj.equals(n2))
					nodesOut.get(i).connect(nodesOut.get(j), newTag);
			}
					
			
		return child;
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
		private MapUTGraph<L, T> subGraph;
		private double score;
		
		public Substructure(MapUTGraph<L, T> subGraph)
		{
			this.subGraph = subGraph;
			calculateScore();
		}
		
		public MapUTGraph<L, T> subgraph()
		{
			return subGraph;
		}

		private void calculateScore()
		{
			score = GraphMDL.mdl(data, subGraph, costThreshold, matcherBeamWidth);
		}
		
		public double score()
		{
			return score;
		}

		public boolean matches(Substructure other)
		{
			InexactMatch<L, T> im = 
					new InexactMatch<L, T>(
							this.subGraph, other.subGraph, costFunction, costThreshold);
			
			return im.matches();
		}
		
		public UTGraph<Token, TagToken> silhouette()
		{
			InexactSubgraphs<L, T> is = 
					new InexactSubgraphs<L, T>(data, subGraph, costFunction, costThreshold, false);
			
			return is.silhouette();
		}

		@Override
		public int compareTo(Substructure other)
		{
			return Double.compare(this.score, other.score);
		}
		
		public String toString()
		{
			InexactSubgraphs<L, T> is = new InexactSubgraphs<L, T>(data, subGraph, costFunction, costThreshold, false, matcherBeamWidth);
			int matches = is.numMatches();
			
			return subGraph.toString() + " " + score + " " + GraphMDL.mdl(subGraph) + " " + matches;
		}
	}
	/**
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
			if(in instanceof Wrapping<?, ?>.LabelToken && out instanceof Wrapping<?, ?>.LabelToken)
				return master.relabel(((Wrapping<L, T>.LabelToken)in).label(), ((Wrapping<L, T>.LabelToken)out).label() );
			return 1.0;
		}

		@Override
		public double removeNode(Token label)
		{
			if(label instanceof Wrapping<?, ?>.LabelToken)
				return master.removeNode(((Wrapping<L, T>.LabelToken)label).label());
			return 1.0;
		}

		@Override
		public double addNode(Token label)
		{
			if(label instanceof Wrapping<?, ?>.LabelToken)
				return master.addNode(((Wrapping<L, T>.LabelToken)label).label());
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
	
	*/
}
