package org.lilian.data.real;

import java.util.List;
import java.util.Random;

/**
 * Represents a class which can generate random real valued multivariate points.
 * 
 * @author Peter
 */
public interface Generator<P>
{
	public Point generate();	
	
	public List<P> generate(int n);
}
