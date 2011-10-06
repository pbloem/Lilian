package org.lilian.corpora.wrappers;

import org.lilian.corpora.Corpus;
import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;
import org.lilian.corpora.wrappers.SequenceMarkers.StartToken;

/**
 * This class modifies a corpus to return the lengths of the tokens instead of 
 * the tokens themselves.
 * 
 * This would turn a sequence like [the, cat, walks] into [3, 3, 5].
 */
public final class Lengths<T>
	extends SequenceWrapper<Integer, T>
{
	public Lengths(SequenceCorpus<T> master) {
		super(master);
	}
	
	public SequenceIterator<Integer> iterator()
	{
		return new LengthIterator();
	}

	private class LengthIterator
		extends WrapperIterator
		implements SequenceIterator<Integer>
	{
		public Integer next()
		{
			return masterIterator.next().toString().length();
		}
	}
	
	public static <T> SequenceCorpus<Integer> wrap(SequenceCorpus<T> in)
	{
		return new Lengths<T>(in);
	}
}
