package org.lilian.data.real;

import java.util.Random;

/**
 * Represents a class which can generate random real valued multivariate points.
 * 
 * @author Peter
 */
public interface Generator
{
	public Point generate();	
	public Point generate(Random random);	
	
	public Point generate(int n);
	public Point generate(int n, Random random);
}
