package org.lilian.util.graphs;

import java.util.Collection;
import java.util.List;

/**
 * A graph with labeled links
 * 
 * @author peter
 *
 * @param <L>
 * @param <N>
 * @param <W> Type for labeling the links
 */
public interface Labeled<L, W, N extends Node<L, N>> extends Graph<L, N> 
{
	
	public interface LabeledNode<L, W, N extends Node<L, N>>
		extends Node<L, N>
	{
		
		/**
		 * Returns the label on the link from this node to the given node,
		 * @param second
		 * @return
		 */
		public W label(N second);
		
		/**
		 * Connects this node to the given node 
		 * 
		 * @param first
		 * @param second
		 */
		public void connect(N other);
		
		/**
		 * A list of the links connecting to this node.
		 * @return
		 */
		public Collection<? extends Link<L, W, N>> links();
		
		/**
		 * A branch view of the links of this node. Each branch singles out the 
		 * node that isn't this node with the 'to' function.
		 * @return
		 */
		public List<? extends Branch<L, W, N>> branches();
		
		
	}
}
