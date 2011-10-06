package org.lilian.models;

import java.util.*;

import static org.lilian.util.Functions.*;

public class Models {

	/**
	 * Calculates the entropy of a set of tokens under a probability model.
	 * 
	 * @param model The probability model which defines the tokens
	 * @param tokens The complete set of tokens for the probability model. The 
	 *               assumption is that the sum of the probabilities of these
	 *               tokens equals one.
	 * @return A non-negative fintie value representing the entropy of the
	 * probability model.
	 */	
	public static <T> double entropy(ProbabilityModel<T> model, Collection<T> tokens)
	{
		double sum = 0.0;
		for(T token : tokens)
		{
			double p = model.probability(token);
			if(p != 0.0)
				sum += p * log2(p);
		}
		
		return - sum;
	}
		

}
