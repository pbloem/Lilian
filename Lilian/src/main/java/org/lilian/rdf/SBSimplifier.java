package org.lilian.rdf;

import static java.lang.String.format;
import static org.nodes.util.Series.series;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
	private boolean keepHubs;
	private int k, i = 0, gccSize;
	
	/**
	 * Creats an SBSimplifier for a given graph. The graph is copied, and the 
	 * resulting Simplifier is not backed by the graph.
	 * 
	 * @param graph
	 * @param k The number of hubs to remove each iteration (0.005 of the number of nodes is a good rule of thumb)
	 * @param keepHubs If true, the hub node is kept and replicated for every 
	 *   position in the graph it occurs. If false, it is simply deleted. 
	 */
	public SBSimplifier(DTGraph<L, T> graph, int k, boolean keepHubs)
	{
		this.graph = MapDTGraph.copy(graph);
		cc = new ConnectionClustering<L>(graph);
		gccSize =  cc.largestCluster().size();
				
		this.k = k;
		this.keepHubs = keepHubs;
	}
	
	public boolean finished()
	{
		return gccSize < k;
	}
	
	@SuppressWarnings("unchecked")
	public void iterate()
	{	

		Global.log().info(i + ") GCC: " + gccSize + ", components: " + cc.numClusters());

		if(gccSize < k)
			throw new RuntimeException("The largest connected component is smaller than c");
		
		Comparator<Node<L>> comp = new DegreeComparator<L>();
		MaxObserver<Node<L>> observer = new MaxObserver<Node<L>>(k, comp);
		for (int index : cc.largestCluster())
			observer.observe(graph.get(index));

		Global.log().info("Current top hubs: " + observer.elements());

		for(Node<L> hub : observer.elements())
			remove((DTNode<L, T>)hub);
		
		cc = new ConnectionClustering<L>(graph);
		gccSize =  cc.largestCluster().size();
		
		i++;
	}
	
	private void remove(DTNode<L, T> top)
	{
		if(keepHubs)
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
