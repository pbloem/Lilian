package org.lilian.graphs.compression;

import static org.lilian.graphs.compression.Functions.prefix;

import java.util.List;

import org.lilian.graphs.DGraph;
import org.lilian.graphs.DNode;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Link;
import org.lilian.graphs.Node;
import org.lilian.graphs.UGraph;

public class NeighborListCompressor<N> extends AbstractGraphCompressor<N>
{

	@Override
	public double compressedSize(Graph<N> graph, List<Integer> order)
	{
		if(graph instanceof UGraph<?>)
			return undirected((UGraph<N>) graph, order);
		
		if(graph instanceof DGraph<?>)
			return directed((DGraph<N>) graph, order);
		
		throw new IllegalArgumentException("Can only handle graphs of type UGraph or DGraph");
	}
	
	public double undirected(UGraph<N> graph, List<Integer> order)
	{
		long bits = 0;
		
		bits += prefix(graph.size()); 
		
		for(Node<N> node : graph.nodes())
		{
			for(Node<N> neighbor : node.neighbors())
				if(order.get(node.index()) <= order.get(neighbor.index()))
					bits += prefix(order.get(neighbor.index()) + 1);
			
			bits += prefix(0);
		}
			
		return bits;
	}
	
	public double directed(DGraph<N> graph, List<Integer> order)
	{
		long bits = 0;
		
		bits += prefix(graph.size()); 
		
		for(DNode<N> node : graph.nodes())
		{
			for(DNode<N> neighbor : node.out())
					bits += prefix(order.get(neighbor.index()) + 1);
			
			bits += prefix(0);
		}
			
		return bits;
	}

}
