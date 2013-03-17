package org.lilian.graphs;

import java.util.Collection;
import java.util.Set;

/**
 * 
 * @author Peter
 *
 * @param <L>
 * @param <T>
 */
public interface TNode<L, T> extends Node<L>
{
	public Collection<? extends TNode<L, T>> neighbors();
	
	public TNode<L, T> neighbor(L label);

	public Collection<? extends TNode<L, T>> neighbors(L label);
	
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
	
	public void connect(TNode<L, T> other, T tag);
	
	/**
	 * Check whether this node is connected to another, with a given tag on the 
	 * connecting link.
	 * 
	 * @param other
	 * @param tag
	 * @return
	 */
	public boolean connected(TNode<L, T> other, T tag);
	
	
	/**
	 * Returns a link connecting this node to the given node.
	 *  
	 * No guarantees are made on the ordering of links. 
	 *  
	 * @param other
	 * @return A TLink object returning a link connection this node to another 
	 * node, null if the nodes are not connected.
	 */
	public TLink<L, T> link(TNode<L, T> other);
	
	public Collection<? extends TLink<L, T>> links(TNode<L, T> other);
	
	/**
	 * Returns the graph object to which these nodes belong. Nodes always belong 
	 * to a single graph and cannot be exchanged between them.
	 * @return
	 */
	public TGraph<L, T> graph();
	
	/**
	 * The index of the node in the graph to which it belongs
	 * @return
	 */
	public int index();
}
