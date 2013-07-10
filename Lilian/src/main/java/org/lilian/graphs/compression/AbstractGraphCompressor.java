package org.lilian.graphs.compression;

import static org.lilian.util.Series.series;

import java.util.List;

import org.lilian.graphs.Graph;
import org.lilian.graphs.Graphs;
import org.lilian.graphs.LightDGraph;
import org.lilian.util.Compressor;
import org.lilian.util.Series;

public abstract class AbstractGraphCompressor<N> implements Compressor<Graph<N>>
{

	@Override
	public double compressedSize(Object... objects)
	{
		if(objects.length == 1)
			return compressedSize((Graph<N>) objects[0]);
		
		Graph<N> graph = new LightDGraph<N>();
		
		for(Object graphObject : objects)
			Graphs.add(graph, (Graph<N>) graphObject);
		
		return compressedSize(graph);
	}
	
	/**
	 * Computes a self-delimiting encoding of this graph
	 * 
	 * @param graph
	 */
	public double compressedSize(Graph<N> graph)
	{
		return compressedSize(graph, series(graph.size()));
	}

	
	/**
	 * Computes a self-delimiting encoding of this graph
	 * 
	 * @param graph
	 */
	public abstract double compressedSize(Graph<N> graph, List<Integer> order);

	@Override
	public double ratio(Object... object)
	{
		throw new UnsupportedOperationException();
	}

}
