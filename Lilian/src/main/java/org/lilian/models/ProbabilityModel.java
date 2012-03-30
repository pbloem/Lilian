package org.lilian.models;

import java.io.Serializable;
import java.util.*;

/**
 * A probability model defines probabilities over tokens. The probabilities 
 * should always lie in the interval [0, 1]. Probability models where the 
 * probabilities over all possible tokens aren't guaranteed to sum to one are 
 * allowed, but this behavior should be clearly documented. 
 *
 * @param <T> The type of token. T should have a proper equals(), hashcode() and 
 * toString() implementation.
 */
public interface ProbabilityModel<T> extends Serializable {
	
	/**
	 * The probability of a token.
	 * 
	 * @param token
	 * @return A non-negative number, less than 1, which represents the 
	 * probability of the token.  
	 */
	double probability(T token);
	
	/**
	 * The natural logarithm of the probability of a token.
	 * 
	 * For some implementations, this may provide an opportunity to return 
	 * smaller values of probability than can be represented straightforwardly.
	 * 
	 * @param token
	 * @return The logarithm of a probability of a token  
	 */
	double logProbability(T token);	
	
	public static class Comparator<T> implements java.util.Comparator<T>
	{
		private ProbabilityModel<T> model;
		
		public Comparator(ProbabilityModel<T> model)
		{
			this.model = model;
		}
		
		@Override
		public int compare(T first, T second) {
			return Double.compare(model.probability(first), model.probability(second));
		}
	}
}
