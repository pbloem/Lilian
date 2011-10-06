package org.lilian.data.real;

/**
 * Represents a probability distribution over real-valued verctors
 * 
 * @author peter
 *
 */
public interface Density
{
	public double density(Point p);
}
