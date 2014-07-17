package org.lilian.data.real;

import java.util.Collection;

import org.lilian.util.Functions;

public abstract class AbstractDensity implements Density
{

	@Override
	public abstract double density(Point p);

	public double logDensity(Collection<Point> points)
	{
		double ld = 0.0;
		for(Point p : points)
			ld += Functions.log2(density(p));
		
		return ld;
	}
	
	@Override
	public abstract int dimension();

}
