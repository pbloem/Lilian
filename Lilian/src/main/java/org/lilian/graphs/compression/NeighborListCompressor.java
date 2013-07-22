package org.lilian.graphs.compression;

import static org.lilian.graphs.compression.Functions.prefix;
import static org.lilian.util.Functions.log2;

import java.util.List;

import org.lilian.Global;
import org.lilian.graphs.DGraph;
import org.lilian.graphs.DNode;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Link;
import org.lilian.graphs.Node;
import org.lilian.graphs.UGraph;
import org.lilian.graphs.draw.Draw;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class NeighborListCompressor<N> extends AbstractGraphCompressor<N>
{

	@Override
	public double structureBits(Graph<N> graph, List<Integer> order)
	{
		if(graph instanceof UGraph<?>)
			return size((UGraph<N>) graph, order, false);
		
		if(graph instanceof DGraph<?>)
			return size((DGraph<N>) graph, order, true);
		
		throw new IllegalArgumentException("Can only handle graphs of type UGraph or DGraph");
	}
	
	public double size(Graph<N> graph, List<Integer> order, boolean directed)
	{
		BasicFrequencyModel<Node<N>> nodes = new BasicFrequencyModel<Node<N>>();
		BasicFrequencyModel<Boolean> directions = new BasicFrequencyModel<Boolean>();
		BasicFrequencyModel<Boolean> delimiter = new BasicFrequencyModel<Boolean>();
		
		List<Integer> inv = Draw.inverse(order);
		
		double bits = 0;
		double pBits = 0;
		
		bits += prefix(graph.size()); 
		for(Node<N> node : graph.nodes())
			nodes.add(node, 0.0);
		
		directions.add(true, 0);
		directions.add(false, 0);
		
		delimiter.add(true, 0);
		delimiter.add(false, 0);
		
		for(int index : Series.series(inv.size()))
		{
			Node<N> node = graph.nodes().get(inv.get(index));
			int size = 0;
			for(Node<N> neighbor : node.neighbors())
			{
				if(order.get(neighbor.index()) <= order.get(node.index()))
				{
					size = 0;
					size++;
					
					// * Encode the reference to the neighboring node
					bits += - log2(p(neighbor, nodes));
					nodes.add(neighbor);
					
					if(directed) // * We encode the direction as an on-line binomial model
					{
						boolean direction = node.connected(neighbor);
						bits += - log2(p(direction, directions));
						directions.add(direction);
					}
					
					delimiter.add(false);
				}
			}
			
			// * encode the size
			bits += prefix(size);
			pBits += prefix(size); 
			
			// * Instead of the size
			// bits += -log2(p(true, delimiter));
			// delimiter.add(true);
		}
			
		
		Global.log().info(pBits + " bits out of " + bits + " spent of encoding sizes (" + (pBits/bits)*100 + " percent). ");
		return bits;
	}
	
	private static <N> double p(N symbol, BasicFrequencyModel<N> model)
	{
		double freq = model.frequency(symbol),
		       total = model.total(),
		       distinct = model.distinct();
			
		return (freq + 0.5) / (total + 0.5 * distinct);
	}

}
