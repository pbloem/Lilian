//package org.lilian.graphs.subdue;
//
//import static org.lilian.util.Functions.log2;
//import static org.lilian.util.Series.series;
//import static org.lilian.graphs.subdue.Wrapping.Token;
//import static org.lilian.graphs.subdue.Wrapping.TagToken;
//import org.lilian.graphs.subdue.Wrapping.LabelToken;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.math.util.MathUtils;
//import org.lilian.graphs.UTGraph;
//import org.lilian.graphs.UTLink;
//import org.lilian.graphs.UTNode;
//import org.lilian.models.BasicFrequencyModel;
//import org.lilian.models.FrequencyModel;
//import org.lilian.util.Functions;
//import org.lilian.util.Series;
//import org.lilian.util.graphs.old.BaseGraph;
//import org.lilian.util.graphs.old.Graph;
//import org.lilian.util.graphs.old.Graphs;
//import org.lilian.util.graphs.old.Node;
//
///**
// * Utility functions for calculating the compressibility of graphs and graphs. 
// * 
// * We assume, for the time being, undirected graphs with with a single, 
// * non-unique label per node, a single tag per link, but multiple links between 
// * nodes. Even for a given tag, there can be multiple links between two nodes.
// * 
// * @author Peter
// *
// */
//public class GraphMDL
//{
//	
//	/**
//	 * Returns an estimate of the number of bits required to store the given graph.
//	 * 
//	 * It is assumed that the sender and receiver share an efficient codebook 
//	 * for the labels and tags of the graph.
//	 * 
//	 * We store the graphs by first storing an adjacency bitmatrix. This matrix
//	 * has in each cell a zero if there are no links between the given nodes, 
//	 * and a one if there is one link or more. We then add a list of pairs to 
//	 * encode the cells that have more than one connection    
//	 *  
//	 * @param graph
//	 * @return
//	 */
//	public static <L, T> double mdl(UTGraph<L, T> graph)
//	{
//		
//		BasicFrequencyModel<L> labels = new BasicFrequencyModel<L>();
//		for(UTNode<L, T> node : graph.nodes())
//			labels.add(node.label());
//		
//		BasicFrequencyModel<T> tags = new BasicFrequencyModel<T>();
//		for(UTLink<L, T> link : graph.links())
//			tags.add(link.tag());
//		
//		return mdl(graph, labels, tags);
//	}
//	
//	public static <L, T> double mdlWrapped(
//			UTGraph<Wrapping.Token, Wrapping.TagToken> graph, 
//			BasicFrequencyModel<L> labelBook, BasicFrequencyModel<T> tagBook, int substructureSize)
//	{
//		int n = graph.size();
//		
//		// * If the graph is empty or size 1, we must 
//		//   establish this. If we used the empty string for either we would lose
//		//   the self-delimiting properties of the representation.
//		if(graph.size() == 0)
//			return 1;
//		if(graph.size() == 1 && labelBook.distinct() == 1.0) 
//			return 2.0;
//		
//		double aBits = matrix(graph);	
//		
//		// * Encode the node labels
//		double nBits = 0.0;
//
//		int symbolNodes = 0;
//		for(UTNode<Token, TagToken> node : graph.nodes())
//		{
//			if(node.label() instanceof Wrapping.LabelToken)
//				nBits += - log2(labelBook.probability(
//						((Wrapping<L, T>.LabelToken)(node.label())).label()
//						));
//			else
//				symbolNodes++;
//		}
//		
//		// * store the number of symbol nodes as the first element in the sequence  
//		nBits += prefix(symbolNodes);
//		
//		
//		//nBits = Math.ceil(nBits);
//		
//		// *  Encode the tags
//		// - We assume that symboltagtokens are not used
//		
//		double tBits = 0.0;
//		for(UTLink<Token, TagToken> link : graph.links())
//		{
//			if(link.tag() instanceof Wrapping.LabelTagToken)
//				tBits += - log2(tagBook.probability(
//							((Wrapping<L, T>.LabelTagToken) (link.tag())).tag()
//						));
//		}
//		
//		// * store the possible connection annotations
//		//   At this point we know the symbol nodes and we have an ordering for 
//		//   them. For each symbolnode we need one annotation per link to the 
//		//   symbol node. We store this as a list of uniformly coded references to 
//		//   nodes inside the substructure. We know the length of this list, 
//		//   because we have the structure of the graph.
//		for(UTNode<Token, TagToken> node : graph.nodes())
//		{
//			if(node.label() instanceof Wrapping.SymbolToken)
//			{	
//				int neighbours = node.neighbors().size();
//				tBits += neighbours * log2(substructureSize);
//			}
//		}
//			
////		System.out.println("data: a " + aBits);
////		System.out.println("data: n " + nBits);
////		System.out.println("data: t " + tBits);
//		
//		return aBits + nBits + tBits;
//	}
//
//	public static <L, T> double mdl(UTGraph<L, T> graph, FrequencyModel<L> labelBook, FrequencyModel<T> tagBook)
//	{
//		// * If the graph is empty or size 1, we must 
//		//   establish this. If we used the empty string for either we would lose
//		//   the self-delimiting properties of the representation.
//		if(graph.size() == 0)
//			return 1;
//		if(graph.size() == 1 && labelBook.distinct() == 1.0) 
//			return 2.0;
//		
//		double aBits = matrix(graph);	
//		
//		// * Encode the node labels
//		double nBits = 0.0;
//
//		for(UTNode<L, T> node : graph.nodes())
//			nBits += - log2(labelBook.probability(node.label()));
//		
//		// nBits++;
//		// nBits = Math.ceil(nBits);
//		
//		// *  Encode the tags
//		double tBits = 0.0;
//		for(UTLink<L, T> link : graph.links())
//			tBits += - log2(tagBook.probability(link.tag()));
//			
////		System.out.println("plain: a " + aBits);
////		System.out.println("plain: n " + nBits);
////		System.out.println("plain: t " + tBits);
//
//		return aBits + nBits + tBits;
//	}
//	
//	private static <L, T> double matrix(UTGraph<L, T> graph)
//	{
//		// * Encode the adjacency matrix row by row
//		int n = graph.size();
//
//		double aBits = 0;
//		
//		// * Store the max number of neighbours a node can have, so that we know
//		//   how many bits to read for the number of neighbours.
//		// int maxNeighbours = Integer.MIN_VALUE;
//		// for(UTNode<L, T> node : graph.nodes())
//		// 	maxNeighbours = Math.max(maxNeighbours, node.neighbors().size());
//		
//		//aBits += length(maxNeighbours + 1);
//		
//		int total = 0; // count the total number of connected pairs for later 
//		
//		int rowSize = 1;
//		for(UTNode<L, T> node : graph.nodes())
//		{
//			int k = 0; 
//			// * Count the neighbours with index above the current one (since 
//			//   we're only storing half the adjacency matrix 
//			for(UTNode<L, T> neighbour : node.neighbors())
//				if(neighbour.index() <= node.index())
//					k++;
//			
//			total += k;
//			
//			// * Store the number of neighbours
//			aBits += prefix(k);
//			// * Store the configuration of neighbours
//			aBits += Math.ceil(MathUtils.binomialCoefficientLog(rowSize, k)/Math.log(2.0));
//			
//			rowSize++;
//		}
//		
////		int cells = (n * n + n) / 2;
////		int connections = 0;
////		for(int i : Series.series(n))
////			for(int j : Series.series(i, n))
////				if(graph.nodes().get(i).connected(graph.nodes().get(j)))
////					connections++;
////		
////		aBits += Math.ceil(MathUtils.binomialCoefficientLog(cells, connections)/Math.log(2.0));
//
//		
//		// * The maximum number of links between a given node pair
//		double maxLinks = Double.MIN_VALUE;
//		for(int i : Series.series(n))
//			for(int j : Series.series(i + 1, n))
//				maxLinks = Math.max(
//						maxLinks, 
//						graph.nodes().get(i).links(graph.nodes().get(j)).size());
//				
//		// * store the cells with more than one link
//		//   We store these as a list of pairs: (which cell, how many links)
//		double multiLink = 0.0;
//		for(int i : Series.series(n))
//			for(int j : Series.series(i + 1, n))
//				if(graph.nodes().get(i).links(graph.nodes().get(j)).size() > 1)
//					multiLink++;
//		
//		// * single bit to signal whether a list of multilink pairs will follow
//		aBits += 1;								
//		if(maxLinks > 1)
//		{
//			aBits += multiLink * (log2(total + 1) + log2(maxLinks - 1)); 
//				// - total + 1 because we need to be able to signal the end of 
//				// the sequence, maxLinks - 1 because we don't need to encode 1  
//			aBits += 1; // * signal the end of the sequence
//		}
//		
//		return aBits;
//	}
//
//	public static <L, T> double mdl(
//			UTGraph<L, T> graph, UTGraph<L, T> substructure, 
//			double threshold)
//	{
//		return mdl(graph, substructure, threshold, -1);
//	}
//	
//	/**
//	 * How many bits are required to store the graph, if we use a symbol for the
//	 * given substructure.
//	 * 
//	 * This method is currently a relatively crude approximation. A more elegant 
//	 * solution is to actually construct the graph with the substructures and get
//	 * the size directly.  
//	 * 
//	 * @param graph
//	 * @param substructure
//	 * @return
//	 */
//	public static <L, T> double mdl(
//			UTGraph<L, T> graph, UTGraph<L, T> substructure, 
//			double threshold, int beamWidth)
//	{
////		System.out.println("graph: " + graph);
////		System.out.println("sub: " + substructure);
//
//		BasicFrequencyModel<L> labels = new BasicFrequencyModel<L>();
//		for(UTNode<L, T> node : graph.nodes())
//			labels.add(node.label());
//		
//		BasicFrequencyModel<T> tags = new BasicFrequencyModel<T>();
//		for(UTLink<L, T> link : graph.links())
//			tags.add(link.tag());		
//				
//		// * Store the substructure
//		//   (the end of this representation is recognizable, so no prefix coding required) 
//		double sub = mdl(substructure, labels, tags);
//		
//		InexactCost<L> cost = CostFunctions.transformationCost(
//			graph.labels().size(), substructure.size(), substructure.numLinks());
//		InexactSubgraphs<L, T> is = 
//			new InexactSubgraphs<L, T>(graph, substructure, cost, threshold, false, beamWidth);
//		
//		// * Store the leftover graph
//		double sil =  mdlWrapped(is.silhouette(), labels, tags, substructure.size());
//		
////		System.out.println(is.numMatches() + " matches");
////		System.out.println(sub + " " + sil);
////		System.out.println(mdl(is.silhouette()));
//		
//		// * Store the transformations for the inexact matches
//		double trans = 0.0;
//		for(int i : Series.series(is.numMatches()))
//			trans += is.transCosts().get(i);
//		
//		return sub + sil + trans;
//	}
//	
//	/**
//	 * The cost of storing the given value in prefix coding
//	 * @param bits
//	 * @return
//	 */
//	public static double prefix(int value)
//	{
////		if(bits == 0)
////			return 1;
////		
////		return clog2(bits) + bits;
//		
//		return prefix(value, 3);
//	}
//	
//	public static int prefix(int value, int d)
//	{
//		if(value < 0)
//			System.out.println(value + " " + d);
//		
//		if(d == 0)
//			return 2 * length(value) + 1;
//		
//		return length(value) + length(prefix(length(value), d - 1));
//	}
//	
//	/**
//	 * The length of the given value in the canonical bitstring representation 
//	 * of integers. (0 = "", 1="0", 2="1", 3="00", etc).
//	 * 
//	 * @param in
//	 * @return
//	 */
//	public static int length(int in)
//	{
//		if(in == 0)
//			return 0;
//		
//		return (int)Math.ceil(log2(in + 1));
//	}
//}
