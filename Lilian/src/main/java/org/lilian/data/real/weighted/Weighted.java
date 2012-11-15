package org.lilian.data.real.weighted;

import java.util.Collection;
import java.util.List;

import org.lilian.data.real.classification.Classified;

/**
 * Represents an extension of {@link List} where each element is associated
 * with a weight. A weight is a nonnegative double value. 
 * 
 * All normal list classes for addition work as if called with a weight of 1.
 * 
 * @author Peter
 *
 * @param <T>
 */
public interface Weighted<T> extends List<T>
{
	/**
	 * Returns the unnormalized weight associated with element i
	 * @param i
	 * @return
	 */
	public double weight(int i);
	
	/**
	 * Returns the probability associated with the element at the given index. 
	 * The probability is the weight normalized buy the sum total of weights.
	 * 
	 * If the sum is zero, the probability is zero as well.
	 * 
	 * @param i
	 * @return
	 */
	public double probability(int i);
	
	/**
	 * Returns a random element from this weighted list where the probability of
	 * each element being chosen is equal to the value returned by 
	 * {@link probability} 
	 * 
	 * @return
	 */
	public T choose();
	
	/**
	 * The total weight
	 * @param i
	 * @return
	 */
	public double sum();
	
	/**
	 * Optional operation 
	 * 
	 * @param item
	 * @param cls
	 */
	public boolean add(T item, double weight);
	
	public boolean add(int index, T item, double weight);

	
	public boolean addAll(Collection<? extends T> c, double weight);
	
	public boolean addAll(int index, Collection<? extends T> c, double weight);
	
	/**
	 * @param data
	 * @param classes
	 * @return
	 */
	public T set(int i, T item, double weight);
	
	public Weighted<T> subWeighted(int from, int to);
}
