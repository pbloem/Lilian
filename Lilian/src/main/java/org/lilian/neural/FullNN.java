package org.lilian.neural;

import static org.lilian.util.Series.series;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.data.real.Point;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.util.Series;

/**
 * A fully connected recurrent neural network
 * 
 * @author Peter
 *
 * @param <Double>
 */
public class FullNN extends AbstractList<Double> implements Parametrizable, Neural<Double> 
{
	private Activation activation;
	private int n;
	
	private RealVector state;
	private RealMatrix weights;
	
	private FullNN()
	{
	}

	@Override
	public List<java.lang.Double> parameters()
	{
		List<Double> parameters = new ArrayList<Double>(n*n);
		
		for(int i : series(n))
			for(int j : series(n))
				parameters.add(weights.getEntry(i, j));
				
		return parameters;
	}
	
	public static Builder<FullNN> builder(int n, Activation activation)
	{ 
		return new FNNBuilder(n, activation);
	}

	private static class FNNBuilder implements Builder<FullNN>
	{
		private static final long serialVersionUID = 1L;
		private int n;
		private Activation activation;
		
		public FNNBuilder(int n, Activation activation)
		{
			this.n = n;
			this.activation = activation;
		}

		@Override
		public FullNN build(List<Double> parameters)
		{
			FullNN fnn = new FullNN();
			
			fnn.n = n;
			fnn.activation = activation;
			fnn.state = new ArrayRealVector(n);
			fnn.weights = new Array2DRowRealMatrix(n, n);
			
			int c = 0;
			for(int i : series(n))
				for(int j : series(n))
					fnn.weights.setEntry(i, j, parameters.get(c ++));
			
			return fnn;
		}
	
		@Override
		public int numParameters()
		{
			return n*n;
		}
	
	}

	public double weight(int i, int j)
	{
		return weights.getEntry(i, j);
	}

	@Override
	public void step()
	{
		state = weights.operate(state);
		for(int i : series(n))
			state.setEntry(i, activation.function(state.getEntry(i)));
	}

	@Override
	public Double get(int index)
	{
		return state.getEntry(index);
	}

	@Override
	public int size()
	{
		return n;
	}

	@Override
	public Double set(int index, Double element)
	{
		state.setEntry(index, element);
		
		return element;
	}
	
	public static FullNN random(int n, double var, Activation activation)
	{
		List<Double> parameters = Point.random(n * n, var);
		return builder(n, activation).build(parameters);
	}
}
