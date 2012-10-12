package org.lilian.graphs;

import java.util.Set;

/**
 * 
 * @author Peter
 *
 * @param <L>
 * @param <T>
 */
public interface DTNode<L, T>
{
	public Set<DTNode<L, T>> neighbors();
	
	public Node<DTNode<L, T>> neighbour(L label);

	public Set<DTNode<L, T>> neighbours(L label);
	
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
	public void connect(DTNode<L, T> other);
	
	public void disconnect(DTNode<L, T> other);
	
	public boolean connected(DTNode<L, T> other);
	
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
