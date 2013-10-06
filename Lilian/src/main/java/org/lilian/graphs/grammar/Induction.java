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
import org.lilian.data.real.Generators;
import org.lilian.graphs.DTGraph;
import org.lilian.graphs.DTLink;
import org.lilian.graphs.DTNode;
import org.lilian.graphs.Graphs;
import org.lilian.graphs.Link;
import org.lilian.graphs.MapDTGraph;
import org.lilian.graphs.Node;
import org.lilian.graphs.Subgraph;
import org.lilian.graphs.TLink;
import org.lilian.graphs.TNode;
import org.lilian.graphs.algorithms.Nauty;
import org.lilian.graphs.random.SubgraphGenerator;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Functions;
import org.lilian.util.Order;
import org.lilian.util.Series;

/**
 * A first stab at a scaleable graph grammar induction algorithm
 * 
 * 
 * - expansion rule: replace a symbol node with a small graph
 * - substitution rule: replace a symbol node with one of a number of symbols 
 * - TODO: Record wiring information
 * @author Peter
 *
 */
public class Induction
{
	// * The minimal subgraph size
	private static int MIN_DEPTH = 5, MAX_DEPTH = 8;
	// * The minimal number of distinct occurrences required for a subgraph to be 
	//   replaced by a symbol node
	private static int MIN_OCCURRENCES = 5;
	
	private boolean COUNT_TIES = false;
	
	private DTGraph<String, String> graphOrig;
	private DTGraph<String, String> current;
	
	private List<SRule> sRules = new ArrayList<SRule>();
	private List<XRule> xRules = new ArrayList<XRule>();
	
	public Induction(DTGraph<String, String> graph)
	{
		this.graphOrig = graph;
		this.current = graph;
	}
	
	public boolean learn(int samples)
	{
		SubgraphGenerator<String> gen = new SubgraphGenerator<String>(current, Generators.uniform(MIN_DEPTH, MAX_DEPTH+1));
		// * Sample subgraphs
		
		SubgraphStore subStore = new SubgraphStore();
		LabelStore labelStore = new LabelStore();
		
		for(int i : Series.series(samples))
		{
			if(samples/10 != 0 && i%(samples/10) == 0)
				Global.log().info("it: " + i);
			
			SubgraphGenerator<String>.Result result = gen.generate();
			DTGraph<String, String> sub = 
					Subgraph.dtSubgraphIndices(current, result.indices());
							
			int ties = 0;
			List<Node<String>> nodes = result.nodes();
			for(Node<String> node : nodes)
				for(Node<String> neighbor : node.neighbors())
					if(! nodes.contains(neighbor))
						for(Link<String> link : node.links(neighbor))
							ties++;
									
			subStore.observe(sub, result.indices(), result.invProbability(), ties);
			labelStore.observe(sub, result.invProbability(), ties);
		}
		
		
		DTGraph<String, String> subMatch = subStore.bestMatch();
		DTGraph<String, String> labelMatch = labelStore.bestMatch();
		
		// * Check if any of the subgraphs found pass the threshold for extraction
		if(subMatch != null)
		{
			DTGraph<String, String> best = subStore.bestMatch();
			System.out.println("To extract: " + best);

			newXRule(best, subStore.occurrences(best));
			
			BasicFrequencyModel<String> labels = new BasicFrequencyModel<String>();
			for(Node<String> node : current.nodes())
				labels.add(node.label());
			System.out.println(labels.entropy());
			
			return true;
		} 

		if(labelMatch != null) 
		{
			System.out.println("To replace: " + labelStore.bestMatch());
			
			BasicFrequencyModel<String> bfm = new BasicFrequencyModel<String>();
			for(Node<String> node : current.nodes())
				bfm.add(node.label());
			
			System.out.println("Graph size " + current.size() + " " + bfm.entropy());

			List<String> toReplace = labelStore.toReplace();
			
			System.out.println("tr: " + toReplace);
			
			newSRule(toReplace);
			
			return true; 
		}
		
		return false;
	}
	
	private void newSRule(List<String> toReplace)
	{
		String first = toReplace.get(0);
		
		boolean label = ! current.nodes(first).isEmpty();
			
		SRule rule = new SRule(toReplace, label);
		
		DTGraph<String, String> old = current;
		
		current = new MapDTGraph<String, String>();
		
		Set<String> labelSet = new HashSet<String>(toReplace);
		
		for(Node<String> node : old.nodes())
			if(labelSet.contains(node.label()))
				current.add(rule.from());
			else
				current.add(node.label());

		for(DTLink<String, String> link : old.links())
			if(labelSet.contains(link.tag()))
				current.get(link.from().index()).connect(
						current.get(link.to().index()),
						rule.from());
			else
				current.get(link.from().index()).connect(
						current.get(link.to().index()),
						link.tag());
	}
	
