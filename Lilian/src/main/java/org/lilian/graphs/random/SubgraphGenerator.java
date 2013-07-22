package org.lilian.graphs.random;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lilian.data.real.AbstractGenerator;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Link;
import org.lilian.graphs.Node;
import org.lilian.graphs.Subgraph;
import org.lilian.graphs.random.LinkGenerators.LinkGenerator;
import org.lilian.util.Functions;
import org.lilian.util.Permutations;
import org.lilian.util.Series;

/**
 * Samples subgraphs from a given graph by the algorithm of Kashtan et al (2004)
 * 
 * @author Peter
 *
 * @param <L>
 */
public class SubgraphGenerator<L> extends AbstractGenerator<SubgraphGenerator<L>.Result>
{
	private int n;
	private Graph<L> graph;
	private LinkGenerator<L> links;
	
	
	public SubgraphGenerator(Graph<L> graph, int n)
	{
		super();
		this.n = n;
		this.graph = graph;
		
		links = new LinkGenerator<L>(graph);
	}

	@Override
	public SubgraphGenerator<L>.Result generate()
	{
		Set<Node<L>> nodes = new LinkedHashSet<Node<L>>(n);
		Set<Link<L>> linksChosen = new LinkedHashSet<Link<L>>();
		Set<Link<L>> linksCandidates = new LinkedHashSet<Link<L>>();
		
		boolean success = false;
		
		while(!success)
		{
			nodes.clear();
			linksChosen.clear();
			linksCandidates.clear();
		
			Link<L> link = links.generate();
			linksCandidates.add(null);
				
			while(! success && ! linksCandidates.isEmpty())
			{
				linksChosen.add(link);
				
				for(Node<L> node : link.nodes())
					nodes.add(node);
				
				if(nodes.size() == n)
				{
					success = true;
					break;
				}
				
				linksCandidates.clear();
				for(Link<L> linkChosen : linksChosen)
					for(Node<L> node : linkChosen.nodes())
						for(Link<L> linkCandidate : node.links())
							if(! linksChosen.contains(linkCandidate))
								linksCandidates.add(linkCandidate);
				
				if(! linksCandidates.isEmpty() )
					link = Functions.choose(linksCandidates);
			}	
		}
		
		return new Result(
				new ArrayList<Node<L>>(nodes), 
				new ArrayList<Link<L>>(linksChosen));
	}
	
	public class Result {
		private List<Node<L>> nodes;
		private List<Link<L>> trail;
		private double logProbability = 0.0;
		
		public Result(List<Node<L>> nodes, List<Link<L>> linkTrail)
		{
			this.nodes = nodes;
			this.trail = linkTrail;
			
			calculateProbability();
		}

		private void calculateProbability()
		{
			int n = trail.size();
			for(int[] perm : new Permutations(n))
			{
				double sub = Functions.log2(1.0 / graph.numLinks());
				
				for(int i : series(1, n))
				{
					Node<L> mid = common(trail.get(perm[i-1]), trail.get(perm[i]));
					if(mid == null) // not a proper trail
					{
						sub = 0.0;
						break;
					} else 
						sub += Functions.log2(1.0/(mid.degree()-1.0));
				}
					
					
				logProbability = Functions.logSum(logProbability, sub);
			}
			
		}
		
		private Node<L> common(Link<L> a, Link<L> b)
		{
			List<Node<L>> nodes = new ArrayList<Node<L>>(a.nodes());
			nodes.retainAll(b.nodes());
			
			if(nodes.isEmpty())
				return null;
			
			return nodes.get(0);
		}

		public List<Node<L>> nodes()
		{
			return nodes;
		}

		public List<Link<L>> linkTrail()
		{
			return trail;
		}

		public double logProbability()
		{
			return logProbability;
		}
		
		public Graph<L> subgraph()
		{
			return Subgraph.subgraph(graph, nodes);
		}
	}
}
