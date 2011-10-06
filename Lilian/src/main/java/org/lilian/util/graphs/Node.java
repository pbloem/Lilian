package org.lilian.util.graphs;

import java.util.Collection;
import java.util.List;

/**
 * The basic interface for a graph node.
 * 
 * @author peter
 *
 * @param <L>
 */
public interface Node<L, N extends Node<L, N>>
{
	
	/**
	 * Returns a collection of all nodes that are linked to this node. 
	 * 
	 * @return
	 */
	public List<N> neighbours();
	
	/**
	 * Returns the first neighbour of this node with the given label 
	 * 
	 * @param label
	 * @return
	 */
	public N neighbour(L label);

	public List<N> neighbours(L label);	
	
	public L label();
	
	/** 
	 * <p>Connects this node to another node. </p>
	 * <p>
	 * The only prescription is that if this method succeeds, the other nod 
	 * shows up in this nodes' {@link neighbours()}</p>
	 * <p>
	 * The particulars of the connection 
	 * are not prescribed by this interface, nor does this interface prescribe 
	 * what should happen when the connection already exists. </p>
	 *  
	 * @param other
	 */
	public void connect(N other);
}
