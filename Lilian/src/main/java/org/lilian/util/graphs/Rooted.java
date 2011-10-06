package org.lilian.util.graphs;

/**
 * A graph which is rooted has a specific single node which is identified as a 
 * kind of starting point. 
 *  
 * @author peter
 *
 * @param <L>
 * @param <N>
 */
public interface Rooted<L, N extends Node<L, N>> extends Graph<L, N>
{
	public N root();
}
