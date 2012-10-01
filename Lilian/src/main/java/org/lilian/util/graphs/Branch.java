package org.lilian.util.graphs;

/**
 * A branch is a type of link which singles out a particular node as the 'to' 
 * node. This is useful for instance when a client requests the undirected links
 * from a given node, to traverse the graph. The new node can provide branches 
 * with the new node returned as the 'to' object even if the links are strictly
 * undirected. In other words, the same link might be represented by two 
 * different branch objects depending on context.
 * 
 * The equals and hashcode 
 * functions of a branch object should ignore the to element so as not to 
 * break inheritance relations with the Link interface.
 * 
 * @author Peter
 *
 */
public interface Branch<L, W, N extends Node<L, N>>
	extends Link<L, W, N>
{
	public N to();
}
