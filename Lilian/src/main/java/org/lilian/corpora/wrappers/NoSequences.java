package org.lilian.corpora.wrappers;

import org.lilian.corpora.Corpus;
import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;

/**
 * This class wraps around a corpus and overrides the behavior of 
 * atSentenceEnd() to always return false (turning the corpus into 
 * one long sentence); 
 */
public class NoSequences<T> 
	extends CorpusWrapper<T, T>
	implements SequenceCorpus<T>
{
	public NoSequences(Corpus<T> master) {
		super(master);
	}
	
	public SequenceIterator<T> iterator()
	{
		return new NSWIterator();
	}

	private class NSWIterator
		extends WrapperIterator
		implements SequenceIterator<T>
	{
		public T next()
		{
			return masterIterator.next();
		}
		
		public boolean atSequenceEnd()
		{
			return false;
		}
	}
}
