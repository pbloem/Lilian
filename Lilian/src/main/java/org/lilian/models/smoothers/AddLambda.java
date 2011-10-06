package org.lilian.models.smoothers;

import org.lilian.*;
import org.lilian.models.*;

/**
 * This class implements a vary basic smoothing algorithm known as add-lambda 
 * smoothing. It adds to the frequency of each token a small value lambda 
 */
public class AddLambda<T> implements FrequencyModel<T> {
	
	private FrequencyModel<T> master;
	private double lambda;
	
	public AddLambda(FrequencyModel<T> master, double lambda)
	{
		this.master = master;
		this.lambda = lambda;
	}

	@Override
	public double frequency(T token) {
		return (master.frequency(token) + lambda);
	}

	@Override
	public double total() {
		return master.total() + distinct() * lambda;
	}

	@Override
	public double probability(T token) {
		return frequency(token)/total();
	}

	@Override
	public double distinct() {
		return master.distinct();
	}
	
	public static <T> AddLambda<T> wrap(FrequencyModel<T> model, double lambda)
	{
		return new AddLambda<T>(model, lambda);
	}

	@Override
	public double logProbability(T token) {
		return Math.log(probability(token));
	}
}
