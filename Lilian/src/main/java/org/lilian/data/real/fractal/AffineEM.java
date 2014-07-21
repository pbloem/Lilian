package org.lilian.data.real.fractal;

import java.util.List;

import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Point;
import org.lilian.data.real.weighted.Weighted;
import org.lilian.search.Builder;

public class AffineEM extends EM<AffineMap>
{
	public static final double PERTURB_VAR = 0.3;
	
	public AffineEM(IFS<AffineMap> initial, List<Point> data,
			int numSources, double spanningPointsVariance)
	{
		this(initial, data, numSources,  
				AffineMap.affineMapBuilder(data.get(0).dimensionality())
				, spanningPointsVariance);
	}
	
	public AffineEM(IFS<AffineMap> initial, List<Point> data,
			int numSources, Builder<AffineMap> builder, double spanningPointsVariance)
	{
		super(initial, data, numSources, PERTURB_VAR, false, builder, spanningPointsVariance);
	}

	@Override
	protected Weighted<List<Integer>> codes(
			Point point, IFS<AffineMap> model, double depth,
			int sources)
	{
		IFS.SearchResult result = 
				IFS.search(model, point, depth, basis(), sources);
		return result.codes();	
	}

	@Override
	protected AffineMap findMap(List<Point> from, List<Point> to, AffineMap old)
	{
		return AffineMap.find(from, to);
	}

}
