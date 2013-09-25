package org.lilian.util.graphs.jung;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import org.junit.Test;
import org.lilian.util.Series;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.graph.Graph;

public class FloydWarshallTest
{

	@Test
	public void test()
	{
		Graph<Integer, Integer> graph = Graphs.random(25, 0.3);
		
		DijkstraDistance<Integer, Integer> dist = new DijkstraDistance<Integer, Integer>(graph);
		FloydWarshall<Integer, Integer> fw = new FloydWarshall<Integer, Integer>(graph);
		
		
		for(int i : graph.getVertices())
			for(int j : graph.getVertices())
				assertEquals((int)dist.getDistance(i, j).doubleValue(), fw.distance(i, j));
			
				
	}

}
