package org.lilian.util.statistics;

import static org.lilian.util.statistics.Functions.zeta;

import java.util.List;

import org.lilian.Global;
import org.lilian.data.real.AbstractGenerator;
import org.lilian.data.real.Generator;

public class DiscretePowerLaw extends AbstractGenerator<Integer>
{
	private double xmin, exponent, pdenum;
	
	public DiscretePowerLaw(double min, double exponent)
	{
		this.xmin = min;
		this.exponent = exponent;
		
		pdenum = zeta(exponent, xmin);
	}

	@Override
	public Integer generate()
	{
		double source = Global.random.nextDouble();

		return pInv(1 - source);
	}

	/**
	 * Returns a value x such that P(x) = q
	 * 
	 * @param q
	 * @return
	 */
	public int pInv(double q)
	{
		double x1, x2 = xmin;

		do 
		{
			x1 = x2;
			x2 = 2.0 * x1;
		} while (p(x2) < q);
		
		return (int) binarySearch(x1, x2, q);
	}
	
	private double binarySearch(double lower, double upper, double target)
	{
		// * stop recursion when the interval fals within a single integer
		if(Math.floor(lower) == Math.floor(upper))
			return lower;
		
		double range = upper - lower;	
		double midpoint = range / 2.0 + lower;

		double pm = p(midpoint);
		
		if(pm > target)
			return binarySearch(lower, midpoint, target);
		else
			return binarySearch(midpoint, upper, target);
	}
	
	/**
	 * The cumulative distribution function.
	 * 
	 * @param x
	 * @return
	 */
	public double p(double x)
	{
		return 1.0 - zeta(exponent, x) / pdenum;
		
	}
}
