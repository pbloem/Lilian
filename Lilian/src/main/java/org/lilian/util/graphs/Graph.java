package org.lilian.util.graphs;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * The parent interface for all types of graphs: simple, directed, weighted, 
 * multigraphs, hypergraphs, trees etc.
 * 
 * For the sake of clarity, the slightly obscure mathematical terminology for 
 * graphs is avoided. Instead of vertices we talk about nodes, instead of edges
 * we talk about links.
 * 
 * The properties of graphs captured and dictated by this interface are as follows
 * <ul>
 *  <li>A graph is a list of Node objects</li>
 *  <li>A link connects several nodes (ie. hypergraphs are included in this interface)</li>
 *  <li>Nodes are always labeled. The generic type of the graph is the generic 
 *      type of the node</li>
 * </ul>
 * 
 * The following properties can be specified by implementations, abstract 
 * classes, subinterfaces or marker interfaces:
 * 
 * <ul>
 * 	 <li> Labels: Labeling links is done by attaching objects to node 
 *        and link objects. This is done by implementing the Labeled interface </li>
 *   <li> Weighting: This is a special case of a labeled link, a link labeled 
 *        with a double value, as described by the Weighted interface </li>
 *   <li> Directedness</li>
 *   <li> Completeness: a completely connected graph can implement the 
 *        Complete interface to signal that it is guaranteed to be fully 
 *        connected. Any node added will automatically be connected to all
 *        others. </li>   
 * </ul>
 * 
 * @author peter
 *
 * @param <N>
 * @param <L>
 */
public interface Graph<L, N extends Node<L, N>> extends Collection<N>
{

	/**
	 * Returns the first node in the Graph which has the given label 
	 * 
	 * @param label
	 * @return
	 */
	public N node(L label);
	
	public Set<N> nodes(L label);
	
	/**
	 * Adds a new node with the given label 
	 */
	public N addNode(L label);
	
	public int numEdges();
	
	/**
	 * Returns the nodelabels
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
	
}
