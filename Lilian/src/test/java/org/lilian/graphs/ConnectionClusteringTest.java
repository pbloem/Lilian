package org.lilian.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.util.Series;

public class ConnectionClusteringTest
{

	@Test
	public void testLargestCluster()
	{
		UTGraph<String, String> graph = Graphs.jbc();
		
		graph.add("lone");
		
		ConnectionClustering<String> c = new ConnectionClustering<String>(graph);
		
		for(int i : Series.series(c.maxClusterIndex()+1))
		{
			System.out.println(Subgraph.subgraphIndices(graph, c.cluster(i)));
		}
		System.out.println(Subgraph.subgraphIndices(graph, c.largestCluster()));

	}

}
