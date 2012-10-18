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
 * A network consisting of three layers with in and output the same size. The
 * hidden layer has
 *  
 * @author Peter
 *
 * @param <Double>
 */
public class ThreeLayer extends AbstractList<Double> implements Parametrizable, Neural<Double> 
{
	private Activation activation;
	private int n;
	private int h;
	
	private RealVector stateIn, stateHidden, stateOut;
	private RealMatrix weights0, weights1;
	
	private ThreeLayer()
	{
	}

	@Override
	public List<java.lang.Double> parameters()
	{
		List<Double> parameters = new ArrayList<Double>(n*n);
		
		for(int i : series(n))
			for(int j : series(h))
				parameters.add(weights0.getEntry(i, j));
		

		for(int i : series(h))
			for(int j : series(n))
				parameters.add(weights1.getEntry(i, j));
				
		return parameters;
	}
	
	public int inputSize()
	{
		return n;
	}
	
	public int outputSize()
	{
		return n;
	}
	
	public int hiddenSize()
	{
		return h;
	}
	
	public void set(List<Double> in)
	{
		for(int j : series(inputSize()))
			set(j, in.get(j));
	}
	
	public List<Double> out()
	{
		return new Point(stateOut);
	}
	
	public static Builder<ThreeLayer> builder(int n, int h, Activation activation)
	{ 
		return new TLBuilder(n, h, activation);
	}

	private static class TLBuilder implements Builder<ThreeLayer>
	{
		private static final long serialVersionUID = 1L;
		private int n, h;
		private Activation activation;
		
		public TLBuilder(int n, int h, Activation activation)
		{
			this.n = n;
			this.h = h;
			this.activation = activation;
		}

		@Override
		public ThreeLayer build(List<Double> parameters)
		{
			ThreeLayer fnn = new ThreeLayer();
			
			fnn.n = n;
			fnn.h = h;
			fnn.activation = activation;
			
			fnn.stateIn = new ArrayRealVector(n);
			fnn.stateHidden = new ArrayRealVector(n);
			fnn.stateOut = new ArrayRealVector(n);
			
			fnn.weights0 = new Array2DRowRealMatrix(h, n);
			fnn.weights1 = new Array2DRowRealMatrix(n, h);
			
			int c = 0;
			for(int i : series(h))
				for(int j : series(n))
					fnn.weights0.setEntry(i, j, parameters.get(c ++));
			for(int i : series(n))
				for(int j : series(h))
					fnn.weights1.setEntry(i, j, parameters.get(c ++));
			
			return fnn;
		}
	
		@Override
		public int numParameters()
		{
			return n * h * 2;
		}
	
	}

	@Override
	public void step()
	{
		stateHidden = weights0.operate(stateIn);
		for(int i : series(h))
			stateHidden.setEntry(i, activation.function(stateHidden.getEntry(i)));
		stateOut = weights1.operate(stateHidden);
	}

	@Override
	public Double get(int index)
	{
		if(index < n)
			return stateIn.getEntry(index);
		if(index < n + h)
			return stateHidden.getEntry(index - n);
		return stateOut.getEntry(index - n - h);
	}

	@Override
	public int size()
	{
		return n * 2 + h;
	}

	@Override
	public Double set(int index, Double element)
	{
		if(index < n)
			stateIn.setEntry(index, element);
		else if(index < n + h)
			stateHidden.setEntry(index - n, element);
		else 
			stateOut.setEntry(index - n - h, element);
		
		return element;
	}
	
	public static ThreeLayer random(int n, int h, double var, Activation activation)
	{
		Builder<ThreeLayer> builder = builder(n, h, activation);
		List<Double> parameters = Point.random(builder.numParameters(), var);
		
		return builder.build(parameters);
	}
}
