package org.lilian.util.graphs;

public interface Tree<L, N extends Tree.Node<L, N>> extends Directed<L, N> 
{
	public interface Node<L, N extends Tree.Node<L, N>> extends Directed.Node<L, N>
	{
	}
}

