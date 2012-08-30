package org.lilian.data.dimension;

import java.util.List;

import org.lilian.Global;
import org.lilian.data.real.AbstractGenerator;
import org.lilian.data.real.Generator;

/**
 * A combination of two Takens distributions. One over[0, x0) and one over [x0, x1)
 * 
 * The distributions are scaled so that the point x_0 has equal probability 
 * under both (ie. the 
 *  
 * @author Peter
 *
 */
public class BiTakens extends AbstractGenerator<Double>
{

	private Takens left;
	private MultiTakens right;
	double leftPrior, rightPrior;
	private double split, max;
	
	public BiTakens(double split, double max, double dimLeft, double dimRight)
	{
		this.split = split;
		this.max = max;
		
		left = new Takens(dimLeft, split);
		right = new MultiTakens(dimRight, split, max);
		
		// * Calculate the priors to make the total smooth
		leftPrior = right.p(split)/(left.p(split) + right.p(split));
		rightPrior = 1 - leftPrior;
		
		Global.log().info("split " + left.p(split) + " " + right.p(split));
	}
	
	@Override
	public Double generate()
	{
		if(Global.random.nextDouble() < leftPrior)
			return left.generate();
		
		return right.generate();
	}

	public double p(double x)
	{
		if(0.0 <= x && x < split)
			return leftPrior * left.p(x);
		
		if(split <= x && x < max)
			return rightPrior * right.p(x);
		
		return 0.0;
	}
	
	public double cdf(double x)
	{
		if(x < 0.0)
			return 0.0;
		
		if(0.0 <= x && x < split)
			return leftPrior * left.cdf(x);
		
		if(split <= x && x < max)
			return leftPrior + rightPrior * right.cdf(x);
		
		if(x >= max)
			return 1.0;
		
		return Double.NaN;
	}

	public double leftPrior()
	{
		return leftPrior;
	}

	public double rightPrior()
	{
		return rightPrior;
	}

	public Takens getleft()
	{
		return left;
	}

	public MultiTakens right()
	{
		return right;
	}

	
}
