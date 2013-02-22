package org.lilian.graphs.random;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.lilian.Global;
import org.lilian.data.real.Generator;
import org.lilian.graphs.Graph;
import org.lilian.graphs.MapUTGraph;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.UTNode;


/**
 * 
 * TODO: This required an UndirectedGraph implementation.
 * @author Peter
 *
 */
public class RandomGraphs
{
	public static final int BA_INITIAL = 3; 
	
	public static UTGraph<String, String> preferentialAttachment(int nodes, int toAttach)
	{
		BAGenerator bag = new BAGenerator(BA_INITIAL, toAttach);
		bag.iterate(nodes - BA_INITIAL);
		
		return bag.graph();
	}
	
	public static UTGraph<String, String> random(int n, double prob)
	{
		MapUTGraph<String, String> graph = new MapUTGraph<String, String>();
		List<UTNode<String, String>> nodes = new ArrayList<UTNode<String, String>>(n);

		for(int i : series(n))
			nodes.add(graph.add("x"));
		
		for(int i : series(n))
			for(int j : series(i+1, n))
				if(Global.random.nextDouble() < prob)
					nodes.get(i).connect(nodes.get(j));
		
		return graph;
	}
}
