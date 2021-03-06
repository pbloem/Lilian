package org.lilian.util.graphs.old;

public interface Tree<L, N extends Tree.Node<L, N>> 
	extends Directed<L, N>, Acyclic
{
	public interface Node<L, N extends Tree.Node<L, N>> extends Directed.Node<L, N>
	{
	}
}

