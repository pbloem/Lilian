package org.lilian.graphs.compression;

import static org.lilian.graphs.compression.Functions.prefix;

import java.util.List;

import org.lilian.graphs.Graph;
import org.lilian.graphs.Link;
import org.lilian.util.Compressor;

public class EdgeListCompressor<N> extends AbstractGraphCompressor<N>
{

	@Override
	public double compressedSize(Graph<N> graph, List<Integer> order)
	{
		long bits = 0;
		
		bits += prefix(graph.numLinks()); 
		
		for(Link<N> link : graph.links())
		{
			bits += prefix(order.get(link.first().index()));
			bits += prefix(order.get(link.second().index()));
		}
		
		return bits;
	}	
}
