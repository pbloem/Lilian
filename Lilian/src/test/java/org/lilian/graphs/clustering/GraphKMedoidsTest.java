package org.lilian.graphs.clustering;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Graphs;

public class GraphKMedoidsTest
{

	@Test
	public void testCluster()
	{
		Graph<String> graph = Graphs.jbc();
		
		Clusterer<String> clusterer = new GraphKMedoids<String>(3);
		System.out.println(clusterer.cluster(graph));
	}

}
