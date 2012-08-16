package org.lilian.data.real;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.CholeskyDecomposition;
import org.apache.commons.math.linear.CholeskyDecompositionImpl;
import org.apache.commons.math.linear.NonSquareMatrixException;
import org.apache.commons.math.linear.NotPositiveDefiniteMatrixException;
import org.apache.commons.math.linear.NotSymmetricMatrixException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularMatrixException;
import org.lilian.Global;
import org.lilian.util.MatrixTools;


/**
 * A multivariate normal distribution
 * @author Peter
 *
 * TODO: 
 * <ul>
 *   <li>
 *   Thoroughly test this implementation, in particular the correspondence 
 *   between the density and generate method.
 *   </li>
 *   <li>
 *   Use cholesky decomp method for faster generation. (not that important now).
 *   </li>
 * </ul>
 */
public class MVN implements Density, Generator<Point>
{
	public static final double THRESHOLD = 10E-10;
	
	// The transformation form the standard normal MVN to this one
	protected AffineMap transform;

	// Properties derived from transform
	protected AffineMap inverse;
	
	protected RealMatrix covariance = null;
	protected Point mean = null;
	
	/**
	 * The standard normal MVN for the given dimension. 
	 * 
	 * This creates a multivariate normal distribution which is centered on the 
	 * origin and has the identity matrix as covariance matrix.
	 *  
	 * @param dim
	 */
	public MVN(int dim)
	{
		transform = AffineMap.identity(dim);
		inverse = transform.inverse();
	}
	
	/**
	 * Centralized MVN with the given variance. The MVN is spherical.
	 * @param dim
	 * @param var
	 */
	public MVN(int dim, double var)
	{
		RealVector s = new ArrayRealVector(dim);
		s.set(var);
		
		RealMatrix matrix = MatrixTools.diag(s);
		transform = new AffineMap(matrix, new ArrayRealVector(dim));

		inverse = transform.inverse();	
	}
	
	/**
	 * Centralized MVN with the given variance. The MVN is spherical.
	 * @param dim
	 * @param var
	 */
	public MVN(Point mean, double var)
	{
		RealVector s = new ArrayRealVector(mean.dimensionality());
		s.set(var);
		
		RealMatrix matrix = MatrixTools.diag(s);
		transform = new AffineMap(matrix, mean.getVector());

		inverse = transform.inverse();	
	}	

	public MVN(Point mean)
	{
		int dim = mean.size();
		transform = new AffineMap(MatrixTools.identity(dim), mean.getVector());
		inverse = transform.inverse();		
	}
	
	public MVN(Point mean, RealMatrix covariance)
	{
		CholeskyDecomposition decomp;
		try
		{
			decomp = new CholeskyDecompositionImpl(covariance, THRESHOLD, THRESHOLD);
		} catch (MathException e)
		{
			throw new RuntimeException("Could not perform Cholesky decomposition on matrix " + covariance, e);
		}
		
		transform = new AffineMap(decomp.getL(), mean.getVector());

		inverse = transform.inverse();	
	}	
	
	public MVN(AffineMap transform)
	{
		if(! transform.invertible())
			throw new IllegalArgumentException("Input argument ("+transform+") must be invertible");
		
		this.transform = transform;
	}	

	/**
	 * Draw a random vector with all elements ~ N(0, 1)
	 * 
	 * @return
	 */
	private Point drawBase()
	{	
		Point base = new Point(dimension());
		for(int i = 0; i < dimension(); i++)
			base.set(i, Global.random.nextGaussian());
		
		return base;
	}	
	
	@Override
	public Point generate()
	{
		Point p = drawBase();
		return transform.map(p);
	}

	@Override
	public List<Point> generate(int n)
	{
		List<Point> points = new ArrayList<Point>(n);
		for(int i = 0; i < n; i++)
			points.add(generate());
		
		return points;		
	}

