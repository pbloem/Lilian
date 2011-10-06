package org.lilian.corpora.wrappers;

import java.io.*;
import java.util.*;

import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;

/**
 * This corpuswrapper removes sentences based on their length.
 * 
 * An example use would be to remove all sentence of length greater than seven 
 * from a corpus. 
 */

public class LengthControl<T> 
	extends SequenceWrapper<T, T>
{
	private int bufferSize = 10;
	
	private ArrayList<ArrayList<T>> buffer;
	private List<T> currentSentence;
	
	private int minLength = -1;
	private int maxLength = -1;
	
	private boolean atSentenceEnd;
	
	/**
	 * Creates a LengthControlWrapper based on a master corpus and two bounds.
	 *
	 * @param master The corpus to get the sentences from. 
	 * @param maxLength The maximum length. Sentences of size > maxLength are 
	 * 		removed. Set -1 for no upper bound.
	 * @param minLength The minimum length. Sentences of size < minLength are 
	 * 		removed. Set -1 for no lower bound.
	 */	
	public LengthControl(SequenceCorpus<T> master, int minLength, int maxLength)
	{	
		super(master);
		
		this.minLength = minLength;
		this.maxLength = maxLength;		
	}
	
	public SequenceIterator<T> iterator()
	{
		return new LCIterator();
	}
	
	private class LCIterator
		extends WrapperIterator
	{
		public LCIterator()
		{
			super();
			
			buffer = new ArrayList<ArrayList<T>>(bufferSize);
			currentSentence = new ArrayList<T>();
			
			atSentenceEnd = false;	
			
			buffer.add(new ArrayList<T>());
		}
		
		public T next()
		{
			// * ensure that the buffer is full and empty sentences have been removed
			ensureBuffer();
			
			if(buffer.isEmpty() && currentSentence.isEmpty())
				throw new NoSuchElementException();
			
			if(currentSentence.isEmpty())
				currentSentence = buffer.remove(0);
			
			if(currentSentence.size() == 1)
				atSentenceEnd = true;
			else
				atSentenceEnd = false;
			
			return currentSentence.remove(0);
		}
		
		public boolean hasNext()
		{
			ensureBuffer();
			if(! buffer.isEmpty())
				return true;
			
			if(! currentSentence.isEmpty())
				return true;
			
			return false;
		}
		
		public boolean atSequenceEnd()
		{
			return atSentenceEnd;
		}
		
		private void ensureBuffer()
		{
			while(buffer.size() < bufferSize && masterIterator.hasNext())
			{
				readSentence();
				trimBuffer();
			}
			
			trimBuffer();
		}
		
		private void readSentence()
		{
			if(! masterIterator.hasNext())
				return; 
			
			ArrayList<T> sentence = new ArrayList<T>();
			
			while(masterIterator.hasNext())
			{
				sentence.add(masterIterator.next());
				if(masterIterator.atSequenceEnd())
					break;
			}
			
			buffer.add(sentence);
		}
		
		private void trimBuffer()
		{
			// Remove all sentences that don't meet the size 
			// requirements from the buffer
			ListIterator<ArrayList<T>> it = buffer.listIterator();
			int size;
			
			while(it.hasNext())
			{
				size = it.next().size();
				
				if(size == 0) 
					it.remove();
				else if(minLength != -1  
						&& size < minLength) 
					it.remove();
				else if(maxLength != -1 
						&& size > maxLength)
					it.remove();			
			}
		}
	}	

	public String toString()
	{
		return "Length control ["+minLength+", "+maxLength+"] for ["+master.toString()+"]";
	}
}
