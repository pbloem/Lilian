package org.lilian.graphs.grammar;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static org.lilian.util.Functions.log2;
import static org.lilian.util.Functions.overlap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.print.DocFlavor.STRING;

import org.lilian.Global;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.Graphs;
import org.nodes.Link;
import org.nodes.MapDTGraph;
import org.nodes.Node;
import org.nodes.Subgraph;
import org.nodes.TLink;
import org.nodes.TNode;
import org.nodes.algorithms.Nauty;
import org.nodes.random.SimpleSubgraphGenerator;
import org.nodes.random.SubgraphGenerator;

import nl.peterbloem.kit.AbstractGenerator;
import nl.peterbloem.kit.FrequencyModel;
import nl.peterbloem.kit.Functions;
import nl.peterbloem.kit.Order;
import nl.peterbloem.kit.Series;

/**
 * Clustering by hierarchical motif substitution.
 * @author Peter
 *
 */
public class Clustering
{
	// * The minimal subgraph size
	private static int MIN_DEPTH = 4, MAX_DEPTH =5;
	// * The minimal number of distinct occurrences required for a subgraph to be 
	//   replaced by a symbol node
	private static int MIN_OCCURRENCES = 5;
	
	private boolean COUNT_TIES = false;
	
	private DTGraph<String, String> graphOrig;
	private DTGraph<String, String> current;
	
	private List<XRule> xRules = new ArrayList<XRule>();
	
	public Clustering(DTGraph<String, String> graph)
	{
		this.graphOrig = graph;
		this.current = graph;
	}
	
	public boolean learn(int samples)
	{
		SubgraphGenerator<String> gen = new SubgraphGenerator<String>(current, new UniformGenerator(MIN_DEPTH, MAX_DEPTH+1));
		// * Sample subgraphs
		
		SubgraphStore subStore = new SubgraphStore();
		
		for(int i : Series.series(samples))
		{
			if(samples/5 != 0 && i%(samples/5) == 0)
				Global.log().info("sample: " + i);
			
			SubgraphGenerator<String>.Result result = gen.generate();
			DTGraph<String, String> sub = 
					Subgraph.dtSubgraphIndices(current, result.indices());
							
			int ties = 0;
			List<Node<String>> nodes = result.nodes();
//			for(Node<String> node : nodes)
//				for(Node<String> neighbor : node.neighbors())
//					if(! nodes.contains(neighbor))
//						for(Link<String> link : node.links(neighbor))
//							ties++;
									
			subStore.observe(sub, result.indices(), result.invProbability());
		}
		
		DTGraph<String, String> best = subStore.bestMatch();
		
		if(best == null)
			return false;
		
		System.out.println(current.size() + ", " + current.numLinks());
		System.out.println("To extract: " + best);
		
		newXRule(best, subStore.occurrences(best));
		
		System.out.println(current.size() + ", " + current.numLinks());
				
		return true;
	}
	
	private void newXRule(DTGraph<String, String> sub, Set<List<Integer>> occurrences)
	{
		XRule rule = new XRule(sub);
			
		// * Collect the nodes that have been extracted
		Set<Node<String>> toRemove = new HashSet<Node<String>>();
		
		// * Translate the occurrences from integers to nodes 
		List<List<DTNode<String, String>>> occ = new ArrayList<List<DTNode<String,String>>>(occurrences.size());
		for(List<Integer> occurrence : occurrences)
		{
			List<DTNode<String, String>> nodes = new ArrayList<DTNode<String, String>>(occurrence.size());
			for(int index : occurrence)
				nodes.add(current.get(index));
			occ.add(nodes);
		}
	
		// * For each occurrence of the motif on the graph
		for(List<DTNode<String, String>> nodes : occ)
			if(alive(nodes)) // -- make sure none of the nodes of the occurrence 
				             //    been removed. If two occurrences overlap, 
			                 //    only the first gets replaced. 
			{
				// * Wire a new symbol node into the graph to represent the occurrence
				DTNode<String, String> newNode = current.add(rule.from());

				for(DTNode<String, String> node : nodes)
					for(DTNode<String, String> neighbor : node.neighbors())
						if(! nodes.contains(neighbor))
						{
							for(DTLink<String, String> link : node.linksOut(neighbor))
								newNode.connect(neighbor, link.tag());
							for(DTLink<String, String> link : node.linksIn(neighbor))
								neighbor.connect(newNode, link.tag());
						}
				
				for(DTNode<String, String> node : nodes)
					node.remove();
			}		
	}	

	private static boolean alive(List<? extends Node<String>> nodes)
	{
		for(Node<String> node : nodes)
			if(node.dead())
				return false;
		
		return true;
	}

	private int numXRule = 0;
	
	private class XRule
	{
		public XRule(DTGraph<String, String> sub)
		{
			from = "X" + numXRule;
			to = sub;
			
			numXRule++;
		}
		
