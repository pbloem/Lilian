package org.lilian.data.real.fractal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List; 

import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Map;
import org.lilian.data.real.MapModel;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.classification.AbstractClassifier;
import org.lilian.data.real.classification.DensityClassifier;
import org.lilian.data.real.fractal.IFS.IFSBuilder;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.SquaredEuclideanDistance;

/**
 * A more efficient implementation of the IFSDensityClassifier, which stores a 
 * great deal of information to speed up classification.
 *   
 * This classifier uses some shortcuts that only works for IFSs built on 
 * similitudes. For this reason these are the only IFS types the classifier accepts. 
 * To construct a classifier on general IFS models, a generic DensityClassifier
 * can be used.
 *
 */
public class IFSClassifier extends AbstractClassifier implements Parametrizable, Serializable
{

	private static final long serialVersionUID = -5452654582429802283L;
	
	protected List<IFS<Similitude>> models = new ArrayList<IFS<Similitude>>();
	protected List<Double> priors = new ArrayList<Double>();
	protected double priorSum = 0.0;
	protected List<AffineMap> preMaps = new ArrayList<AffineMap>();
	
	protected Distance<Point> distance = new SquaredEuclideanDistance();
	public List<Store> stores = new ArrayList<Store>();
	protected int depth;
	
	public IFSClassifier(IFS<Similitude> firstModel, double firstPrior, AffineMap map, int depth)
	{
		super(firstModel.dimension(), 0);
		this.depth = depth;
		
		add(firstModel, firstPrior, map);
		
		checkStore();		
	}
	
	public void add(IFS<Similitude> model, double prior, AffineMap map)
	{
		models.add(model);
		priors.add(prior);
		priorSum += prior;
		
		preMaps.add(map);
		
		super.numClasses++;
		
		if(stores != null) // stores == null if we're in the superconstructor
			checkStore();
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
	 */
	protected double[] density(Point point, int index) 
	{
		point = preMaps.get(index).map(point);
		
		int size = (int)Math.ceil(Math.pow(models.get(index).size(), depth));

		Store store = stores.get(index);
		
		double sum = 0.0;
		double prod, sqDist;
		
		double backup = 0.0;
		
		for(int i  = 0; i < size; i++)
		{
			double scale = store.scales.get(i);
				
			sqDist = distance.distance(point, store.means.get(i));

			prod =  Math.pow(scale, -dimension());
			prod *= Math.exp(-(1.0/(2.0 * scale * scale) * sqDist));
			prod *= store.priors.get(i);
			
			// This value is used when the prob density is zero for all points
			backup += Math.exp(sqDist) * store.priors.get(i);
			sum += prod;
		}		
		
		return new double[] {sum, backup};
	}
	
	private void checkStore()
	{
		while(stores.size() < numClasses)
			stores.add(null);
		
		for(int i = 0; i < numClasses; i++)
		{
			if(stores.get(i) == null)
			{
				IFS<Similitude> model = models.get(i);
				
				int size = (int)Math.ceil(Math.pow(model.size(), depth));
		
				List<Point> means = new ArrayList<Point>(size);
				List<Double> priors = new ArrayList<Double>(size);
				List<Double> scales = new ArrayList<Double>(size);		
		
				endPoints(model, depth, means, priors, scales);
						
				Store store = new Store(means, scales, priors);
		
				stores.set(i, store);
			}
		}
	}
	
	
	public class Store implements Serializable 
	{
		private static final long serialVersionUID = 5997041299265837943L;
		public List<Point> means;
		public List<Double> scales;		
		public List<Double> priors;
		
		public Store(List<Point> means, List<Double> scales,
				List<Double> priors) 
		{
			super();
			this.means = means;
			this.scales = scales;
			this.priors = priors;
		}
		
		public void out()
		{
			for(int i = 0; i < means.size(); i++)
				System.out.println(means.get(i) + " " + scales.get(i) + " " + priors.get(i));
		}
	}
	
	public void endPoints(IFS<Similitude> model, int depth, 
			List<Point> points, List<Double> weights, List<Double> scales)
	{
		endPoints(model, new Point(dimension()), 1.0, 1.0, depth, points, weights, scales);
	}
	
	private void endPoints(
			IFS<Similitude> model, Point point, double weight, double scale, int depth, 
			List<Point> points, List<Double> weights, 
			List<Double> scales)
	{
		if(	depth <= 0)
		{
			points.add(new Point(point));
			if(weights != null)
				weights.add(weight);
			
			if(scales != null)
				scales.add(scale);
			
			return;
		}
		
		for(int i = 0; i < model.size(); i++)
		{
			Similitude map = model.get(i);			
			double prob   = model.probability(i);
			
			double sim = map.scalar();
			
			endPoints(model, map.map(point), weight * prob, scale * sim, 
					  depth - 1, points, weights, scales);
		}
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
	
	public static Builder<IFSClassifier> builder(int size, int depth, Builder<IFS<Similitude>> ifsBuilder, Builder<AffineMap> mapBuilder)
	{
		return new IFSClassifierBuilder(size, depth, ifsBuilder, mapBuilder);
	}
	
	protected static class IFSClassifierBuilder implements Builder<IFSClassifier>
	{
		private static final long serialVersionUID = 6666857793341545956L;
		private Builder<IFS<Similitude>> ifsBuilder;
		private Builder<AffineMap> mapBuilder;
		private int size;
		private int depth;

		public IFSClassifierBuilder(
				int size, int depth, 
				Builder<IFS<Similitude>> ifsBuilder,
				Builder<AffineMap> mapBuilder) 
		{
			this.size = size;
			this.ifsBuilder = ifsBuilder;
			this.mapBuilder = mapBuilder;
			this.depth = depth;
		}

		@Override
		public IFSClassifier build(List<Double> parameters) 
		{
			return IFSClassifier.build(parameters, ifsBuilder, mapBuilder, depth);
		}

		@Override
		public int numParameters() 
		{
			return size * (1 + ifsBuilder.numParameters());
		}
	}
	
	public static IFSClassifier build(
			List<Double> parameters, 
			Builder<IFS<Similitude>> builder, 
			Builder<AffineMap> mapBuilder,
			int depth)
	{
		
		/****** NOTE: Untested code *********/
		
		int 
			nComp = builder.numParameters(),
			nMap = builder.numParameters(),
			s = parameters.size();
				
		if( s % (nComp + nMap +1)  != 0)
			throw new IllegalArgumentException("Number of parameters ("+s+") should be divisible by the number of parameters per component ("+nComp+") plus the number of parameters for an affine map ("+nMap+") plus 1");
		
		IFSClassifier model = null;
		for(int from = 0; from + nComp + nMap < s; from += nComp + nMap + 1)
		{
			int to = from + nComp;
			

			List<Double> ifsParams = parameters.subList(from, from + nComp);
			IFS<Similitude> ifs = builder.build(ifsParams);
			
			List<Double> mapParams = parameters.subList(from + nComp, from + nComp + nMap);
			AffineMap map = mapBuilder.build(mapParams);
			
			double weight = Math.abs(parameters.get(to));

			if(model == null)
				model = new IFSClassifier(ifs, weight, map, depth);
			else
				model.add(ifs, weight, map);
		}	
		
		return model;
	}
	
	
}
