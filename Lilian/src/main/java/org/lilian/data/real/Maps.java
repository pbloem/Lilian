//package org.lilian.data.real;
//
//public class Maps
//{
//
//	/**
//	 * Finds the best fitting affine transformation from the points in xSet to 
//	 * the points in ySet. The transformation is composed of a rotation matrix R, 
//	 * a scalar c and a translation vector t. The transformation combines these 
//	 * as y = cRx + t.
//	 * 
//	 * The error that is minimized by the result of this method is the mean 
//	 * squared error: e = (1/n) * sum_i-n (||y_i-(cRx_i + t)||^2)
//	 *  
//	 * This is an implementation of the method set out by Shinji Umeyama, in the 
//	 * correspondence "Least squares estimation of transformation parameters
//	 * between two point patterns".
//	 * 
//	 * If the sizes of the two lists of point are not the same, behavior is 
//	 * undefined.
//	 *  
//	 * @param xSet
//	 * @param ySet
//	 * @return An AffineMap from xSet to ySet. null if such a map could not be 
//	 * 	found (this happens when the covariance matrix of x and y cannot 
//	 * 	be decomposed with a singular value decomposition) 
//	 */
//	public static AffineMap findMap(List<Point> xSet, List<Point> ySet)
//	{
//		int dim = xSet.get(0).dimensionality();
//		int size = xSet.size();
//		
//		//* Calculate the means
//		DenseVector xMean = new DenseVector(dim);
//		for(Point x : xSet)
//			xMean.add(x.getVector());
//		xMean.scale(1.0/xSet.size());
//		
//		DenseVector yMean = new DenseVector(dim);
//		for(Point y : ySet)
//			yMean.add(y.getVector());
//		yMean.scale(1.0/xSet.size());
//				
//		//* Calculate the standard deviations
//		DenseVector difference = new DenseVector(dim);
//		double norm;
//		
//		double xStdDev = 0.0;
//		for(Point x : xSet)
//		{
//			difference.set(x.getVector());
//			difference.add(-1.0, xMean);
//			norm = difference.norm(Vector.Norm.Two);
//			xStdDev += norm * norm;
//		}	
//		xStdDev = xStdDev / xSet.size();
//		
//		double yStdDev = 0.0;
//		for(Point y : ySet)
//		{
//			difference.set(y.getVector());
//			difference.add(-1.0, yMean);
//			norm = difference.norm(Vector.Norm.Two);
//			yStdDev += norm * norm;
//		}	
//		yStdDev = yStdDev / ySet.size();
//	
//		//* Calculate the covariance martix
//	
//		DenseVector xDifference = new DenseVector(dim),
//					yDifference = new DenseVector(dim);
//		
//		DenseMatrix covariance = new DenseMatrix(dim, dim);
//		
//		for(int i = 0; i < size;i++)
//		{
//			xDifference.set(xSet.get(i).getVector());
//			xDifference.add(-1.0, xMean);
//			
//			yDifference.set(ySet.get(i).getVector());
//			yDifference.add(-1.0, yMean);
//			
//			covariance.rank1(yDifference, xDifference);
//		}
//		
//		covariance.scale(1.0/size);
//		
//		//* Find U, V and S
//		
//		SVD svd = null;
//		try {
//			svd = SVD.factorize(covariance);
//		} catch (NotConvergedException e)
//		{
//			return null;
//		}
//		DenseMatrix u  = svd.getU();
//		DenseMatrix vt = svd.getVt();
//	
//		
//		double det = Functions.determinant(covariance); // FIXME: this may not work (broken LU decomp)
//		DenseMatrix s = Matrices.identity(dim);
//		
//		if(det < 0)
//			s.set(dim-1, dim-1, -1.0);
//		
//		//* Calculate R
//		Matrix r0, r;
//		
//		r0 =  u.mult(s,  new DenseMatrix(dim, dim));
//		r  = r0.mult(vt, new DenseMatrix(dim, dim));
//		
//		//* Calculate c
//		double trace = 0.0;
//		double[] values = svd.getS();
//		for(int i = 0; i < dim-1; i++)
//			trace += values[i];
//		trace += det < 0 ? -values[dim-1] : values[dim-1];
//		
//		double c = 1.0/xStdDev * trace;
//		
//		r.scale(c);
//		
//		//* calculate t
//		
//		DenseVector t = new DenseVector(dim);
//		t.set(yMean);
//		t.add(-1.0, r.mult(xMean, new DenseVector(dim)));
//		
//		//* Create the map
//		return new AffineMap(r, t);
//	}
//
//	
//	
//}
