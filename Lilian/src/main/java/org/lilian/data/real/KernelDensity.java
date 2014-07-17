package org.lilian.data.real;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KernelDensity extends AbstractDensity
{
	private static final long serialVersionUID = -1576741835502637155L;
	private List<MVN> mvns;

	public KernelDensity(List<Point> data, double variance)
	{
		mvns = new ArrayList<MVN>(data.size());
		
		for(Point point : data)
			mvns.add(new MVN(point, variance));
	}

	@Override
	public double density(Point p)
	{
		double density = 0.0;
		
		for(MVN mvn : mvns)
			density += mvn.density(p);
		

		return (density/mvns.size());
	}

	@Override
	public int dimension()
	{
		return mvns.get(0).dimension();
	}
}
