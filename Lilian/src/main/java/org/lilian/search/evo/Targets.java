package org.lilian.search.evo;

import java.util.Comparator;

public class Targets
{
	/**
	 * Returns a 
	 * @param target
	 * @return
	 */
	public static<P> Comparator<P> comparator(Target<P> target)
	{
		return new TargetComparator<P>(target);
	}
	
	private static class TargetComparator<P> implements Comparator<P>
	{
		private Target<P> target;

		public TargetComparator(Target<P> target)
		{
			this.target = target;
		}

		@Override
		public int compare(P first, P second)
		{
			return -Double.compare(target.score(first), target.score(second));
		} 
	}
}
