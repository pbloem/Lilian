package org.lilian.data.real.fractal;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Point;
import org.lilian.data.real.Rotation;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.weighted.Weighted;
import org.lilian.search.Builder;

public class SimEM extends EM<Similitude>
{
	public static final double PERTURB_VAR = 0.3;
	
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
		super(initial, data, numSources, PERTURB_VAR, true, builder, spanningPointsVariance);
	}

	@Override
	protected Weighted<List<Integer>> codes(
			Point point, IFS<Similitude> model, double depth,
			int sources)
	{
		IFS.SearchResult result = 
				IFS.search(model, point, depth, basis(), sources);
		return result.codes();	
	}

	@Override
	protected Similitude findMap(List<Point> from, List<Point> to)
	{
		Similitude sim = org.lilian.data.real.Maps.findSimilitude(from, to);
		
		return sim;
	}
	
	@Override
	public Similitude combine(List<Similitude> maps, List<Double> weights)
	{
		double wSum = 0;
		for(int i : series(maps.size()))
			if(maps.get(i) != null)
				wSum += weights.get(i);
		 
		RealMatrix mat = new Array2DRowRealMatrix(dimension, dimension);
		double scalar = 0.0;
		RealVector vect = new ArrayRealVector(dimension);
		
		boolean allNull = true;
		for(int i : series(maps.size()))
		{
			if(maps.get(i) != null)
			{
				
				RealMatrix rotation = Rotation.toRotationMatrix(maps.get(i).angles());

				mat = mat.add(rotation.scalarMultiply(weights.get(i)/wSum));
				vect = vect.add(maps.get(i).getTranslation().mapMultiply(weights.get(i)/wSum));
				scalar += maps.get(i).scalar() * (weights.get(i)/wSum);
				
				allNull = false;
			}
		}
		
		if(allNull)
			return null;
		
		List<Double> angles = Rotation.findAngles(mat);

		return new Similitude(scalar, new Point(vect), new Point(angles));
	}
	
	private void add(RealVector vect, List<Double> list, double scale)
	{
		for(int i : series(vect.getDimension()))
			vect.setEntry(i, vect.getEntry(i) + list.get(i) * scale);
	}

	@Override
	protected double logLikelihood(List<Point> sample, IFS<Similitude> model, int depth)
	{
		double ll = 0.0;
		for(Point p : sample) 
			ll += Math.log(IFS.density(model, p, depth, basis()));
		
		return ll;
	}

}
