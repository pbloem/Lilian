package org.lilian.data.real;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;
import org.lilian.Global;
import org.lilian.data.real.Maps.FindSimilitudeResult;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.search.evo.Target;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

/**
 * A similitude (or similarity transform) consists of rotation, a uniform 
 * scaling and a translation. 
 * 
 * NOTE: In the parameter representation of a similitude the angles are normalized so 
 * that 1.0 represents an angle of 360 degrees (or 2 pi radians). 
 * 
 * @author Peter
 *
 */
public class Similitude extends AffineMap
{
	private static final long serialVersionUID = 3717926722178382627L;

	protected int dimension;
	
	protected double scalar;
	protected List<Double> angles; // in radians
	
	/**
	 * NOTE: the angles in this parameter vector are in radians div by 2 pi 
	 * (that way all parameters are roughly the same scale)
	 * 
	 * @param parameters
	 */
	public Similitude(List<Double> parameters)
	{
		super();
		int s = parameters.size();
		double ddim =  ( -1.0 + Math.sqrt(-7.0 + s * 8.0))/2.0;
		
		dimension = (int)Math.round(ddim);
		
		if(1 + dimension + (dimension*dimension - dimension)/2  != s)
			throw new IllegalArgumentException("Number of parameters ("+s+") should satisfy 1 + d + (d^2 - d)/2 (d="+ddim+", " + dimension + ")");

		init(
				parameters.get(0), 
				parameters.subList(1, dimension+1), 
				toRad(parameters.subList(dimension + 1, parameters.size())));		
	}	
	
	/**
	 * 
	 * 
	 * @param scalar
	 * @param translation
	 * @param angles In radians
	 */
	public Similitude(double scalar, List<Double> translation, List<Double> angles)
	{
		int dim = translation.size();
		int a = angles.size();
		
		if( (dim*dim - dim)/2 != a)
			throw new IllegalArgumentException("Wrong number of angles ("+a+") for the given dimension (size of translation vector = "+dim+"). The number of angles should be (d*d - d) / 2");
				
		init(scalar, translation, angles);
	}
	
	/**
	 * 
	 * @param scalar
	 * @param translation
	 * @param angles in radians
	 */
	private void init(double scalar, List<Double> translation, List<Double> angles)
	{
		dimension = translation.size();
		
		this.scalar	= scalar;
		this.translation = MatrixTools.toVector(translation);
		
		this.angles = new ArrayList<Double>(angles);
		
		RealMatrix rotation = Rotation.toRotationMatrix(angles);
		this.transformation = rotation.scalarMultiply(scalar);
	}

	
	/**
	 * Returns a representation of this similitude as a list of double values.
	 * 
	 * @param mode
	 * @return
	 */
	public List<Double> parameters()
	{
		int dim = dimension();
		int size = 1 + dim + (dim * dim - dim)/2;
		List<Double> result = new ArrayList<Double>(size);
		
		result.add(scalar);
		
		for(double d : translation.getData())
			result.add(d);
			
		
		result.addAll(fromRad(angles));
			
		return result;
	}	
	
	/**
	 * Scaling ratio of the map, can be negative.
	 * 
	 * @return
	 */
	public double scalar()
	{
		return scalar;	
	}
	
	/**
	 * Returns the angles that define the rotation part of this similitude 
	 * (in radians)
	 * 
	 * @return
	 */
	public List<Double> angles() 
	{
		return Collections.unmodifiableList(angles);
	}
	
	public List<Double> translation() 
	{
		return new Point(translation);
	}

	@Override
	public Point map(Point in)
	{
		RealVector result = transformation.operate(in.getVector());
		
		return new Point(result.add(translation));
	}

	public static int numParameters(int dimension)
	{
		return 1 + dimension + (dimension * dimension - dimension)/2;
	}
	
	public Map compose(Map other)
	{
		
		if(other instanceof Similitude)
		{
			// * return t(o(x)) = <s_ts_o, R_tR_o, s_t * R_t * t_o + t_t> 
			Similitude sim = (Similitude) other;
			
			double newScalar = this.scalar * sim.scalar;

			List<Double> newAngles = new ArrayList<Double>(angles.size());
			for(int i = 0; i < angles.size(); i++)
				newAngles.add(this.angles.get(i) + sim.angles.get(i));
			
			// * note that this.transformation contains both the scaling an the rotation
			RealVector newTrans = this.transformation.operate(sim.translation).add(this.translation);
			
			return new Similitude(newScalar, new Point(newTrans), newAngles);
		}
		
		return super.compose(other);
	}

	@Override
	public boolean invertible()
	{
		return scalar != 0;
	}
	
