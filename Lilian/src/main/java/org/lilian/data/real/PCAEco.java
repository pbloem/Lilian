package org.lilian.data.real;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.EigenDecomposition;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;
import org.lilian.util.MatrixTools;

/** 
 * An economical version of PCA which uses the transpose of the data matrix to
 * calculate the eigenvectors. The upshot is that it can deal with high 
 * dimensional data, the downside is that it cannot return more eigenvectors 
 * than there are instances in the dataset. 
 * 
 * @author Peter
 *
 */
public class PCAEco
{
	private List<Point> data;
	
	// * mean subtracted data
	private RealMatrix d;
	// * transposed mean subtracted data
	private RealMatrix dt;
	// * cov of mean subtracted data
	private RealMatrix ddt;
	
	// * first 'size' eigenvectors
	private RealMatrix v, vt;
	
	private EigenDecomposition eig;
	
	private RealMatrix signals;
	
	private RealVector mean; 
	
	private int dim, size;

	public PCAEco(List<Point> data)
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
		d = new Array2DRowRealMatrix(size, dim);
		for(int i : series(size))
		{
			Point datum = data.get(i);
			
			RealVector norm = datum.getVector().subtract(mean);
			d.setRowVector(i, norm);
		}
		
		dt = d.transpose();
		ddt = d.multiply(dt);
		// ddt = ddt.scalarMultiply(1.0/(size - 1.0));
				
		eig = new EigenDecompositionImpl(ddt, Double.NaN);
		
		v = dt.multiply(eig.getV());  // * raw eigenvectors
		// * normalize eigenvectors
		RealVector norms = new ArrayRealVector(v.getColumnDimension());
		for(int col : series(v.getColumnDimension()))
		{
			double sum = 0.0;
			for(int row : series(v.getRowDimension()))
				sum += v.getEntry(row, col) * v.getEntry(row, col);
			
			norms.setEntry(col, Math.sqrt(sum));
		}
		for(int col : series(v.getColumnDimension()))
		{
			double scalar = 1.0 / norms.getEntry(col);
			for(int row : series(v.getRowDimension()))
				v.multiplyEntry(row, col, scalar);
		}
		
		// vt = v.transpose();
		
		signals = d.multiply(v);
	}
	
	/**
	 * The data mean
	 * @return
	 */
	public Point mean()
	{
		return new Point(mean);
	}
	
	public List<Double> eigenValues()
	{
		return new Point(eig.getRealEigenvalues());
	}
	
	public Point eigenVector(int index)
	{
		RealVector ev = v.getColumnVector(index);
		
		double max = Double.NEGATIVE_INFINITY,
		       min = Double.POSITIVE_INFINITY;
		
		for(int i : series(ev.getDimension()))
		{
			max = Math.max(max, ev.getEntry(i));
			min = Math.min(min, ev.getEntry(i));
		}
		for(int i : series(ev.getDimension()))
		{
			double v = ev.getEntry(i);
			v = (v - min)/(max - min);
			ev.setEntry(i, v);
		}
		
		return new Point(ev);
	}
	
	public Point normalizedEigenvector(int index)
	{
		RealVector ev = v.getColumnVector(index);
		
		ev.unitize();
		
		return new Point(ev);
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
				point.set(j, signals.getEntry(i, j));
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
		RealVector out = v.preMultiply(in.getVector().subtract(mean));
		
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
		RealVector reconstruction = mean.copy();
		
		for(int i : series(simplified.size()))
		{
			double scale = simplified.get(i);
			reconstruction = reconstruction.add(v.getColumnVector(i).mapMultiply(scale));
		}
		// System.out.println(reconstruction);

		return new Point(reconstruction);
	}
}
