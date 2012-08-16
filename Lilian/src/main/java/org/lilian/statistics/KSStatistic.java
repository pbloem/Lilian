package org.lilian.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class KSStatistic
{
	
	public static double ks(List<Double> x, List<Double> y, boolean sorted)
	{
		if(!sorted)
		{
			x = new ArrayList<Double>(x);
			y = new ArrayList<Double>(y);
			
			Collections.sort(x);
			Collections.sort(y);
		}
		
		double maxDiff = Double.NEGATIVE_INFINITY;
		
		// * Loop through x and y combined, using the next smallest element from 
		//   either (ie. as if walking through the combined and sorted x and y).
		
		int i = -1, j = -1;
		while(i < x.size() - 1 || j < y.size() - 1)
		{
			double nextX = i < x.size() - 1 ? x.get(i + 1) : Double.POSITIVE_INFINITY;
			double nextY = j < y.size() - 1 ? y.get(j + 1) : Double.POSITIVE_INFINITY;

			double next;
			if(nextX == nextY) // test at nextX == nextY
			{
				i++;
				j++;
				next = nextX;
			} else if(nextX < nextY) // test at nextX
			{
				i++;
				next = nextX;
			} else 	// test at nextY
			{
				j++;
				next = nextY;
			}
			
			//System.out.println("at " + next +  "\t" + (i+1) / (double)x.size() + "\t" + (j+1)/ (double)y.size());
			
			double diff = Math.abs((i+1)/(double)x.size() - (j+1)/(double)y.size());
			
			maxDiff = Math.max(diff, maxDiff);
		}
		
		return maxDiff;
	}

}
