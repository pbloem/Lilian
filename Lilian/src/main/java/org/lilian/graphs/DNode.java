package org.lilian.graphs;

import java.util.Collection;
import java.util.Set;

public interface DNode<L> extends Node<L> 
{
	public Collection<? extends DNode<L>> neighbors();
	
	public DNode<L> neighbor(L label);

	public Collection<? extends DNode<L>> neighbors(L label);
	
	public Collection<? extends DNode<L>> to();
	
	public Collection<? extends DNode<L>> to(L label);	
	
	public Collection<? extends DNode<L>> from();
	
	public Collection<? extends DNode<L>> from(L label);		
	
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
	public void connect(Node<L> to);
	
	public void disconnect(Node<L> other);
	
	public boolean connected(Node<L> to);
	
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
	
	public int inDegree();
	
	public int outDegree();
}