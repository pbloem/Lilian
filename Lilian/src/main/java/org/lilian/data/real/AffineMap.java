package org.lilian.data.real;

import java.io.IOException;
import java.util.*;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.doublematrix.DenseDoubleMatrix2D;
import org.ujmp.core.doublematrix.impl.DefaultDenseDoubleMatrix2D;


/**
 * Represents an affine transformation 
 *
 */
public class AffineMap extends AbstractMap
{

	private int dim = -1;
	private boolean invertible = false;
	
	private Matrix transformation = null;
	private Matrix translation = null;
	
	private AffineMap inverse = null;
	
	private List<Double> parameters = null;

	private AffineMap()
	{
	}
	
	public AffineMap(Matrix transformation, Matrix translation)
	{
		assert(	transformation.isSquare() && 
				transformation.getSize(0) == translation.getSize(0));
		
		this.transformation = transformation.clone();
		this.translation = translation.clone();
		
		this.invertible = ! transformation.isSingular();
		
		this.dim = (int)translation.getSize(0);
	}	
	
	public AffineMap(List<Double> parameters)
	{
		int s = parameters.size();
		dim = dimension(s); 
		
		if(dim * dim + dim != s)
			throw new IllegalArgumentException("Number of parameters ("+s+") should satisfy d^2 + d (d="+dim+")");
		
		transformation = DenseDoubleMatrix2D.factory.zeros(dim, dim);
		for(int i = 0; i < dim; i++)
			for(int j = 0; j < dim; j++)
				transformation.setAsDouble(parameters.get(i * dim + j), i, j);
		
		translation = DenseDoubleMatrix2D.factory.zeros(dim, 1);
		for(int i = 0; i < dim; i++)
			translation.setAsDouble(parameters.get(dim*dim+i), i, 0);
		
		this.invertible = ! transformation.isSingular();
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
			Matrix invTransform = transformation.inv();
			
			Matrix invTranslate = invTransform.mtimes(Ret.NEW, true, translation);
			invTranslate.mtimes(-1.0);
			
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
	
	public Matrix getTransformation() {
		return transformation.clone();
	}

	public Matrix getTranslation() {
		return translation.clone();
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
				parameters.add(transformation.getAsDouble(i,j));
		
		for(int i = 0; i < dim; i++)
			parameters.add(translation.getAsDouble(i, 0));
		
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
		Matrix newRot = transformation.mtimes(m.transformation);
		
		Matrix newTrans = transformation.mtimes(m.translation);
		newTrans.plus(translation);
		
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
				DenseDoubleMatrix2D.factory.eye(dim, dim), 
				DenseDoubleMatrix2D.factory.zeros(dim, 1));
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
		Matrix vector = in.getVector();
		Matrix out = transformation.mtimes(Ret.NEW, true, vector);
		out.plus(Ret.ORIG, true, translation);
		
		return new Point(out);
	}	
}
