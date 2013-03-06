package org.lilian.graphs.random;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.lilian.Global;
import org.lilian.graphs.MapUTGraph;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.UTLink;
import org.lilian.graphs.UTNode;
import org.lilian.util.Series;

public class FractalGenerator
{
	public static final String LABEL = "x";
	
	// * Size of offspring per node
	private int offspring;
	// * number of links between offspring
	private int linksBetweenOffspring;
	// * Probability that hubs stay connected
	private double hubConnectionProb;
		
	private UTGraph<String, String> graph;
	
	/**
	 * Creates a fractal network generator
	 * 
	 * @param offspring
	 * @param linksBetweenOffspring
	 * @param hubConnectionProb The probability that hubs stay connected. If 
	 *    this value is 1, the network is pure small-world (mode 1) if it is zero,
	 *    the network is pure fractal (mode 2). 
	 */
	public FractalGenerator(int offspring, int linksBetweenOffspring,
			double hubConnectionProb)
	{
		this.offspring = offspring;
		this.linksBetweenOffspring = linksBetweenOffspring;
		this.hubConnectionProb = hubConnectionProb;
		
		graph = new MapUTGraph<String, String>();
		graph.add(LABEL).connect(graph.add(LABEL));
	}

	public void iterate()
	{
		
		// * Copy the links to avoid concurrent modification
		List<UTLink<String, String>> links = new ArrayList<UTLink<String,String>>(graph.links());
		
		for(UTLink<String, String> link : links)
		{
			List<UTNode<String, String>> nodesA = new ArrayList<UTNode<String,String>>(offspring);
			List<UTNode<String, String>> nodesB = new ArrayList<UTNode<String,String>>(offspring);
			
			for(int i : series(offspring))
			{
				nodesA.add(graph.add(LABEL));
				nodesB.add(graph.add(LABEL));
				
				nodesA.get(i).connect(link.first());
				nodesB.get(i).connect(link.second());
			}
			
			int last = 0;
			for(int i : series(linksBetweenOffspring))
			{
				nodesA.get(i).connect(nodesB.get(i));
				last = i;
			}
			
			if(Global.random.nextDouble() < hubConnectionProb)
			{
				link.remove();
				nodesA.get(last + 1).connect(nodesB.get(last + 1));				
			}
		}
	}

	public UTGraph<String, String> graph()
	{
		return graph; 	
	}
}

/* Python Code

def fractal_model(generation,m,x,e):
	"""
	Returns the fractal model introduced by 
	Song, Havlin, Makse in Nature Physics 2, 275.
	generation = number of generations
	m = number of offspring per node
	x = number of connections between offsprings
	e = probability that hubs stay connected
	1-e = probability that x offsprings connect.
	If e=1 we are in MODE 1 (pure small-world).
	If e=0 we are in MODE 2 (pure fractal).
	"""
	G=Graph()
	G.add_edge(0,1) #This is the seed for the network (generation 0)
	node_index = 2
	for n in range(1,generation+1):
		all_links = G.edges()
		while all_links:
			link = all_links.pop()
			
			new_nodes_a = range(node_index,node_index + m)
			node_index += m
			new_nodes_b = range(node_index,node_index + m)
			node_index += m
			
			G.add_edges_from([(link[0],node) for node in new_nodes_a])
			G.add_edges_from([(link[1],node) for node in new_nodes_b])
			
			repulsive_links = zip(new_nodes_a,new_nodes_b)
			G.add_edges_from([repulsive_links.pop() for i in range(x-1)])
			
			if random.random() > e:
				G.remove_edge(link[0],link[1])
				rl = repulsive_links.pop()
				G.add_edge(rl[0],rl[1])
	return G
*/
