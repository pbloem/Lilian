package org.lilian.data.real;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;
import org.lilian.util.MatrixTools;

public class Maps
{

	/**
	 * Finds the best fitting affine transformation from the points in xSet to 
	 * the points in ySet. The transformation is composed of a rotation matrix R, 
	 * a scalar c and a translation vector t. The transformation combines these 
	 * as y = cRx + t.
	 * 
	 * The error that is minimized by the result of this method is the mean 
	 * squared error: e = (1/n) * sum_i-n (||y_i-(cRx_i + t)||^2)
	 *  
	 * This is an implementation of the method set out by Shinji Umeyama, in the 
	 * correspondence "Least squares estimation of transformation parameters
	 * between two point patterns".
	 * 
	 * If the sizes of the two lists of point are not the same, behavior is 
	 * undefined.
	 *  
	 * @param xSet
	 * @param ySet
	 * @return An AffineMap from xSet to ySet. null if such a map could not be 
	 * 	found (this happens when the covariance matrix of x and y cannot 
	 * 	be decomposed with a singular value decomposition) 
	 */
	public static AffineMap findMap(List<Point> xSet, List<Point> ySet)
	{
		return findMapResult(xSet, ySet).affineMap();

	}
	
	public static Similitude findMap(List<Point> xSet, List<Point> ySet, int gen, int pop)
	{
		return findMapResult(xSet, ySet).similitude(gen, pop);
	}
	
	public static MapResult findMapResult(List<Point> xSet, List<Point> ySet)
	{
		int dim = xSet.get(0).dimensionality();
		int size = xSet.size();
		
		// * Calculate the means
		//  (optimize by doing in place summation manually on a double[]
		RealVector xMean = new ArrayRealVector(dim);
		for(Point x : xSet)
			xMean = xMean.add(x.getBackingData());
		xMean.mapMultiplyToSelf(1.0/xSet.size());
		
		RealVector yMean = new ArrayRealVector(dim);
		for(Point y : ySet)
			yMean = yMean.add(y.getBackingData());
		yMean.mapMultiplyToSelf(1.0/xSet.size());
				
		// * Calculate the standard deviations
		RealVector difference;
		double norm;
		
		double xStdDev = 0.0;
		for(Point x : xSet)
		{
			difference = x.getVector().subtract(xMean);
			norm = difference.getNorm();
			xStdDev += norm * norm;
		}	
		xStdDev = xStdDev / xSet.size();
		
		double yStdDev = 0.0;
		for(Point y : ySet)
		{
			difference = y.getVector().subtract(yMean);
			norm = difference.getNorm();
			yStdDev += norm * norm;
		}	
		yStdDev = yStdDev / ySet.size();
	
		// * Calculate the covariance martix
	
		RealVector xDifference, yDifference;
		
		RealMatrix covariance = new Array2DRowRealMatrix(dim, dim);
		
		for(int i = 0; i < size;i++)
		{
			xDifference = xSet.get(i).getVector().subtract(xMean);
			yDifference = ySet.get(i).getVector().subtract(yMean);
			
			
			RealMatrix term = yDifference.outerProduct(xDifference); 
			covariance =  covariance.add(term);
		}
		
		covariance = covariance.scalarMultiply(1.0/size);
		
		// * Find U, V and S
		
		SingularValueDecomposition svd = new SingularValueDecompositionImpl(covariance);
	
		RealMatrix u  = svd.getU();
		RealMatrix vt = svd.getVT();
		
		RealMatrix s = MatrixTools.identity(dim);
		double det = new LUDecompositionImpl(covariance).getDeterminant();	
		if(det < 0)
			s.setEntry(dim-1, dim-1, -1.0);
		
		// * Calculate R
		RealMatrix r =  u.multiply(s).multiply(vt);
		
		double detU = new LUDecompositionImpl(u).getDeterminant();	
		//   a matrix and it's trans have the same det
		double detV = new LUDecompositionImpl(vt).getDeterminant(); 
				
		// * Calculate c
		double trace = 0.0;
		//   obtain the non-ordered singular values
		RealVector values = MatrixTools.diag(svd.getS()); 
		//   trace of DS
		for(int i = 0; i < dim-1; i++)
			trace += values.getEntry(i);
		trace += detU*detV < 0 ? -values.getEntry(dim-1) : values.getEntry(dim-1);
		
		double c = (1.0 / xStdDev) * trace;
		
		// * Calculate t
		RealVector t = yMean.subtract(r.scalarMultiply(c).operate(xMean));
		
		// * Calculate the error
		double e = yStdDev - (trace*trace)/xStdDev;
		
		// * Create the MapResult
		return new MapResult(c, r, t, e);
	}
	
	public static class MapResult implements Comparable<MapResult>
	{
		double scale;
		double error;
		RealMatrix rotation;
		RealVector translation;
		
		private MapResult(double scale, RealMatrix rotation,
				RealVector translation, double error)
		{
			super();
			this.scale = scale;
			this.rotation = rotation;
			this.translation = translation;
			this.error = error;
		}

		public double scale()
		{
			return scale;
		}

		public RealMatrix rotation()
		{
			return rotation;
		}

		public RealVector translation()
		{
			return translation;
		}
		
		public double error()
		{
			return error;
		}
		
		public AffineMap affineMap()
		{
			// * Create the map
			return new AffineMap(
					rotation().scalarMultiply(scale()),
					translation()
				);
		}
		
		public Similitude similitude(int gen , int pop)
		{
			List<Double> angles = Rotation.findAngles(rotation(), gen, pop);
		
			// * Create the map
			return new Similitude(
					scale(), 
					new Point(translation().getData()),
					angles
				);			
		}

		@Override
		public int compareTo(MapResult other)
		{
			return Double.compare(this.error, other.error);
		}
	}
}
