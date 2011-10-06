//package org.lilian.data.real;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Vector;
//
//import org.lilian.util.MatrixTools;
//import org.ujmp.core.Matrix;
//
///**
// * A TSR map is represented by a translation vector, a scaling vector and
// * a rotation matrix. Since the matrix only rotates we can represent it with 
// * fewer than d*d numbers.
// * 
// * @author peter
// */
//public class TSRMap
//{
//	private Matrix translation,
//	               scale,
//	               rotation;
//	
//	private List<Double> parameters; 
//	
//	public TSRMap(List<Double> parameters)
//	{
//		int s = parameters.size();
//		int dim = dimension(s);
//		
//		if(2 * dim + (dim*dim - dim)/2  != s)
//			throw new IllegalArgumentException("Number of parameters ("+s+") should satisfy 2d + (d^2 - d)/2 (d="+ddim+", " + dim + ")");
//
//		this.translation   = MatrixTools.toVector(parameters.subList(0, dim)); 		
//		Matrix scaleVector = MatrixTools.toVector(parameters.subList(dim, 2 * dim));
//		
//		List<Double> angles = parameters.subList(2 * dim, parameters.size());
//		
//		rotation  = MatrixTools.toRotationMatrix(angles);
//
//		Matrix scaleMatrix = Functions.diag(scale);		
//		this.transformation = rotation.mult(scaleMatrix, new DenseMatrix(dim, dim));
//		
//		this.invertible = Functions.isInvertible(transformation);
//		
//		this.scale = scale;
//		this.angles = new ArrayList<Double>(angles.size());
//		this.angles.addAll(angles);
//	}	
//	
//	/**
//	 * Returns a list of parameters representing this map
//	 * 
//	 * @param mode
//	 * @return
//	 */
//	public List<Double> parameters()
//	{
//		List<Double> result = null;
//		int size;
//		
//		size = 2* dim + (dim * dim - dim)/2;
//		result = new ArrayList<Double>(size);
//		
//		for(VectorEntry entry : translation)
//			result.add(entry.get());
//			
//		for(VectorEntry entry : scale)
//			result.add(entry.get());
//			
//		result.addAll(angles);
//		
//		return result;
//	}	
//	
//	@Override
//	public Point map(Point in)
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	public static int numParameters(int dimension)
//	{
//		return 2 * dimension +  (dimension * dimension - dimension)/2;
//	}
//	
//	public static int dimension(int numParameters)
//	{
//		double ddim =  ( -3.0 + Math.sqrt(9 + s * 8.0))/2.0;
//		return (int) ddim;
//	}
//
//}
