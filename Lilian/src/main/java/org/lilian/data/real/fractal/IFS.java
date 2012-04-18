package org.lilian.data.real.fractal;

import java.io.Serializable;
import java.util.ArrayList;
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
	
	// * The density model used to build the IFs model from   
	protected MVN basis;
	
	public IFS(M map, double weight)
	{
		super(map, weight);
		
		basis = new MVN(map.dimension());
	}

	/**
	 * Returns a generator which generates points using the Chaos game.
	 * 
	 * This generator is quick and efficient, but always simulates the IFS
	 * at infinite depth.
	 * 
	 * @return
	 */
	public Generator<Point> generator()
	{
		return new IFSGenerator();
	}
		
	private class IFSGenerator extends AbstractGenerator<Point>
	{
		Point p = basis.generate();

		public IFSGenerator()
		{
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
		return MapModel.numParameters(size, perMap);
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
			return IFS.numParameters(size, mapBuilder.numParameters());
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
		Result res = code(
				ifs, point, depth, new Result(),
				new ArrayList<Integer>(depth), 0.0, 
				MatrixTools.identity(ifs.dimension()), new ArrayRealVector(ifs.dimension()));
		return res.code();
	}
	
	private static <M extends AffineMap> Result code(
			IFS<M> ifs, Point point, int depth, 
			Result result, List<Integer> current, 
			double prior, RealMatrix transform, RealVector translate)	
	{
		if(current.size() == depth)
		{
			double prob;
			
			AffineMap map = new AffineMap(transform, translate);
			if(map.invertible())
			{
				MVN mvn = new MVN(new AffineMap(transform, translate));
				prob = prior + Math.log(mvn.density(point));
			} else prob = 
					Double.NEGATIVE_INFINITY;
			
			double dist = dist(point, translate);
			
			result.show(prob, new ArrayList<Integer>(current), dist);
			return result;
		}
		
		for(int i = 0; i < ifs.size(); i ++)
		{
			current.add(i);
			
			RealMatrix cr = transform.multiply(ifs.get(i).getTransformation());
			RealVector ct = transform.operate(ifs.get(i).getTranslation());
			ct = ct.add(translate);
			
			code(ifs, point, depth, result, current, 
					prior + Math.log(ifs.probability(i)), cr, ct);
			
			current.remove(current.size() - 1);
		}
		
		return result;
		
	}
	
	private static class Result {
		private double prob = Double.NEGATIVE_INFINITY;
		private double distance = Double.POSITIVE_INFINITY;
		private List<Integer> code = null;
		private List<Integer> codeFallback = null;
		
		public void show(double prob, List<Integer> code, double distance)
		{
			if(prob > this.prob && !Double.isNaN(prob) && !Double.isInfinite(prob))
			{	
				this.prob = prob;
				this.code = code;
			}
			
			if(distance < this.distance && !Double.isNaN(distance) && !Double.isInfinite(distance))
			{
				this.distance = distance;
				this.codeFallback = code;
			}
		}
		
		public double prob()
		{
			return prob;
		}
		
		public List<Integer> code()
		{
			return code != null ? code : codeFallback;
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
}
