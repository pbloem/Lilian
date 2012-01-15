package org.lilian.util.distance;

import org.lilian.data.real.Point;

public class SquaredEuclideanDistance implements Distance<Point> 
{
	private static final long serialVersionUID = -6615291183116441738L;

	public double distance(Point a, Point b) 
	{		
		double[] aData = a.getBackingData();
		double[] bData = b.getBackingData();

		double distance = 0.0, d;
		for(int i = 0; i < aData.length; i++)
		{
			d = aData[i] - bData[i];
			distance += d * d;
		}

		return distance;
	}
	
	public static double dist(Point a, Point b) 
	{
		double[] aData = a.getBackingData();
		double[] bData = b.getBackingData();

		double distance = 0.0, d;
		for(int i = 0; i < aData.length; i++)
		{
			d = aData[i] - bData[i];
			distance += d * d;
		}

		return distance;
	}	
	
	
}
