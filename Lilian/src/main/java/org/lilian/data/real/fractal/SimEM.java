package org.lilian.data.real.fractal;

import java.util.List;

import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.weighted.Weighted;
import org.lilian.search.Builder;

public class SimEM extends EM<Similitude>
{
	public SimEM(IFS<Similitude> initial, List<Point> data,
			int numSources, double spanningPointsVariance)
	{
		this(initial, data, numSources,  
				Similitude.similitudeBuilder(data.get(0).dimensionality())
				, spanningPointsVariance);
	}
	
	public SimEM(IFS<Similitude> initial, List<Point> data,
			int numSources, Builder<Similitude> builder, double spanningPointsVariance)
	{
		super(initial, data, numSources,  builder, spanningPointsVariance);
	}

	@Override
	protected Weighted<List<Integer>> codes(
			Point point, IFS<Similitude> model, int depth,
			int sources)
	{
		IFS.SearchResult result = 
				IFS.search(model, point, depth, basis(), sources);
		return result.codes();	
	}

	@Override
	protected Similitude findMap(List<Point> from, List<Point> to, Similitude old)
	{
		return org.lilian.data.real.Maps.findMap(from, to);
	}

}