		public String from()
		{
			return from;
		}
		
		private String from;
		private DTGraph<String, String> to;
	}

	private class SubgraphStore 
	{
		private FrequencyModel<DTGraph<String, String>> bfmWeighted = new FrequencyModel<DTGraph<String,String>>();
		private FrequencyModel<DTGraph<String, String>> bfmRaw = new FrequencyModel<DTGraph<String,String>>();
		private FrequencyModel<DTGraph<String, String>> tiesSum = new FrequencyModel<DTGraph<String,String>>();

		private Map<DTGraph<String, String>, Set<List<Integer>>> occurrences = 
				new LinkedHashMap<DTGraph<String, String>, Set<List<Integer>>>();
		
		public void observe(DTGraph<String, String> subgraph, List<Integer> nodes, double weight)
		{
			Order order = Nauty.order(subgraph, new Functions.NaturalComparator<String>());
			DTGraph<String, String> canonical = Graphs.reorder(subgraph, order);
			
			bfmRaw.add(canonical);
			bfmWeighted.add(canonical, weight);
			
			if(! occurrences.containsKey(canonical))
				occurrences.put(canonical, new LinkedHashSet<List<Integer>>());
			
			occurrences.get(canonical).add(unmodifiableList(nodes));
						
			tiesSum.add(canonical);
					
		}
		
		public double frequency(DTGraph<String, String> subgraph)
		{
			Order order = Nauty.order(subgraph, new Functions.NaturalComparator<String>());
			DTGraph<String, String> canonical = Graphs.reorder(subgraph, order);
			
			return bfmWeighted.frequency(canonical);
		}
		
		public Set<List<Integer>> occurrences(DTGraph<String, String> subgraph)
		{
			Order order = Nauty.order(subgraph, new Functions.NaturalComparator<String>());
			DTGraph<String, String> canonical = Graphs.reorder(subgraph, order);
			
			if(! occurrences.containsKey(canonical))
				return emptySet();
			
			return unmodifiableSet(occurrences.get(canonical));
		}
		
		/**
		 * The ties are the links from the rest of the graph into the subgraph 
		 * @return
		 */
		public double averageTies(DTGraph<String, String> subgraph)
		{
			Order order = Nauty.order(subgraph, new Functions.NaturalComparator<String>());
			DTGraph<String, String> canonical = Graphs.reorder(subgraph, order);
			
			return tiesSum.frequency(canonical) / bfmRaw.frequency(canonical);
		}
		
		/**
		 * The estimated increase in compression achieved by replacing the given 
		 * subgraph with an expansion rule. The cost is as a multiple of a 
		 * an assumed average number of bits per link.
		 * 
		 * 
		 * @return
		 */
		public double benefit(DTGraph<String, String> subgraph)
		{
			return (subgraph.numLinks() * log2(current.size())) 
					- (COUNT_TIES ? averageTies(subgraph) * log2(subgraph.size()) : 0);
		}
		
		/**
		 * FIXME: this may overcount some occurrences...
		 *
		 * @param subgraph
		 * @return
		 */
		public double numOccurrences(DTGraph<String, String> subgraph)
		{
			return occurrences(subgraph).size();
		}
		
		/**
		 * Candidate for replacement
		 * @return
		 */
		public DTGraph<String, String> bestMatch()
		{
			DTGraph<String, String> best = null;
			double bestWeight = Double.NEGATIVE_INFINITY; 
			
			for(DTGraph<String, String> candidate : bfmWeighted.tokens())
			{
				boolean enoughOccurrences = numOccurrences(candidate) >= MIN_OCCURRENCES;
				boolean positiveBenefit = benefit(candidate) >= 0;
				double weight = bfmWeighted.frequency(candidate);
				
				if(enoughOccurrences && positiveBenefit)
					if(weight > bestWeight)
					{
						best = candidate;
						bestWeight = weight;
					}
			}
				
			return best; 
		}
		
	}
	
	private class LabelStore 
	{
		private Map<DTGraph<String, String>, List<FrequencyModel<String>>> labels =
				new LinkedHashMap<DTGraph<String,String>, List<FrequencyModel<String>>>();
		private Map<DTGraph<String, String>, List<FrequencyModel<String>>> tags =
				new LinkedHashMap<DTGraph<String,String>, List<FrequencyModel<String>>>();
		
		private FrequencyModel<DTGraph<String, String>> bfmWeighted = new FrequencyModel<DTGraph<String,String>>();
		private FrequencyModel<DTGraph<String, String>> bfmRaw = new FrequencyModel<DTGraph<String,String>>();
		private FrequencyModel<DTGraph<String, String>> tiesSum = new FrequencyModel<DTGraph<String,String>>();
		
