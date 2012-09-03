package org.lilian.data.real;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.Global;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

public class MOG extends MapModel<AffineMap> implements Generator<Point>, Density
{
	private static final long serialVersionUID = -3478114010368720453L;

	public MOG(AffineMap map, double weight)
	{
		super(map, weight);
	}

	public double density(Point p)
	{
		double density = 0.0; 
		
		for(int i : series(size()))
			density += probability(i) * mvn(i).density(p);
		
		return density;
	}
	
	public MVN mvn(int i)
	{
		return new MVN(get(i));
	}

	public List<Double> code(Point p)
	{
		List<Double> probs = new ArrayList<Double>(size());
		for(int i : series(size()))
			probs.add(probability(i) * mvn(i).density(p));
		
		normalize(probs);
		return probs;
	}
	
	public static void normalize(List<Double> in)
	{
		double sum = 0.0;
		for(double value : in)
			sum += value;
		
		if(sum == 0.0)
		{
			Global.log().info("Array ("+in+") sums to zero. Cannot normalize.");
			for(int i = 0; i < in.size(); i++)
				in.set(i, 1.0/in.size());
		} else
		{			
			for(int i = 0; i < in.size(); i++)
				in.set(i, in.get(i)/sum);
		}
	}
		
	@Override
	public Point generate()
	{
		// * This could be optimized
		MVN mvn = new MVN(this.random());
		
		return mvn.generate();
	}

	@Override
	public List<Point> generate(int n)
	{
		List<Point> points = new ArrayList<Point>(n);
		for(int i = 0; i < n; i++)
			points.add(generate());
		
		return points;
	}

	/**
	 * Creates an initial distribution of components over the data set. Each
	 * point is assigned a component at random with full responsibility.
	 * 
	 * @param size
	 * @param k
	 * @param var
	 * @return
	 */
	public static List<List<Double>> initial(int size, int k)
	{
		List<List<Double>> result = new ArrayList<List<Double>>(size);
		for(int i : series(size))
		{
			List<Double> code = new Point(k);
			code.set(Global.random.nextInt(k), (Double)1.0);
			result.add(code);
		}
		return result;
	}

	public List<List<Double>> expectation(List<Point> data)
	{
		List<List<Double>> result = new ArrayList<List<Double>>(data.size());
		for(Point point : data)
			result.add(code(point));
		return result;
	}
	
	public static MOG maximization(List<List<Double>> codes, List<Point> data)
	{	
		MOG mog = null;
		
		for(int k : Series.series(codes.get(0).size()))
		{
			double sum = 0.0;
			for(int i : series(data.size()))
				sum += codes.get(i).get(k);	
			
			int dim = data.get(0).dimensionality();
			
			// * Calculate the mean
			double[] mean = new double[dim];
			for(int i : series(data.size()))
				for(int j : series(dim))
					mean[j] += data.get(i).get(j) * (codes.get(i).get(k) / sum);
				
			// * Calculate the covariance
			RealMatrix cov = MatrixTools.identity(dim);
			
			for(int i : series(data.size()))
			{
				Point x = data.get(i);
				RealVector difference = x.getVector().subtract(mean);
				difference.mapMultiplyToSelf(codes.get(i).get(k));
				
				cov = cov.add(difference.outerProduct(difference));
			}
			
			cov = cov.scalarMultiply(1.0/sum);
			
			MVN mvn = new MVN(new Point(mean), cov);
			
			if(mog == null)
				mog = new MOG(mvn.map(), sum);
			else 
				mog.addMap(mvn.map(), sum);
		}

		return mog;
	}
}

