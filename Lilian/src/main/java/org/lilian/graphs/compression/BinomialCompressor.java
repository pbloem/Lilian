package org.lilian.graphs.compression;

import static org.lilian.graphs.compression.Functions.prefix;
import static org.lilian.util.Functions.log2;
import static org.lilian.util.Functions.logChoose;

import java.util.List;

import org.lilian.graphs.DGraph;
import org.lilian.graphs.DNode;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Link;
import org.lilian.graphs.Node;
import org.lilian.graphs.UGraph;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Functions;
import org.lilian.util.FunctionsTest;

public class BinomialCompressor<N> extends AbstractGraphCompressor<N>
{

	@Override
	public double structureBits(Graph<N> graph, List<Integer> order)
	{
		if(graph instanceof UGraph<?>)
			return undirected((UGraph<N>) graph);
		
		if(graph instanceof DGraph<?>)
			return directed((DGraph<N>) graph);
		
		throw new IllegalArgumentException("Can only handle graphs of type UGraph or DGraph");
	}
	
	public double undirected(Graph<N> graph)
	{
		int n = graph.size();
		int t = n * (n + 1) / 2;
		
		return prefix(n) + Functions.log2(t) + logChoose(graph.numLinks(), t);
	}
	
	public double directed(DGraph<N> graph)
	{
		double n = graph.size();
		double t = n * (double) n;
		
		return prefix(graph.size()) + Functions.log2(t) + logChoose(graph.numLinks(), t);
	}
	
	
}
