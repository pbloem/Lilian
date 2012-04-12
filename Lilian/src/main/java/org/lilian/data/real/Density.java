package org.lilian.data.real;

import java.io.Serializable;

/**
 * Represents a probability distribution over real-valued vectors
 * 
 * @author peter
 *
 */
public interface Density extends Serializable
{
	public double density(Point p);
	
	public int dimension();
}
