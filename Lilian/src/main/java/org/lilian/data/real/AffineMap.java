package org.lilian.data.real;

import java.io.IOException;
import java.util.*;

import org.apache.commons.math.linear.*;
import org.lilian.util.MatrixTools;


/**
 * Represents an affine transformation 
 */
public class AffineMap extends AbstractMap
{
	private static final long serialVersionUID = 7470030390150319468L;
	
	private int dim = -1;
	private boolean invertible = false;
	
	private RealMatrix transformation = null;
	private RealVector translation = null;
	
	private AffineMap inverse = null;
	
	private List<Double> parameters = null;

	private AffineMap()
	{
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
	
	public AffineMap inverse()
	{
		if(inverse == null)
		{
			RealMatrix invTransform = transformation.inverse();
			
			RealVector invTranslate = invTransform.operate(translation);
					//invTransform.multiply(translation);
			invTranslate.mapMultiply(-1.0);
			
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
	
	/**
	 * Creates a new map, which is a composition of this map and the argument,
	 * such that this.compose(m).map(x); is equal to this.map(m.map(x)); though
	 * it need not be implemented that way.  
	 * 
	 * optional operation
	 * @return
	 */	
	public AffineMap compose(AffineMap m)
	{
		RealMatrix newRot = transformation.multiply(m.transformation);
		
		RealVector newTrans = transformation.operate(m.translation);
		newTrans.add(translation);
		
		return new AffineMap(newRot, newTrans); 
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

	@Override
	public Point map(Point in)
	{
		RealVector vector = in.getVector(); //optimize here
		RealVector out = transformation.operate(vector);
		out = out.add(translation); // here
		
		return new Point(out.getData()); // and here
	}	
}
