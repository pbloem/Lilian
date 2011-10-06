package org.lilian.corpora.wrappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;

/**
 * A wrapper which filters out any characters rejected by the mask.
 * 
 * @author peter
 *
 * @param <T>
 */
public class TokenMask<T> extends SequenceWrapper<T, T> 
{
	protected Mask<T> mask;

	public TokenMask(SequenceCorpus<T> master, Mask<T> mask) 
	{
		super(master);
		
		this.mask = mask;
	}
	
	@Override
	public SequenceIterator<T> iterator() 
	{
		return new WIterator();
	}
	
	private class WIterator extends WrapperIterator
	{
		private List<Entry> buffer = new LinkedList<Entry>();
		private boolean atEnd = false;
		
		@Override
		public T next() 
		{
			read();
			
			Entry e = buffer.remove(0);
			atEnd = e.atEnd;
			
			return e.token; 
		}
		
		private void read()
		{
			while(buffer.size() < 5)
			{
				if(! masterIterator.hasNext())
					return;
				
				T token = masterIterator.next();
				if(! mask.reject(token))
					buffer.add(new Entry(token));
				
				if(masterIterator.atSequenceEnd() && (! buffer.isEmpty()) )
					buffer.get(buffer.size() - 1).atEnd = true;
			}
		}
		
		/**
		 * Returns whether the corpus can return more tokens
		 */
		public boolean hasNext()
		{
			read();
			return ! buffer.isEmpty();
		}
		
		public boolean atSequenceEnd()
		{
			return atEnd;
		}		
		
		private class Entry
		{
			T token;
			boolean atEnd = false;
			
			public Entry(T token) 
			{
				this.token = token;
			}
			
			
		}
	}
	
	public static <T> SequenceCorpus<T> wrap(SequenceCorpus<T> master, Mask<T> mask)
	{
		return new TokenMask<T>(master, mask); 
	}

	public static <T> SequenceCorpus<T> inverse(SequenceCorpus<T> master, Mask<T> mask)
	{
		return new TokenMask<T>(master, invert(mask)); 
	}
	
	public static <T> SequenceCorpus<T> mask(SequenceCorpus<T> master, T ... tokens)
	{
		return new TokenMask<T>(master, setMask(tokens)); 
	}
	
	public static <T> SequenceCorpus<T> mask(SequenceCorpus<T> master, Collection<T> tokens)
	{
		return new TokenMask<T>(master, setMask(tokens)); 
	}	
	
	public static <T> SequenceCorpus<T> unmask(SequenceCorpus<T> master, T ... tokens)
	{
		return new TokenMask<T>(master, invert(setMask(tokens))); 
	}	

	/**
	 * 
	 * @author peter
	 *
	 * @param <T>
	 */
	public static interface Mask<T>
	{		
		public boolean reject(T token);
	}
	
	/**
	 * A basic mask using a set of characters. Any characters not in the set are
	 * rejected. Characters not in the set are rejected.
	 * 
	 * @return
	 */
	public static <T> Mask<T> setMask(T ... tokens)
	{
		Set<T> set = new HashSet<T>();
		for(T token : tokens)
			set.add(token);
				
		return new SetMask<T>(set); 
	}
	
	/**
	 * A basic mask using a set of characters. Any characters not in the set are
	 * rejected. Characters not in the set are rejected.
	 * 
	 * @return
	 */	
	public static <T> Mask<T> setMask(Collection<T> tokens)
	{
		Set<T> set = new HashSet<T>();
		for(T token : tokens)
			set.add(token);
				
		return new SetMask<T>(set); 
	}	
	
	private static class SetMask<T> implements Mask<T>
	{
		private Set<T> mask;
		
		public SetMask(Set<T> mask)
		{
			this.mask = mask;
		}

		@Override
		public boolean reject(T token) 
		{
			return ! mask.contains(token);			
		}	
	}
	
	/**
	 * Inverts a given mask. Tokens rejected by the source are included and vice 
	 * versa
	 * 
	 * @return
	 */
	public static <T> Mask<T> invert(Mask<T> source)
	{
		return new Invert<T>(source);
	}
	
	private static class Invert<T> implements Mask<T>
	{
		private Mask<T> master;
		
		public Invert(Mask<T> master)
		{
			this.master = master;
		}

		@Override
		public boolean reject(T token) 
		{
			return ! master.reject(token);			
		}	
	}
	
	
}
