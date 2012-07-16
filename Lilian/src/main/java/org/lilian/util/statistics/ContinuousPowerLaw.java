package org.lilian.util.statistics;

import java.util.List;

import org.lilian.Global;
import org.lilian.data.real.AbstractGenerator;
import org.lilian.data.real.Generator;

public class ContinuousPowerLaw extends AbstractGenerator<Double>
{
	private double cutoff, exponent;
	
	public ContinuousPowerLaw(double min, double exponent)
	{
		this.cutoff = min;
		this.exponent = exponent;
		
	}

	@Override
	public Double generate()
	{
		double source = Global.random.nextDouble();
		
		double p = - 1.0 / (exponent - 1.0);
		return cutoff * Math.pow(1.0 - source, p);
	}

}
