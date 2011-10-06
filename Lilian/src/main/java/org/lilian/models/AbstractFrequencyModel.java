package org.lilian.models;

/**
 * A base implementation of the frequencymodel.
 * 
 * 
 * @author peter
 *
 * @param <T>
 */
public abstract class AbstractFrequencyModel<T> implements FrequencyModel<T> {

	@Override
	public double probability(T token) {
		return frequency(token)/total();
	}

	@Override
	public double logProbability(T token) {
		return Math.log(probability(token));
	}

	@Override
	public abstract double frequency(T token);

	@Override
	public abstract double total();

	@Override
	public abstract double distinct();
}
