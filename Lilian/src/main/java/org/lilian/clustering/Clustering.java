package org.lilian.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lilian.util.distance.CosineDistance;
import org.lilian.util.distance.DistanceComparator;
import org.lilian.util.distance.Metrizable;

/**
 * A collection of static methods to help with clustering
 * 
 * @author peter
 *
 */
public class Clustering
{

	/**
	 * Returns a list containing, for each point in a given list, the n points 
	 * nearest to it. 
	 * 
	 * If the distance function is well defined, then each neighbour list will 
	 * contain the reference point as its first element.
	 * 
	 * @param <T>
	 * @param points
	 * @param n
	 * @return A list of lists of neighbours so that the list at index i 
	 * 	contains the the n elements from points closest to points.get(i) (
	 *  including points.get(i) itself), ordered by distance to points.get(i). 
	 */
	public static <T extends Metrizable<T>> List<List<T>> 
		nearestNeighbours(List<T> points, int n)
	{
		return nearestNeighbours(points, points, n); 
	}

	/**
	 * Returns a list containing, for each point in a given list, the n points 
	 * from another list nearest to it.
	 * 
	 * @param <T>
	 * @param points
	 * @param n
	 * @return
	 */
	public static <T extends Metrizable<T>> List<List<T>> 
		nearestNeighbours(List<T> points, List<T> neighbours, int n)
	{
		// * 
		List<List<T>> result = new ArrayList<List<T>>(points.size());
		
		// * Copy neighbours into a modifiable list so we can sort it
		List<T> neighboursMod = new ArrayList<T>(neighbours);
		
		for(T reference : points)
		{
			// * Sort neighboursMod by distance to reference
			Comparator<T> comp = 
				new DistanceComparator<T>(reference);
			Collections.sort(neighboursMod, comp);
			
			List<T> sublist = new ArrayList<T>(neighboursMod.subList(0, n));
			result.add(sublist);
			System.out.print('.');
		}
		
		return result;
	}
}
