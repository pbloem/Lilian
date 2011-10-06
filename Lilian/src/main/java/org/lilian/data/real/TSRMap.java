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
//public class TSRMap
//{
//
//	/** 
//	 * Returns a TSR map
//	 * @param scale
//	 * @param translate
//	 * @param angles
//	 */
//	public TSRMap(Matrix scale, Matrix translate, List<Double> angles)
//	{
//		this.translation = translate;
//		
//		this.scale = scale;
//		this.angles = new ArrayList<Double>(angles.size());
//		this.angles.addAll(angles);
//		
//		this.dim = scale.size();
//		Matrix rotation = Functions.toRotationMatrix(angles);
//		Matrix scaleMatrix = Functions.diag(scale);
//		this.transformation = rotation.mult(scaleMatrix, new DenseMatrix(dim, dim));
//		
//		this.invertible = Functions.isInvertible(transformation);
//	}	
//	
//	public TSRMap(List<Double> parameters)
//	{
//		int s = parameters.size();
//		double ddim =  ( -3.0 + Math.sqrt(9 + s * 8.0))/2.0;
//		dim = (int)Math.round(ddim);
//		
//		if(2 * dim + (dim*dim - dim)/2  != s)
//			throw new IllegalArgumentException("Number of parameters ("+s+") should satisfy 2d + (d^2 - d)/2 (d="+ddim+", " + dim + ")");
//
//		this.translation = Functions.toVector(parameters.subList(0, dim)); 		
//		Vector scale     = Functions.toVector(parameters.subList(dim, 2 * dim));
//		List<Double> angles = parameters.subList(2 * dim, parameters.size());
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
//	}	
//	
//	/**
//	 * Returns a list of parameters representing this map
//	 * 
//	 * @param mode
//	 * @return
//	 */
//	public List<Double> parameters(Mode mode)
//	{
//		List<Double> result = null;
//		int size;
//		switch(mode)
//		{
//			case STRAIGHT:
//				size = dim * dim + dim;
//				result = new ArrayList<Double>(size);
//				
//				for(int i = 0; i < dim; i++)
//					for(int j = 0; j < dim; j++)
//						result.add(transformation.get(i,j));
//				for(VectorEntry entry : translation)
//					result.add(entry.get());
//				
//				break;
//			case TSR:
//				// TODO: implement this with QR decomp.	
//				if(this.mode != Mode.TSR)
//					throw new IllegalArgumentException("This class was not created with TSR parameters, so it cannot output TSR parameters.");
//				
//				size = 2* dim + (dim * dim - dim)/2;
//				result = new ArrayList<Double>(size);
//				
//				for(VectorEntry entry : translation)
//					result.add(entry.get());
//					
//				for(VectorEntry entry : scale)
//					result.add(entry.get());
//					
//				result.addAll(angles);
//				
//				break;
//			case SIMILITUDE:
//				if(this.mode != Mode.SIMILITUDE && this.mode != Mode.TSR)
//					throw new IllegalArgumentException("This class was not created with SIMILITUDE or TSR parameters, so it cannot output SIMILITUDE parameters.");
//				
//				size = 1 + dim + (dim * dim - dim)/2;
//				result = new ArrayList<Double>(size);
//				
//				if(this.mode == Mode.SIMILITUDE)
//					result.add(scale.get(0));
//				
//				if(this.mode == Mode.TSR)
//				{
//					// Add the average of the scales
//					double sumScale = 0.0;
//					for(VectorEntry entry : scale)
//						sumScale += entry.get();
//					result.add(sumScale / scale.size());						
//				}				
//				
//				for(VectorEntry entry : translation)
//					result.add(entry.get());
//					
//				result.addAll(angles);
//			
//				break;
//		}
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
//}
