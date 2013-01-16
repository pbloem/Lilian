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
	
	public boolean connected(TNode<L, T> other, T tag);
	
	
	/**
	 * Returns the link connecting this node to the given node
	 *  
	 * @param other
	 * @return
	 */
	public DTLink<L, T> link(TNode<L, T> other);
	
	public Collection<? extends DTLink<L, T>> links(TNode<L, T> other);
	
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
