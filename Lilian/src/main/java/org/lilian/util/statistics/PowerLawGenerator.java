package org.lilian.util.statistics;

import java.util.List;

import org.lilian.Global;
import org.lilian.data.real.AbstractGenerator;
import org.lilian.data.real.Generator;

public class PowerLawGenerator extends AbstractGenerator<Double>
{
	private double cutoff, exponent;
	
	public PowerLawGenerator(double cutoff, double exponent)
	{
		super();
		this.cutoff = cutoff;
		this.exponent = exponent;
	}

	@Override
	public Double generate()
	{
		double source = Global.random.nextDouble();
		return null;
	}

}
