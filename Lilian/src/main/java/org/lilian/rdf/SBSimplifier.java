package org.lilian.rdf;

import static java.lang.String.format;
import static org.nodes.util.Series.series;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import org.lilian.Global;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.DegreeComparator;
import org.nodes.MapDTGraph;
import org.nodes.Node;
import org.nodes.clustering.ConnectionClusterer.ConnectionClustering;
import org.nodes.data.GML;
import org.nodes.data.RDF;
import org.nodes.util.MaxObserver;

/**
 * Uses the Slashburn principle
 * 
 * @author Peter
 * 
 */
public class SBSimplifier<L, T>
{
	private DTGraph<L, T> graph;
	private ConnectionClustering<L> cc;
	
	/**
	 * Creats an SBSimplifier for a given graph. The graph is copied, and the 
	 * resulting Simplifier is not backed by the graph.
	 * @param graph
	 */
	public SBSimplifier(DTGraph<L, T> graph)
	{
		this.graph = MapDTGraph.copy(graph);
		ConnectionClustering<L> cc = new ConnectionClustering<L>(graph);

	}
	
	public void iterate()
	{
		Comparator<Node<L>> comp = new DegreeComparator<L>();
		MaxObserver<Node<L>> observer = new MaxObserver<Node<L>>(1, comp);

		for (int index : cc.largestCluster())
			observer.observe(graph.get(index));

		@SuppressWarnings("unchecked")
		DTNode<L, T> top = (DTNode<L, T>) observer
				.elements().get(0);

		Global.log().info("Current top hub: " + top + " (d: " + top.degree() + ")");

		for (DTLink<L, T> link : top.links())
		{
			// * ignore self-links
			if (link.from().equals(link.to()))
				continue;

			DTNode<L, T> neighbor = link.other(top);
			DTNode<L, T> newNode = graph.add(top.label());

			if (link.from().equals(top))
				newNode.connect(neighbor, link.tag());
			else
				neighbor.connect(newNode, link.tag());

			link.remove();
		}

		top.remove();
	}
	
	/**
	 * Returns the current version of the graph. Note that this SBSimplifier is
	 * backed by this object.
	 * 
	 * @return
	 */
	public DTGraph<L, T> graph()
	{
		return graph;
	}
	
	/**
	 * Returns the clustering into weakly connected components for the current 
	 * version of the graph.
	 *  
	 * @return
	 */
	public ConnectionClustering<L> clustering()
	{
		return cc;
	}
}
