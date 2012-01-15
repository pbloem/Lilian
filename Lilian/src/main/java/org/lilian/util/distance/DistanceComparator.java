package org.lilian.util.distance;

import java.util.Comparator;


/**
 * Compares elements by their distance to a given element
 * @author peter
 *
 * @param <T>
 */
public class DistanceComparator<T> implements Comparator<T>
{
	private T reference;
	private Distance<T> distance;
	
	private Metrizable<T> metRef;

	/**
	 * Creates a Distancecomparator which sorts metrizable elements according 
	 * to their natural distance to 'reference'   
	 * 
	 * 
	 * @param reference
	 * @param distance
	 */
	public DistanceComparator(Metrizable<T> reference)
	{
		this.reference = null;
		this.distance = null;
		
		metRef = reference;
	}	
	
	/**
	 * Creates a Distancecomparator which sorts elements according to their 
	 * distance to 'reference'   
	 * 
	 * 
	 * @param reference
	 * @param distance
	 */
	public DistanceComparator(T reference, Distance<T> distance)
	{
		this.reference = reference;
		this.distance = distance;
	}


	@Override
	public int compare(T first, T second)
	{
		if(reference == null)
			return Double.compare(
				metRef.distance(first), 
				metRef.distance(second));
		
		return Double.compare(
			distance.distance(reference, first), 
			distance.distance(reference, second));
	}
	
	public static <T> Comparator<T> make(T reference, Distance<T> distance)
	{
		return new DistanceComparator<T>(reference, distance);
	}
}
