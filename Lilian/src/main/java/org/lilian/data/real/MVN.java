//package org.lilian.data.real;
//
//import static java.lang.Math.PI;
//import static java.lang.Math.abs;
//import static java.lang.Math.exp;
//import static java.lang.Math.pow;
//
//import java.util.Random;
//
//import no.uib.cipr.matrix.DenseMatrix;
//import no.uib.cipr.matrix.DenseVector;
//import no.uib.cipr.matrix.Vector;
//
//import org.ujmp.core.Matrix;
//import org.ujmp.core.calculation.Calculation.Ret;
//import org.ujmp.core.doublematrix.DenseDoubleMatrix2D;
//
///**
// * A multivariate normal distribution
// * @author Peter
// *
// */
//public class MVN implements Density, Generator
//{
//	// The transformation form the standard normal MVN to this one
//	protected AffineMap transform;
//
//	// Properties derived from transform
//	protected AffineMap inverse;
//	
//	protected Matrix covariance = null;
//	protected Point mean = null;
//	
//	public MVN(int dim);
//	{
//		transform = AffineMap.identity(dim);
//		inverse = transform.inverse();
//	}
//
//	public MVN(Point mean)
//	{
//		int dim = mean.size();
//		transform = new AffineMap(DenseDoubleMatrix2D.factory.eye(dim), mean.getVector());
//		inverse = transform.inverse();		
//	}
//	
////	public MVN(Point mean, Matrix covariance)
////	{
////		
////		
////		transform = new AffineMap( ..., mean.getVector());
////
////		inverse = transform.inverse();	
////	}	
//	
//	public MVN(AffineMap transform)
//	{
//		if(! transform.invertible())
//			throw new IllegalArgumentException("Input argument ("+transform+") must be invertible");
//		
//		this.transform = transform;
//	}	
//
//	
//	@Override
//	public Point generate()
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Point generate(Random random)
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Point generate(int n)
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Point generate(int n, Random random)
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public double density(Point p)
//	{
//		double det = abs(covariance().det());
//		Matrix diff = p.getVector().diff(Ret.NEW,, arg2)
//		
//		double scalar = 1.0 / (pow(2.0 * PI, dimension()/2.0) * pow(det, 0.5)) ;
//		double exponent = -0.5 * diff.dot(covDiff); 
//
//		return scalar * exp(exponent);	
//		
//	}
//	
//	public Matrix covariance()
//	{
//		if(covariance == null)
//		{
//			Matrix mat, trans;
//			
//			mat = transform.getTransformation().clone();
//			trans = transform.getTransformation().clone().transpose();
//			
//			covariance = mat.times(trans);
//		}
//		
//		return covariance;
//	}
//	
//	public mean(Point mean)
//	{
//		if(mean == null)
//		{
//			Point orig = new Point(dimension());
//			mean = transform.map(orig);
//		}
//		
//		return mean;
//	}
//	
//	public double dimension()
//	{
//		return transform.dimension();
//	}
//	
//	/**
//	 * Returns a standard normal distribution (zero mean and the identity matrix as covariance matrix)
//	 * with the given number of dimensions
//	 * 
//	 * @return A standard normal MVN for the given dimensionality
//	 */
//	public static MVN standard(int dim)
//	{
//		return new MVN(dim);
//	}
//
//}
