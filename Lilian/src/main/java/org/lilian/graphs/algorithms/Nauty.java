package org.lilian.graphs.algorithms;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lilian.graphs.DGraph;
import org.lilian.graphs.DNode;
import org.lilian.graphs.DTGraph;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Link;
import org.lilian.graphs.Node;
import org.lilian.graphs.TGraph;
import org.lilian.graphs.TLink;
import org.lilian.graphs.TNode;
import org.lilian.graphs.UGraph;
import org.lilian.graphs.UNode;
import org.lilian.graphs.UTGraph;
import org.lilian.util.Functions;
import org.lilian.util.Order;
import org.lilian.util.Series;

/**
 * This is a general implementation of the nauty algorithm, originally by
 * McKay.
 * 
 * TODO: Implement automorphism pruning
 * 
 * @author Peter
 *
 */
public class Nauty
{
	
	public static <L> Order order(Graph<L> graph)
	{
		if(graph instanceof DGraph<?>)
			return order((DGraph<L>)graph);
		
		if(graph instanceof UGraph<?>)
			return order((UGraph<L>)graph);

		throw new RuntimeException("Type of graph ("+graph.getClass()+") not recognized");		
	}
	
	public static <L> Order order(UGraph<L> graph)
	{
		return order(graph, false, false);
	}
	
	public static <L> Order order(DGraph<L> graph)
	{
		return order(graph, true, false);
	}
	
	public static <L, T> Order order(UTGraph<L, T> graph)
	{
		return order(graph, false, true);
	}
	
	public static <L, T> Order order(DTGraph<L, T> graph)
	{
		return order(graph, true, true);
	}
	
	@SuppressWarnings("unchecked")
	private static <L> Order order(Graph<L> graph, boolean directed, boolean tagged)
	{
		// * Start with the unit partition
		// SPEEDUP: partition by label first
		List<List<Node<L>>> partition = new ArrayList<List<Node<L>>>(graph.size());
		partition.add(new ArrayList<Node<L>>(graph.nodes()));
		
		List<Object> tags = null;
		if(tagged)
			tags = new ArrayList<Object>( ((TGraph<Object, Object>)graph).tags() );

		// * The equitable refinement procedure.
		partition = tagged ? refine(partition, tags) : refine(partition);
	
		// * Start the search for the maximal isomorph
		Search<L> search = new Search<L>(partition, directed);
		search.search();
		
		List<Integer> order = new ArrayList<Integer>(graph.size());
		List<List<Node<L>>> max = search.max();
		
		for(List<Node<L>> cell : max)
		{
			assert(cell.size() == 1);
			
			Node<L> node = cell.get(0);
			order.add(node.index());
		}
		
		assert(order.size() == graph.size());
		
		return new Order(order).inverse();
	}
	
	/**
	 * This object encapsulates the information in a single search.
	 * 
	 * @author Peter
	 *
	 */
	private static class Search<T>
	{
		private Deque<SNode<T>> buffer = new LinkedList<SNode<T>>();
		private boolean directed;
		
		private SNode<T> max = null;
		private String maxString;

		public Search(List<List<Node<T>>> initial, boolean directed)
		{
			// ** Set up the search stack
			buffer.add(new SNode<T>(initial));
			this.directed = directed;

		}
		
		public void search()
		{
			while(! buffer.isEmpty())
			{
				SNode<T> current = buffer.poll();
				
				List<SNode<T>> children = current.children();
				if(children.isEmpty())
					observe(current);
				
				for(SNode<T> child : children)
					buffer.addFirst(child);
			}
		}
		
		private void observe(SNode<T> node)
		{
			String nodeString = Nauty.toString(node.partition(), directed);
			
			if(max == null || nodeString.compareTo(maxString) > 0)
			{
				max = node;
				maxString = nodeString;
			}
		}
		
		public List<List<Node<T>>> max()
		{
			return max.partition();
		}
	}

