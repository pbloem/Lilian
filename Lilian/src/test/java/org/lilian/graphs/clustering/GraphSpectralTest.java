package org.lilian.graphs.clustering;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.data.real.clustering.SpectralClustering;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Graphs;
import org.lilian.graphs.UTGraph;
import org.lilian.util.Series;

public class GraphSpectralTest
{

	@Test
	public void testCluster()
	{
		int n = 10;
		
		UTGraph<String, String> c = Graphs.k(n, "x");
		Graphs.add(c, Graphs.k(n, "x"));
		
		for(int i : series(1))
		{
			int j = Global.random.nextInt(2 * n);
			int k = Global.random.nextInt(2 * n);
			
			c.nodes().get(j).connect(c.nodes().get(k));
		}
		
		Clusterer<String> clusterer;
		
		clusterer = new GraphKMedoids<String>(2);
		System.out.println(clusterer.cluster(c).classes());
		
		clusterer = new GraphSpectral<String>(2);
		System.out.println(clusterer.cluster(c).classes());

	}

}
