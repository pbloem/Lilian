package org.lilian.util.graphs.jung;

import java.util.Collection;

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
	public static <V,E> double assortativity(UndirectedGraph<V, E> graph)
	{
		double s1 = 0.0, s2 = 0.0, s3 = 0.0, se = 0.0;
		
		for(E edge : graph.getEdges())
		{
			Collection<V> vs = graph.getIncidentVertices(edge);
			double m = 1.0;
			for(V vert : vs)
				m *= graph.degree(vert);
			se += m;
		}
		se *= 2.0;
		
		for(V vert : graph.getVertices())
		{
			int k = graph.degree(vert); 
			s1 += k;
			s2 += k * k;
			s3 += k * k * k;
		}
		
		return (s1 * se - s2 * s2) / (s1 * s3 - s2 * s2);
	}
}
