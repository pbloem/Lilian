package org.lilian.util.graphs.old;

import java.util.List;

public interface Directed<L, N extends Directed.Node<L, N>> extends Graph<L, N>
{
	
	public interface Node<L, N extends Directed.Node<L, N>> 
			extends org.lilian.util.graphs.old.Node<L, N>
	{	
		public List<N> in();
		public List<N> out();
	}

}
