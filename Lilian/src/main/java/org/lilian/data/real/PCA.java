package org.lilian.data.real;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;

/** 
 * Basic PCA
 * 
 * @author Peter
 *
 */
public class PCA
{
	private List<Point> data;
	
	// * mean subtracted data
	private RealMatrix d;
	// * d, transposed and multiplied by 1/sqrt(N-1)
	private RealMatrix y;
	
	private RealMatrix signals;
	
	private RealVector mean; 
	
	private int dim, size;
	
	private SingularValueDecomposition svd;

	public PCA(List<Point> data)
	{
		this.data = data;
		
		dim = data.get(0).dimensionality();
		size = data.size();
		
		mean = new ArrayRealVector(dim);
		
		// * Calculate the mean
		for(Point datum : data)
			mean = mean.add(datum.getBackingData());
		mean.mapMultiplyToSelf(1.0/size);
		
		// * Populate the y and d matrices
		y = new Array2DRowRealMatrix(size, dim);
		d = new Array2DRowRealMatrix(dim, size);
		double a = 1.0/Math.sqrt(size - 1.0);
		for(int i : series(size))
		{
			Point datum = data.get(i);
			
			RealVector norm = datum.getVector().subtract(mean);
			d.setColumnVector(i, norm);
			
			norm.mapMultiplyToSelf(a);
			y.setRowVector(i, norm);
		}
			
		svd = new SingularValueDecompositionImpl(y);
		
		signals = svd.getVT().multiply(d); 
	}
	
	/**
	 * The data mean
	 * @return
	 */
	public Point mean()
	{
		return new Point(mean);
	}
	
	/**
	 * Returns a simplified version of the data set
	 * 
	 * @param dim
	 * @return
	 */
	public List<Point> simplify(int dim)
	{
		List<Point> result = new ArrayList<Point>(size);
		
		for(int i : series(size))
		{
			Point point = new Point(dim);
			for(int j : series(dim))
				point.set(j, signals.getEntry(j, i));
			result.add(point);
		}
		
		return result;
	}

	/**
	 * Simplifies a given point from the data-space to the lower dimensional
	 * subspace determined by this PCA.
	 * 
	 * @param in
	 * @param dim
	 * @return
	 */
	public Point simplify(Point in, int dim)
	{
		RealVector out = svd.getVT().operate(in.getVector().subtract(mean));
		
		return new Point(out.getSubVector(0, dim));
	}	
	
	/**
	 * Maps a point in the simplified space back to the data space. 
	 * 
	 * @param simplified
	 * @return
	 */
	public Point mapBack(Point simplified)
	{
		RealVector xs = new ArrayRealVector(dim);
		xs.setSubVector(0, simplified.getBackingData());
		
		return new Point(svd.getV().operate(xs).add(mean));
	}
}
