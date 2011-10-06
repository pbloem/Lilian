package org.lilian.corpora.wrappers;

import java.util.*;

import org.lilian.corpora.*;

/**
 * A corpuswrapper for sequence corpora
 *
 */

public abstract class SequenceWrapper<T, S> 
	extends AbstractCorpus<T>
	implements SequenceCorpus<T>
{
	protected SequenceCorpus<S> master;

	public SequenceWrapper(SequenceCorpus<S> master)
	{
		this.master = master;
	}
	
	public abstract SequenceIterator<T> iterator();
	
	protected abstract class WrapperIterator
		extends AbstractCorpusIterator<T>
		implements SequenceIterator<T>
	{
		protected SequenceIterator<S> masterIterator = master.iterator(); 
		
		public WrapperIterator()
		{
		}
		
		/**
		 * Returns the next token in the corpus.
		 * 
		 */
		public abstract T next();
	
		/**
		 * Returns whether the corpus can return more tokens
		 */
		public boolean hasNext()
		{
			return masterIterator.hasNext();
		}
		
		public boolean atSequenceEnd()
		{
			return masterIterator.atSequenceEnd();
		}
	}
}
