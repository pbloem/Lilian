package org.lilian.util.graphs;

/**
 * A graph with labeled links
 * 
 * @author peter
 *
 * @param <L>
 * @param <N>
 * @param <W>
 */
public interface Labeled<L, N extends Node<L, N>, W> extends Graph<L, N> 
{

}
