package org.lilian.models.markov;

import java.util.*;

import org.lilian.models.BasicFrequencyModel;
import org.lilian.models.FrequencyModel;

public class BasicMarkovModel<T> extends AbstractMarkovModel<T> 
{
	private List<BasicFrequencyModel<List<T>>> models;

	public BasicMarkovModel(int order) {
		super(order);
		
		models = new ArrayList<BasicFrequencyModel<List<T>>>(order());
		for(int i = 0; i < order ; i++)
			models.add(new BasicFrequencyModel<List<T>>());
	}

	@Override
	protected void add(List<T> token)
	{
		int n = token.size();
		
		assert(n > 0);
		assert(n <= order());
		
		models.get(n - 1).add(token);
	}

	@Override
	public double frequency(List<T> nGram) 
	{
		checkOrder(nGram.size());

		return models.get(nGram.size() - 1).frequency(nGram);
	}

	@Override
	public double total(int order) 
	{
		checkOrder(order);		

		return models.get(order - 1).total();		
	}

	@Override
	public double distinct(int order) {
		checkOrder(order);		
		
		return models.get(order - 1).distinct();
	}
	
	@Override
	public FrequencyModel<List<T>> model(int order)
	{
		return models.get(order - 1);
	}

	@Override
	public Set<List<T>> tokens(int order) 
	{
		return ((BasicFrequencyModel)model(order)).tokens();
	}

}
