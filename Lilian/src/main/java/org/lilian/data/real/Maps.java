package org.lilian.data.real;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

public class Maps
{
	public static int MAX_SVD_RETRIES = 50;
	
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
	 * 
	 * @param xSet
	 * @param ySet
	 * @return An AffineMap from xSet to ySet. null if such a map could not be 
	 * 	found (this happens when the covariance matrix of x and y cannot 
	 * 	be decomposed with a singular value decomposition) 
	 */
	public static AffineMap findSimilitudeMap(List<Point> xSet, List<Point> ySet)
	{
		FindSimilitudeResult res = findSimilitudeResult(xSet, ySet);
		if(res == null)
			return null;
		
		return res.affineMap(); 
	}
	
	/**
	 * Like findAffineMap, but converts the rotation matrix to a set of angles
	 * 
	 * @param xSet
	 * @param ySet
	 * @return
	 */
	public static Similitude findSimilitude(List<Point> xSet, List<Point> ySet)
	{
		FindSimilitudeResult res = findSimilitudeResult(xSet, ySet);
		if(res == null)
			return null;
		
		return res.similitude();
	}
	
	public static FindSimilitudeResult findSimilitudeResult(List<Point> xSet, List<Point> ySet)
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
		int retries = 0;
		boolean success = false;
		SingularValueDecomposition svd = null;
		while(! success)
		{
			try 
			{
				svd = new SingularValueDecompositionImpl(covariance);
				success = true;
			} catch (InvalidMatrixException e)
			{
				retries++;
				if(retries > MAX_SVD_RETRIES)
					return null;
			}
		}
		
	
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
		return new FindSimilitudeResult(c, r, t, e);
	}
	
	public static class FindSimilitudeResult implements Comparable<FindSimilitudeResult>
	{
		double scale;
		double error;
		RealMatrix rotation;
		RealVector translation;
		
		private FindSimilitudeResult(double scale, RealMatrix rotation,
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
		
		public Similitude similitude()
		{
			List<Double> angles = Rotation.findAngles(rotation());
		
			// * Create the map
			return new Similitude(
					scale(), 
					new Point(translation().getData()),
					angles
				);			
		}

		@Override
		public int compareTo(FindSimilitudeResult other)
		{
			return Double.compare(this.error, other.error);
		}
	}
	
	/**
	 * Returns a map that scales the data to fit the 
	 * bi-unit square exactly
	 * @param data
	 * @return
	 */
	public static AffineMap centered(List<Point> data)
	{
		if(data.size() == 0)
			throw new IllegalArgumentException("Cannot find centering map for empty dataset");
		
		int dim = data.get(0).dimensionality();
		
		double min[]  = new double[dim],
		       max[]   = new double[dim],
		       scale[] = new double[dim];
		
		for(int i : Series.series(dim))
		{
			min[i] = Double.MAX_VALUE;
			max[i] = Double.MIN_VALUE;
		}
		
		for(Point p : data)
			for(int i : Series.series(dim))
			{
				min[i] = min(min[i], p.get(i));
				max[i] = max(max[i], p.get(i));
			}
		
		for(int i : Series.series(dim))
			scale[i] = 1.0/(max[i] - min[i]);
		
		RealVector t = new ArrayRealVector(min);
		t = t.mapMultiply(-1.0);
		
		RealVector s = new ArrayRealVector(scale);
		s = s.mapMultiply(2.0);
		RealMatrix rot = MatrixTools.diag(s);
		
		t = rot.operate(t);
		t = t.mapAdd(-1.0); // * subtract 1 to move [0, 2] to [-1, 1]
		
		return new AffineMap(rot, t);
	}
	
	/**
	 * Returns a map that scales the data uniformly to fit the bi-unit cube
	 * @param data
	 * @return
	 */
	public static AffineMap centerUniform(List<Point> data)
	{
		if(data.size() == 0)
			throw new IllegalArgumentException("Cannot find centering map for empty dataset");
		
		int dim = data.get(0).dimensionality();
		
		double min[]  = new double[dim],
		       max[]   = new double[dim],
		       range[] = new double[dim],
		       rmax = Double.NEGATIVE_INFINITY;
		
		for(int i : Series.series(dim))
		{
			min[i] = Double.POSITIVE_INFINITY;
			max[i] = Double.NEGATIVE_INFINITY; 
		}
		
		for(Point p : data)
			for(int i : series(dim))
			{
				min[i] = min(min[i], p.get(i));
				max[i] = max(max[i], p.get(i));
			}
		
		for(int i : series(dim))
		{
			range[i] = max[i] - min[i];
			rmax = max(range[i], rmax);
		}
		
		List<Double> t = new ArrayList<Double>(dim);
		for(int i : series(dim))
			t.add(
					(- (min[i] + 0.5 * range[i])) * (2.0 / rmax)
				);
		
		int angles = (dim * dim - dim) / 2; 
		return new Similitude(2.0 / rmax, t, new Point(angles));
	}	
	
	/**
	 * Returns a map that centers the center of mass of the dataset at the 
	 * origin and scales everything to fit in the bi-unit cube.
	 * 
	 * @param data
	 * @return
	 */
	public static AffineMap centeredWeighted(List<Point> data)
	{
		int dim = data.get(0).dimensionality();
		
		double[] mean = new double[dim];
		
		double min[]   = new double[dim],
		       max[]   = new double[dim],
		       scale[] = new double[dim];
		
		for(int i : Series.series(dim))
		{
			mean[i] = 0.0;
			min[i] = Double.MAX_VALUE;
			max[i] = Double.MIN_VALUE;
		}
		
		for(Point p : data)
			for(int i : Series.series(dim))
			{
				mean[i] += p.get(i);
				min[i] = min(min[i], p.get(i));
				max[i] = max(max[i], p.get(i));
			}
		
		for(int i : Series.series(dim))
			mean[i] /= (double) data.size();
		
		for(int i : Series.series(dim))
			scale[i] = 1.0 / Math.max(Math.abs(min[i] - mean[i]), Math.abs(max[i] - mean[i]));
		
		// * Subtract the mean
		RealVector t = new ArrayRealVector(mean);
		t = t.mapMultiply(-1.0);
		
		// * Scale to [-1, 1] 
		RealVector s = new ArrayRealVector(scale);
		RealMatrix rot = MatrixTools.diag(s);
		
		t = rot.operate(t);
		
		return new AffineMap(rot, t);
	}
	
	
	public static Map logistic(double r)
	{
		return new LogisticMap(r);
	}
	
	private static class LogisticMap extends AbstractMap
	{
		private static final long serialVersionUID = -1472793577322007041L;
		private double r;
		
		public LogisticMap(double r)
		{
			this.r = r;
		}

		@Override
		public Point map(Point in)
		{
			double x = in.get(0);
			x = r * x * (1.0 - x);
			return new Point(x);
		}

		@Override
		public boolean invertible()
		{
			return false;
		}

		@Override
		public Map inverse()
		{
			return null;
		}

		@Override
		public int dimension()
		{
			return 1;
		}
		
		
	}
	
	
	public static Map henon()
	{
		return new HenonMap();
	}
	
	private static class HenonMap extends AbstractMap
	{
		double a, b;
		
		public HenonMap()
		{
			this(1.4, 0.3);
		}
		
		public HenonMap(double a, double b)
		{
			this.a = a;
			this.b = b;
		}

		@Override
		public Point map(Point in)
		{
			double x = in.get(0);
			double y = in.get(1);
			
			double xx = y + 1 - a * x * x,
				   yy = b * x;
				
			x = xx;
			y = yy;
				
			return new Point(x, y);
		}

		@Override
		public boolean invertible()
		{
			return true;
		}

		@Override
		public Map inverse()
		{
			return new HenonInvMap(a, b);
		}

		@Override
		public int dimension()
		{
			return 2;
		}	
	}
	
	private static class HenonInvMap extends AbstractMap
	{
		double a, b;
		
		public HenonInvMap()
		{
			this(1.4, 0.3);
		}
		
		public HenonInvMap(double a, double b)
		{
			this.a = a;
			this.b = b;
		}

		@Override
		public Point map(Point in)
		{
			double x = in.get(0);
			double y = in.get(1);
			
			double xx = y / b,
				   yy = x - 1.0 + a * y * y * (1.0 / (b * b));
				
			x = xx;
			y = yy;
				
			return new Point(x, y);
		}

		@Override
		public boolean invertible()
		{
			return true;
		}

		@Override
		public Map inverse()
		{
			return new HenonMap(a, b);
		}

		@Override
		public int dimension()
		{
			return 2;
		}	
	}	

}