	@Override
	public Similitude inverse()
	{
		// inv(s, R, t) = <1/s, R', -1/s * R'>
		
		double invScalar = 1.0 / scalar;
		
		List<Double> invAngles = new ArrayList<Double>(angles.size());
		for(int i = 0 ; i < angles.size(); i++)
			invAngles.add(-angles.get(i));
		RealMatrix invRotScale = Rotation.toRotationMatrix(invAngles).scalarMultiply(-invScalar);
		RealVector invTranslation = invRotScale.operate(translation);

		return new Similitude(invScalar, new Point(invTranslation), invAngles);
	}

	@Override
	public int dimension()
	{
		return dimension;
	}
	
	public static Builder<Similitude> similitudeBuilder(int dimension)
	{
		return new SBuilder(dimension);
	}
	
	public static Similitude identity(int dim)
	{
		return new Similitude(1.0, new Point(dim), new Point((dim*dim - dim)/2));
	}
	
	private static class SBuilder implements Builder<Similitude>
	{
		private int dimension;

		public SBuilder(int dimension) 
		{
			this.dimension = dimension;
		}

		@Override
		public Similitude build(List<Double> parameters) 
		{
			return new Similitude(parameters);
		}

		@Override
		public int numParameters() 
		{
			return Similitude.numParameters(dimension);
		}
	}

	@Override
	public String toString()
	{
		return "Similitude [s" + scalar + ", a=" + angles
				+ ", t=" + translation + "]";
	}
	
	/**
	 * Takes a list of angles in rad rep, and converts to 0-1 rep
	 * @param in
	 * @return
	 */
	public List<Double> fromRad(List<Double> in)
	{
		List<Double> out = new ArrayList<Double>(in.size());
		
		for(double angle : in)
			out.add(angle / (2.0 * Math.PI));
		
		return out;
	}
	
	public List<Double> toRad(List<Double> in)
	{
		List<Double> out = new ArrayList<Double>(in.size());
		
		for(double angle : in)
			out.add(angle * (2.0 * Math.PI));
		
		return out;
	}
	
	public static Similitude find(List<Point> xSet, List<Point> ySet)
	{
		if(xSet.size() == 0)
			return null;
			
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
		
		List<Double> angles = Rotation.findAngles(r);
		
		return new Similitude(c, new Point(t), angles);
	}
	

	public static Similitude find(List<Point> xSet, List<Point> ySet, List<Double> weights)
	{
		return find(xSet, ySet, weights, new LinkedHashMap<String, Double>());
	}
	
	public static Similitude find(List<Point> xSet, List<Point> ySet, List<Double> weights, HashMap<String, Double> extra)
	{
		List<Point> xs = new ArrayList<Point>(xSet.size());
		List<Point> ys = new ArrayList<Point>(ySet.size());
		List<Double> ws = new ArrayList<Double>(weights.size());
		
		for(int i : series(xSet.size()))
			if(weights.get(i) > 0.0)
			{
				xs.add(xSet.get(i));
				ys.add(ySet.get(i));
				ws.add(weights.get(i));
			}
		
		xSet = xs;
		ySet = ys;
		weights = ws;
		
		if(xSet.size() == 0)
			return null;
			
		int dim = xSet.get(0).dimensionality();
		int size = xSet.size();
		
		double weightSum = 0.0;
		for(double weight : weights)
			weightSum += weight;
		
		// * Calculate the means
		//  (optimize by doing in place summation manually on a double[]
		RealVector xMean = new ArrayRealVector(dim);
		for(int i : series(xSet.size()))
		{
			Point x = xSet.get(i);
			xMean = xMean.add(x.getVector().mapMultiply(weights.get(i)));
		}
		xMean.mapMultiplyToSelf(1.0/weightSum);
		
		RealVector yMean = new ArrayRealVector(dim);
		for(int i : series(ySet.size()))
		{
			Point y = ySet.get(i);
			yMean = yMean.add(y.getVector().mapMultiply(weights.get(i)));
		}
		yMean.mapMultiplyToSelf(1.0/weightSum);
				
		// * Calculate the standard deviations		
		double xStdDev = 0.0;
		for(int i : series(xSet.size()))
		{
			Point x = xSet.get(i);
			RealVector difference = x.getVector().subtract(xMean);
			double norm = difference.getNorm();
			
			xStdDev += norm * norm * weights.get(i);
		}	
		xStdDev = xStdDev / weightSum;
		
		double yStdDev = 0.0;
		for(int i : series(ySet.size()))
		{
			Point y = ySet.get(i);
			RealVector difference = y.getVector().subtract(yMean);
			double norm = difference.getNorm();
			
			yStdDev += norm * norm * weights.get(i);
		}	
		yStdDev = yStdDev / weightSum;
	
		// * Calculate the covariance martix
	
		RealVector xDifference, yDifference;
		
		RealMatrix covariance = new Array2DRowRealMatrix(dim, dim);
		
		for(int i = 0; i < size;i++)
		{
			xDifference = xSet.get(i).getVector().subtract(xMean);
			yDifference = ySet.get(i).getVector().subtract(yMean);
			
			
			RealMatrix term = yDifference.outerProduct(xDifference).scalarMultiply(weights.get(i)); 
			covariance =  covariance.add(term);
		}
		
		covariance = covariance.scalarMultiply(1.0/weightSum); // I think we can leave this one out ....
		
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
		trace += detU*detV < 0 ? - values.getEntry(dim-1) : values.getEntry(dim-1);
		
		double c = (trace / xStdDev);
		
		// * Calculate t
		RealVector t = yMean.subtract(r.scalarMultiply(c).operate(xMean));
		
		double e = yStdDev - (trace*trace) / xStdDev;
		double stdDev = e / (weights.size() * dim);
		
		extra.put("error", e);
		extra.put("std dev", stdDev);
		
		List<Double> angles = Rotation.findAngles(r);
		
		return new Similitude(c, new Point(t), angles);
	}
	
