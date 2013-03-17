package org.lilian.graphs;

import java.util.Collection;

public interface DTLink<L, T> extends TLink<L, T>, DLink<L>
{
	public Collection<? extends DTNode<L, T>> nodes();

	public DTGraph<L, T> graph();
}
