package org.lilian.data.real;

import static org.lilian.util.Functions.choose;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.Global;
import org.lilian.search.Parameters;
import org.lilian.util.BitString;
import org.lilian.util.Functions;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

public class MOG extends MapModel<AffineMap> implements Generator<Point>, Density
{
	public static final double PERTURB_VAR = 0.03;
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
	
	public double logDensity(Point p)
	{
		double density = 0.0; 
		
		List<Double> terms = new ArrayList<Double>(this.size());
		for(int i : series(this.size()))
			terms.add(Math.log(probability(i)) + mvn(i).logDensity(p));
		
		return Functions.logSum(Math.E, terms);
	}
	
	public double logDensity(Collection<Point> points)
	{
		double ld = 0.0;
		for(Point p : points)
			ld += logDensity(p);
		
		return ld;
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
		int numComponents = codes.get(0).size();
				
		List<MVN> components = new ArrayList<MVN>(numComponents);
		List<Double> priors = new ArrayList<Double>(numComponents);
		
		int dim = data.get(0).dimensionality();
		
		for(int k : series(numComponents))
		{
			components.add(null);
			priors.add(null);
		}
		
		for(int k : series(numComponents))
		{
			double sum = 0.0;
			for(int i : series(data.size()))
				sum += codes.get(i).get(k);	
			
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
			
			if(containsNaN(cov) )
			{
				System.out.println(cov);
				continue;
			}
//			if(! MatrixTools.isInvertible(cov))
//				continue;
			
			MVN mvn = new MVN(new Point(mean), cov);
			
			components.set(k, mvn);
			priors.set(k, sum);
		}
		
		// * This shouldn't happen
		if(allNull(components))
			throw new IllegalStateException("All components bad.");
		
		// * If we have bad components (either because they received no points, 
		//   or are non-invertible), we take all the bad components, assign them
		//   to a good one, and split the good component into slightly perturbed
		//   copies of itself.
		if(hasNull(components))
		{
			// * collect the good components
			List<Integer> good = new ArrayList<Integer>(numComponents),
			              bad = new ArrayList<Integer>(numComponents);
			
			
			for(int k : series(numComponents))
				if(components.get(k) != null)
					good.add(k);
				else
					bad.add(k);
			
			Global.log().info("Bad components: " + bad + ", good:" + good);
			
			// * Assign each bad component to a good one
			Map<Integer, List<Integer>> map = new LinkedHashMap<Integer, List<Integer>>();
			for(int k : series(numComponents))
				if(components.get(k) == null)
				{
					int rGood = choose(good);
					if(! map.containsKey(rGood))
					{
						map.put(rGood, new ArrayList<Integer>(numComponents));
						map.get(rGood).add(rGood);
					}
					
					map.get(rGood).add(k);
				}
			
			for(List<Integer> comps : map.values())
			{
				MVN initialComponent = components.get(comps.get(0));
				double initialPrior = priors.get(comps.get(0));
				
				for(int i : comps)
				{
					// * Create a new, perturbed component 
					MVN perturbed = Parameters.perturb(initialComponent, MVN.builder(dim), PERTURB_VAR);
					double prior = initialPrior / numComponents;
					
					components.set(i, perturbed);
					priors.set(i, prior);
				}
			}
		}
		
		MOG mog = null;
		for(int k : series(numComponents))
			if(mog == null)
				mog = new MOG(components.get(k).map(), priors.get(k));
			else
				mog.addMap(components.get(k).map(), priors.get(k));
		
		return mog;
	}
	
	private static boolean allNull(Collection<?> coll)
	{
		for(Object o : coll)
			if(o != null)
				return false;
		
		return true;
	}
	
	private static boolean hasNull(Collection<?> coll)
	{
		for(Object o : coll)
			if(o == null)
				return true;
		
		return false;
	}
	
	
	private static boolean containsNaN(RealMatrix cov)
	{
		for(int i : series(cov.getRowDimension()))
			for(int j : series(cov.getColumnDimension()))
				if(Double.isNaN(cov.getEntry(i, j)))
					return true;
		
		return false;
	}

	public String toString()
	{
		String out = "[";
		
		for(int i : Series.series(this.size()))
		{
			if(i != 0)
				out += ", ";
			
			out += "prior: "+probability(i) + " ";
			out += "mean: "+mvn(i).mean()+" ";
			out += "cov: "+mvn(i).covariance()+" ";
		}
		return out + "]";
	}
	
}

