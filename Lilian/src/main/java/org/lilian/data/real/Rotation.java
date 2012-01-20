package org.lilian.data.real;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.search.evo.ES;
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
public class Rotation extends AbstractMap implements Parametrizable
{
	private static final long serialVersionUID = 3717926722178382627L;

	protected int dimension;
	

	protected List<Double> angles;
	
	// combines the rotation and scaling
	protected RealMatrix rot;
		
	public Rotation(List<Double> parameters)
	{
		int s = parameters.size();
		double ddim =  (0.5 + Math.sqrt(0.25 + s * 2.0));
		
		dimension = (int)Math.round(ddim);
		
		if((dimension*dimension - dimension)/2  != s)
			throw new IllegalArgumentException("Number of parameters ("+s+") should satisfy (d^2 - d)/2 (d="+ddim+", " + dimension + ")");
		
		this.angles = new ArrayList<Double>(parameters);
		
		RealMatrix rotation = toRotationMatrix(angles);
		this.rot = rotation;
	}

	
	/**
	 * Returns a representation of this similitude as a list of double values.
	 * 
	 * @param mode
	 * @return
	 */
	public List<Double> parameters()
	{
		return Collections.unmodifiableList(angles);
	}	
	
	/**
	 * Returns a rotation matrix for this rotation
	 * @return
	 */
	public RealMatrix matrix()
	{
		return rot.copy();
	}

	@Override
	public Point map(Point in)
	{
		RealVector result = rot.operate(in.getVector());
		
		return new Point(result);
	}

	public static int numParameters(int dimension)
	{
		return (dimension * dimension - dimension) / 2;
	}
	
	public Map compose(Map other)
	{
		
		if(other instanceof Rotation)
		{
			Rotation sim = (Rotation) other;
			
			List<Double> newAngles = new ArrayList<Double>(angles.size());
			for(int i = 0; i < angles.size(); i++)
				newAngles.add(angles.get(i) + sim.angles.get(i));
					
			return new Rotation(newAngles);
		}
		
		return super.compose(other);
	}

	@Override
	public boolean invertible()
	{
		return true;
	}

	@Override
	public Rotation inverse()
	{		
		List<Double> invAngles = new ArrayList<Double>(angles.size());
		for(int i = 0 ; i < angles.size(); i++)
			invAngles.add(-angles.get(i));

		return new Rotation(invAngles);
	}

	@Override
	public int dimension()
	{
		return dimension;
	}
	
	public static Builder<Rotation> builder(int dimension)
	{
		return new SBuilder(dimension);
	}
	
	private static class SBuilder implements Builder<Rotation>
	{
		private int dimension;

		public SBuilder(int dimension) 
		{
			this.dimension = dimension;
		}

		@Override
		public Rotation build(List<Double> parameters) 
		{
			return new Rotation(parameters);
		}

		@Override
		public int numParameters() 
		{
			return Rotation.numParameters(dimension);
		}
	}
	
	/**
	 * Transforms a given set of angles to a rotation matrix.  
	 * 
	 * To represent arbitrary rotations in dimension d, <code>(d^2-d)/2</code> 
	 * angles are required. Therefore, when this function is provided with a 
	 * angles, it assumes a dimension of (1+sqrt(1+8a))/2.  
	 * 
	 * @param angles A list of angles 
	 * @param dimension The target dimension of the rotation transformation 
	 * 					represented by the resulting matrix 
	 * @return A transformation matrix constructed for the given angles
	 */
	public static RealMatrix toRotationMatrix(List<Double> angles)
	{
		// calculate the dimension
		double dimDouble = (1.0 + sqrt(1.0 + 8.0 * angles.size()))/2.0;
		int dim = (int)Math.floor(dimDouble);
		
		RealMatrix left, right;
		
		double[] cosa = new double[angles.size()];
		double[] sina = new double[angles.size()];		
		for(int i = 0; i < angles.size(); i++)
		{
			cosa[i] = cos(angles.get(i));
			sina[i] = sin(angles.get(i));
		}
		

		left   = MatrixTools.identity(dim);
		right  = new Array2DRowRealMatrix(dim, dim);
		
		int k = 0;
		for(int i = 0; i < dim-1; i++)
			for(int j = i+1; j < dim; j++)
			{
				// Reset the elementary rotation matrix (this should be faster
				// than generating a new eye(dim) )
				MatrixTools.zero(right);
				for(int m = 0; m < dim; m++)
					right.setEntry(m, m, 1.0);

				// k = (((2 * dim - i - 1) * (i + 2))/2) - 2 * dim + j; 
				// This is just a bloody counter!

				right.setEntry(i, i,  cosa[k]);
				right.setEntry(j, j,  cosa[k]);				
				right.setEntry(i, j, -sina[k]);
				right.setEntry(j, i,  sina[k]);
	
				// Multiply
				left = left.multiply(right);
				
				k++;
			}
	
		return left;
	}
	
	/**
	 * Uses the ES Algorithm to find angles for a given rotation matrix
	 * 
	 * @return
	 */
	public static List<Double> findAngles(RealMatrix matrix, int generations)
	{
		int dim =  matrix.getColumnDimension();
		ES<Rotation> es = new ES<Rotation>(
				Rotation.builder(dim), 
				new AngleTarget(matrix),
				ES.initial(400, dim, Math.PI),
				2, 800, 0, ES.CrossoverMode.UNIFORM,
				0.00005,
				0.08
				);
		
		for(int i : series(generations))
			es.breed();
		
		return es.best().parameters();
	}
	
	private static class AngleTarget implements Target<Rotation> 
	{
		private AffineMap target;
		private int sampleSize = 20;

		public AngleTarget(RealMatrix target)
		{
			this.target = new AffineMap(
					target, 
					new ArrayRealVector(target.getColumn(0)));
		}

		@Override
		public double score(Rotation r)
		{	
			return -comp(r, target, sampleSize);
		}
		
		/**
		 * Tests whether these maps are functionally equal
		 * 
		 * @param first
		 * @param second
		 * @param margin
		 * @return
		 */
		public static double comp(Map first, Map second, int samples)
		{
			List<Point> in = new ArrayList<Point>(samples);
			for(int i = 0; i < samples; i++)
				in.add(Point.random(first.dimension(), 3.0));
				
			List<Point> outFirst = first.map(in);
			List<Point> outSecond = second.map(in);
			
			double error = 0.0;
			for(int i = 0; i < samples; i++)
				for(int j = 0; j < first.dimension(); j++)
				{
					double e = outFirst.get(i).get(j) - outSecond.get(i).get(j);
					error += e*e;
				}
			
			return error;
		}
	}
}
