package org.lilian.util.graphs.old;

import java.util.Iterator;

/**
 * A walkwrapper lets you turn any iterable over nodes into a walk. This is
 * helpful if you have, for instance, a list of nodes, which you would like to 
 * treat like a walk.
 * 
 * Example code:
 * <code>
 * List<Node<String, T>> list = graph.nodes("a");  
 * </code>
 * 
 * @author peter
 *
 * @param <L>
 * @param <N>
 */
public class WalkWrapper<L, N extends Node<L, N>> extends AbstractWalk<L, N>
{
	protected Iterable<N> master;
	
	public WalkWrapper(Iterable<N> master)
	{
		this.master = master;
	}

	@Override
	public java.util.Iterator<N> iterator()
	{
		return new Iterator(); 
	}
	
	private class Iterator implements java.util.Iterator<N>
	{
		java.util.Iterator<N> master = WalkWrapper.this.master.iterator();;

		@Override
		public boolean hasNext()
		{
			return master.hasNext();
		}

		@Override
		public N next()
		{
			return master.next();
		}

		@Override
		public void remove()
		{
			master.remove();
		}
		
	}

}
