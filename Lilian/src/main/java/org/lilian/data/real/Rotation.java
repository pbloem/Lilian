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
import org.lilian.util.distance.SquaredEuclideanDistance;

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
				// TODO: It is inefficient to use full matrix multiplication for
				// given matrices
				
				k++;
			}
	
		return left;
	}
	
	/**
	 * Uses the ES Algorithm to find angles for a given rotation matrix
	 * 
	 * @return
	 */
	public static List<Double> findAngles(RealMatrix matrix, int generations, int pop)
	{
		int dim =  matrix.getColumnDimension();
		
		Builder<Rotation> builder = Rotation.builder(dim);
		ES<Rotation> es = new ES<Rotation>(
				builder, 
				new AngleTarget(matrix),
				ES.initial(pop, builder.numParameters(), Math.PI),
				2, 2*pop, 0, ES.CrossoverMode.UNIFORM,
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
		List<Point> in;
		List<Point> out;
		
		public AngleTarget(RealMatrix target)
		{
			int dim = target.getColumnDimension();
			int n = dim * (dim - 1) / 2;
			
			this.target = new AffineMap(
					target, 
					new ArrayRealVector(target.getColumn(0)));
			
			in = new ArrayList<Point>(dim);
			for(int i = 0; i < dim; i++)
				in.add(Point.random(dim, 3.0));
				
			out = this.target.map(in);
		}

		@Override
		public double score(Rotation r)
		{	
			List<Point> outSecond = r.map(in);

			double error = 0.0;
			for(int i = 0; i < in.size(); i++)
				for(int j = 0; j < r.dimension(); j++)
				{
					double e = out.get(i).get(j) - outSecond.get(i).get(j);
					error += e*e;
				}
			
			return - error;
		}
	}

	/**
	 * Uses the ES Algorithm to find angles for a given rotation matrix
	 * 
	 * @return
	 */
	public static List<Double> findAngles(List<Point> a, List<Point> b, int generations, int pop)
	{
		int dim = a.get(0).dimensionality();
		
		Builder<Rotation> builder = Rotation.builder(dim);
		ES<Rotation> es = new ES<Rotation>(
				builder, 
				new AngleTarget2(a, b),
				ES.initial(pop, builder.numParameters(), 0.0),
				2, 2 * pop, 0, ES.CrossoverMode.UNIFORM,
				0.00005,
				0.08
				);
		
		for(int i : series(generations))
			es.breed();
		
		return es.best().parameters();
	}
	
	private static class AngleTarget2 implements Target<Rotation> 
	{
		private List<Point> a, b;
		
		public AngleTarget2(List<Point> a, List<Point> b)
		{
			this.a = a;
			this.b = b;
		}

		@Override
		public double score(Rotation r)
		{	
			double e = 0;
			
			for(int i = 0; i < a.size(); i++)
				e += SquaredEuclideanDistance.dist(b.get(i), r.map(a.get(i)));
			return - e;
		}
	}	
	
//	public static List<Double> findAngles(RealMatrix matrix)
//	{
//		int dim =  matrix.getColumnDimension();
//		
//		int k = 0;
//		for(int i = 0; i < dim-1; i++)
//			for(int j = i+1; j < dim; j++)
//			{
//				
//			}
//	}
}
