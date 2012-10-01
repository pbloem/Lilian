package org.lilian.util.graphs;

public interface DirectedLink<L, W, N extends Node<L, N>> extends Link<L, W, N>
{

	public N from();
	public N to();
}
