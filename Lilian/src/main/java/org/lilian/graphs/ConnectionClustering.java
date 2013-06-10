package org.lilian.graphs;

import java.util.ArrayList;
import java.util.List;

import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Series;

public class ConnectionClustering<N>
{

	private BasicFrequencyModel<Integer> fm = new BasicFrequencyModel<Integer>();
	private List<Integer> clusters;
	private int maxCluster = -1;
	
	
	public ConnectionClustering(Graph<N> data)
	{	
		clusters = new ArrayList<Integer>();
		for(int i : Series.series(data.size()))
			clusters.add(null);
		
		for(int i : Series.series(data.size()))
			search(data, data.nodes().get(i));
		
	}
	
	private void search(Graph<N> data, Node<N> node)
	{
		List<Node<N>> buffer = new ArrayList<Node<N>>();
		List<Node<N>> children = new ArrayList<Node<N>>();
		
		if(clusters.get(node.index()) != null)
			return;
		
		maxCluster ++;
		int cluster = maxCluster;
		
		buffer.add(node);
		while(! buffer.isEmpty())
		{
			// * Add all children to the children buffer
			for(Node<N> parent : buffer)
			{
				for(Node<N> child : parent.neighbors())
					if(clusters.get(child.index()) == null)
						children.add(child);
			}
			
			// * Set the cluster for every node in the buffer
			for(Node<N> parent : buffer)
			{
				if(clusters.get(parent.index()) == null)
					clusters.set(parent.index(), cluster);
				
				fm.add(cluster);
			}
			
			List<Node<N>> tmp = buffer;
			buffer = children;
			children = tmp;
			children.clear();
		}
	}
	
	public int maxClusterIndex()
	{
		return maxCluster;
	}
	
	public int largestClusterIndex()
	{
		return fm.maxToken();
		
	}
	
	public List<Integer> cluster(int cluster)
	{
		List<Integer> indices = new ArrayList<Integer>((int)fm.frequency(cluster));
		
		for(int i : Series.series(clusters.size()))
			if(clusters.get(i) == cluster)
				indices.add(i);
		
		return indices;	
	}
	
	public List<Integer> largestCluster()
	{
		return cluster(largestClusterIndex());
	}
	
}
