package org.lilian.data.real;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.Global;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;

/**
 * Represents a 2D ellipse in a high dimensional space.  
 * 
 * @author peter
 */
public class Ellipse implements Parametrizable
{
	private RealMatrix transform;
	private RealVector translate;
	
	public Ellipse(RealMatrix transform, RealVector translate)
	{
		if(transform.getColumnDimension() != 2)
			throw new IllegalArgumentException("Column dimension of transformation matrix must be 2 (was "+transform.getColumnDimension()+"");
		
		this.transform = transform;
		this.translate = translate;
	}

	/**
	 * Turns points on the 2D unit circle (as represented by an angle) in to a
	 * point on the nD ellipse.
	 * 
	 * @param angle
	 * @return
	 */
	public Point point(double angle)
	{
		double x = Math.sin(angle);
		double y = Math.cos(angle);
		
		RealVector in = new ArrayRealVector(2);
		in.setEntry(0, x);
		in.setEntry(1, y);
		
		RealVector out = transform.operate(in);
		out = out.add(translate);
		
		return new Point(out);
	}
	
	public int dimension()
	{
		return transform.getColumnDimension();
	}
	
	/**
	 * Constructs a random ellipse. 
	 * 
	 * @param dimension
	 * @param var
	 * @return
	 */
	public static Ellipse random(int dimension, double var)
	{
		Builder<Ellipse> builder = builder(dimension);
		
		List<Double> params = new ArrayList<Double>(builder.numParameters());
		for(int i = 0 ; i < builder.numParameters(); i++)
			params.add(Global.random.nextGaussian() *  var);
		
		return builder.build(params);
	}

	@Override
	public List<Double> parameters()
	{
		int size = transform.getRowDimension() * transform.getColumnDimension() 
				+ translate.getDimension();
		
		List<Double> parameters = new ArrayList<Double>(size);
		for(int i = 0; i < transform.getRowDimension(); i ++)
			for(int j = 0; j < transform.getColumnDimension(); j ++)
				parameters.add(transform.getEntry(i, j));
		
		for(int i = 0; i < translate.getDimension(); i ++)
			parameters.add(translate.getEntry(i));
			
		return parameters;
	}
	
	public static Builder<Ellipse> builder(int dimension)
	{
		return new EBuilder(dimension);
	}
	
	private static class EBuilder implements Builder<Ellipse>
	{
		private int dimension;
		
		public EBuilder(int dimension)
		{
			this.dimension = dimension;
		}

		@Override
		public Ellipse build(List<Double> parameters)
		{
			RealMatrix transform = new Array2DRowRealMatrix(dimension, 2);
			RealVector translate = new ArrayRealVector(dimension);
			
			int p = 0;
			for(int i = 0; i < transform.getRowDimension(); i ++)
				for(int j = 0; j < transform.getColumnDimension(); j ++)
				{
					transform.setEntry(i, j, parameters.get(p));
					p++;
				}
			
			for(int i = 0; i < translate.getDimension(); i ++)
			{
				translate.setEntry(i, parameters.get(p));
				p++;
			}
			
			return new Ellipse(transform, translate);
		}

		@Override
		public int numParameters()
		{
			return 2 * dimension + dimension;
		}
			
	}
}
