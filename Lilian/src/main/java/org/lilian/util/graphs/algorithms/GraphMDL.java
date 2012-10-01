package org.lilian.util.graphs.algorithms;

import static org.lilian.util.Functions.log2;

import org.apache.commons.math.util.MathUtils;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Functions;
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

	public <L, N extends Node<L, N>> double mdl(Graph<L, N> graph)
	{
		int n = graph.size();
		
		
		// * Encode the node labels
		double nBits = 0.0;
		
		BasicFrequencyModel<L> labels = new BasicFrequencyModel<L>();
		for(N node : graph)
			labels.add(node.label());
		
		for(N node : graph)
			nBits += log2(labels.probability(node.label()));
		
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
		
		
		// *  Node edge labels yet, so no bits required for that.
			
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
	public <L, N extends Node<L, N>> double mdl(Graph<L, N> graph, Graph<L, N> substructure)
	{
		double bits = 0.0;
		
		bits += mdl(substructure);
		
		int numSubs = 0;
		
		BaseGraph<L> copy = new BaseGraph<L>(graph);
		
		
		// * copy the graph
		// * delete the substructures
		// * add a transformation cost if the substructure match is inexact
		// * Add log(# of substructures)
		// * for each link from outside the substructure to inside the substructure
		//   add log(|V|) + log(|S|)
		
		
		return bits;
	}
	
	
}
