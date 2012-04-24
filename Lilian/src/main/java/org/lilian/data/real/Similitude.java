package org.lilian.data.real;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.search.evo.Target;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

/**
 * A similitude (or similarity transform) consists of rotation, a uniform 
 * scaling and a translation. 
 * 
 * @author Peter
 *
 */
public class Similitude extends AffineMap
{
	private static final long serialVersionUID = 3717926722178382627L;

	protected int dimension;
	
	protected double scalar;
	protected List<Double> angles;
	
	public Similitude(List<Double> parameters)
	{
		super();
		int s = parameters.size();
		double ddim =  ( -1.0 + Math.sqrt(-7.0 + s * 8.0))/2.0;
		
		dimension = (int)Math.round(ddim);
		
		if(1 + dimension + (dimension*dimension - dimension)/2  != s)
			throw new IllegalArgumentException("Number of parameters ("+s+") should satisfy 1 + d + (d^2 - d)/2 (d="+ddim+", " + dimension + ")");

		init(
				parameters.get(0), 
				parameters.subList(1, dimension+1), 
				parameters.subList(dimension + 1, parameters.size()));		
	}	
	
	public Similitude(double scalar, List<Double> translation, List<Double> angles)
	{
		int dim = translation.size();
		int a = angles.size();
		
		if( (dim*dim - dim)/2 != a)
			throw new IllegalArgumentException("Wrong number of angles ("+a+") for the given dimension (size of translation vector = "+dim+"). The number of angles should be (d*d - d) / 2");
		
		init(scalar, translation, angles);
	}
	
	private void init(double scalar, List<Double> translation, List<Double> angles)
	{
		dimension = translation.size();
		
		this.scalar	= scalar;
		this.translation = MatrixTools.toVector(translation); 		
		this.angles = new ArrayList<Double>(angles);
		
		RealMatrix rotation = Rotation.toRotationMatrix(angles);
		this.transformation = rotation.scalarMultiply(scalar);
	}

	
	/**
	 * Returns a representation of this similitude as a list of double values.
	 * 
	 * @param mode
	 * @return
	 */
	public List<Double> parameters()
	{
		int dim = dimension();
		int size = 1 + dim + (dim * dim - dim)/2;
		List<Double> result = new ArrayList<Double>(size);
		
		result.add(scalar);
		
		for(double d : translation.getData())
			result.add(d);
			
		result.addAll(angles);
			
		return result;
	}	
	
	/**
	 * Scaling ratio of the map, can be negative.
	 * 
	 * @return
	 */
	public double scalar()
	{
		return scalar;	
	}
	
	public List<Double> angles() 
	{
		return Collections.unmodifiableList(angles);
	}
	
	public List<Double> translation() 
	{
		return new Point(translation);
	}

	@Override
	public Point map(Point in)
	{
		RealVector result = transformation.operate(in.getVector());
		
		return new Point(result.add(translation));
	}

	public static int numParameters(int dimension)
	{
		return 1 + dimension + (dimension * dimension - dimension)/2;
	}
	
	public Map compose(Map other)
	{
		
		if(other instanceof Similitude)
		{
			Similitude sim = (Similitude) other;
			
			double newScalar = this.scalar * sim.scalar;

			List<Double> newAngles = new ArrayList<Double>(angles.size());
			for(int i = 0; i < angles.size(); i++)
				newAngles.add(angles.get(i) + sim.angles.get(i));
			
			RealVector newTrans = sim.transformation.operate(this.translation).add(sim.translation);
			
			return new Similitude(newScalar, new Point(newTrans), newAngles);
		}
		
		return super.compose(other);
	}

	@Override
	public boolean invertible()
	{
		return scalar != 0;
	}

	@Override
	public Similitude inverse()
	{
		// inv(s, R, t) = <1/s, R', -1/s * R'>
		
		double invScalar = 1.0 / scalar;
		
		List<Double> invAngles = new ArrayList<Double>(angles.size());
		for(int i = 0 ; i < angles.size(); i++)
			invAngles.add(-angles.get(i));
		RealMatrix invRotScale = Rotation.toRotationMatrix(invAngles).scalarMultiply(-invScalar);
		RealVector invTranslation = invRotScale.operate(translation);

		return new Similitude(invScalar, new Point(invTranslation), invAngles);
	}

	@Override
	public int dimension()
	{
		return dimension;
	}
	
	public static Builder<Similitude> similitudeBuilder(int dimension)
	{
		return new SBuilder(dimension);
	}
	
	private static class SBuilder implements Builder<Similitude>
	{
		private int dimension;

		public SBuilder(int dimension) 
		{
			this.dimension = dimension;
		}

		@Override
		public Similitude build(List<Double> parameters) 
		{
			return new Similitude(parameters);
		}

		@Override
		public int numParameters() 
		{
			return Similitude.numParameters(dimension);
		}
	}

	@Override
	public String toString()
	{
		return "Similitude [s" + scalar + ", a=" + angles
				+ ", t=" + translation + "]";
	}
}
