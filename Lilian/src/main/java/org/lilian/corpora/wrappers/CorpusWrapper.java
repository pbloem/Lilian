package org.lilian.corpora.wrappers;

import java.util.*;

import org.lilian.corpora.AbstractCorpus;
import org.lilian.corpora.AbstractCorpusIterator;
import org.lilian.corpora.Corpus;

/**
 * This class wraps around another corpus.
 *  
 * This class is useful if the behavior of an existing corpus class needs
 * to be modified slightly. For instance, if a Corpus<String> class is needed,
 * but only a Corpus<T> is available, then we can extend CorpusWrapper to wrap 
 * around the Corpus<T> object, and overwrite just the next() method to call 
 * toString() on all the returned classes. Other examples include a corpuswrapper
 * that modifies the behavior of atSentenceEnd(), or a wrapper that chops the 
 * tokens into subtokens, and returns those. 
 *
 * The class has two type parameters, S is the type wrapped corpus,
 * and T is the type of the tokens returned 
 * 
 * If you want to use this class, have a look at noSentenceWrapper for a simple example
 */

public abstract class CorpusWrapper<T, S> 
	extends AbstractCorpus<T>
{
	protected Corpus<S> master;

	public CorpusWrapper(Corpus<S> master)
	{
		this.master = master;
	}
	
	public abstract Iterator<T> iterator();
	
	protected abstract class WrapperIterator
		extends AbstractCorpusIterator<T>
	{
		protected Iterator<S> masterIterator = master.iterator(); 
		
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
	}
	
	public String toString()
	{
		return this.getClass().toString() + " around [" + master.toString() + "] ";
	}	
}
