package org.lilian.graphs.compression;

import static org.lilian.graphs.compression.Functions.prefix;
import static org.lilian.util.Functions.log2;
import static org.lilian.util.Functions.logFactorial;

import java.util.List;

import org.lilian.graphs.DGraph;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Link;
import org.lilian.graphs.Node;
import org.lilian.graphs.UGraph;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Compressor;

public class EdgeListCompressor<N> extends AbstractGraphCompressor<N>
{

	@Override
	public double structureBits(Graph<N> graph, List<Integer> order)
	{
		if(graph instanceof UGraph<?>)
			return undirected((UGraph<N>) graph, order);
		
		if(graph instanceof DGraph<?>)
			return directed((DGraph<N>) graph, order);
		
		throw new IllegalArgumentException("Can only handle graphs of type UGraph or DGraph");
	}
	
	public double undirected(Graph<N> graph, List<Integer> order)
	{
		
		BasicFrequencyModel<Node<N>> model = new BasicFrequencyModel<Node<N>>();
		
		long bits = 0;
		
		bits += prefix(graph.size());
		bits += prefix(graph.numLinks());
		
		for(Node<N> node : graph.nodes())
			model.add(node, 0.0);
				
		for(Link<N> link : graph.links())
		{
			double p = p(link.first(), model) * p(link.second(), model);
			if(! link.first().equals(link.second()))
				p *= 2.0;
			
			model.add(link.first());
			model.add(link.second());
			
			bits += -log2(p);
		}
		
		bits -= logFactorial(graph.numLinks());
		
		return bits;
	}	
	
	public double directed(Graph<N> graph, List<Integer> order)
	{
		
		BasicFrequencyModel<Node<N>> source = new BasicFrequencyModel<Node<N>>();
		BasicFrequencyModel<Node<N>> target = new BasicFrequencyModel<Node<N>>();

		
		long bits = 0;
		
		bits += prefix(graph.size());
		bits += prefix(graph.numLinks());
		
		for(Node<N> node : graph.nodes())
		{
			source.add(node, 0.0);
			target.add(node, 0.0);
		}
				
		for(Link<N> link : graph.links())
		{
			double p = p(link.first(), source) * p(link.second(), target);
			
			source.add(link.first());
			target.add(link.second());
			
			bits += -log2(p);
		}
		
		bits -= logFactorial(graph.numLinks());
		
		return bits;
	}	
	
	private static <N> double p(N symbol, BasicFrequencyModel<N> model)
	{
		double freq = model.frequency(symbol),
		       total = model.total(),
		       distinct = model.distinct();
			
		// TODO parametrize smoothing
		return (freq + 0.5) / (total + 0.5 * distinct);
	}
}
