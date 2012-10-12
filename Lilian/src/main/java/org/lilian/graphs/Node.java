package org.lilian.graphs;

import java.util.Set;

public interface Node<L>
{
	public Set<Node<L>> neighbors();
	
	public Node<L> neighbour(L label);

	public Set<Node<L>> neighbours(L label);
	
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
	public void connect(Node<L> other);
	
	public void disconnect(Node<L> other);
	
	public boolean connected(Node<L> other);
	
	/**
	 * Returns the graph object to which these nodes belong. Nodes always belong 
	 * to a single graph and cannot be exchanged between them.
	 * @return
	 */
	public Graph<L> graph();
	
	/**
	 * The index of the node in the graph to which it belongs
	 * @return
	 */
	public int index();
}