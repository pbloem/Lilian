package org.lilian.data.real;

import static java.lang.Math.*;
import static org.lilian.util.MatrixTools.*;
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
public class Rotation extends AffineMap implements Parametrizable
{
	private static final long serialVersionUID = 3717926722178382627L;

	protected int dimension;
	public static double ACCURACY = 10E-12;
	

	protected List<Double> angles;
	
	protected RealMatrix transformation;
		
	public Rotation(List<Double> parameters)
	{
		int s = parameters.size();
		double ddim =  (0.5 + Math.sqrt(0.25 + s * 2.0));
		
		dimension = (int)Math.round(ddim);
		
		if((dimension*dimension - dimension)/2  != s)
			throw new IllegalArgumentException("Number of parameters ("+s+") should satisfy (d^2 - d)/2 (d="+ddim+", " + dimension + ")");
		
		this.angles = new ArrayList<Double>(parameters);
		
		RealMatrix rotation = toRotationMatrix(angles);
		this.transformation = rotation;
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
		return transformation.copy();
	}

	@Override
	public Point map(Point in)
	{
		RealVector result = transformation.operate(in.getVector());
		
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
	
	public static Builder<Rotation> rotationBuilder(int dimension)
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
//		
//		double[] cosa = new double[angles.size()];
//		double[] sina = new double[angles.size()];		
//		for(int i = 0; i < angles.size(); i++)
//		{
//			cosa[i] = cos(angles.get(i));
//			sina[i] = sin(angles.get(i));
//		}
//		

		left   = MatrixTools.identity(dim);
		right  = new Array2DRowRealMatrix(dim, dim);
		
		// * The columns iterate from high to low, and the rows from low to 
		//   high, to facilitate the inverse operation of retrieving the angles 
		//   from a rotation matrix
		
		int k = 0;
		for(int j = dim-2; j >= 0; j--) // - columns
			for(int i = j+1; i < dim; i++) // rows
			{
//				// Reset the elementary rotation matrix (this should be faster
//				// than generating a new eye(dim) )
//				MatrixTools.zero(right);
//				for(int m = 0; m < dim; m++)
//					right.setEntry(m, m, 1.0);
//
//				// k = (((2 * dim - i - 1) * (i + 2))/2) - 2 * dim + j; 
//				// This is just a bloody counter!
//
//				right.setEntry(i, i,  cosa[k]);
//				right.setEntry(j, j,  cosa[k]);				
//				right.setEntry(i, j, -sina[k]);
//				right.setEntry(j, i,  sina[k]);

				right = elementary(dim, i, j, angles.get(k));
	
				// Multiply
				left = left.multiply(right);
				// TODO: It is inefficient to use full matrix multiplication for
				// givens matrices
				
				k++;
			}
	
		return left;
	}
	
	/**
	 * Uses the method described here:
	 * 	http://math.stackexchange.com/questions/119770/retrieving-angles-from-a-rotation-matrix/119797#comment278460_119797
	 * 
	 * to find the angles for a rotation matrix
	 * @return
	 */
	public static List<Double> findAngles(RealMatrix matrix)
	{
		if(! matrix.isSquare())
			throw new IllegalArgumentException("Matrix ("+matrix+") not square");
		if(! MatrixTools.isInvertible(matrix))
			throw new IllegalArgumentException("Matrix ("+matrix+") not invertible");
		
		int d = matrix.getColumnDimension();
		int num = (d*d - d) / 2;
		List<Double> angles = new ArrayList<Double>(num);
		
		RealMatrix product = MatrixTools.inverse(matrix); // product of all rotations so far
		
		// * Iterate over axes-pairs in the opposite direction from toRotationMatrix
		for(int j = 0; j < d-1 ; j++) 
		{
			RealVector ek = base(d, j), m = null;
			for(int i = d-1; i >= j+1; i--)
			{
				// * Choose R_ik such that element i of  (R_ik * product * e_k) is 0
				m = product.operate(ek);
				
				double angle = angle(m.getEntry(i), m.getEntry(j));
				angles.add(0, angle);	
												
				RealMatrix rik = elementary(d, i, j , angle);
				product = rik.multiply(product);	
				
				System.out.println(i + " - " + j + " " + product.operate(ek));

			}
			
			// * at this point m(k) = +1 or -1
			m = product.operate(ek);
			System.out.println("ek k" + m.getEntry(j));
			if(m.getEntry(j) < 0)
			{
				// * add pi to the last angle
				angles.set(0, angles.get(0) + Math.PI);
				product = elementary(d, j, j+1, PI).multiply(product);
			}
		}
		// * at this point product * R^-1 = I
		
		return angles;
	}
	
	/**
	 * Returns an elementary rotation matrix in R^d (also known as a Givens 
	 * matrix).
	 * 
	 * The matrix rotates by a given angle in the plane between axes i and j.
	 * 
	 * @param dim
	 * @param i
	 * @param j
	 * @param angle
	 * @return
	 */
	public static RealMatrix elementary(int dim, int i, int j, double angle)
	{		
		// System.out.println(i + ", " + j);
		
		RealMatrix m = MatrixTools.identity(dim);
		
		double s = sin(angle);
		double c = cos(angle);
		
		m.setEntry(i, i,  c);
		m.setEntry(j, j,  c);
		m.setEntry(i, j, -s);
		m.setEntry(j, i,  s);

		return m;
	}	
	
	/**
	 * Solve cos(a) * h_i - sin(a) * h_j = 0 for a
	 * 
	 * This is the angle for the givens rotation on dimensions i and k 
	 * 
	 * @param hi
	 * @param hj
	 * @return
	 */
	public static double angle(double hi, double hj)
	{
		if(zero(hi) && zero(hj))
			return 0.0; // the angle doesn't matter
		if(zero(hj))
			return 1.0;
		if(zero(hi))
			return 0.0;
		
		double c = hj/hi;
		
		double a = 2.0 * atan( sqrt(c*c + 1.0) - c);
		
		if(Double.isInfinite(a) || Double.isNaN(a))
			a = 2.0 * atan( - sqrt(c*c + 1.0) - c);
		
		if(Double.isInfinite(a) || Double.isNaN(a))
			throw new IllegalStateException("Result ("+a+") is infinite or NaN for inputs hi = "+hi+" and hk = "+hj+".");
				
		return a;
	}
	
	private static boolean zero(double in)
	{
		return Math.abs(in) < ACCURACY;
	}
	
	/**
	 * Uses the ES Algorithm to find angles for a given rotation matrix
	 * 
	 * @return
	 */
	public static List<Double> findAngles(RealMatrix matrix, int generations, int pop)
	{
		int dim =  matrix.getColumnDimension();
		
		Builder<Rotation> builder = Rotation.rotationBuilder(dim);
		ES<Rotation> es = new ES<Rotation>(
				builder, 
				new AngleTarget(matrix),
				ES.initial(pop, builder.numParameters(), Math.PI),
				2, 2*pop, 0, ES.CrossoverMode.UNIFORM,
				0.000005,
				0.02
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
		
		Builder<Rotation> builder = Rotation.rotationBuilder(dim);
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
