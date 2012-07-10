package org.lilian.util.graphs.jung;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.lilian.Global;
import org.lilian.util.BitString;
import org.lilian.util.Series;

import edu.uci.ics.jung.algorithms.generators.EvolvingGraphGenerator;
import edu.uci.ics.jung.algorithms.generators.GraphGenerator;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * Helper classes to simplify dealing with JUNG Graphs.
 * 
 * @author Peter
 *
 */
public class Graphs
{

	/**
	 * Returns a simple random graph with the given number of nodes and the 
	 * given probability of finding an edge between any two pair of nodes.
	 * 
	 * This is known as an Erods-Renyi random graph.
	 * 
	 * @param numNodes
	 * @param edgesProb
	 * @return
	 */
	public static Graph<Integer, Integer> random(int numNodes, double edgeProb)
	{
		GraphGenerator<Integer, Integer> gen =
				new ErdosRenyiGenerator<Integer, Integer>(
					new UGraphFactory(),
					new IntegerFactory(), 
					new IntegerFactory(), 
					numNodes, edgeProb);
			return gen.create();
	}
	
	/**
	 * Barabasi-Albert random graph. The BA algorithm attaches new nodes by 
	 * preferential attachment, generating a scale free network
	 * @param numNodes
	 * @return
	 */
	public static Graph<Integer, Integer> abRandom(int numNodes, int startNodes, int numEdgesToAttach)
	{
		IntegerFactory nodeFactory = new IntegerFactory();
		
		Set<Integer> initNodes = new HashSet<Integer>();
		for(int i : Series.series(startNodes))
			initNodes.add(nodeFactory.create());
		
		EvolvingGraphGenerator<Integer, Integer> gen = 
				new BarabasiAlbertGenerator<Integer, Integer>(
						new GraphFactory(), 
						nodeFactory,
						new IntegerFactory(), 
						startNodes, numEdgesToAttach,
						Global.random.nextInt(), initNodes);
		
		gen.evolveGraph(numNodes-startNodes);
		return gen.create();
	}
	
	private static class UGraphFactory implements Factory<UndirectedGraph<Integer, Integer>>
	{
		@Override
		public UndirectedGraph<Integer, Integer> create()
		{
			return new UndirectedSparseGraph<Integer, Integer>();
		}	
	}	
	
	private static class GraphFactory implements Factory<Graph<Integer, Integer>>
	{
		@Override
		public Graph<Integer, Integer> create()
		{
			return new UndirectedSparseGraph<Integer, Integer>();
		}	
	}
	
	private static class IntegerFactory implements Factory<Integer>
	{
		private int i = 0;

		@Override
		public Integer create()
		{
			return i++;
		}
		
		public int max()
		{
			return i-1;
		}
	}
	
	/**
	 * Returns the flattened lower half of the adjacency matrix as a bitstring
	 * @return
	 */
	public static <V, E> BitString toBits(Graph<V, E> graph)
	{
		return toBits(graph, new ArrayList<V>(graph.getVertices()));
	}
	
	/**
	 * Returns the flattened lower half of the adjacency matrix as a bitstring
	 * for the given ordering of vertices
	 * @return
	 */
	public static <V, E> BitString toBits(Graph<V, E> graph, List<V> list)
	{
		long n = graph.getVertexCount();
		
		BitString res = new BitString((int)(n*n - n) / 2);
		
		for(int i : Series.series((int)n))
			for(int j : Series.series(i+1, (int)n))
					res.add(graph.isNeighbor(list.get(i), list.get(j)));
			
		return res;
	}	
}
