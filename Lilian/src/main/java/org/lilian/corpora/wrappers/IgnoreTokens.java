package org.lilian.corpora.wrappers;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;

/**
 * Abstract clas for wrappers that want to exclude tokens. 
 * 
 * @author peter
 * @param <T>
 */

public abstract class IgnoreTokens<T> extends SequenceWrapper<T, T>
{

	public IgnoreTokens(SequenceCorpus<T> master)
	{
		super(master);
	}

	@Override
	public SequenceIterator<T> iterator()
	{
		// TODO Auto-generated method stub
		return new ITIterator();
	}
	
	private class ITIterator implements SequenceIterator<T>  
	{
		protected SequenceIterator<T> masterIterator = master.iterator(); 
		private Deque<Token> buffer = new LinkedList<Token>();
		private boolean atSequenceEnd = false;
		
		@Override
		public T next()
		{
			fillBuffer();
			Token t = buffer.pop();
			atSequenceEnd = t.isLast;
			return t.inner;
		}
		
		private void fillBuffer()
		{
			while(masterIterator.hasNext() && buffer.size() < 5)
			{
				T inner = masterIterator.next();
				if(! ignore(inner))
				{
					Token t = new Token();
					t.inner = inner;
					t.isLast = masterIterator.atSequenceEnd();
					
					buffer.add(t);
				}
			}
		}
		
		private class Token {
			T inner;
			boolean isLast = false;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasNext()
		{
			fillBuffer();
			return ! buffer.isEmpty();
		}

		@Override
		public boolean atSequenceEnd()
		{
			return atSequenceEnd;
		}
	}	
	
	protected abstract boolean ignore(T token);
}
