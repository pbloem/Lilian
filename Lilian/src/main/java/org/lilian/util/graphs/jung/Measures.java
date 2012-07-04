package org.lilian.util.graphs.jung;

import java.util.Collection;
import java.util.Iterator;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;

public class Measures
{

	/**
	 * <p>
	 * A measure for the extent to which the degree of a node indicate the 
	 * degrees of its neighbours. Basically the correlation between the degrees
	 * of both sides of a random edge.
	 * </p><p>
	 * Implementation as described in "Networks" by Mark Newman (p267) 
	 * @param graph
	 * @return
	 */
	public static <V,E> double assortativity(Graph<V, E> graph)
	{
		double mi;
		double a = 0.0, b = 0.0, c = 0.0;
		
		mi = 1.0/graph.getEdgeCount();
		for(E edge : graph.getEdges())
		{
			Iterator<V> it = graph.getIncidentVertices(edge).iterator();
			V vj = it.next();
			V vk = it.next();
			
			int j = graph.degree(vj), k = graph.degree(vk);
			
			a += j * k;
			b += 0.5 * (j + k);
			c += 0.5 * (j*j + k*k);
		}
		
		return (mi*a - (mi*mi * b*b)) / (mi*c - (mi*mi * b*b));
	}
}