	private void newXRule(DTGraph<String, String> sub, Set<List<Integer>> occurrences)
	{
		XRule rule = new XRule(sub);
			
		Set<Node<String>> toRemove = new HashSet<Node<String>>();
		
		// * Wire the new symbol into the graph
		DTNode<String, String> newNode = current.add(rule.from());
		
		for(List<Integer> occurrenceInts : occurrences)
		{
			List<DTNode<String, String>> occurrence = new ArrayList<DTNode<String,String>>();
			for(int i : occurrenceInts)
				occurrence.add(current.get(i));
			
			if(overlap(occurrence, toRemove) == 0) // Avoid overlaps
			{ 
				for(DTNode<String, String> node : occurrence)
				{
					for(DTNode<String, String> neighbor : node.neighbors())
						if(! occurrence.contains(neighbor))
							for(DTLink<String, String> link : node.links(neighbor))
							{
								if(link.from().equals(node))
									newNode.connect(link.to(), link.tag());
								else
									link.to().connect(newNode, link.tag());
							}
				}
				
				toRemove.addAll(occurrence);
			}
		}
		
		for(Node<String> node : toRemove)
			node.remove();
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
	

	private int numSRule = 0;

	private class SRule
	{
		
		public SRule(List<String> labels, boolean label)
		{
			// * Count the occurrences
			to = new BasicFrequencyModel<String>();
			
			for(String token : labels)
				to.add(token, current.nodes(token).size());
			
			from = "S" + numSRule;
			
			numSRule++;
		}
		
		public String from()
		{
			return from;
		}
		
		private String from;
		private BasicFrequencyModel<String> to;
	}

	private class SubgraphStore 
	{
		private BasicFrequencyModel<DTGraph<String, String>> bfmWeighted = new BasicFrequencyModel<DTGraph<String,String>>();
		private BasicFrequencyModel<DTGraph<String, String>> bfmRaw = new BasicFrequencyModel<DTGraph<String,String>>();
		private BasicFrequencyModel<DTGraph<String, String>> tiesSum = new BasicFrequencyModel<DTGraph<String,String>>();

		private Map<DTGraph<String, String>, Set<List<Integer>>> occurrences = 
				new LinkedHashMap<DTGraph<String, String>, Set<List<Integer>>>();
		

		
		public void observe(DTGraph<String, String> subgraph, List<Integer> nodes, double weight, int ties)
		{
			Order order = Nauty.order(subgraph, new Functions.NaturalComparator<String>());
			DTGraph<String, String> canonical = Graphs.reorder(subgraph, order);
			
			bfmRaw.add(canonical);
			bfmWeighted.add(canonical, weight);
			
			if(! occurrences.containsKey(canonical))
				occurrences.put(canonical, new LinkedHashSet<List<Integer>>());
			
			occurrences.get(canonical).add(unmodifiableList(nodes));
						
			tiesSum.add(canonical, ties);
					
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
		private Map<DTGraph<String, String>, List<BasicFrequencyModel<String>>> labels =
				new LinkedHashMap<DTGraph<String,String>, List<BasicFrequencyModel<String>>>();
		private Map<DTGraph<String, String>, List<BasicFrequencyModel<String>>> tags =
				new LinkedHashMap<DTGraph<String,String>, List<BasicFrequencyModel<String>>>();
		
		private BasicFrequencyModel<DTGraph<String, String>> bfmWeighted = new BasicFrequencyModel<DTGraph<String,String>>();
		private BasicFrequencyModel<DTGraph<String, String>> bfmRaw = new BasicFrequencyModel<DTGraph<String,String>>();
		private BasicFrequencyModel<DTGraph<String, String>> tiesSum = new BasicFrequencyModel<DTGraph<String,String>>();
		
		public void observe(DTGraph<String, String> subgraph, double weight, int ties)
		{
			DTGraph<String, String> blanked = Graphs.blank(subgraph, "");
			
			Order order = Nauty.order(blanked, new Functions.NaturalComparator<String>());
			
			DTGraph<String, String> canonicalBlanked = Graphs.reorder(blanked, order);
			DTGraph<String, String> canonicalLabeled = Graphs.reorder(subgraph, order);

			// * Set up a list of bfms for the labels
			if(! labels.containsKey(canonicalBlanked))
			{
				List<BasicFrequencyModel<String>> list = new ArrayList<BasicFrequencyModel<String>>();
				for(Node<String> node : canonicalBlanked.nodes())
					list.add(new BasicFrequencyModel<String>());
					
				labels.put(canonicalBlanked, list);
			}
			
			// * Set up a list of bfms for the tags 
			if(! tags.containsKey(canonicalBlanked))
			{
				List<BasicFrequencyModel<String>> list = new ArrayList<BasicFrequencyModel<String>>();
				for(Link<String> link : canonicalBlanked.links())
					list.add(new BasicFrequencyModel<String>());
					
				tags.put(canonicalBlanked, list);
			}
				
			// * Record label frequencies
			List<BasicFrequencyModel<String>> labelList = labels.get(canonicalBlanked);
			
			int c = 0;
			for(Node<String> node : canonicalLabeled.nodes())
			{
				labelList.get(c).add(node.label());
				c++;
			}
			
			// * Record tag frequencies
			List<BasicFrequencyModel<String>> tagList = tags.get(canonicalBlanked);
			
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
			List<BasicFrequencyModel<String>> bfmLabels = labels.get(best);
			List<BasicFrequencyModel<String>> bfmTags = tags.get(best);
			
			double bestLabelEntropy = Double.NEGATIVE_INFINITY, bestTagEntropy = Double.NEGATIVE_INFINITY;
			BasicFrequencyModel<String> bestLabel = null, bestTag = null;
			
			for(BasicFrequencyModel<String> bfm : bfmLabels)
			{
				if(bfm.entropy() > bestLabelEntropy)
				{
					bestLabelEntropy = bfm.entropy();
					bestLabel = bfm;
				}
			}
				
			for(BasicFrequencyModel<String> bfm : bfmTags)
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
}
