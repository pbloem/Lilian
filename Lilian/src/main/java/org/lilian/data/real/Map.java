package org.lilian.data.real;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a function from a Euclidean space onto itself.
 * 
 * @author Peter
 *
 */
public interface Map extends Serializable {
	
	public Point map(Point in);
	
	/**
	 * Returns whether this map has an inverse map 
	 */
	public boolean invertible();
	
	/**
	 * Returns the inverse of this map
	 * 
	 * @throws RuntimeException if !isInvertible() 
	 */
	public Map inverse();
	
	public int dimension();
	
	public List<Point> map(List<Point> points);
	
	/**
	 * Returns a map whose function is equal to applying first this map, and then
	 * the argument to the result of the first mapping.
	 * @param other
	 * @return
	 */
	public Map compose(Map other);
}