	@Override
	public double density(Point p)
	{
		double det = abs( MatrixTools.getDeterminant(covariance()));
		if(MatrixTools.isSingular(covariance()))
			return 0.0;
		
		RealMatrix covInv = null;
		try {
			covInv = MatrixTools.inverse(covariance());
		} catch(SingularMatrixException e)
		{
			System.out.println(covariance());
			System.out.println(covariance.isSingular());
			System.out.println(MatrixTools.isSingular(covariance()));
			
			throw e;
		}
		
		RealVector diff = p.getVector().subtract(mean().getVector());
		
		double scalar = 1.0 / (pow(2.0 * PI, dimension()/2.0) * pow(det, 0.5)) ;
		double exponent = -0.5 * diff.dotProduct( covInv.operate(diff) ); 

		return scalar * exp(exponent);	
	}
	
	public AffineMap map()
	{
		return transform;
	}
	
	public RealMatrix covariance()
	{
		if(covariance == null)
		{
			RealMatrix mat, trans;
			
			mat = transform.getTransformation();
			trans = transform.getTransformation().transpose();
			
			covariance = mat.multiply(trans);
		}
		
		return covariance;
	}
	
	public Point mean()
	{
		if(mean == null)
			mean = new Point(transform.getTranslation());
		
		return mean;
	}
	
	public int dimension()
	{
		return transform.dimension();
	}
	
	/**
	 * Returns a standard normal distribution (zero mean and the identity matrix as covariance matrix)
	 * with the given number of dimensions
	 * 
	 * @return A standard normal MVN for the given dimensionality
	 */
	public static MVN standard(int dim)
	{
		return new MVN(dim);
	}
	
	
	
	@Override
	public String toString()
	{
		return "[covariance=" + covariance() + ", mean=" + mean() + "]";
	}

	/**
	 * Takes a list of points of the same dimensionality and estimates a 
	 * multivariate normal distribution for them.
	 * 
	 * The MVN is estimated as the sample mean and sample covariance matrix.
	 * 
	 * @return
	 */
	public static MVN find(List<Point> points)
	{
		int dim = points.get(0).dimensionality();
		int size = points.size();
		
		// * Calculate the mean
		//  (optimize by doing in place summation manually on a double[]
		RealVector mean = new ArrayRealVector(dim);
		for(Point x : points)
			mean = mean.add(x.getBackingData());
		mean.mapMultiplyToSelf(1.0/size);
	
		// * Calculate the covariance
		RealVector difference;
		RealMatrix cov = MatrixTools.identity(dim);
		
		double xStdDev = 0.0;
		for(Point x : points)
		{
			difference = x.getVector().subtract(mean);
			cov = cov.add(difference.outerProduct(difference));
		}
		
		cov = cov.scalarMultiply(1.0/(size-1));
		
		return new MVN(new Point(mean), cov);
	}
	
	
	/**
	 * Takes a list of points of the same dimensionality and estimates a 
	 * spherical multivariate normal distribution for them.
	 * 
	 * A sphreical MVN is defined by a mean and a scalar s such that sI is 
	 * the covariance matrix  
	 * 
	 * @return
	 */
	public static MVN findSpherical(List<Point> points)
	{
		int dim = points.get(0).dimensionality();
		int size = points.size();
		
		// * Calculate the mean
		//  (optimize by doing in place summation manually on a double[]
		RealVector mean = new ArrayRealVector(dim);
		for(Point x : points)
			mean = mean.add(x.getBackingData());
		mean.mapMultiplyToSelf(1.0/size);
	
		// * Calculate s
		RealVector difference;
		double sumQuadrance = 0.0;
		
		for(Point x : points)
		{
			difference = x.getVector().subtract(mean);
			sumQuadrance += difference.dotProduct(difference);
		}
		
		double s = sumQuadrance / (size * dim);
		
		return new MVN(new Point(mean), MatrixTools.identity(dim).scalarMultiply(s));
	}	
}
