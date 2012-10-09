package org.lilian.util.graphs.algorithms;

import static org.lilian.util.Functions.log2;
import static org.lilian.util.Series.series;

import org.apache.commons.math.util.MathUtils;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.models.FrequencyModel;
import org.lilian.util.Functions;
import org.lilian.util.Series;
import org.lilian.util.graphs.BaseGraph;
import org.lilian.util.graphs.Graph;
import org.lilian.util.graphs.Node;

/**
 * Utility functions for calculating the compressibility of graphs and graphs 
 * with substructures
 * @author Peter
 *
 */
public class GraphMDL
{

	public static <L, N extends Node<L, N>> double mdl(Graph<L, N> graph)
	{
		
		BasicFrequencyModel<L> labels = new BasicFrequencyModel<L>();
		for(N node : graph)
			labels.add(node.label());
		
		return mdl(graph, labels);
	}
		

	public static <L, N extends Node<L, N>> double mdl(Graph<L, N> graph, FrequencyModel<L> codebook)
	{
		int n = graph.size();
		
		// * Encode the node labels
		double nBits = 0.0;

		for(N node : graph)
			nBits += log2(codebook.probability(node.label()));
		
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
		for(N node : graph)
			maxNeighbours = Math.max(maxNeighbours, node.neighbours().size());
		
		aBits += log2(maxNeighbours + 1);
		for(N node : graph)
		{
			int k = node.neighbours().size();
			aBits += log2(maxNeighbours + 1) + MathUtils.binomialCoefficientLog(n, k)/Math.log(2.0);
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
	public static <L, N extends Node<L, N>> double mdl(Graph<L, N> graph, Graph<L, N> substructure, double threshold)
	{
		System.out.println("graph: " + graph);
		System.out.println("sub: " + substructure);
		
		BasicFrequencyModel<L> labels = new BasicFrequencyModel<L>();
		for(N node : graph)
			labels.add(node.label());
		
		double bits = 0.0;
		
		// * Store the substructure in
		//   (the end of this representation is recognizable, so no prefix coding required) 
		bits += mdl(substructure, labels);
		
		InexactCost<L> cost = CostFunctions.transformationCost(
				graph.labels().size(), substructure.size(), substructure.numEdges());
		InexactSubgraphs<L, N> is = new InexactSubgraphs<L, N>(graph, substructure, cost, threshold, false);
		
		// * Store the leftover graph
		bits += mdl(is.silhouette());
		
		// * Store the size of the final graph
		bits += prefix(log2(graph.size()));
		
		// * for each substructure
		for(int i : series(is.numMatches()))
		{
			// * store the transformation cost
			bits += prefix(is.transCosts().get(i));
			
			// * store the number of links
			bits += prefix(log2(is.numLinks().get(i) + 1));
			// * Store each link
			bits += is.numLinks().get(i) * (log2(graph.size()) + log2(substructure.size()));
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
		
		return log2(bits) + bits;
	}
}
