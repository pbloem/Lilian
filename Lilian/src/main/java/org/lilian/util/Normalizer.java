package org.lilian.util;

import java.util.Collection;

import org.lilian.models.SequenceModel;

public class Normalizer
{
	double max = Double.MIN_VALUE, min= Double.MAX_VALUE; 

	public void add(Double token)
	{
		min = Math.min(min, token);
		max = Math.max(max, token);		
	}
	
	public void add(Collection<Double> collection)
	{
		for(Double d : collection)
			add(d);
	}
	
	public double normalize(double in)
	{
		double range = max - min;
		
		return (in - min) /range; 
	}
	
	public static Normalizer normalizer(Collection<Double> collection)
	{
		Normalizer normalizer = new Normalizer();
		normalizer.add(collection);
		return normalizer;
	}
}

