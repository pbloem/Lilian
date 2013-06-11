package org.lilian.graphs.clustering;

import java.util.List;

import org.lilian.data.real.classification.Classified;
import org.lilian.data.real.clustering.KMedioids;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Node;
import org.lilian.graphs.algorithms.FloydWarshall;
import org.lilian.util.Series;

public class GraphKMedoids<L> implements Clusterer<L>
{
	private static int ITS = 50;
	private int k = 7;


	public GraphKMedoids()
	{
	}

	public GraphKMedoids(int k)
	{
		this.k = k;
	}

	@Override
	public Classified<Node<L>> cluster(Graph<L> graph)
	{
		FloydWarshall<L> fw = new FloydWarshall<L>(graph);
		List<Node<L>> nodes = (List<Node<L>>) graph.nodes();
		
		KMedioids<Node<L>> meds = 
				new KMedioids<Node<L>>(nodes, fw, k);
		
		meds.iterate(ITS);
		return meds.clustered();
	}
}
