package org.lilian.corpora;

import java.io.IOException;
import java.util.*;

/**
 * This corpus provides a way of defining a SequenceCorpus with collections
 * 
 * Consider, for instance, a situation where you have a number of Vectors of
 * Strings that you want to use as a corpus, each vector representing a line.
 * You can put all these vectors in another vector, and construct
 * CollectionCorpus around it, to create a corpus.
 * 
 * The corpus is not backed by the collection from which it is constructed,
 * although the tokens themselves aren't copied.
 */

public class CollectionCorpus<T> 
	extends AbstractCorpus<T> 
	implements SequenceCorpus<T> {
	
	protected List<List<T>> collection;	
	
	public CollectionCorpus(Collection<? extends Collection<T>> collection)
	{
		this.collection = new ArrayList<List<T>>(collection.size());
		for(Collection<T> sequence : collection)
		{
			List<T> copy = new ArrayList<T>(sequence);
			this.collection.add(copy);
		}
	}
	
	/**
	 * Creates an in-memory copy of a given corpus by running through it and 
	 * copying its tokens to a collection.
	 *  
	 * @param master
	 */
	public CollectionCorpus(SequenceCorpus<? extends T> master)
	{
		collection = new ArrayList<List<T>>(master.size());
		
		List<T> sentence = new ArrayList<T>();
		
		SequenceIterator<? extends T> si = master.iterator();
		while(si.hasNext())
		{
			sentence.add(si.next());
			if(si.atSequenceEnd())
			{
				collection.add(sentence);
				sentence = new ArrayList<T>();
			}
		}
	}

	@Override
	public SequenceIterator<T> iterator() 
	{
		return new CollectionCorpusIterator();
	}
	
	private class CollectionCorpusIterator
		extends AbstractCorpusIterator<T>
		implements SequenceIterator<T>
	{
		private Iterator<List<T>> sentenceIt;
		private Iterator<T> tokenIt;
		
		private int lastSentence;
		private int currentSentence;
		
		protected CollectionCorpusIterator()
		{		
			// find the index of the last sentence
			// The index of the last sentence is the last nonempty element of the main
			// collection
			Iterator<List<T>> it = collection.iterator();
			
			for(int i = 0; it.hasNext(); i++)
				if(it.next().size() > 0)
					lastSentence = i;

			sentenceIt = collection.iterator();
			if(sentenceIt.hasNext())
				tokenIt = sentenceIt.next().iterator();
			
			currentSentence = 0;			
		}
		
		public boolean hasNext()
		{
			if(tokenIt == null)
				return false;
			
			if(tokenIt.hasNext())
				return currentSentence <= lastSentence;
			
			return currentSentence < lastSentence;
			
		}
	
		public T next()
		{
			if(tokenIt == null)
				throw new NoSuchElementException();
	
			while(hasNext())
			{
				if(tokenIt.hasNext())
					return tokenIt.next();
				if(sentenceIt.hasNext())
				{
					tokenIt = sentenceIt.next().iterator();
					currentSentence++;
				}
			}
			
			throw new NoSuchElementException();
		}
	
		public boolean atSequenceEnd()
		{
			return ! tokenIt.hasNext();
		}
	
	}	

}
