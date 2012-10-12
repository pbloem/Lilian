package org.lilian.graphs.other;

import java.util.Set;

import org.lilian.graphs.DNode;

/**
 * 
 * @author Peter
 *
 * @param <L>
 * @param <T>
 */
public interface TNode<L, T>
{
	public Set<TNode<L, T>> neighbors();
	
	public Node<TNode<L, T>> neighbour(L label);

	public Set<TNode<L, T>> neighbours(L label);
	
	
	public Set<TNode<L, T>> to();
	
	public Set<TNode<L, T>> to(L label);	
	
	public Set<TNode<L, T>> from();
	
	public Set<TNode<L, T>> from(L label);
		
	
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
	public void connect(TNode<L, T> other);
	
	public void disconnect(TNode<L, T> other);
	
	public boolean connected(TNode<L, T> other);
	
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
