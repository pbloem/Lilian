package org.lilian.data.real;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
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
public class MVN implements Density, Generator
{
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

	public MVN(Point mean)
	{
		int dim = mean.size();
		transform = new AffineMap(MatrixTools.identity(dim), mean.getVector());
		inverse = transform.inverse();		
	}
	
//	public MVN(Point mean, Matrix covariance)
//	{
//		
//		
//		transform = new AffineMap( ..., mean.getVector());
//
//		inverse = transform.inverse();	
//	}	
	
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
		RealMatrix covInv = MatrixTools.inverse(covariance());
		
		RealVector diff = p.getVector().subtract(mean().getVector());
		
		double scalar = 1.0 / (pow(2.0 * PI, dimension()/2.0) * pow(det, 0.5)) ;
		double exponent = -0.5 * diff.dotProduct( covInv.operate(diff) ); 

		return scalar * exp(exponent);	
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
		{
			Point orig = new Point(dimension());
			mean = transform.map(orig);
		}
		
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
	
	/**
	 * Takes a list of points of the same diomensionality and estimates a 
	 * multivariate normal distribution for them.
	 * 
	 * The MVN is estimated as the sample mean and sample covariance matrix.
	 * 
	 * @return
	 */
	public static MVN fromPoints(List<Point> points)
	{
		return null;
	}

}