	public static Similitude find(List<Point> from, List<Point> to, RealMatrix cor)
	{
		return find(from, to, cor, new HashMap<String, Double>());
	
	}

	/**
	 * Same as above, but in this case, the points in x and y do not correspond. 
	 * Instead the matrix cor gives a weight to each pair.
	 * 
	 * Each row in cor contains the values for a given 'to' point, each column for a given from point.
	 * @param from
	 * @param to
	 * @param cor
	 * @param extra
	 * @return
	 */
	public static Similitude find(List<Point> from, List<Point> to, RealMatrix cor, HashMap<String, Double> extra)
	{
		// TODO Implement as a series of matrix multiplications
		
		if(from.size() == 0)
			return null;
			
		int dim = from.get(0).dimensionality();
		int size = from.size();
		
		double weightSum = 0.0;
		for(int row : series(cor.getRowDimension()))
			for(int column : series(cor.getColumnDimension()))
				weightSum += cor.getEntry(row, column);
		
		RealVector fromWeights = cor.preMultiply(new ArrayRealVector(cor.getRowDimension(), 1.0));
		RealVector toWeights = cor.operate(new ArrayRealVector(cor.getColumnDimension(), 1.0));
		
		// * Calculate the means
		//  (optimize by doing in place summation manually on a double[]
		RealVector fromMean = new ArrayRealVector(dim);
		for(int i : series(from.size()))
		{
			Point f = from.get(i);
			fromMean = fromMean.add(f.getVector().mapMultiply(fromWeights.getEntry(i)));
		}
		fromMean.mapMultiplyToSelf(1.0/weightSum);
		
		RealVector toMean = new ArrayRealVector(dim);
		for(int i : series(to.size()))
		{
			Point t = to.get(i);
			toMean = toMean.add(t.getVector().mapMultiply(toWeights.getEntry(i)));
		}
		toMean.mapMultiplyToSelf(1.0/weightSum);
				
		// * Calculate the standard deviations		
		double fromStdDev = 0.0;
		for(int i : series(from.size()))
		{
			Point x = from.get(i);
			RealVector difference = x.getVector().subtract(fromMean);
			double norm = difference.getNorm();
			
			fromStdDev += norm * norm * fromWeights.getEntry(i);
		}	
		fromStdDev = fromStdDev / weightSum;
		
		double toStdDev = 0.0;
		for(int i : series(to.size()))
		{
			Point y = to.get(i);
			RealVector difference = y.getVector().subtract(toMean);
			double norm = difference.getNorm();
			
			toStdDev += norm * norm * toWeights.getEntry(i);
		}	
		toStdDev = toStdDev / weightSum;
	
		// * Calculate the covariance martix
	
		RealVector fDifference, tDifference;
		
		RealMatrix covariance = new Array2DRowRealMatrix(dim, dim);
		
		for(int f : series(from.size()))
			for(int t : series(to.size()))
			{
				fDifference = from.get(f).getVector().subtract(fromMean);
				tDifference = to.get(t).getVector().subtract(toMean);
				
				
				RealMatrix term = tDifference.outerProduct(fDifference).scalarMultiply(cor.getEntry(t, f)); 
				covariance =  covariance.add(term);
			}
		
		covariance = covariance.scalarMultiply(1.0/weightSum); // I think we can leave this one out ....
		
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
				{
					Global.log().warning("Could not find SVD decomposition for matrix: " + covariance);
					
					return null;
				}
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
		trace += detU*detV < 0 ? - values.getEntry(dim-1) : values.getEntry(dim-1);
		
		double c = (trace / fromStdDev);
		
		// * Calculate t
		RealVector t = toMean.subtract(r.scalarMultiply(c).operate(fromMean));
		
		double e = toStdDev - (trace*trace) / fromStdDev;
		double stdDev = e / (weightSum * dim);
		
		extra.put("error", e);
		extra.put("std dev", stdDev);
		
		List<Double> angles = Rotation.findAngles(r);
		
		return new Similitude(c, new Point(t), angles);
	}	
	
}
