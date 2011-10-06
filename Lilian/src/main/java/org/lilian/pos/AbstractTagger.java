package org.lilian.pos;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;
import org.lilian.corpora.wrappers.SequenceWrapper;

public abstract class AbstractTagger 
	extends SequenceWrapper<TaggedWord, String>
	implements Tagger
{
	public AbstractTagger(SequenceCorpus<String> master)
	{
		super(master);
	}
	
	@Override
	public SequenceIterator<TaggedWord> iterator()
	{
		return new ATIterator();
	}

	private class ATIterator 
		extends SequenceWrapper<TaggedWord, String>.WrapperIterator
	{
		private Deque<Token> buffer = new LinkedList<Token>();
		private boolean last = false;

		@Override
		public TaggedWord next()
		{
			fillBuffer();
			
			if(buffer.isEmpty())
				throw new NoSuchElementException();
			
			Token t = buffer.poll();
			last = t.last();
			return t.word();
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
			return last;
		}

		private void fillBuffer()
		{
			if(buffer.size() < 5 && masterIterator.hasNext())
			{
				// Read a sentence from the master corpus ...
				List<String> sentence = new LinkedList<String>();
				while(	masterIterator.hasNext() 
						&& (sentence.isEmpty() || !masterIterator.atSequenceEnd()))
					sentence.add(masterIterator.next());
				
				// ... tag it ...
				List<TaggedWord> taggedSentence = tag(sentence);
				assert taggedSentence.size() == sentence.size();
	
				// ... add it to the buffer (wrapped in Token objects so that we can 
				//     preserve sentence information) ...  
				for(TaggedWord tw : taggedSentence)
					buffer.add(new Token(tw));
	
				// ... and indicate that the last token added was the last token in 
				//     the sentence.
				if(masterIterator.atSequenceEnd() && ! buffer.isEmpty())
					buffer.peekLast().makeLast();
			}
			
		}
		
		private class Token {
			private TaggedWord word;
			private boolean last = false;
			
			public Token(TaggedWord word)
			{
				this.word = word;
			}
			public TaggedWord word()
			{
				return word;
			}
			public boolean last()
			{
				return last;
			}
			
			public void makeLast()
			{
				last = true;
			}
		}
	}
	
	protected abstract List<TaggedWord> tag(List<String> sentence);

}
