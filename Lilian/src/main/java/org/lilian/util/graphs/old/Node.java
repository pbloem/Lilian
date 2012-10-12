package org.lilian.util.graphs.old;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
	public Set<N> neighbours();
	
	/**
	 * Returns the first neighbour of this node with the given label 
	 * 
	 * @param label
	 * @return
	 */
	public N neighbour(L label);

	public Set<N> neighbours(L label);	
	
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
	
	public void disconnect(N other);
	
	public boolean connected(N other);
	
	/**
	 * Returns the graph object to which these nodes belong. Nodes always belong 
	 * to a single graph and cannot be exchanged between them.
	 * @return
	 */
	public Graph<L, N> graph();
	
	/**
	 * The id of a node is a long that uniquely identifies it within its graph.
	 * It is distinct from its label in that multiple nodes can have the same 
	 * label.
	 * 
	 * @return
	 */
	public int id();
	
}
