package org.lilian.util.graphs.algorithms;

public class CostFunctions
{

	/**
	 * A uniform cost function (every operation costs 1.0).
	 * @return
	 */
	public static <L> InexactCost<L> uniform()
	{
		return new UniformCost<L>();
	}
	
	private static class UniformCost<L> implements InexactCost<L>
	{

		@Override
		public double relabel(L in, L out)
		{
			return 1.0;
		}

		@Override
		public double removeNode(L label)
		{
			return 1.0;
		}

		@Override
		public double addNode(L label)
		{
			return 1.0;
		}

		@Override
		public double removeLink()
		{
			return 1.0;
		}

		@Override
		public double addLink()
		{
			return 1.0;
		}
		
	}
}
