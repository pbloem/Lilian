package org.lilian.models;

import org.lilian.corpora.SequenceCorpus;

/**
 * A sequencemodel is any model that is built by adding tokens in a specific 
 * sequence. Seuqnece ends (such as sentence ends) are signalled by a specific 
 * method called cut().
 * 
 * @author peter
 *
 * @param <T>
 */
public interface SequenceModel<T> {

	/**
	 * Add a corpus to the model. the cut method is called between sequences
	 * 
	 * @corpus The sequence corpus to add.
	 */
	public void add(SequenceCorpus<T> corpus);	
	
	/**
	 * Add a token to the model
	 * 
	 * @param token The token to add to the model
	 */
	public void add(T token);
	
	/**
	 * Signals the end of a sequence.
	 */
	public void cut();

	/**
	 * An integer number representing the state of the model. 
	 * 
	 * If this number has changed, then so has the state of the model. If the 
	 * state is the same, then the model has not been modified.
	 * 
	 * A common usage scenario is to compare the current state of a model to a 
	 * previous state to see if cached values need to be updated.
	 * 
	 * @return
	 */
	public long state();
}
