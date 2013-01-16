package org.lilian.graphs;

import java.util.Collection;

/**
 * Represents a link in a graph. 
 * 
 * @author Peter
 *
 * @param <L>
 */
public interface Link<L>
{

	public Collection<? extends Node<L>> nodes();
	
	public Graph<L> graph();
	
	/**
	 * Removes this link from the network.
	 * 
	 * If this method is called during an iteration through the links of this 
	 * network (including walks), it may lead to a ConcurrentModificationException 
	 */
	public void remove();
	
	public boolean dead();
}
