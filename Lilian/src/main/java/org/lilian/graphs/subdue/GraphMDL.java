package org.lilian.graphs.subdue;

import static org.lilian.util.Functions.log2;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.util.MathUtils;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.UTNode;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.models.FrequencyModel;
import org.lilian.util.Functions;
import org.lilian.util.Series;
import org.lilian.util.graphs.old.BaseGraph;
import org.lilian.util.graphs.old.Graph;
import org.lilian.util.graphs.old.Graphs;
import org.lilian.util.graphs.old.Node;

/**
 * Utility functions for calculating the compressibility of graphs and graphs 
 * 
 * with substructures
 * @author Peter
 *
 */
public class GraphMDL
{
	
	public static <L, T> double mdlSparse(UTGraph<L, T> graph)
	{
		
		BasicFrequencyModel<L> labels = new BasicFrequencyModel<L>();
		for(UTNode<L, T> node : graph.nodes())
			labels.add(node.label());
		
		return mdl(graph, labels);
	}
	
	public static <L, T> double mdlSparse(UTGraph<L, T> graph, FrequencyModel<L> codebook)
	{
		double bits = 0.0;
		
		int perEdge = (int) clog2(graph.size());
		// * store the max node index, so that the decoder knows how many bits 
		//  per edge
		bits += prefix(clog2(graph.size()));
		
		// * store the edges
		bits+= perEdge * 2.0 * graph.numLinks();
		
		// * store the node labels
		for(UTNode<L, T> node : graph.nodes())
			bits += log2(codebook.probability(node.label()));
		bits++;
		bits = Math.ceil(bits);
		
		return bits;
	}

	public static <L, T> double mdl(UTGraph<L, T> graph)
	{
		
		BasicFrequencyModel<L> labels = new BasicFrequencyModel<L>();
		for(UTNode<L, T> node : graph.nodes())
			labels.add(node.label());
		
		return mdl(graph, labels);
	}
		

	public static <L, T> double mdl(UTGraph<L, T> graph, FrequencyModel<L> codebook)
	{
		int n = graph.size();
		
		// * Encode the node labels
		double nBits = 0.0;

		for(UTNode<L, T> node : graph.nodes())
			nBits += log2(codebook.probability(node.label()));
		nBits++;
		nBits = Math.ceil(nBits);
		
		// * If the graph is empty or size 1, we must 
		//   establish this. If we used the empty string for either we would lose
		//   the self-delimiting properties of the representation.
		if(graph.size() == 0)
			return 1;
		if(graph.size() == 1 && nBits == 0) 
			return 2.0;
		
		// * Encode the adjacency matrix
		double aBits = 0;
		
		double maxNeighbours = Double.MIN_VALUE;
		for(UTNode<L, T> node : graph.nodes())
			maxNeighbours = Math.max(maxNeighbours, node.neighbors().size());
		
		aBits += clog2(maxNeighbours + 1);
		for(UTNode<L, T> node : graph.nodes())
		{
			int k = node.neighbors().size();
			aBits += clog2(maxNeighbours + 1) + Math.ceil(MathUtils.binomialCoefficientLog(n, k)/Math.log(2.0));
		}
		
		// *  No node edge labels yet, so no bits required for that.
			
		return nBits + aBits;
	}

	/**
	 * How many bits are required to store the graph, if we use a symbol for the
	 * given substructure.
	 * 
	 * This method is currently a relatively crude approximation. A more elegant 
	 * solution is to actually construct the graph with the substructures and get
	 * the size directly.  
	 * 
	 * @param graph
	 * @param substructure
	 * @return
	 */
	public static <L, T> double mdl(
			UTGraph<L, T> graph, UTGraph<L, T> substructure, 
			double threshold, boolean sparse)
	{
		System.out.println("graph: " + graph);
		System.out.println("sub: " + substructure);
		
		BasicFrequencyModel<L> labels = new BasicFrequencyModel<L>();
		for(UTNode<L, T> node : graph.nodes())
			labels.add(node.label());
		
		double bits = 0.0;
		
		// * Store the substructure
		//   (the end of this representation is recognizable, so no prefix coding required) 
		bits += sparse ? mdlSparse(substructure, labels) : mdl(substructure, labels);
		
		InexactCost<L> cost = CostFunctions.transformationCost(
				graph.labels().size(), substructure.size(), substructure.numLinks());
		InexactSubgraphs<L, T> is = new InexactSubgraphs<L, T>(graph, substructure, cost, threshold, false);
		
		// * Store the leftover graph
		bits += sparse ? mdlSparse(is.silhouette(), labels) : mdl(is.silhouette(), labels);

		// * Store the size of the final graph
		bits += prefix(clog2(graph.size()));
		
		// * for each substructure
		for(int i : series(is.numMatches()))
		{
			// * store the transformation cost
			bits += prefix(is.transCosts().get(i));
			
			// * store the number of links
			bits += prefix(clog2(is.numLinks().get(i) + 1));
			
			// * Store each link
			bits += is.numLinks().get(i) * (clog2(graph.size()) + clog2(substructure.size()));
		}
		
		return bits;
	}
	
	/**
	 * The cost of storing the given number of bits in prefix coding.
	 * @param bits
	 * @return
	 */
	public static double prefix(double bits)
	{
		if(bits == 0)
			return 1;
		
		return clog2(bits) + bits;
	}
	
	public static double clog2(double in)
	{
		return Math.ceil(log2(in));
	}
}
