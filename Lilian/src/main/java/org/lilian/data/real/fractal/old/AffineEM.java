//package org.lilian.data.real.fractal.old;
//
//import static org.lilian.util.Series.series;
//
//import java.util.List;
//
//import org.apache.commons.math.linear.Array2DRowRealMatrix;
//import org.apache.commons.math.linear.ArrayRealVector;
//import org.apache.commons.math.linear.RealMatrix;
//import org.apache.commons.math.linear.RealVector;
//import org.lilian.data.real.AffineMap;
//import org.lilian.data.real.Point;
//import org.lilian.data.real.Similitude;
//import org.lilian.data.real.weighted.Weighted;
//import org.lilian.search.Builder;
//import org.lilian.util.Series;
//
//public class AffineEM extends EMOld<AffineMap>
//{
//	public static final double PERTURB_VAR = 0.3;
//	
//	public AffineEM(IFS<AffineMap> initial, List<Point> data,
//			int numSources, double spanningPointsVariance)
//	{
//		this(initial, data, numSources,  
//				AffineMap.affineMapBuilder(data.get(0).dimensionality())
//				, spanningPointsVariance);
//	}
//	
//	public AffineEM(IFS<AffineMap> initial, List<Point> data,
//			int numSources, Builder<AffineMap> builder, double spanningPointsVariance)
//	{
//		super(initial, data, numSources, PERTURB_VAR, false, builder, spanningPointsVariance);
//	}
//
//	@Override
//	protected Weighted<List<Integer>> codes(
//			Point point, IFS<AffineMap> model, double depth,
//			int sources)
//	{
//		IFS.SearchResult result = 
//				IFS.search(model, point, depth, basis(), sources);
//		return result.codes();	
//	}
//
//	@Override
//	protected AffineMap findMap(List<Point> from, List<Point> to, List<Double> weights, int k)
//	{
//		// TODO Implement weights 
//		return null;
//	}
//
//	@Override
//	public AffineMap combine(List<AffineMap> maps, List<Double> weights)
//	{
//		double wSum = 0;
//		for(int i : series(maps.size()))
//			if(maps.get(i) != null)
//				wSum += weights.get(i);
//		
//		RealMatrix  mat = new Array2DRowRealMatrix();
//		RealVector vect = new ArrayRealVector();
//		
//		boolean allNull = true;
//		for(int i : series(maps.size()))
//		{
//			if(maps.get(i) != null)
//			{
//				mat = mat.add(maps.get(i).getTransformation().scalarMultiply(weights.get(i)/wSum));
//				vect = vect.add(maps.get(i).getTranslation().mapMultiply(weights.get(i)/wSum));
//				allNull = false;
//			}
//		}
//		
//		if(allNull)
//			return null;
//		
//		return new AffineMap(mat, vect);
//	}
//	
//	@Override
//	protected double logLikelihood(List<Point> sample, IFS<AffineMap> model, int depth)
//	{
//		double ll = 0.0;
//		for(Point p : sample) 
//			ll += Math.log(IFS.density(model, p, depth, basis()));
//		
//		return ll;
//	}
//
//}
