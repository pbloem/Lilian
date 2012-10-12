package org.lilian.util.graphs.old;

import java.util.List;

/**
 * The link interface is used for graphs with labeled links.
 * 
 * @author Peter
 *
 * @param <N>
 */
public interface Link<L, W, N extends Node<L, N>>
{
	public W label();
	
	public List<N> nodes();
}