	/**
	 * Refine the given partition to a coarsest equitable refinement.
	 *  
	 * @param partition
	 * @param graph
	 * @return
	 */
	public static <T> List<List<Node<T>>> refine(List<List<Node<T>>> partition)
	{
		List<List<Node<T>>> result = new ArrayList<List<Node<T>>>();
		for(List<Node<T>> cell : partition)
			result.add(new ArrayList<Node<T>>(cell));

		while(searchShattering(result));
		
		return result;
	}
	
	public static <L, T> List<List<Node<L>>> refine(List<List<Node<L>>> partition, List<T> tags)
	{		
		List<List<Node<L>>> result = new ArrayList<List<Node<L>>>();
		for(List<Node<L>> cell : partition)
			result.add(new ArrayList<Node<L>>(cell));

		while(searchShattering(result, tags));
		
		return result;
	}
	
	private static <T> boolean searchShattering(List<List<Node<T>>> partition)
	{		
		// * Loop through every pair of partition cells
		for(int i : series(partition.size()))
		{
			for(int j : series(partition.size()))
			{	
				if(shatters(partition.get(i), partition.get(j)))
				{
					List<List<Node<T>>> shattering = shattering(partition.get(i), partition.get(j));
					
					// * This edit to the list we're looping over is safe, 
					//   because we return right after
					partition.remove(i);

					partition.addAll(i, shattering);
										
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static <L, T> boolean searchShattering(List<List<Node<L>>> partition, List<T> tags)
	{
		// * Loop through every pair of partition cells
		for(int i : series(partition.size()))
		{
			for(int j : series(partition.size()))
			{	
				T shatteringTag = shatters(partition.get(i), partition.get(j), tags);
				
				if(shatteringTag != null)
				{
					List<List<Node<L>>> shattering = 
							shattering(partition.get(i), partition.get(j), shatteringTag);

					// * This edit to the list we're looping over is safe, 
					//   because we return right after
					partition.remove(i);
					partition.addAll(i, shattering);
										
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Re-orders the nodes in 'from' by their degree relative to 'to'
	 * @param from
	 * @param to
	 * @return
	 */
	public static <L> List<List<Node<L>>> shattering(List<Node<L>> from, List<Node<L>> to)
	{
		Map<Integer, List<Node<L>>> byDegree = new LinkedHashMap<Integer, List<Node<L>>>();
		
		for(Node<L> node : from)
		{
			int degree = degree(node, to);
			
			if(! byDegree.containsKey(degree))
				byDegree.put(degree, new ArrayList<Node<L>>());
				
			byDegree.get(degree).add(node);
		}
		
		List<Integer> keys = new ArrayList<Integer>(byDegree.keySet());
		Collections.sort(keys);
		
		List<List<Node<L>>> result = new ArrayList<List<Node<L>>>();
		for(int key : keys)
			result.add(byDegree.get(key));
		
		return result;
	}
	
	/**
	 * Re-orders the nodes in 'from' by their degree relative to 'to' under the given tag
	 * @param from
	 * @param to
	 * @return
	 */
	public static <L, T> List<List<Node<L>>> shattering(List<Node<L>> from, List<Node<L>> to, T tag)
	{
		Map<Integer, List<Node<L>>> byDegree = new LinkedHashMap<Integer, List<Node<L>>>();
		
		for(Node<L> node : from)
		{
			int degree = degree(node, to, tag);
			
			if(! byDegree.containsKey(degree))
				byDegree.put(degree, new ArrayList<Node<L>>());
				
			byDegree.get(degree).add(node);
		}
		
		List<Integer> keys = new ArrayList<Integer>(byDegree.keySet());
		Collections.sort(keys);
		
		List<List<Node<L>>> result = new ArrayList<List<Node<L>>>();
		for(int key : keys)
			result.add(byDegree.get(key));
		
		return result;
	}

	/**
	 * A set of nodes shatters another set of nodes, if the the outdegree 
	 * relative to the second set differs between members of the first.
	 *  
	 * @param from
	 * @param to
	 * @return
	 */
	public static <L> boolean shatters(List<Node<L>> from, List<Node<L>> to)
	{
		int num = -1;
		
		for(Node<L> node : from)
			if(num == -1)
				num = degree(node, to);
			else
				if(num != degree(node, to))
					return true;
			
		return false;
	}
	
	/**
	 * A set of nodes shatters another set of nodes, if the the outdegree 
	 * relative to the second set, given some tag, differs between members of the first.
	 *  
	 * @param from
	 * @param to
	 * @return The first shattering tag if it exists, null otherwise 
	 */
	public static <L, T> T shatters(List<Node<L>> from, List<Node<L>> to, List<T> tags)
	{		
		for(T tag : tags)
		{
			int num = -1;

			for(Node<L> node : from)
				if(num == -1)
					num = degree(node, to, tag);
				else if(num != degree(node, to, tag))
					return tag;
		}
			
		return null;
	}
	
	public static <L> int degree(Node<L> from, List<Node<L>> to)
	{
		int sum = 0;
		
		for(Node<L> node : to) // * this should automatically work right for directed/undirected
			sum += from.links(node).size();
		
		return sum;
	}
	
	@SuppressWarnings("unchecked")
	public static <L, T> int degree(Node<L> from, List<Node<L>> to, T tag)
	{
		int sum = 0;
		
		for(Node<L> node : to) // * this should automatically work right for directed/undirected
			for(Link<L> link : from.links(node))
				if(link instanceof TLink<?, ?>)
					if(Functions.equals(((TLink<Object, Object>)link).tag(), tag))
						sum++;

		return sum;
	}
	
	private static class SNode<T> 
	{
		private List<List<Node<T>>> partition;

		public SNode(List<List<Node<T>>> partition)
		{
			super();
			this.partition = partition;
		}
		
		public List<List<Node<T>>> partition()
		{
			return partition;
		}

		public List<SNode<T>> children()
		{
			List<SNode<T>> children = new ArrayList<SNode<T>>(partition.size() + 1);
			
			for(int cellIndex : series(partition.size()))
			{
				List<Node<T>> cell = partition.get(cellIndex);
				if(cell.size() > 1)
					for(int nodeIndex : series(cell.size()))
					{
						List<Node<T>> rest = new ArrayList<Node<T>>(cell);
						List<Node<T>> single = Arrays.asList(rest.remove(nodeIndex));
						
						// * Careful... We're shallow copying the cells. We must 
						//   make sure never to modify a cell.
						List<List<Node<T>>> newPartition = new ArrayList<List<Node<T>>>(partition);
						
						newPartition.remove(cellIndex);
						newPartition.add(cellIndex, single);
						newPartition.add(cellIndex + 1, rest);
						
						children.add(new SNode<T>(newPartition));
					}
			}
			
			return children;
		}
	}
	
	/**
	 * Converts a trivial partition to a string representing the graph's 
	 * structure (without labels) in a particular format.
	 *  
	 * @param partition
	 * @return
	 */
	private static <T> String toString(List<List<Node<T>>> partition, boolean directed)
	{		
		StringBuffer buffer = new StringBuffer();
				
		int[] order = new int[partition.size()];
		int i = 0;
		for(List<Node<T>> cell : partition)
		{
			order[cell.get(0).index()] = i;
			i++;
		}
		
		int myIndex = 0; 
		for(List<Node<T>> cell : partition)
		{
			assert(cell.size() == 1);
			Node<T> current = cell.get(0);
			
			List<Integer> neighbors = new ArrayList<Integer>(current.neighbors().size());
			for(Node<T> neighbor : current.neighbors())
			{
				int rawIndex = neighbor.index(); // index in the original graph
				int neighborIndex = order[rawIndex]; // index in the re-ordered graph
				
				if( (! directed) || current.connected(neighbor))
					neighbors.add(neighborIndex);
			}
			
			Collections.sort(neighbors);
			for(int neighborIndex : neighbors)
				buffer.append(neighborIndex).append(' ');
			
			buffer.append(',');
			
			myIndex++;
		}
		
		return buffer.toString();
	}
}
