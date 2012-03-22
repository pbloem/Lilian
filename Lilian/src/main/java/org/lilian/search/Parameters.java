package org.lilian.search;

import java.util.ArrayList;
import java.util.List;

import org.lilian.Global;

/**
 * Static helper functions for parametrizable objects
 * @author Peter
 *
 */
public class Parameters
{

	public static List<Double> random(int size, double var)
	{
		List<Double> r = new ArrayList<Double>(size);
		
		for(int i = 0; i < size; i++)
			r.add(Global.random.nextGaussian() * var);
		
		return r;		
	}
	
	public static <M extends Parametrizable> M perturb(
			M in, Builder<M> builder, double variance)
	{
		List<Double> params = new ArrayList<Double>(in.parameters());
		
		for(int i = 0; i < params.size(); i++)
			params.set(i,  params.get(i) + Global.random.nextGaussian() * variance);
		
		return builder.build(params);
	}
}
