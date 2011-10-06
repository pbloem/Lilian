package org.lilian.corpora.wrappers;

import java.util.Deque;
import java.util.LinkedList;

import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;

public abstract class TokenTransform<O, I>
	extends SequenceWrapper<O, I>
{

	public TokenTransform(SequenceCorpus<I> master)
	{
		super(master);
	}

	@Override
	public SequenceIterator<O> iterator()
	{
		// TODO Auto-generated method stub
		return new TTIterator();
	}
	
	private class TTIterator extends SequenceWrapper<O, I>.WrapperIterator
	{
		public O next()
		{
			return transform(masterIterator.next());
		}

	}	
	
	protected abstract O transform(I input);
}
