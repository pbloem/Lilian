package org.lilian.data.real;

/**
 * A single point in a euclidean space (ie. a vector of double values).
 * 
 */

import java.io.Serializable;
import java.util.*;

import org.lilian.util.Metrizable;
import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.DenseDoubleMatrix2D;
import org.ujmp.core.doublematrix.DoubleMatrix;
import org.ujmp.core.doublematrix.impl.ArrayDenseDoubleMatrix2D;

public class Point 
	extends AbstractList<Double>
	implements Metrizable<Point>, Serializable
{
	private static final long serialVersionUID = 3199154235479870697L;
	
	private Matrix values; 

	/**
	 * Creates a Point of the given dimensionality, with zero values. 
	 * 
	 * @param dimensionality
	 */
	public Point(int dimensionality)
	{
		this.values = DenseDoubleMatrix2D.factory.zeros(dimensionality, 1);
	}
	
	/**
	 * Creates a point directly from values specified in the parameters.
	 * 
	 * The point will be backed by the input array. Unless performance is a 
	 * concern, the use of {@link Point.from(double[])} is recommended
	 * 
	 * @throws IllegalArgumentException If zero arguments are specified 
	 */
	public Point(double... values) 
	{
		this.values = new ArrayDenseDoubleMatrix2D(values);
	}
	
	public Point(Matrix values)
	{
		this.values = values;
		
	}

	public int dimensionality()
	{
		return (int)values.getSize(0);
	}
	
	public void set(int index, double value)
	{
		values.setAsDouble(value, index, 1);
	}
	
	@Override
	public int size()
	{
		return (int)values.getSize(0);
	}

	@Override
	public Double get(int index)
	{
		return values.getAsDouble(index, 0);

	}

	@Override
	public double distance(Point other)
	{
		return distance(this, other);
	}
	
	/**
	 * Returns this Point represented as a one dimensional matrix.
	 * 
	 */
	public Matrix getVector()
	{
		return new ArrayDenseDoubleMatrix2D(values);
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		boolean first = true;
		
		for(int i = 0; i < size(); i++)
		{
			if(first) first = false;
			else sb.append(", ");
			
			sb.append(values.getAsDouble(i, 0));
		}
		sb.append("]");		
		
		return sb.toString();
	}

	/**
	 * Defines the euclidean distance between two points.
	 * 
	 * If the dimensionality of one of the points is smaller than the other,
	 * its values for the remaining dimensions are assumed to be zero.
	 */
	public static double distance(Point a, Point b)
	{
		double distSq = 0.0, d;
		
		for(int i = 0; i < a.size(); i++)
		{
			d = a.values.getAsDouble(i, 0) - b.values.getAsDouble(i, 0);
			distSq += d * d;
		}
		
		return Math.sqrt(distSq);
	}
	
}
