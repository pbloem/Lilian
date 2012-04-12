package org.lilian.data.real.classification;

import java.util.*;

import org.lilian.data.real.Point;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.SquaredEuclideanDistance;


/**
 * A simple classifier that classifies points on the basis of k 
 * master points (for k classes, with one master point per 
 * class) so that a point gets assigned the class for the 
 * master point to which it is closest.   
 * 
 * @author peter
 *
 */
public class DistanceClassifier extends AbstractClassifier {
	
	private List<Point> points = new ArrayList<Point>(3);
	
	private static Distance<Point> distance = new SquaredEuclideanDistance();

	public DistanceClassifier(Point first) {
		super(first.dimensionality(), 0);

		addPoint(first);
	}
	
	public void addPoint(Point point)
	{
		points.add(point);
		numClasses++;
	}

	@Override
	public int classify(Point pointIn) {
		
		Point point;
		if(pointIn instanceof Point)
			point = (Point)pointIn;
		else
			point = new Point(pointIn);
		
		double dist, minDist = Double.MAX_VALUE; 
		int index = -1;
		for(int i = 0; i < numClasses; i++)
		{
			dist = distance.distance(points.get(i), point);
			if(dist < minDist)
			{
				minDist = dist;
				index = i;
			}
		}
		
		return index;
	}

	@Override
	public int dimension()
	{
		return points.get(0).size();
	}

	@Override
	public int size()
	{
		return points.size();
	}	
}
