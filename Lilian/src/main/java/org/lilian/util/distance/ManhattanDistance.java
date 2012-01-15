package org.lilian.util.distance;

import java.util.*;

import org.lilian.data.real.Point;

public class ManhattanDistance implements Distance<Point> {
	
	private static final long serialVersionUID = 6651888026683835759L;

	public double distance(Point a, Point b) 
	{
		return dist(a, b);
	}
	
	public static double dist(Point a, Point b) 
	{
		double distance = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < a.size(); i++)
			distance = Math.max(distance, Math.abs(a.get(i) - b.get(i)));
		
		return distance;
	}	
	
	
}
