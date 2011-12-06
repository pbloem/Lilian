package org.lilian.util.distance;

import java.util.ArrayList;
import java.util.List;

import org.lilian.models.BasicFrequencyModel;

/**
 * Determines the distance between two lists of doubles by the cosine of the 
 * angle between them.
 * 
 * The value returned as the distance is 1.0 - cos(angle).
 *  
 * @author peter
 */
public class CosineDistance implements Distance<List<Double>>
{
	private static final long serialVersionUID = 1L;

	@Override
	public double distance(List<Double> a, List<Double> b)
	{
//		if(a.size() != b.size())
//			throw new IllegalArgumentException("Input sizes (a.size()="+a.size()+", b.size()="+b.size()+") must be equal");
		
		int n = a.size();
		int i = 0;
		
		// The dot product between the two vectors
		double dotProduct = 0.0;
		for(; i < n ; i++)
			dotProduct += a.get(i) * b.get(i);
		
		// The sq magnitude of vector a
		double aMag = 0.0;
		for(i = 0; i < n; i++)
			aMag += a.get(i) * a.get(i);
		
		// The sq magnitude of vector b
		double bMag = 0.0;
		for(i = 0; i < n; i++)
			bMag += b.get(i) * b.get(i);
		
		// The cosine
		if(aMag == 0.0 || bMag == 0.0)
			return 1.0;
		
		double cos = Math.sqrt( (dotProduct * dotProduct) / (aMag * bMag) );
		return 1.0 - cos;		
	}
	
	public static double distance(double[] a, double[] b)
	{
		int n = a.length;
		int i = 0;
		
		// The dot product between the two vectors
		double dotProduct = 0.0;
		for(; i < n ; i++)
			dotProduct += a[i] * b[i];
		
		// The sq magnitude of vector a
		double aMag = 0.0;
		for(i = 0; i < n; i++)
			aMag += a[i] * a[i];
		
		// The sq magnitude of vector b
		double bMag = 0.0;
		for(i = 0; i < n; i++)
			
		// The cosine
		if(aMag == 0.0 || bMag == 0.0)
			return 1.0;
		
		double cos = Math.sqrt( (dotProduct * dotProduct) / (aMag * bMag) );
		return 1.0 - cos;
	}
	
//	public static double distance(Matrix a, Matrix b)
//	{
//		long n = a.getSize(0);
//		long i = 0;
//		
//		// The dot product between the two vectors
//		double dotProduct = 0.0;
//		for(; i < n ; i++)
//			dotProduct += a.getAsDouble(i) * b.getAsDouble(i);
//		
//		// The sq magnitude of vector a
//		double aMag = 0.0;
//		for(i = 0; i < n; i++)
//			aMag += a.getAsDouble(i) * a.getAsDouble(i);
//		
//		// The sq magnitude of vector b
//		double bMag = 0.0;
//		for(i = 0; i < n; i++)
//			bMag += b.getAsDouble(i) * b.getAsDouble(i);;
//			
//		// The cosine
//		if(aMag == 0.0 || bMag == 0.0)
//			return 1.0;
//		
//		double cos = Math.sqrt( (dotProduct * dotProduct) / (aMag * bMag) );
//		return 1.0 - cos;	
//	}	

}

