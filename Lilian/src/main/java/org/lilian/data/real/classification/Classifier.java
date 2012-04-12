package org.lilian.data.real.classification;

import java.util.List;

import org.lilian.data.real.Point;

public interface Classifier
{
	/**
	 * Classify a given point
	 * 
	 * @return
	 */
	public int classify(Point in);
	
	/**
	 * Returns a probability distribution over classes for the given point.
	 * 
	 * The values returned may also be densities, and so do not necessarily sum
	 * to one.
	 * @param point
	 * @return
	 */
	public List<Double> probabilities(Point point);	
	
	public int dimension();
	
	/**
	 * The number of classes.
	 * 
	 * @return
	 */
	public int size();
}
