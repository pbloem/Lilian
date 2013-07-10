package org.lilian.graphs;

import java.util.Collection;

public interface ULink<L> extends Link<L>
{
	@Override
	public UNode<L> first();
	
	@Override
	public UNode<L> second();

	@Override
	public Collection<? extends UNode<L>> nodes();
	
}
