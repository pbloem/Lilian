package org.lilian.corpora.wrappers;

import java.io.*;
import java.util.*;

import org.lilian.util.*;
import org.lilian.util.trees.Tree;
import org.lilian.corpora.*;


/**
 *
 */
public class StripSentences<T> extends CorpusWrapper<T, Tree<T>>
{
	private int bufferSize = 15;

	public StripSentences(Corpus<Tree<T>> master) throws IOException
	{
		super(master);
	}
	
	public Iterator<T> iterator()
	{
		return new SSCIterator();
	}

	private class SSCIterator
		extends WrapperIterator
		implements SequenceIterator<T>
	{
		protected List<Token> buffer = new ArrayList<Token>();
		protected Token lastToken = null;

		@Override
		public T next()
		{
			lastToken = buffer.remove(0);
			
			return lastToken.get();
		}

		@Override
		public boolean hasNext()
		{
			ensureBuffer();
			
			return ! buffer.isEmpty();			
		}
		
		@Override
		public boolean atSequenceEnd() {
			return (lastToken != null && lastToken.last());
		}

		private void ensureBuffer()
		{
			while(buffer.size() < bufferSize && masterIterator.hasNext())
			{
				for(T t : masterIterator.next().getLeaves())
					buffer.add(new Token(t));
				
				buffer.get(buffer.size() - 1).setLast();				
			}
		}
		
		private class Token
		{
			private T token;
			private boolean lastInSentence = false;
			
			public Token(T token) {
				this.token = token;
			}
			
			public void setLast()
			{
				lastInSentence = true;
			}
			
			public boolean last()
			{
				return lastInSentence;
			}
			
			public T get()
			{
				return token;
			}
		}
	}
}
