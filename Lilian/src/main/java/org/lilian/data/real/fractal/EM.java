package org.lilian.data.real.fractal;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.util.Series;
import org.nodes.util.Pair;

public class EM
{

	public static RealMatrix expectation(List<Double> depths, IFS<Similitude> model, Similitude post,  List<Point> data)
	{
		// * construct and return  P_ij
		return null;
	}
	
	public static List<Double> maximizeDepths(
			int k, int maxDepth, List<Point> data, Similitude post, RealMatrix resp)
	{
		return null;
	}
	
	public static IFS<Similitude> maximizeIFS(
			int k, int maxDepth, List<Point> data, Similitude post, RealMatrix resp)
	{
		// * find depth vector 
		
		// * find 
		
		
		return null;
	}
	
	public static Similitude maximizeSimilitude(
			int k, int maxDepth, List<Point> data, Similitude post, RealMatrix resp)
	{
		return null;
	}
	
	protected static int indexOf(List<Integer> code, int k)
	{
		if(code.isEmpty())
			return 0;
		
		int res = (1 - (int)pow(k, code.size()))/(1 - k) - 1;
		
		for(int i : Series.series(code.size()))
			res += code.get(i) * Math.pow(k, i);
		
		return res + 1;
	}
	
	protected static List<Integer> code(int index, int k)
	{
		if(index == 0)
			return Collections.emptyList();
		
		double dl = Math.ceil(log((index+1)*(k-1) + 1)/log(k)) - 1.0; 
		int length = (dl == Double.NEGATIVE_INFINITY ? 0 : (int) dl);
		int base = (1 - (int)pow(k, length)) / (1 - k) ;

		System.out.println(dl + " " + base);
		
		List<Integer> code = new ArrayList<Integer>(length);
		for(int i : series(length))
			code.add(-1);
		
		index -= base;
		for(int i : series(length-1, -1, -1))
		{			
			int pow = (int)pow(k, i);
			
			int digit = index / pow ;
			System.out.println(i + " " + digit + " " + index  + " " + pow);
			code.set(i, digit);
			index = index % pow;
		}
		return code;
	}
		
	// * Find matrix P
		
		
	// * Find matrices P^k
	
		
	// * Tests:
		
	/* 
	 *  - sample data from mixture of MVNs
	 *  - inverse transform by similitude
	 *  - check if we can recover similitude correctly
	 *    - given correct responsibilities
	 *    - EM
	 */
		
	/* 
	 *  - Sample data from Sierpinski (D=2)
	 *  - Inspect P_ij^k for all k
	 */

}
