package org.lilian.data.real;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.math.linear.*;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.util.MatrixTools;


/**
 * Represents an affine transformation 
 */
public class AffineMap extends AbstractMap implements Parametrizable, Serializable
{
	private static final long serialVersionUID = 7470030390150319468L;
	
	protected int dim = -1;
	protected boolean invertible = false;
	
	protected RealMatrix transformation = null;
	protected RealVector translation = null;
	
	protected AffineMap inverse = null;
	
	protected List<Double> parameters = null;

	protected AffineMap()
	{
	}
	
	public AffineMap(AffineMap map)
	{
		this(map.getTransformation(), map.getTranslation());
	}
	
	public AffineMap(RealMatrix transformation, RealVector translation)
	{
		assert(	transformation.isSquare() && 
				transformation.getColumnDimension() == translation.getDimension());
		
		this.transformation = transformation.copy();
		this.translation = translation.copy();
		
		this.invertible = MatrixTools.isInvertible(transformation);
		
		this.dim = (int)translation.getDimension();
	}	
	
	public AffineMap(List<Double> parameters)
	{
		int s = parameters.size();
		dim = dimension(s); 
		
		if(dim * dim + dim != s)
			throw new IllegalArgumentException("Number of parameters ("+s+") should satisfy d^2 + d (d="+dim+")");
		
		transformation = new Array2DRowRealMatrix(dim, dim);
		for(int i = 0; i < dim; i++)
			for(int j = 0; j < dim; j++)
				transformation.setEntry(i, j, parameters.get(i * dim + j));
		
		translation = new ArrayRealVector(dim);
		for(int i = 0; i < dim; i++)
			translation.setEntry(i, parameters.get(dim*dim+i));
		
		this.invertible = MatrixTools.isInvertible(transformation);
	}
	
	public boolean invertible()
	{
		return invertible;
	}
	
	public String toString()
	{
		return 	  "[t:"  + translation + ", R:" + transformation
				+ ", inv:" + invertible + "]";		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dim;
		result = prime * result + (invertible ? 1231 : 1237);
		result = prime * result
				+ ((transformation == null) ? 0 : transformation.hashCode());
		result = prime * result
				+ ((translation == null) ? 0 : translation.hashCode());
		return result;
	}

	/**
	 * Two AffineMaps are equal if their dimensions are equal and the double 
	 * values making up their transformations are equal.
	 * 
	 * Note that this means testing double values using equality.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AffineMap other = (AffineMap) obj;
		if (dim != other.dim)
			return false;
		if (invertible != other.invertible)
			return false;
		if (transformation == null) {
			if (other.transformation != null)
				return false;
		} else if (!transformation.equals(other.transformation))
			return false;
		if (translation == null) {
			if (other.translation != null)
				return false;
		} else if (!translation.equals(other.translation))
			return false;
		return true;
	}

	/**
	 * True is this affinemap has the same parameters as the argument, to within 
	 * a given margin (the difference for each individual parameter should be 
	 * smaller than the margin).
	 * 
	 * @param other
	 * @param margin
	 * @return
	 */
	public boolean equals(AffineMap other, double margin) 
	{
		List<Double> otherParameters = other.parameters();
		for(int i = 0; i < parameters().size(); i++)
			if(Math.abs(otherParameters.get(i) - parameters.get(i)) > margin)
				return false;
		
		return true;
	}


	public AffineMap inverse()
	{
		if(inverse == null)
		{
			RealMatrix invTransform = MatrixTools.inverse(transformation);
			
			RealVector invTranslate = invTransform.operate(translation);
			invTranslate = invTranslate.mapMultiply(-1.0);
			
			inverse = new AffineMap();
			inverse.transformation = invTransform;
			inverse.translation    = invTranslate;
			inverse.invertible = true;
			inverse.dim = this.dim;		
			inverse.inverse = this;
		}
		
		return inverse;
	}
	
	public int dimension()
	{
		return dim;
	}
	
	/** 
	 * Returns the transformation matrix for this affine transformation
	 * 
	 * @return
	 */
	public RealMatrix getTransformation() {
		return transformation.copy();
	}

	public RealVector getTranslation() {
		return translation.copy();
	}
	
	/**
	 * Returns a flat list of double values that can be used to represent this
	 * map 
	 * 
	 * @param mode
	 * @return
	 */
	public List<Double> parameters()
	{
		if(parameters != null)
			return parameters;
		
		int size;

		size = dim * dim + dim;
		parameters = new ArrayList<Double>(size);
		
		for(int i = 0; i < dim; i++)
			for(int j = 0; j < dim; j++)
				parameters.add(transformation.getEntry(i,j));
		
		for(int i = 0; i < dim; i++)
			parameters.add(translation.getEntry(i));
		
		parameters = Collections.unmodifiableList(parameters); 
		
		return parameters; 
	}
	
	public Map compose(Map m)
	{
		if(m instanceof AffineMap)
		{
			// return t(o(x)) = R_t * R_o * x + R_t * t_o + t_t 
			
			AffineMap other = (AffineMap)m;
			
			RealMatrix newRot = this.transformation.multiply(other.transformation);
		
			RealVector newTrans = this.transformation.operate(other.translation);
			newTrans = newTrans.add(this.translation);
			
			return new AffineMap(newRot, newTrans); 
		}
		
		return super.compose(m);
	}	
	
	/**
	 * Returns an affine representation of the identity function f(x) = x.
	 * 
	 * @param dim
	 * @return
	 */
	public static AffineMap identity(int dim)
	{
		return new AffineMap(
				MatrixTools.identity(dim), 
				new ArrayRealVector(dim));
	}
	
	/**
	 * Determines the number of parameters required to represent this map
	 */
	public static int numParameters(int dimension)
	{
		return dimension * dimension + dimension;
	}
	
	/**
	 * Determines the dimension from a given number of parameters
	 */
	public static int dimension(int numParameters)
	{
		double ddim = Math.floor(-1.0 + Math.sqrt(4.0 * numParameters + 1.0))/2.0; 
		return (int) ddim; 
	}

	public static Builder<AffineMap> affineMapBuilder(int dimension)
	{
		return new AMBuilder(dimension);
	}
	
	private static class AMBuilder implements Builder<AffineMap>
	{
		private int dimension;

		public AMBuilder(int dimension) 
		{
			this.dimension = dimension;
		}

		@Override
		public AffineMap build(List<Double> parameters) 
		{
			return new AffineMap(parameters);
		}

		@Override
		public int numParameters() 
		{
			return AffineMap.numParameters(dimension);
		}
	}
	
	@Override
	public Point map(Point in)
	{
		RealVector vector = in.getVector(); //optimize here
		RealVector out = transformation.operate(vector);
		out = out.add(translation); // here
		
		return new Point(out.getData()); // and here
	}

}
