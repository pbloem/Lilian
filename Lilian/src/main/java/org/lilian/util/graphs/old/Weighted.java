package org.lilian.util.graphs.old;

public interface Weighted<L, N extends Weighted.WeightedNode<L, N>> extends Labeled<L, Double, N>
{
	public interface WeightedNode<L, N extends WeightedNode<L, N>> 
			extends Labeled.LabeledNode<L, Double, N>
	{
		public double sum();
	}
}
