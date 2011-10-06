package org.lilian.util.graphs;

public interface Stochastic<L, N extends Stochastic.Node<L, N>> extends Weighted<L, N> 
{

	
	public interface Node<L, N extends Stochastic.Node<L, N>> 
			extends Weighted.Node<L, N> {
	}
}
