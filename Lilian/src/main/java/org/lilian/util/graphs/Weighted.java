package org.lilian.util.graphs;

public interface Weighted<L, N extends Weighted.Node<L, N>> extends Labeled<L, N, Double>
{
	public interface Node<L, N extends Weighted.Node<L, N>> 
			extends org.lilian.util.graphs.Node<L, N>
	{
	}
}
