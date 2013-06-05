package org.lilian.data.real.fractal.random;


import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lilian.Global;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
import org.lilian.data.real.fractal.IFS;
import org.lilian.search.Parametrizable;
import org.lilian.util.Functions;
import org.lilian.util.Series;

/**
 * Provides basic implementation for most of the method of the RandomIFSModel 
 * interface. 
 * 
 * Overriding classes need only implement the draw(Random) method.
 * 
 * 
 * @author peter
 *
 */
public abstract class AbstractRIFS<M extends Map & Parametrizable> implements RIFS<M>
{
	protected int dim;
	protected MVN basis;
	
	public AbstractRIFS(int dim)
	{
		this.dim = dim;
		basis = new MVN(dim);
	}

	/**
	 * @param n the total number of points. The final set will have 
	 * 		approximately this number of points
	 */
	public List<Point> randomInstance(int n, int depth) {
		return randomInstance(n, depth, Global.random.nextLong());
	}
	
	public List<List<Point>> randomInstances(int n, int nSets, int depth) 
	{
		List<List<Point>> result = new ArrayList<List<Point>>(nSets);
		for(int i : series(nSets))
			result.add(randomInstance(n, depth, Global.random.nextLong()));
		
		return result;
	}


	/**
	 * Returns points drawn from an instance specified by a seed. The seed 
	 * determines a particular random instance, but not the points drawn from it.
	 * 
	 * Calling this function twice with the same seed will result in two different 
	 * sets of points drawn from the same probability distribution.
	 */
	public List<Point> randomInstance(int n, int depth, long seed) {
		Random rand = new Random(seed);
		
		return randomInstance(n, depth, rand);
	}
	
	private List<Point> randomInstance(double n, int depth, Random random)
	{
		if(depth <= 0)
			return basis.generate((int)Functions.probRound(n));
		
		IFS<M> model = random(random);
		List<List<Point>> instances = new ArrayList<List<Point>>();
		
		for(int i = 0; i < model.size(); i++)
			instances.add(randomInstance(n * model.probability(i), depth-1, random));
		
		List<Point> instance = new ArrayList<Point>();
		for(int i = 0; i < model.size(); i++)
			for(Point point : instances.get(i))
				instance.add(model.get(i).map(point));
		
		return instance;
	}
	
	public List<Point> meanInstance(int n, int depth)
	{
		List<Point> points = new ArrayList<Point>();
		
		for(int i = 0; i < n; i++)
			points.add(meanPoint(depth));
		
		return points;
	}
	
	private Point meanPoint(int depth)
	{
		if(depth <= 0)
			return basis.generate();
		
		IFS<M> model = random();

		M map = model.random();
		
		return map.map(meanPoint(depth - 1));
	}	
	
	public IFS<M> random()
	{
		return random(Global.random);
	}
	
	public abstract IFS<M> random(Random rand);
	
	public int dimension() 
	{
		return dim;
	}
}
