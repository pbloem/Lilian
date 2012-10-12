package org.lilian.graphs.other;

import java.util.List;
import java.util.Set;

/**
 * Design choice: Graph gets subclassed and modified, Node never does.
 * @param <L>
 */
public interface Graph<L> extends List<Node<L>>
{
	/**
	 * Returns the first node in the Graph which has the given label 
	 * 
	 * @param label
	 * @return
	 */
	public Node<L> node(L label);
	
	public Set<Node<L>> nodes(L label);
	
	/**
	 * Adds a new node with the given label 
	 */
	public Node<L> addNode(L label);
	
	public int numLinks();
	
	/**
	 * Returns the node labels
	 * @return
	 */
	public Set<L> labels();
	
	/**
	 * Checks whether two nodes exist with the given labels that are connected.
	 * 
	 * If multiple pairs of nodes exist with these labels, only one of them 
	 * needs to be connected for the method to return true.
	 *  
	 * @param first
	 * @param second
	 * @return
	 */
	public boolean connected(L first, L second);

	/**
	 * The state of a graph indicates whether it has changed. If the value 
	 * returned by this method has changed, then a modification has been made. 
	 * If the value is the same, then with great likelihood, the graph has not 
	 * been modified.
	 * 
	 * @return
	 */
	public long state();
}
