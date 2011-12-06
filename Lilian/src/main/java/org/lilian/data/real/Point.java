package org.lilian.data.real;

/**
 * A single point in a euclidean space (ie. a vector of double values).
 * 
 */

import java.io.Serializable;
import java.util.*;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.lilian.util.Metrizable;

public class Point 
	extends AbstractList<Double>
	implements Metrizable<Point>, Serializable
{
	private static final long serialVersionUID = 3199154235479870697L;
	
	private double[] values; 

	/**
	 * Creates a Point of the given dimensionality, with all zero values. 
	 * 
	 * @param dimensionality
	 */
	public Point(int dimensionality)
	{
		this.values = new double[dimensionality];
	}
	
	/**
	 * Creates a point directly from values specified in the parameters.
	 * 
	 * The point will be not be backed by the input array. 
	 * 
	 * @throws IllegalArgumentException If zero arguments are specified 
	 */
	public Point(double... values) 
	{
		this.values = Arrays.copyOf(values, values.length);
	}
	
	/**
	 * @param values
	 */
	public Point(RealVector values)
	{
		this.values = values.toArray();
	}

	public int dimensionality()
	{
		return values.length;
	}
	
	public void set(int index, double value)
	{
		values[index] = value;
	}
	
	@Override
	public int size()
	{
		return values.length;
	}

	@Override
	public Double get(int index)
	{
		return values[index];
	}

	@Override
	public double distance(Point other)
	{
		return distance(this, other);
	}
	
	/**
	 * Returns this Point represented as a one dimensional matrix.
	 */
	public RealVector getVector()
	{
		return new ArrayRealVector(values);
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
			
			sb.append(get(i));
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
		Point mi, ma;
		if(a.values.length < b.values.length)
		{
			mi = a;
			ma = b;
		} else
		{
			mi = b;
			ma = a;
		}
					
		double distSq = 0.0, d;
		
		int i;
		for(i = 0; i < mi.size(); i++)
		{
			d = mi.values[i] - ma.values[i];
			distSq += d * d;
		}
		
		for(; i < ma.size(); i++)
		{
			d = ma.values[i];
			distSq += d * d;
		}
		
		return Math.sqrt(distSq);
	}
	
}
