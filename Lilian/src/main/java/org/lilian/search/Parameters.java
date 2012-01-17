package org.lilian.search;

import java.util.ArrayList;
import java.util.List;

import org.lilian.Global;

public class Parameters
{

	public static List<Double> random(int size, double var)
	{
		List<Double> r = new ArrayList<Double>(size);
		
		for(int i = 0; i < size; i++)
			r.add(Global.random.nextGaussian() * var);
		
		return r;		
	}
}
