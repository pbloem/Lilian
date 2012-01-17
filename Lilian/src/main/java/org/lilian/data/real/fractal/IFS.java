package org.lilian.data.real.fractal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.lilian.data.real.Generator;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Map;
import org.lilian.data.real.MapModel;
import org.lilian.data.real.Point;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;

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
		
	private class IFSGenerator implements Generator<Point>
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

		@Override
		public List<Point> generate(int n)
		{
			List<Point> points = new ArrayList<Point>(n);
			for(int i = 0; i < n; i++)
				points.add(generate());
			
			return points;
		}
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
}
