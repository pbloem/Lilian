package org.lilian.corpora;

/**
 * A corpus that is delimited into sequences.
 * 
 * This extension of corpus output a specific type of iterator. During iteration
 * the user can check for sentence-ends between tokens.
 * 
 * @author peter
 *
 * @param <T>
 */
public interface SequenceCorpus<T> extends Corpus<T> {
	public SequenceIterator<T> iterator();
}
