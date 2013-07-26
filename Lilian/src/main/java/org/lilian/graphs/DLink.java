package org.lilian.graphs;

public interface DLink<L> extends Link<L>
{

	public DNode<L> first();
	
	public DNode<L> second();
	
	public DNode<L> from();
	
	public DNode<L> to();
	
}
