package org.lilian.graphs.subdue;

import static org.lilian.util.Functions.log2;
import static org.lilian.util.graphs.old.algorithms.GraphMDL.clog2;

import org.lilian.util.Functions;
import org.lilian.util.graphs.old.Graph;

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
	
	/**
	 * A cost function based on describing exactly the transformation of one
	 * graph into another. Due to the limitations of the InexactCost interface
	 * this is an approximation.
	 * 
	 * @return
	 */
	public static <L> InexactCost<L> transformationCost(int numLabels, int numNodes, int numEdges)
	{
		return new TransformationCost<L>(numLabels, numNodes, numEdges);
	}
	
	public static <L> InexactCost<L> transformationCost(Graph<L, ?> graph)
	{
		return new TransformationCost<L>(graph.labels().size(), graph.size(), graph.numEdges());
	}
	
	private static class UniformCost<L> implements InexactCost<L>
	{

		@Override
		public double relabel(L in, L out)
		{
			return 10.0;
		}

		@Override
		public double removeNode(L label)
		{
			return 10.0;
		}

		@Override
		public double addNode(L label)
		{
			return 10.0;
		}

		@Override
		public double removeLink()
		{
			return 10.0;
		}

		@Override
		public double addLink()
		{
			return 10.0;
		}
		
	}
	
	private static class TransformationCost<L> implements InexactCost<L>
	{
		private int numLabels;
		private int numNodes, numEdges;
		
		double base = Math.ceil(log2(5.0)); // * the cost of coding the operation
		
		public TransformationCost(int numLabels, int numNodes, int numEdges)
		{
			this.numLabels = numLabels;
			this.numNodes = numNodes;
			this.numEdges = numEdges;
		}

		@Override
		public double relabel(L in, L out)
		{
			return clog2(numNodes) + clog2(numLabels) + base;
		}

		@Override
		public double removeNode(L label)
		{
			return clog2(numNodes) + base;
		}

		@Override
		public double addNode(L label)
		{
			return clog2(numNodes + 1) - clog2(numNodes) + base;  
		}

		@Override
		public double removeLink()
		{
			return clog2(numEdges) + base;
		}

		@Override
		public double addLink()
		{
			return clog2(numNodes) * 2 + base;
		}
		
	}
}
