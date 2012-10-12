package org.lilian.graphs.other;

import java.util.Set;

import org.lilian.graphs.DNode;

/**
 * @author Peter
 *
 * @param <L>
 */
public interface Node<L>
{
	/**
	 * Neighbours are those node that can be reached from this node in a single 
	 * step or from which this node can be reached in a single step. (for 
	 * undirected graphs, the distinction is meaningless).
	 * 
	 * @return
	 */
	public Set<Node<L>> neighbours();
	
	public Node<L> neighbour(L label);

	public Set<Node<L>> neighbours(L label);
	
	public Set<DNode<L>> to();
	
	public Set<DNode<L>> to(L label);	
	
	public Set<DNode<L>> from();
	
	public Set<DNode<L>> from(L label);
	
	/** 
	 * <p>Connects this node to another node. </p>
	 * <p>
	 * The only prescription is that if this method succeeds, the other node 
	 * shows up in this nodes' {@link neighbours()}</p>
	 * <p>
	 * The particulars of the connection 
	 * are not prescribed by this interface, nor does this interface prescribe 
	 * what should happen when the connection already exists. </p>
	 * The other node
	 * @param other
	 */
	public void connect(Node<L> other);
	
	public void disconnect(Node<L> other);
	
	public boolean connected(Node<L> to);
	
	public L label();
	
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