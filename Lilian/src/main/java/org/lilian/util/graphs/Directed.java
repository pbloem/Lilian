package org.lilian.util.graphs;

import java.util.List;

public interface Directed<L, N extends Directed.Node<L, N>> extends Graph<L, N>
{

	
	public interface Node<L, N extends Directed.Node<L, N>> 
			extends org.lilian.util.graphs.Node<L, N>
	{	
		public List<N> in();
		public List<N> out();
	}

}
