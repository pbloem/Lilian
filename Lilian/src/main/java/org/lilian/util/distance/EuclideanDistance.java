package org.lilian.util.distance;

import java.util.List;

public class EuclideanDistance implements Distance<List<Double>>
{
	private static final long serialVersionUID = 1L;

	@Override
	public double distance(List<Double> a, List<Double> b)
	{
		if(a.size() != b.size())
			throw new IllegalArgumentException("Input sizes (a.size()="+a.size()+", b.size()="+b.size()+") must be equal"); 
		
		double distance = 0.0;
		for(int i = 0; i < a.size(); i++)
		{
			double d = a.get(i) - b.get(i);
			distance += d * d;
		}
		
		return Math.sqrt(distance);
	}
}
