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
public interface DTNode<L, T> extends DNode<L>, TNode<L, T>
{
	public Collection<? extends DTNode<L, T>> neighbors();
	
	public DTNode<L, T> neighbor(L label);

	public Collection<? extends DTNode<L, T>> neighbors(L label);
	
	public Collection<? extends DTNode<L, T>> toTag(T tag);

	public Collection<? extends DTNode<L, T>> fromTag(T tag);
	
	/** 
	 * <p>Connects this node to another node. </p>
	 * <p>
	 * The only prescription is that if this method succeeds, the other node 
	 * shows up in this nodes' {@link neighbours()}</p>
	 * <p>
	 * The particulars of the connection  are not prescribed by this interface, 
	 * nor does this interface prescribe what should happen when the connection
	 * already exists. </p>
	 *  
	 * @param other
	 */
	public L label();
	
	/**
	 * Returns the graph object to which these nodes belong. Nodes always belong 
	 * to a single graph and cannot be exchanged between them.
	 * @return
	 */
	public DTGraph<L, T> graph();
	
	/**
	 * The index of the node in the graph to which it belongs
	 * @return
	 */
	public int index();
}
