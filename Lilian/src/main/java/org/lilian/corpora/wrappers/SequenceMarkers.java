package org.lilian.corpora.wrappers;

import java.io.*;
import java.util.*;

import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;

/**
 * Wraps the master corpus in custom tokens, so that sentence markers can be 
 * used. 
 *
 * We recommend using the following static import for this wrapper:
 * <pre>
 *   import static org.lilian.corpora.SentenceMarkerWrapper.*;
 * </pre>
 * This way you can refer to Token<String> as your type, instead of 
 * SentenceMarkerWrapper.Token<String>.  
 *  
 */
public class SequenceMarkers<T> 
	extends CorpusWrapper<SequenceMarkers.Token<T>, T>
{
	private SequenceCorpus<T> master;
	
	public SequenceMarkers(SequenceCorpus<T> master)
		throws IOException
	{
		super(master);
		this.master = master;
	}
	
	public Iterator<Token<T>>  iterator()
	{
		return new SMWIterator();
	}
	
	private class SMWIterator
		extends WrapperIterator
	{
		private List<Token<T>> buffer = new LinkedList<Token<T>>();
		private Token<T> lastReturned = null;
		
		private SequenceIterator<T> masterIterator;
		
		public SMWIterator()
		{
			super();
			masterIterator = master.iterator();
			
			if(masterIterator.hasNext())
				buffer.add(new StartToken<T>());
		}
		
		public Token<T> next()
		{
			if(buffer.size() > 0)
				lastReturned = buffer.remove(0);
			else{
				Token<T> masterNext = new WrappingToken<T>(masterIterator.next());
				
				if(masterIterator.atSequenceEnd())
				{
					buffer.add(new EndToken<T>());
					if(masterIterator.hasNext())
						buffer.add(new StartToken<T>());
				}
				
				lastReturned = masterNext;			
			}
	
			return lastReturned;
		}
	
		@Override
		public boolean hasNext()
		{
			return masterIterator.hasNext() || buffer.size() > 0;
		}
	}
	
	public static class Token<T> implements Serializable
	{
	}
	
	public static class StartToken<T> extends Token<T> 
	{
		@Override
		public boolean equals(Object other) {
			if(! (other instanceof SequenceMarkers.EndToken))
				return true;

			return false;
		}

		@Override
		public int hashCode() {
			return -1;
		}

		public String toString()
		{
			return "%START%";
		}				
	}
	
	public static  class EndToken<T>  extends Token<T> 
	{
		@Override
		public boolean equals(Object other) {
			if(! (other instanceof SequenceMarkers.EndToken))
				return true;

			return false;
		}

		@Override
		public int hashCode() {
			return 1;
		}

		public String toString()
		{
			return "%END%";
		}		
	}
	
	/**
	 * Tokens which wrap around the original tokens 
	 * 
	 * @author peter
	 */
	public static  class WrappingToken<T> extends Token<T> 
	{
		private T value;
		
		public WrappingToken(T value) {
			this.value = value;
		}
		
		public T value() {
			return value;
		}

		@Override
		public boolean equals(Object other) {
			if(! (other instanceof SequenceMarkers.WrappingToken))
				return false;
			
			WrappingToken wToken = (WrappingToken)other; 
			return wToken.value().equals(value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		public String toString()
		{
			return value.toString();
		}
	}

	/**
	 * Takes a sequence of T's, wraps each in WrappingTokens and adds start and 
	 * end tokens.
	 * @param <T>
	 * @param original
	 * @return
	 */
	public static <T> List<Token<T>> wrap(List<T> original)
	{
		List<Token<T>> result = new ArrayList<Token<T>>(original.size());
		
		for(T t : original)
			result.add(new WrappingToken<T>(t));
		
		return result;
	}
	
	public static <T> List<Token<T>> wrap(List<T> original, boolean includeMarkers)
	{
		List<Token<T>> result = new ArrayList<Token<T>>(original.size());
		
		if(includeMarkers)
			result.add(new StartToken<T>());
		for(T t : original)
			result.add(new WrappingToken<T>(t));
		
		if(includeMarkers)
			result.add(new EndToken<T>());
		
		return result;
	}
	
}
