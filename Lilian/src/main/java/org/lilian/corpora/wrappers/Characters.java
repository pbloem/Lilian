package org.lilian.corpora.wrappers;

import java.util.*;
import java.io.*;

import org.lilian.Log;
import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;
import org.lilian.grammars.TestGrammars;


/**
 * This class wraps around a string corpus and returns the characters of the 
 * words as tokens.
 * 
 * @author Peter
 */

public class Characters 
	extends SequenceWrapper<String, String>
{
	private int bufferSize = 15;
	private String space = null;
	
	private final boolean useWordEnds;
	
	/**
	 * Creastes a character wrapper. No characters will be inserted between 
	 * words. 
	 * 
	 * @param master The string corpus to base this corpus on
	 * @param useWordEnds If this is true, this corpus will report sentence ends 
	 * 		where the words from the master corpus ends (each sentence in this 
	 * 		corpus is a word in the master) if this is false, it will simply take 
	 * 		the sentence ends from the original. 
	 */
	public Characters(SequenceCorpus<String> master, boolean useWordEnds)
	{
		this(master, useWordEnds, null);
	}
	
	/**
	 * @param master The string corpus to base this corpus on
	 * @param useWordEnds If this is true, this corpus will report sentence ends 
	 * 		where the words from the master corpus ends (each sentence in this 
	 * 		corpus is a word in the master) if this is false, it will simply take 
	 * 		the sentence ends from the original.
	 * @param space This string will be inserted after each word. 
	 */	
	public Characters(SequenceCorpus<String> master, boolean useWordEnds, String space)
	{
		super(master);
		
		this.useWordEnds = useWordEnds;
		this.space = space;
	}
	
	public SequenceIterator<String> iterator()
	{
		return new CWIterator();
	}
	
	private class CWIterator
		extends WrapperIterator
		implements SequenceIterator<String>
	{
		private Vector<Token> buffer = new Vector<Token>();;
		private Token lastToken = null;
		
		public String next()
		{
			ensureBuffer();
			
			lastToken = buffer.remove(0);
			
			return lastToken.get();
		}
	
		public boolean atSequenceEnd() 
		{
			return (lastToken != null && lastToken.last());
		}

		public boolean hasNext() 
		{
			ensureBuffer();
			
			return ! buffer.isEmpty();
		}
	
		private void ensureBuffer()
		{
			while(buffer.size() < bufferSize && masterIterator.hasNext())
			{
				String word = masterIterator.next();
				
				for(int i = 0 ; i < word.length(); i++)
					buffer.add(new Token(word.substring(i, i+1)));
				
				if(space != null)
					buffer.add(new Token(space));
				
				if(useWordEnds)
					buffer.get(buffer.size() - 1).setLast();
				else if(masterIterator.atSequenceEnd())
					buffer.get(buffer.size() - 1).setLast();
			}
		}
	
		private class Token
		{
			private String token;
			private boolean lastInSentence = false;
			
			public Token(String token)
			{
				this.token = token;
			}
	
			public String get()
			{
				return token;
			}
			
			public void setLast()
			{
				this.lastInSentence = true;
			}
			
			public boolean last()
			{
				return lastInSentence;
			}
		}
	}
	
	public static SequenceCorpus<String> wrap(SequenceCorpus<String> in)
	{
		return wrap(in, false, null);
	}
	
	/**
	 * Create a characters corpus wrapper from the input corpus
	 * 
	 * @param in The input corpus
	 * @param useWordEnds Whether the word ends should become the sequence ends
	 * 		If false, the sentence ends of <tt>in</tt> are used as sequence ends
	 * @return A sequence corpus which returns characters.
	 */
	public static SequenceCorpus<String> wrap(SequenceCorpus<String> in, boolean useWordEnds, String space)
	{
		if(space == null)
			return new Characters(in, useWordEnds);
		
		return new Characters(in, useWordEnds, space);		
	}
}