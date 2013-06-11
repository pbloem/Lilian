package org.lilian.graphs.clustering;

import org.lilian.data.real.classification.Classified;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Node;

public interface Clusterer<L>
{
	public Classified<Node<L>> cluster(Graph<L> graph);
}
