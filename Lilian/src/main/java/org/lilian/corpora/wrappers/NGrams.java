package org.lilian.corpora.wrappers;

import java.util.*;

import org.lilian.corpora.*;

/**
 * This wrapper generates n-grams from an input corpus.
 * 
 * An order-2 ngram wrapper around the corpus <tt>[the, cat, sees, a man]</tt>
 * returns the tokens <tt>[the, cat], [cat, sees], [sees, a], [a, man]</tt>.
 * 
 *
 * @param <T>
 */
public class NGrams<T> extends CorpusWrapper<List<T>, T> {
	
	private int order;
	
	public NGrams(Corpus<T> master, int order)
	{
		super(master);		
		this.order = order;
	}

	@Override
	public Iterator<List<T>> iterator() {
		return new NGramIterator();
	}

	public class NGramIterator 
		extends WrapperIterator
	{
		private List<T> buffer = new LinkedList<T>();

		@Override
		public List<T> next() 
		{
			ensureBuffer();
			
			if(buffer.size() != order)
				throw new NoSuchElementException();
			
			List<T> token = new ArrayList<T>(buffer);
			
			buffer.remove(0);
			
			return token;
		}
		
		public boolean hasNext()
		{
			ensureBuffer();
			return buffer.size() == order;
		}
		
		private void ensureBuffer()
		{
			while(buffer.size() < order && masterIterator.hasNext())
				buffer.add(masterIterator.next());
		}
	}
	
	public static <T> Corpus<List<T>> wrap(Corpus<T> in, int order)
	{
		return new NGrams<T>(in, order); 
	}
}