		public void observe(DTGraph<String, String> subgraph, double weight, int ties)
		{
			DTGraph<String, String> blanked = Graphs.blank(subgraph, "");
			
			Order order = Nauty.order(blanked, new Functions.NaturalComparator<String>());
			
			DTGraph<String, String> canonicalBlanked = Graphs.reorder(blanked, order);
			DTGraph<String, String> canonicalLabeled = Graphs.reorder(subgraph, order);

			// * Set up a list of bfms for the labels
			if(! labels.containsKey(canonicalBlanked))
			{
				List<FrequencyModel<String>> list = new ArrayList<FrequencyModel<String>>();
				for(Node<String> node : canonicalBlanked.nodes())
					list.add(new FrequencyModel<String>());
					
				labels.put(canonicalBlanked, list);
			}
			
			// * Set up a list of bfms for the tags 
			if(! tags.containsKey(canonicalBlanked))
			{
				List<FrequencyModel<String>> list = new ArrayList<FrequencyModel<String>>();
				for(Link<String> link : canonicalBlanked.links())
					list.add(new FrequencyModel<String>());
					
				tags.put(canonicalBlanked, list);
			}
				
			// * Record label frequencies
			List<FrequencyModel<String>> labelList = labels.get(canonicalBlanked);
			
			int c = 0;
			for(Node<String> node : canonicalLabeled.nodes())
			{
				labelList.get(c).add(node.label());
				c++;
			}
			
			// * Record tag frequencies
			List<FrequencyModel<String>> tagList = tags.get(canonicalBlanked);
			
			c = 0;
			for(TLink<String, String> link : canonicalLabeled.links())
			{
				tagList.get(c).add(link.tag());
				c++;
			}
			
			bfmWeighted.add(canonicalBlanked, weight);
			bfmRaw.add(canonicalBlanked);
			tiesSum.add(canonicalBlanked, ties);
		}
		
		/**
		 * The ties are the links from the rest of the graph into the subgraph 
		 * @return
		 */
		public double averageTies(DTGraph<String, String> subgraph)
		{
			DTGraph<String, String> blanked = Graphs.blank(subgraph, "");

			Order order = Nauty.order(blanked, new Functions.NaturalComparator<String>());
			DTGraph<String, String> canonicalBlanked = Graphs.reorder(blanked, order);
			
			return tiesSum.frequency(canonicalBlanked) / bfmRaw.frequency(canonicalBlanked);
		}
		
		public double benefit(DTGraph<String, String> subgraph)
		{
			return (subgraph.numLinks() * Functions.log2(current.size())) 
					- (COUNT_TIES ? averageTies(subgraph) * Functions.log2(subgraph.size()) : 0);
		}
		
		public List<String> toReplace()
		{
			DTGraph<String, String> best = bestMatch();
			
			// * Find the node with the highest entropy
			List<FrequencyModel<String>> bfmLabels = labels.get(best);
			List<FrequencyModel<String>> bfmTags = tags.get(best);
			
			double bestLabelEntropy = Double.NEGATIVE_INFINITY, bestTagEntropy = Double.NEGATIVE_INFINITY;
			FrequencyModel<String> bestLabel = null, bestTag = null;
			int bestI = -1;
			
			int i = 0;
			for(FrequencyModel<String> bfm : bfmLabels)
			{
				if(bfm.entropy() > bestLabelEntropy)
				{
					bestI = i;
					bestLabelEntropy = bfm.entropy();
					bestLabel = bfm;
				}
				i++;
			}
			
			System.out.println("Best label, node i: " + best.nodes().get(bestI));
			
			for(FrequencyModel<String> bfm : bfmTags)
			{
				if(bfm.entropy() > bestTagEntropy)
				{
					bestTagEntropy = bfm.entropy();
					bestTag = bfm;
				}
			}
			
			if(bestLabelEntropy > bestTagEntropy)
				return new ArrayList<String>(bestLabel.tokens());
			
			return new ArrayList<String>(bestTag.tokens());
		}
		
		/**
		 * Best blank candidate for replacement
		 * @return
		 */
		public DTGraph<String, String> bestMatch()
		{
			DTGraph<String, String> best = null;
			double bestWeight = Double.NEGATIVE_INFINITY; 
			
			for(DTGraph<String, String> candidate : bfmWeighted.tokens())
			{
				double weight = bfmWeighted.frequency(candidate);
				
				if(weight > bestWeight)
				{
					best = candidate;
					bestWeight = weight;
				}
			}
				
			return best; 
		}
	}
	
	private static class UniformGenerator extends AbstractGenerator<Integer>
	{
		private int lower, upper;

		public UniformGenerator(int lower, int upper)
		{
			this.lower = lower;
			this.upper = upper;
		}
		
		@Override
		public Integer generate()
		{	
			return Global.random.nextInt(upper - lower) + lower;
		}
	}
	
	
	public DTGraph<String, String> current()
	{
		return current;
	}
}
