package org.lilian.data.real;

import java.util.ArrayList;
import java.util.List;

import org.lilian.util.Series;

public class SpatialIndex
{
	
	public static List<Integer> code(Point point, int depth)
	{
		List<Integer> code = new ArrayList<Integer>(depth);
		for(int i : Series.series(depth))
			code.add(-1);
		
		int dim = point.dimensionality();
		code(point, Point.value(dim, -1.0), Point.value(dim, 1.0), code, depth);
		
		return code;
	}
	
	public static void code(
			Point point, Point lower, Point upper, 
			List<Integer> code, int depth)
	{
		if(depth <= 0)
			return;
		
		int dim = point.dimensionality();
		Point newLower = new Point(dim), newUpper = new Point(dim);
		
		int codon = 0;
		for(int i : Series.series(dim))
		{
			double mid = (lower.get(i) + upper.get(i)) / 2.0;
			
			if(point.get(i) < mid)
			{
				
				newLower.set(i, lower.get(i));
				newUpper.set(i, mid);
			} else
			{
				newLower.set(i, mid);
				newUpper.set(i, upper.get(i));
				
				codon += (int) Math.pow(2.0, i);
			}	
		}
		
		code.set(code.size() - depth, codon);
		
		code(point, newLower, newUpper, code, depth - 1);
		
	}

}
