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
 * In the parameter representation of a similitude the angles are normalized so 
 * that 1.0 represents an angle of 360 degrees (or 2 pi radians). 
 * 
 * @author Peter
 *
 */
public class Similitude extends AffineMap
{
	private static final long serialVersionUID = 3717926722178382627L;

	protected int dimension;
	
	protected double scalar;
	protected List<Double> angles; // in radians
	
	/**
	 * Note: the angles in this parameter vector are in radians div by 2 pi 
	 * (that way all parameters are roughly the same scale)
	 * 
	 * @param parameters
	 */
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
	
	/**
	 * 
	 * 
	 * @param scalar
	 * @param translation
	 * @param angles In radians
	 */
	public Similitude(double scalar, List<Double> translation, List<Double> angles)
	{
		int dim = translation.size();
		int a = angles.size();
		
		if( (dim*dim - dim)/2 != a)
			throw new IllegalArgumentException("Wrong number of angles ("+a+") for the given dimension (size of translation vector = "+dim+"). The number of angles should be (d*d - d) / 2");
		
		List<Double> angs = new ArrayList<Double>(angles.size());
		for(double angle : angles)
			angs.add(angle / (2.0 * Math.PI));
				
		init(scalar, translation, angs);
	}
	
	private void init(double scalar, List<Double> translation, List<Double> angles)
	{
		dimension = translation.size();
		
		this.scalar	= scalar;
		this.translation = MatrixTools.toVector(translation);
		
		this.angles = new ArrayList<Double>(angles);
		for(int i : Series.series(angles.size()))
			angles.set(i, angles.get(i) * 2.0 * Math.PI);
		
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
			
		for(double angle : angles)
			result.add(angle / (2.0 * Math.PI));
			
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
	
	/**
	 * Returns the angles that define the rotation part of this similitude 
	 * (in radians)
	 * 
	 * @return
	 */
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
			// * return t(o(x)) = <s_ts_o, R_tR_o, s_t * R_t * t_o + t_t> 
			Similitude sim = (Similitude) other;
			
			double newScalar = this.scalar * sim.scalar;

			List<Double> newAngles = new ArrayList<Double>(angles.size());
			for(int i = 0; i < angles.size(); i++)
				newAngles.add(this.angles.get(i) + sim.angles.get(i));
			
			// * note that this.transformation contains both the scaling an the rotation
			RealVector newTrans = this.transformation.operate(sim.translation).add(sim.translation);
			
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
	
	public static Similitude identity(int dim)
	{
		return new Similitude(1.0, new Point(dim), new Point((dim*dim - dim)/2));
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
