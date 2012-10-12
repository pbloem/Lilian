package org.lilian.graphs;

import java.util.Set;

public interface DNode<L>
{
	public Set<DNode<L>> neighbors();
	
	public DNode<L> neighbour(L label);

	public Set<DNode<L>> neighbours(L label);
	
	public Set<DNode<L>> to();
	
	public Set<DNode<L>> to(L label);	
	
	public Set<DNode<L>> from();
	
	public Set<DNode<L>> from(L label);		
	
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
	public void connect(DNode<L> to);
	
	public void disconnect(DNode<L> other);
	
	public boolean connected(DNode<L> to);
	
	/**
	 * Returns the graph object to which these nodes belong. Nodes always belong 
	 * to a single graph and cannot be exchanged between them.
	 * @return
	 */
	public DGraph<L> graph();
	
	/**
	 * The index of the node in the graph to which it belongs
	 * @return
	 */
	public int index();
}