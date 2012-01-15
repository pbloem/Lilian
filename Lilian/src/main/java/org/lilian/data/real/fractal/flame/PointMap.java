package org.lilian.data.real.fractal.flame;

import java.util.ArrayList;
import java.util.List;

import org.lilian.data.real.AbstractMap;
import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;

/**
 * This operation moves the point a certain ratio closer to a given point.
 * 
 */
public class PointMap extends FlameMap 	
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1979870442400152794L;
	private Point mid;
	private double ratio;
	private List<Double> params = new ArrayList<Double>(2 + 1 + 4);
	
	public PointMap(Point mid, double ratio, Point color) 
	{
		super(color);
		this.mid = mid;
		this.ratio = ratio;
	}

	/**
	 * Modifies the input
	 */
	@Override
	public Point map(Point p)
	{
		for(int i = 0; i < mid.size(); i++)
			p.set(i, p.get(i) + (mid.get(i) - p.get(i)) * ratio);
		
		return p;
	}
	
	@Override
	public int dimension() 
	{
		return mid.size();
	}

	@Override
	public List<Double> parameters() 
	{
		return params ;
	}
	
	public static Builder<PointMap> builder()
	{
		return new PMBuilder();
	}
	
	private static class PMBuilder implements Builder<PointMap>
	{

		@Override
		public PointMap build(List<Double> parameters)
		{
			Point mid = new Point(parameters.get(0), parameters.get(1));
			double ratio = parameters.get(2);
			Point color = new Point(
					parameters.get(3), parameters.get(4),
					parameters.get(5), parameters.get(6)
					);

			return new PointMap(mid, ratio, color);
		}

		@Override
		public int numParameters()
		{
			return 9;
		}
	}
}
