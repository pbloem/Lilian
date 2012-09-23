package org.lilian.util.graphs.algorithms;

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

import org.lilian.util.Series;
import org.lilian.util.distance.Metrizable;
import org.lilian.util.graphs.BaseGraph;
import org.lilian.util.graphs.Graph;
import org.lilian.util.graphs.Node;

/**
 * Subdue (Jonyer, Cook, Holder, 2003) finds relevant substructures in a graph 
 * based on an MDL criterion
 * 
 *  
 * @author Peter
 *
 */
public class Subdue<L, N extends Node<L, N>>
{
	private double costThreshold = 0.0;
	private InexactCost<Token> costFunction = null;
	
	private Graph<L, N> graph;
	private Set<L> labels;
	
	public Subdue(Graph<L, N> graph)
	{
		this.graph = graph;
		labels();
	}

	public Subdue(Graph<L, N> graph, InexactCost<L> costFunction, double costThreshold)
	{
		this.costThreshold = costThreshold;
		this.costFunction = new CostWrapper(costFunction);
		this.graph = graph;
		
		labels();
	}
	
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
			// * generate all extensions of the parents
			List<BaseGraph<Token>> extensions = new LinkedList<BaseGraph<Token>>();
			for(Substructure parent : parents)
				for(BaseGraph<Token> child : substructures(parent.subgraph()))
					if(child.size() <= maxSubSize)
						extensions.add(child);
			
			// * check for isomorphic children
			while(! extensions.isEmpty())
			{
				BaseGraph<Token> next = extensions.remove(0);
				
				Iterator<BaseGraph<Token>> iterator = extensions.iterator();
				while(iterator.hasNext())
				{
					BaseGraph<Token> other = iterator.next();
					
					InexactMatch<Token, BaseGraph<Token>.Node> im = 
							new InexactMatch<Token, BaseGraph<Token>.Node>(
									next, other, costFunction, costThreshold);
					
					if(im.matches())
						iterator.remove();
				}
				
				children.add(new Substructure(next));
			}
			
			sort(children);
			while(children.size() > beamWidth)
				children.pollLast();
			
			bestList.addAll(parents);
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
	public BaseGraph<Token> substructure(L label)
	{
		BaseGraph<Token> graph = new BaseGraph<Token>();
		graph.addNode(new LabelToken(label));
		
		return graph;
	}
	
	private void labels()
	{
		labels = new LinkedHashSet<L>();

		for(N node : graph)
			labels.add(node.label());
	}
	
	/**
	 * Returns all substructures that can be derived by adding a link to the 
	 * given parent (possibly by also adding a new node)
	 */
	public List<BaseGraph<Token>> substructures(BaseGraph<Token> parent)
	{
		List<BaseGraph<Token>.Node> nodes = new ArrayList<BaseGraph<Token>.Node>(parent);
		List<BaseGraph<Token>> substructures = new ArrayList<BaseGraph<Token>>();
		
		// * Try all additional connections between existing nodes
		for(int i : series(nodes.size()))
			for(int j : series(i, nodes.size()))
			{
				BaseGraph<Token>.Node n1 = nodes.get(i), n2 = nodes.get(j);
				if(! n1.connected(n2))
				{
					BaseGraph<Token> sub = copy(parent, n1, n2);
					substructures.add(sub);
				}
			}
		
		// * Try all connections to a new node
		for(int i : series(nodes.size()))
			for(L label : labels)
			{
				BaseGraph<Token>.Node n1 = nodes.get(i);
				BaseGraph<Token> sub = copy(parent, n1, label);
				substructures.add(sub);
			}
		
		return substructures;

	}
	
	private BaseGraph<Token> copy(BaseGraph<Token> parent, BaseGraph<Token>.Node n1, L label)
	{
		BaseGraph<Token> child = new BaseGraph<Token>();
		List<BaseGraph<Token>.Node> nodesIn = new ArrayList<BaseGraph<Token>.Node>(parent);
		List<BaseGraph<Token>.Node> nodesOut = new ArrayList<BaseGraph<Token>.Node>(parent.size());

		
		for(BaseGraph<Token>.Node nodeIn : nodesIn)
			nodesOut.add(child.addNode(nodeIn.label()));
		BaseGraph<Token>.Node newNode = child.addNode(new LabelToken(label));
		
		for(int i : series(nodesIn.size()))
		{
			BaseGraph<Token>.Node ni = nodesIn.get(i);
			for(int j : series(i, nodesIn.size()))
			{
				BaseGraph<Token>.Node nj = nodesIn.get(j);
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
	private BaseGraph<Token> copy(
			BaseGraph<Token> parent,
			BaseGraph<Token>.Node n1,
			BaseGraph<Token>.Node n2)
	{
		BaseGraph<Token> child = new BaseGraph<Token>();
		List<BaseGraph<Token>.Node> nodesIn = new ArrayList<BaseGraph<Token>.Node>(parent);
		List<BaseGraph<Token>.Node> nodesOut = new ArrayList<BaseGraph<Token>.Node>(parent.size());
		
		for(BaseGraph<Token>.Node nodeIn : nodesIn)
			nodesOut.add(child.addNode(nodeIn.label()));
		
		for(int i : series(nodesIn.size()))
			for(int j : series(i, nodesIn.size()))
			{
				BaseGraph<Token>.Node ni = nodesIn.get(i), nj = nodesIn.get(j);
				if(ni.connected(nj))
					nodesOut.get(i).connect(nodesOut.get(j));
				if(ni.equals(n1) && nj.equals(n2))
					nodesOut.get(i).connect(nodesOut.get(j));
			}
					
			
		return child;
	}


	/**
	 * A token is a node label in a substructure. It can represent a labeled 
	 * node in the graph or a variable node.
	 * 
	 * @author Peter
	 *
	 */
	private interface Token {
		
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
			return label.toString();
		}
	}
	
	/**
	 * A subgraph with an associated MDL score
	 * 
	 * @author Peter
	 *
	 */
	private class Substructure 
		implements Comparable<Substructure>
	{
		private BaseGraph<Token> subGraph;
		private double score;
		
		public Substructure(BaseGraph<Token> subGraph)
		{
			this.subGraph = subGraph;
			calculateScore();
		}
		
		public BaseGraph<Token> subgraph()
		{
			return subGraph;
		}

		private void calculateScore()
		{
			score = 1;
		}
		
		public double score()
		{
			return score;
		}

		public boolean matches(Substructure other)
		{
			InexactMatch<Token, BaseGraph<Token>.Node> im = 
					new InexactMatch<Subdue.Token, BaseGraph<Token>.Node>(
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
			return subGraph.toString() + "_" + score;
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
