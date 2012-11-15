package org.lilian.data.real.fractal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.data.real.AbstractGenerator;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Generator;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Map;
import org.lilian.data.real.MapModel;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.classification.Classified;
import org.lilian.data.real.weighted.Weighted;
import org.lilian.data.real.weighted.WeightedLists;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.util.MatrixTools;

/**
 * Represents an Iterated Function System, a collection of weighted maps.
 * 
 * Iterating some initial images (probability distribution) under these maps
 * will generate a fractal (eg. the Sierpinski gasket).
 * 
 * NOTE: The original code contains a great deal more functionality, some of it
 * no longer used. This should be ported over as required. For now, the chaos
 * game generator is all we need.
 * 
 * @author Peter
 */
public class IFS<M extends Map & Parametrizable > 
	extends MapModel<M>
	implements Serializable, Parametrizable
{

	private static final long serialVersionUID = 8913224252890438800L;

	// * the number of steps to take intially when generating points
	public static final int INITIAL_STEPS = 50;
		
	public IFS(M map, double weight)
	{
		super(map, weight);
	}

	/**
	 * Returns a generator which generates points using the Chaos game.
	 * 
	 * This generator is quick and efficient, but always simulates the IFS
	 * at infinite depth.
	 * <p/>
	 * This generator does not provide an option for a basis distribution, as it 
	 * generates to infinite depth, so all initial distributions will result in 
	 * roughly the same behavior.
	 * 
	 * @return
	 */
	public Generator<Point> generator()
	{
		return new IFSGenerator();
	}

	/**
	 * Returns a generator which generates points to a fixed depth. It's a 
	 * little slower than the chaos game (about a factor of depth), but can draw
	 * points from low-depth models and draws properly iid points.
	 * 
	 * @return
	 */
	public Generator<Point> generator(int depth)
	{
		return new IFSFixedDepthGenerator(depth);
	}
	
	public Generator<Point> generator(int depth, Generator<Point> basis)
	{
		return new IFSFixedDepthGenerator(depth, basis);
	}
	
	private class IFSGenerator extends AbstractGenerator<Point>
	{
		Generator<Point> basis;
		Point p;

		public IFSGenerator()
		{
			this(new MVN(dimension));
		}
		
		public IFSGenerator(Generator<Point> basis)
		{
			this.basis = basis;
			p = this.basis.generate();
			
			for(int i = 0; i < INITIAL_STEPS; i++)
				p = random().map(p);
		}
		
		@Override
		public Point generate()
		{
			p = random().map(p);
			return p;
		}
	}
	
	private class IFSFixedDepthGenerator extends AbstractGenerator<Point>
	{
		Generator<Point> basis;
		int depth;
	
		public IFSFixedDepthGenerator(int depth, Generator<Point> basis)
		{
			this.basis = basis;
			this.depth = depth;
		}
		
		public IFSFixedDepthGenerator(int depth)
		{
			this(depth, new MVN(dimension()));
		}
		
		@Override
		public Point generate()
		{
			Point p = basis.generate();
			
			for(int i = 0; i < depth; i++)
				p = random().map(p);
			
			return p;
		}
	}
	

	/**
	 * Returns the composition of the maps indicated by the list of integers.
	 * If the code is (0, 2, 1, 2), the map returned behaves as 
	 * t_0(t_2(t_1(t_2(x))))).
	 * 
	 * @param code
	 * @return
	 */
	public Map compose(List<Integer> code)
	{
		Map m = null;
		
		for(int i : code)
			m = m == null? get(i) : m.compose(get(i));
			
		return m;
	}

	/**
	 * The number of parameters required to represent a MapModel.
	 */	
	public static <M extends Map & Parametrizable> int numParameters(int size, int perMap)
	{
		return MapModel.<M>numParameters(size, perMap);
	}
	
	public static <M extends Map & Parametrizable> Builder<IFS<M>> builder(int size, Builder<M> mapBuilder)
	{
		return new IFSBuilder<M>(size, mapBuilder);
	}
	
	protected static class IFSBuilder<M extends Map & Parametrizable> implements Builder<IFS<M>>
	{
		private static final long serialVersionUID = 6666857793341545956L;
		private Builder<M> mapBuilder;
		private int size;

		public IFSBuilder(int size, Builder<M> mapBuilder) 
		{
			this.size = size;
			this.mapBuilder = mapBuilder;
		}

		@Override
		public IFS<M> build(List<Double> parameters) 
		{
			return IFS.build(parameters, mapBuilder);
		}

		@Override
		public int numParameters() 
		{
			return IFS.<M>numParameters(size, mapBuilder.numParameters());
		}
	}
	
	public static <M extends Map & Parametrizable> IFS<M> build(List<Double> parameters, Builder<M> builder)
	{
		int n = builder.numParameters(), s = parameters.size();
				
		if( s % (n+1)  != 0)
			throw new IllegalArgumentException("Number of parameters ("+s+") should be divisible by the number of parameters per component ("+n+") plus one");
		
		IFS<M> model = null;
		for(int from = 0; from + n < s; from += n + 1)
		{
			int to = from + n;
			List<Double> mapParams = parameters.subList(from, to);
			double weight = Math.abs(parameters.get(to));
			
			M map = builder.build(mapParams);
			if(model == null)
				model = new IFS<M>(map, weight);
			else
				model.addMap(map, weight);
		}	
		
		return model;
	}
	
	public static <M extends AffineMap> IFS<AffineMap> makeAffine(IFS<M> in)
	{
		IFS<AffineMap> ifs = new IFS<AffineMap>(
				new AffineMap(in.get(0).getTransformation(), in.get(0).getTranslation()), 
				in.probability(0));
		for(int i = 1; i < in.size(); i++)
			ifs.addMap(
					new AffineMap(in.get(i).getTransformation(), in.get(i).getTranslation()),
					in.probability(i));
		return ifs;
	}
	
	public static <M extends AffineMap> double density(IFS<M> ifs, Point point, int depth)
	{
		return density(ifs, point, depth, new MVN(ifs.dimension()));
	}
	
	public static <M extends AffineMap> double density(IFS<M> ifs, Point point, int depth, MVN basis)
	{
		SearchResult result = search(ifs, point, depth, basis);
		
		return result.probSum();
	}
	
	/** 
	 * The endpoints of an IFS are the means of the distributions mapped to a 
	 * given depth. This method returns the endpoint of the distribution whose 
	 * code is assigned to this point by code(...).
	 * 
	 * @param ifs
	 * @param point
	 * @param depth
	 * @return
	 */
	public static Point endpoint(IFS<Similitude> ifs, Point point, int depth)
	{
		return endpoint(ifs, point, depth, new MVN(ifs.dimension()));
	}
	
	public static Point endpoint(IFS<Similitude> ifs, Point point, int depth,
			MVN basis)
	{
		SearchResult result = search(ifs, point, depth, basis);
		
		return result.mean();
	}
	
	public static Point endpoint(IFS<Similitude> ifs, List<Integer> code)
	{
		return endpoint(ifs, code, new Point(ifs.dimension()));
	}
	
	public static Point endpoint(IFS<Similitude> ifs, List<Integer> code, Point current)
	{
		if(code.isEmpty())
			return current;
		
		Point next = ifs.get(code.get(code.size() - 1)).map(current);
		
		return endpoint(ifs, code.subList(0, code.size() - 1), next);
	}
	
	/**
	 * Finds the transformation of the initial distribution that is most likely 
	 * to generate the given point. Transformations considered are all d length
	 * compositions of the base transformations of this IFS model.
	 * 
	 * @return null If all codes represent probability distributions which assigns
	 * a density to this point that is too low to be represented as a double
	 * 
	 */
	public static <M extends AffineMap> List<Integer> code(
			IFS<M> ifs, Point point, int depth)
	{
		return code(ifs, point, depth, new MVN(ifs.dimension()));
	}
		
	public static <M extends AffineMap> List<Integer> code(
			IFS<M> ifs, Point point, int depth, MVN basis)
	{		
		SearchResult res = search(
				ifs, point, depth, basis);
		return res.code();
	}
	
	public static <M extends AffineMap> SearchResult search(
			IFS<M> ifs, Point point, int depth)
	{
		return search(ifs, point, depth, new MVN(ifs.dimension()));
	}
	
	public static <M extends AffineMap> SearchResult search(
			IFS<M> ifs, Point point, int depth, MVN basis)
	{
		return search(ifs, point, depth, basis, 1);
	}
	
	/**
	 * Performs a walk over all endpoint distributions of the given IFS. It 
	 * collects information like codes and probability density for the given 
	 * point.
	 * 
	 * @param ifs
	 * @param point
	 * @param depth
	 * @param basis
	 * @param bufferLimit The SearchResult object returns a weighted list of 
	 * 	top codes. This parameter determines the maximum number of codes to 
	 *  return
	 * @return
	 */
	public static <M extends AffineMap> SearchResult search(
			IFS<M> ifs, Point point, int depth, MVN basis, int bufferLimit)
	{

		SearchResult res = search(
				ifs, point, depth, new SearchResultImpl(bufferLimit),
				new ArrayList<Integer>(depth), 0.0,
				MatrixTools.identity(ifs.dimension()), new ArrayRealVector(ifs.dimension()),
				basis);
		return res;
	}
	
	private static <M extends AffineMap> SearchResult search(
			IFS<M> ifs, Point point, int depth, 
			SearchResultImpl result, List<Integer> current, 
			double logPrior, 
			RealMatrix transform, RealVector translate,
			MVN basis)	
	{
		if(current.size() == depth)
		{
			double logProb;
			
			AffineMap map = new AffineMap(transform, translate);
			AffineMap mvnMap = basis.map();
			
			map = (AffineMap) map.compose(mvnMap);
			
			if(map.invertible())
			{
				MVN mvn = new MVN(map);
				logProb = logPrior + Math.log(mvn.density(point));
			} else { 
				logProb = Double.NEGATIVE_INFINITY;
			}
			
			double dist = dist(point, translate);
			
			result.show(logProb, new ArrayList<Integer>(current), dist, new Point(translate), logPrior);
			return result;
		}
		
		for(int i = 0; i < ifs.size(); i ++)
		{
			current.add(i);
			
			RealMatrix cr = transform.multiply(ifs.get(i).getTransformation());
			RealVector ct = transform.operate(ifs.get(i).getTranslation());
			ct = ct.add(translate);
			
			search(ifs, point, depth, result, current, 
					logPrior + Math.log(ifs.probability(i)), cr, ct, basis);
			
			current.remove(current.size() - 1);
		}
		
		return result;
		
	}
	
	/** 
	 * The result of a search through all endpoint distributions
	 * @author Peter
	 *
	 */
	public static interface SearchResult
	{
		public double logProb();
		
		public List<Integer> code();
				
		public Point mean();
		
		/**
		 * The sum of all the probability densities. This is an estimate for the
		 * probability density of the point.
		 * @return
		 */
		public double probSum();
		
		/**
		 * This value works as an approximation to the probability density. This 
		 * can be used if probSum() returns zero for all points under 
		 * investigation.
		 * 
		 *   
		 * Should only be used to compare different values (ie. the point with 
		 * the highest approximate value probably has highest density).
		 * @return
		 */
		
		public double approximation();
		
		/**
		 * A weighted list of the codes with maximum responsibility for the 
		 * given point. The weights are the responsibilities (likelihood of the 
		 * point under the endpoint distribution).  
		 * 
		 * Only codes with definite likelihoods are counted. There is no 
		 * fallback in distances (as with {@link code()}) 
		 * 
		 * @return
		 */
		public Weighted<List<Integer>> codes();

	}
	
	private static class SearchResultImpl implements SearchResult
	{
		private int bufferLimit = 1;
		private double logProb = Double.NEGATIVE_INFINITY;
		private double codeApprox = Double.NEGATIVE_INFINITY;
		
		private List<Integer> code = null;
		private List<Integer> codeFallback = null;
		
		private List<WCode> buffer = new ArrayList<WCode>();
		private List<WCode> bufferFallback = new ArrayList<WCode>();

		
		private Point mean = null;
		private Point meanFallback = null;
		
		private double probSum = 0.0;
		private double densityApprox = 0.0;
		private int daTotal = 0;
		
		public SearchResultImpl()
		{
			
		}
		
		public SearchResultImpl(int bufferLimit)
		{
			this.bufferLimit = bufferLimit;
		}
		
		public void show(double logProb, List<Integer> code, double distance, Point mean, double logPrior)
		{
			if(!Double.isNaN(logProb) && !Double.isInfinite(logProb))
			{
				probSum += Math.exp(logProb);
				
				if(Math.exp(logProb) > 0.0 && bufferLimit > 1)
				{
					buffer.add(new WCode(code, Math.exp(logProb)));
					Collections.sort(buffer);
					while(buffer.size() > bufferLimit)
						buffer.remove(buffer.size() - 1);
				}
			}
			
			if(!Double.isNaN(distance) && !Double.isInfinite(distance))
			{
				// densityApprox += Math.exp(-0.5 * distance * distance) * Math.exp(logPrior);
				double approx = Math.exp(-0.5 * distance * distance) * Math.exp(logPrior);
				densityApprox = Math.max(densityApprox, approx);
				
				if(approx > 0.0 && bufferLimit > 1)
				{
					bufferFallback.add(new WCode(code, approx));
					Collections.sort(bufferFallback);
					while(bufferFallback.size() > bufferLimit)
						bufferFallback.remove(bufferFallback.size() - 1);
				}				
			}			
			
			if(logProb > this.logProb && !Double.isNaN(logProb) && !Double.isInfinite(logProb))
			{	
				this.logProb = logProb;
				this.code = code;
				this.mean = mean;
			}
			
			double app = - distance;
			if(app > this.codeApprox && !Double.isNaN(distance) && !Double.isInfinite(distance))
			{
				this.codeApprox = app;
				this.codeFallback = code;
				this.meanFallback = mean;
			}
		}
		
		public double logProb()
		{
			return logProb;
		}
		
		public List<Integer> code()
		{
			return code != null ? code : codeFallback;
		}
		
		public Point mean()
		{
			return mean != null ? mean : meanFallback;
		}
		
		/**
		 * The sum of all the probability densities. This is an estimate for the
		 * probability density of the point.
		 * @return
		 */
		public double probSum()
		{
			return probSum;
		}

		@Override
		public double approximation()
		{
			// return Math.exp(0.5 * codeApprox);
			return densityApprox;
		}
		
		public Weighted<List<Integer>> codes()
		{
			List<WCode> buff = buffer.size() > 0 ? buffer : bufferFallback;
			
			Weighted<List<Integer>> codes = WeightedLists.empty();
			for(WCode wc : buff)
				codes.add(wc.code(), wc.weight());
			
			return codes;
		}
		
		private class WCode implements Comparable<WCode>
		{
			private List<Integer> code;
			private double weight;

			public WCode(List<Integer> code, double weight)
			{
				this.code = code;
				this.weight = weight;
			}

			@Override
			public int compareTo(WCode other)
			{
				return - Double.compare(weight, other.weight);
			}
			
			public List<Integer> code()
			{
				return code;
			}
			
			public double weight()
			{
				return weight;
			}
		}
	}	
	
	/**
	 * The squared distance between a point and a vector
	 * @param point
	 * @param vector
	 * @return
	 */
	private static double dist(Point point, RealVector vector)
	{
		double dist = 0.0;
		for(int i = 0 ; i < point.size(); i++)
		{
			double d = point.get(i) - vector.getEntry(i);
			dist += d*d;
		}
		return dist;
	}

	public static List<List<Integer>> codes(IFS<Similitude> ifs,
			List<Point> points, int depth)
	{
		List<List<Integer>> codes = new ArrayList<List<Integer>>(points.size());
		
		for(Point point : points)	
			codes.add(code(ifs, point, depth));
		
		return codes;
	}

	public static List<Point> endpoints(IFS<Similitude> ifs,
			List<List<Integer>> codes)
	{
		List<Point> points = new ArrayList<Point>(codes.size());
		
		for(List<Integer> code : codes)	
			points.add(endpoint(ifs, code));
		
		return points;
	}
}
