package org.lilian.data.real;

import java.io.Serializable;
import java.util.*;

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
}
