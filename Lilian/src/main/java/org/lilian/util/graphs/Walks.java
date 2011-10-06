package org.lilian.util.graphs;

import static org.lilian.util.Functions.reverse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lilian.util.Functions;

/**
 * <p>
 * This class provides several factories for Walks, Iterables over graphs.
 * The point of this class is to make it simple to treaverse graphs.
 * 	</p><p>
 * For example:
 * <code>
 * 		// * Follow a given track
		List<String> track = Arrays.asList("a", "b", "c", "d");
		for( : Walks.track(graph, track))
			...
		
		// * Look for a label depth first 
		String target = "z";
		for( String label : Walk.depthFirst(graph, graph.get(0)) )
			if(label.equals(target))
				... 
 * </code>
 *
 * @author peter
 */
public class Walks
{
	/**
	 * Returns a breadth first walk that visits each node in the graph exactly 
	 * once.
	 * 
	 * The walk maintains a history of nodes it has already returned, unless the
	 * graph is guaranteed acyclic (because it implements {@link Acyclic}. 
	 * 
	 * @param <L> The type of node labels in the graph
	 * @param <N> The node-type of the graph
	 * @param graph The grap
	 * @param root The node to start the walk.
	 * @return 
	 */
	public static <L, N extends Node<L, N>> Walk<L, N> breadthFirst(Graph<L, N> graph, N root)
	{
		return new BFWalk<L, N>(graph, root);
	}
	
	private static class BFWalk<L, N extends Node<L, N>> extends AbstractWalk<L, N>
	{
		protected Graph<L, N> graph;
		protected N start;		
		
		public BFWalk(Graph<L, N> graph, N start)
		{
			this.graph = graph;
			this.start = start;
		}

		public java.util.Iterator<N> iterator()
		{
			return new Iterator(); 
		}
		
		private class Iterator implements java.util.Iterator<N>
		{
			private LinkedList<N> buffer;
			private int next = 0; // The next node in the buffer to unpack
			
			// * Whether we can assume that the given graph is acyclic
			private boolean acyclic = (graph instanceof Acyclic);  
			// * History of visited nodes
			private Set<N> history = acyclic ? new HashSet<N>() : null;
			
			public Iterator() {
				buffer = new LinkedList<N>();
				buffer.add(start);
			}

			@Override
			public boolean hasNext()
			{
				ensure();
				
				return ! buffer.isEmpty();
				
			}

			@Override
			public N next()
			{
				ensure();

				next --;
				
				if(acyclic) history.add(buffer.peek());
				return buffer.poll();
			}

			@Override
			public void remove()
			{
				// TODO: implement
				throw new UnsupportedOperationException();
			}
			
			private void ensure()
			{
				if(buffer.size() < 5 && next < buffer.size())
					for(N node : buffer.get(next).neighbours())
						if(! history.contains(node))
							buffer.add(node);
				
				next++;
			}
		}
	}

	/**
	 * 
	 * @param <T>
	 * @return
	 */
	public static <L, N extends Node<L, N>> Walk<L, N> depthFirst(Graph<L, N> graph, N root)
	{
		return new DFWalk<L, N>(graph, root);
	}

	private static class DFWalk<L, N extends Node<L, N>> extends AbstractWalk<L, N>
	{
		protected Graph<L, N> graph;
		protected N start;		
		
		public DFWalk(Graph<L, N> graph, N start)
		{
			this.graph = graph;
			this.start = start;
		}

		public java.util.Iterator<N> iterator()
		{
			return new Iterator(); 
		}
		
		private class Iterator implements java.util.Iterator<N>
		{
			private LinkedList<N> buffer;
			private int next = 0; // The next node in the buffer to unpack
			
			// * Whether we can assume that the given graph is acyclic
			private boolean acyclic = (graph instanceof Acyclic);  
			// * History of visited nodes
			private Set<N> history = acyclic ? new HashSet<N>() : null;
			
			public Iterator() {
				buffer = new LinkedList<N>();
				buffer.add(start);
			}

			@Override
			public boolean hasNext()
			{
				ensure();
				
				return ! buffer.isEmpty();
				
			}

			@Override
			public N next()
			{
				ensure();

				next --;
				
				if(acyclic) history.add(buffer.peek());
				return buffer.poll();
			}

			@Override
			public void remove()
			{
				// TODO: implement
				throw new UnsupportedOperationException();
			}

			/**
			 * Ensures that the buffer has enough nodes, if they are available
			 */
			private void ensure()
			{

				if(buffer.size() < 5 && next < buffer.size())
					for(N node : reverse(buffer.get(next).neighbours()) )
						buffer.add(next+1, node);
				
				next++;
			}
			
		}
	}



}
