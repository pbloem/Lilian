package org.lilian.data.real;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.util.Functions;

/**
 * <p>
 * A model consisting of a finite number of weighted maps.
 * </p><p>
 * For instance, a Mixture-of-Gaussians model can be considered as the sum 
 * of n weighted transformations of the standard MVN.
 * </p><p>
 * The weights do not need to sum to one, but they will be normalized when
 * probabilities are required. (Though the unnormalized versions are stored).
 * </p><p>
 * NOTE: It's important that the parameters are stored as they come in. If a 
 * model has a weight parameter of -1, that weight will be 1, but it's
 * preferable if it will still generate the negative value if 
 * {@link parameters()} is called. 
 * 
 * @author Peter
 *
 * @param <T>
 */
public class MapModel<M extends Map & Parametrizable> 
	extends AbstractList<M>
	implements Parametrizable, Serializable
{
	private static final long serialVersionUID = -6894388080122503364L;

	protected int dimension;

	protected ArrayList<Double> weights = new ArrayList<Double>();
	
	protected List<M> maps = new ArrayList<M>();
	protected List<Map> inverseMaps = new ArrayList<Map>();
	// * true if all operations are invertible
	protected boolean invertible = true;
	
	protected double weightSum = 0.0;
	
	// * A flat representation of the above parameters	
	protected ArrayList<Double> parametersFlat = new ArrayList<Double>();

	public MapModel(M map, double weight)
	{
		 addMap(map, weight);
		 dimension = map.dimension();
	}
	
	public void addMap(M map, double weight)
	{
		assert(map.dimension() == dimension);
		
		maps.add(map);
		weights.add(Math.abs(weight));
				
		
		parametersFlat.addAll(map.parameters());
		parametersFlat.add(weight);
		
		weightSum += Math.abs(weight);
		
		if(invertible)
		{
			if(map.invertible())
				inverseMaps.add(map.inverse());
			else
				invertible = false;
		}
	}
		
	/**
	 * If one of the component transformations of this model isn't invertible,
	 * this returns false, otherwise, it returns true;
	 */
	public boolean invertible()
	{
		return invertible;
	}
	
	public int dimension()
	{
		return dimension;
	}
	
	/**
	 * The number of maps
	 * 
	 * @return
	 */
	public int size()
	{
		return maps.size();
	}
	
	@Override
	public M get(int index) 
	{
		return maps.get(index);
	}	
	
	/** 
	 * Returns the probability (the normalized weight) of operation i. The 
	 * weights are not normalized when stored. This function should be called
	 * to get the proper probability so that all probabilities sum to one. 
	 * 
	 * @param i The operation for which to return the probability.
	 */
	public double probability(int index)
	{
		return weights.get(index) / weightSum;
	}
		
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < size(); i++)
			sb.append("p: " +probability(i) + " : " + maps.get(i).toString()); 
		sb.append(" inv: " + invertible());
		
		return sb.toString();
	}


	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((maps == null) ? 0 : maps.hashCode());
		result = prime * result + ((weights == null) ? 0 : weights.hashCode());
		return result;
	}

	/**
	 * Two mapmodels are equal if they contain the same maps with the same 
	 * weights <em>in the same order</em>. Note that this usually entails
	 * exact equality testing on doubles, so should be used sparingly (mainly 
	 * for unit testing).
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapModel other = (MapModel) obj;
		if (maps == null)
		{
			if (other.maps != null)
				return false;
		} else if (!maps.equals(other.maps))
			return false;
		if (weights == null)
		{
			if (other.weights != null)
				return false;
		} else if (!weights.equals(other.weights))
			return false;
		return true;
	}

	/**
	 * Draws a random map according to the model's prior probabilities.
	 * 
	 * @return
	 */
	public M random()
	{
		int index = Functions.draw(weights, weightSum);
		
		return maps.get(index);
	}
	
	@Override
	public List<Double> parameters() 
	{
		return Collections.unmodifiableList(parametersFlat);
	}	

	/**
	 * The number of parameters required to represent a MapModel.
	 */	
	public static <M extends Map & Parametrizable> int numParameters(int size, int perMap)
	{
		return (perMap + 1) * size;
	}
	
	public static <M extends Map & Parametrizable> Builder<? extends MapModel<M>> builder(int size, Builder<M> mapBuilder)
	{
		return new MMBuilder<M>(size, mapBuilder);
	}
	
	protected static class MMBuilder<M extends Map & Parametrizable> implements Builder<MapModel<M>>
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6919361840502575913L;
		private Builder<M> mapBuilder;
		private int size;

		public MMBuilder(int size, Builder<M> mapBuilder) 
		{
			this.size = size;
			this.mapBuilder = mapBuilder;
		}

		@Override
		public MapModel<M> build(List<Double> parameters) 
		{
			return MapModel.build(parameters, mapBuilder);
		}

		@Override
		public int numParameters() 
		{
			// return MapModel.numParameters(size, mapBuilder.numParameters());
			return (mapBuilder.numParameters() + 1) * size;

		}
	}
	
	public static <M extends Map & Parametrizable> MapModel<M> build(List<Double> parameters, Builder<M> builder)
	{
		int n = builder.numParameters(), s = parameters.size();
				
		if( s % (n+1)  != 0)
			throw new IllegalArgumentException("Number of parameters ("+s+") should be divisible by the number of parameters per component ("+n+") plus one");
		
		MapModel<M> model = null;
		for(int from = 0; from + n < s; from += n + 1)
		{
			int to = from + n;
			List<Double> mapParams = parameters.subList(from, to);
			double weight = Math.abs(parameters.get(to));
			
			M map = builder.build(mapParams);
			if(model == null)
				model = new MapModel<M>(map, weight);
			else
				model.addMap(map, weight);
		}	
		
		return model;
	}
}
	
