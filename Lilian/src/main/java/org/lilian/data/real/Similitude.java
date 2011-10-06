//package org.lilian.data.real;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Vector;
//
//import no.uib.cipr.matrix.DenseMatrix;
//
//import org.ujmp.core.Matrix;
//
//public class Similitude
//{
//	Matrix scale;
//	
//	public Similitude(List<Double> parameters)
//	{
//		int s = parameters.size();
//		double ddim =  ( -1.0 + Math.sqrt(-7.0 + s * 8.0))/2.0;
//		dim = (int)Math.round(ddim);
//		
//		if(1 + dim + (dim*dim - dim)/2  != s)
//			throw new IllegalArgumentException("Number of parameters ("+s+") should satisfy 1 + d + (d^2 - d)/2 (d="+ddim+", " + dim + ")");
//
//		double scalar	 = parameters.get(0); 
//		Vector scale 	 = Functions.singleValue(dim, scalar);
//		this.translation = Functions.toVector(parameters.subList(1, dim+1)); 		
//		List<Double> angles = parameters.subList(dim + 1, parameters.size());
//		Matrix rotation  = Functions.toRotationMatrix(angles);
//		
//		Matrix scaleMatrix = Functions.diag(scale);		
//		this.transformation = rotation.mult(scaleMatrix, new DenseMatrix(dim, dim));
//		
//		this.invertible = Functions.isInvertible(transformation);
//		
//		this.scale = scale;
//		this.angles = new ArrayList<Double>(angles.size());
//		this.angles.addAll(angles);
//		
//		mode = Mode.SIMILITUDE;
//	}	
//	
//	/**
//	 * Returns a list of parameters in the given mode
//	 * 
//	 * <ul>
//	 * 	<li/> If the map was created with TSR parameters, and is asked to output
//	 * SIMILITUDE parameters, it will average the scale vector to get the 
//	 * similitude scaling factor. 
//	 * 	<li/>
//	 * 	<li/>
//	 * 	<li/>
//	 * 	<li/>
//	 * </ul>
//	 * 
//	 * @param mode
//	 * @return
//	 */
//	public List<Double> parameters(Mode mode)
//	{
//		List<Double> result = null;
//		int size;
//				
//		size = 1 + dim + (dim * dim - dim)/2;
//		result = new ArrayList<Double>(size);
//		
//		result.add(scale.get(0));
//		
//		for(double d : translation)
//			result.add(d);
//			
//		result.addAll(angles);
//			
//		return result;
//	}	
//	
//	/**
//	 * Scaling ratio of the map, can be negative.
//	 * 
//	 * @return
//	 */
//	public double similitude()
//	{
//		return scale.get(0);	
//	}	
//
//	@Override
//	public Point map(Point in)
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public static int numParameters(dimension)
//	{
//		s = 1 + dimension + (dimension * dimension - dimension)/2; break;
//	}
//}
