package org.lilian.data.real.fractal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List; 

import org.lilian.data.real.AffineMap;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Map;
import org.lilian.data.real.MapModel;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.classification.AbstractClassifier;
import org.lilian.data.real.classification.DensityClassifier;
import org.lilian.data.real.fractal.IFS.IFSBuilder;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.SquaredEuclideanDistance;

/**
 * Basic version of the IFS classifier which calculates a straightforward
 * density.
 *
 */
public class IFSClassifierBasic extends AbstractClassifier implements Parametrizable, Serializable
{

	private static final long serialVersionUID = -5452654582429802283L;
	
	protected List<IFS<Similitude>> models = new ArrayList<IFS<Similitude>>();
	protected List<Double> priors = new ArrayList<Double>();
	protected double priorSum = 0.0;
	protected List<AffineMap> preMaps = new ArrayList<AffineMap>();
	// * The determinants of the preMaps
	protected List<Double> determinants = new ArrayList<Double>();
	protected List<MVN> bases = new ArrayList<MVN>();
	
	protected Distance<Point> distance = new SquaredEuclideanDistance();
	
	protected int depth;
	
	public IFSClassifierBasic(IFS<Similitude> firstModel, double firstPrior, AffineMap map, MVN basis, int depth)
	{
		super(firstModel.dimension(), 0);
		this.depth = depth;
		
		add(firstModel, firstPrior, map, basis);
	}
	
	public void add(IFS<Similitude> model, double prior, AffineMap map, MVN basis)
	{
		models.add(model);
		priors.add(prior);
		
		priorSum += prior;
		
		preMaps.add(map);
		determinants.add(MatrixTools.getDeterminant(map.getTransformation()));
		
		bases.add(basis);
		
		super.numClasses++;
	}
	
	public List<Double> probabilities(Point point) 
	{
		List<Double> probs = new ArrayList<Double>(numClasses);
		List<Double> backups = new ArrayList<Double>(numClasses);
		
		boolean allZero = true;
				
		double pSum = 0.0, bSum = 0.0, value, backup;
		for(int i = 0; i < numClasses; i++)
		{
			double[] d = density(point, i);
			value  = prior(i) * d[0];
			backup = prior(i) * d[1]; 
			
			probs.add(value);
			backups.add(backup);
			
			allZero = allZero && (value == 0.0 || Double.isNaN(value));
			
			// * sums for later normalization
			bSum += backup;
			pSum += value;
		}
			
		// normalize
		for(int i = 0; i < numClasses; i++)
		{
			if(allZero)
				backups.set(i, backups.get(i)/bSum);
			else
				probs.set(i, probs.get(i)/pSum);
		}
		
		if(allZero)
			return backups;
		
		return probs;
	}	
	
	public double prior(int i)
	{
		return priors.get(i) / priorSum;		
	}

	/**
	 * This method doesn't return a true density (for reasons of speed and 
	 * stability), but for the purposes of classification, it's good enough).
	 * 
	 * It attempts to get a real (log) probability density from the model. If 
	 * the point is too far off the support of all IFSs these will all be zero. 
	 * It will then use the backup value, which is e^d * prior where d is the 
	 * distance to the closest endpoint. 
	 */
	protected double[] density(Point point, int index) 
	{
		
		point = preMaps.get(index).map(point);
		
		IFS.SearchResult result = IFS.search(models.get(index), point, depth, bases.get(index));
		
		double density = result.probSum() * determinants.get(index);
		double approximation = result.approximation() * determinants.get(index);
		
		return new double[] {density, approximation};
	}

	@Override
	public List<Double> parameters()
	{
		List<Double> params = new ArrayList<Double>();
		
		for(int i : Series.series(models.size()))
		{
			params.addAll(models.get(i).parameters());	
			params.addAll(preMaps.get(i).parameters());	
			
			params.add(priors.get(i));

		}
		
		return params;
	}
	
	public IFS<Similitude> model(int i)
	{
		return models.get(i);
	}
	
	public AffineMap preMap(int i)
	{
		return preMaps.get(i);
	}
	
	public static Builder<IFSClassifierBasic> builder(int size, int depth, Builder<IFS<Similitude>> ifsBuilder, Builder<AffineMap> mapBuilder, MVN basis)
	{
		return new IFSClassifierBasicBuilder(size, depth, ifsBuilder, mapBuilder, basis);
	}
	
	protected static class IFSClassifierBasicBuilder implements Builder<IFSClassifierBasic>
	{
		private static final long serialVersionUID = 6666857793341545956L;
		private Builder<IFS<Similitude>> ifsBuilder;
		private Builder<AffineMap> mapBuilder;
		private int size;
		private int depth;
		private MVN basis;

		public IFSClassifierBasicBuilder(
				int size, int depth, 
				Builder<IFS<Similitude>> ifsBuilder,
				Builder<AffineMap> mapBuilder,
				MVN basis) 
		{
			this.size = size;
			this.ifsBuilder = ifsBuilder;
			this.mapBuilder = mapBuilder;
			this.depth = depth;
			this.basis = basis;
		}
		
		@Override
		public IFSClassifierBasic build(List<Double> parameters) 
		{
			return IFSClassifierBasic.build(parameters, ifsBuilder, mapBuilder, depth, basis);
		}

		@Override
		public int numParameters() 
		{
			return size * (1 + ifsBuilder.numParameters());
		}
	}
	
	public static IFSClassifierBasic build(
			List<Double> parameters, 
			Builder<IFS<Similitude>> builder, 
			Builder<AffineMap> mapBuilder,
			int depth,
			MVN basis)
	{
		
		/****** NOTE: Untested code *********/
		
		int 
			nComp = builder.numParameters(),
			nMap = builder.numParameters(),
			s = parameters.size();
				
		if( s % (nComp + nMap +1)  != 0)
			throw new IllegalArgumentException("Number of parameters ("+s+") should be divisible by the number of parameters per component ("+nComp+") plus the number of parameters for an affine map ("+nMap+") plus 1");
		
		IFSClassifierBasic model = null;
		for(int from = 0; from + nComp + nMap < s; from += nComp + nMap + 1)
		{
			int to = from + nComp;
			

			List<Double> ifsParams = parameters.subList(from, from + nComp);
			IFS<Similitude> ifs = builder.build(ifsParams);
			
			List<Double> mapParams = parameters.subList(from + nComp, from + nComp + nMap);
			AffineMap map = mapBuilder.build(mapParams);
			
			double weight = Math.abs(parameters.get(to));

			if(model == null)
				model = new IFSClassifierBasic(ifs, weight, map, basis, depth);
			else
				model.add(ifs, weight, map, basis);
		}	
		
		return model;
	}
	
	
}
