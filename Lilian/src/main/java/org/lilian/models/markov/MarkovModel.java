package org.lilian.models.markov;

import java.util.Collection;
import java.util.List;

import org.lilian.models.FrequencyModel;
import org.lilian.models.ProbabilityModel;
import org.lilian.models.SequenceModel;

/**
 * A markov model represents a collection of frequency models over n-grams of 
 * various orders.
 * 
 * @author peter
 *
 * @param <T>
 */
public interface MarkovModel<T> extends SequenceModel<T> 
{
	
	public double frequency(List<T> nGram);
	
	public double total(int order);

	public double distinct(int order);	
	
	public int order();
	
	public Collection<List<T>> tokens(int order);
	
	public List<List<T>> sortedTokens(int order);
	
	public FrequencyModel<List<T>> model(int order);
	
	/**
	 * A sequenceModel is a probability model over sequences of arbitrary length
	 * 
	 * @return
	 */
	public ProbabilityModel<List<T>> sequenceModel();
	
	public ProbabilityModel<List<T>> sequenceModel(int maxOrder);
	
}

