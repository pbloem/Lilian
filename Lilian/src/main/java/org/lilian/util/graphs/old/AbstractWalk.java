package org.lilian.util.graphs.old;

import java.util.Iterator;

public abstract class AbstractWalk<L, N extends Node<L, N>> implements Walk<L, N>
{
	@Override
	public Iterable<L> labels()
	{
		return new Labels();
	}
	
	private class Labels implements Iterable<L>
	{
		@Override
		public java.util.Iterator<L> iterator()
		{
			return new Iterator();
		}
		
		
		private class Iterator implements java.util.Iterator<L>
		{
			java.util.Iterator<N> master = AbstractWalk.this.iterator();

			@Override
			public boolean hasNext()
			{
				return master.hasNext();
			}

			@Override
			public L next()
			{
				return master.next().label();
			}

			@Override
			public void remove()
			{
				master.remove();
			}

		}

	}

	
}
