package org.lilian.data.real.fractal.random;

import java.util.List;

import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
import org.lilian.search.Parametrizable;


/**
 * This interface represents a Random Probability Measure.
 * 
 * @author peter
 *
 */

public interface RIFS<M extends Map & Parametrizable> {
	
	/**
	 * Draws a set of random points from a single randomly drawn instance of 
	 * this RIFS. 
	 * 
	 * @return
	 */
	public List<Point> randomInstance(int n, int depth);

	/**
	 * Creates a random probability distribution from the given seed, and uses 
	 * it to draw a set of points. 
	 * 
	 * NOTE: the seed fully determines the probability distribution, but not the
	 * set of points drawn from it. This means that two calls to this method
	 * with the same seed parameter will result in different point sets drawn 
	 * from the same distribution.
	 * 
	 * The process generating the points will of course use Global.random for 
	 * its random numbers, so it can be made deterministic by resetting that
	 * object's seed before drawing.
	 *
	 */
	public List<Point> randomInstance(int n, int depth, long seed);	

	public List<Point> meanInstance(int n, int depth);	
}
