package org.lilian.corpora.wrappers;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lilian.corpora.Corpus;
import org.lilian.util.Series;

public class EveryN<T> extends CorpusWrapper<T, T> 
{
	private int offset = 0;
	private int distance;

	/**
	 * 
	 * @param master
	 * @param distance The distance between tokens. If 1, this wrapper behaves as its source. 
	 */
	public EveryN(Corpus<T> master, int distance) {
		super(master);
		this.distance = distance;
	}

	public EveryN(Corpus<T> master, int distance, int offset) {
		super(master);
		this.distance = distance;
		this.offset = offset;
	}	

	@Override
	public java.util.Iterator<T> iterator() {
		return new Iterator();
	}
	
	private class Iterator extends WrapperIterator 
	{
		List<T> buffer = new LinkedList<T>();
		
		public Iterator()
		{
			for(int i = 0; i < offset; i++)
				if(masterIterator.hasNext()) 
					masterIterator.next();
		}

		public T next() 
		{
			read();
			return buffer.remove(0);
		}
		
		public boolean hasNext()
		{
			read();
			return ! buffer.isEmpty();
		}

		private void read()
		{
			while(buffer.size() < 5)
			{
				if(!masterIterator.hasNext())
					return;
				
				buffer.add(masterIterator.next());
				for(int i = 0; i < distance-1; i++)
					if(masterIterator.hasNext()) 
						masterIterator.next();				
			}
		}
		
	}

	
	public static <T> EveryN<T> wrap(Corpus<T> master, int distance, int offset)
	{
		return new EveryN<T>(master, distance, offset);
	}
	
	public static <T> List<EveryN<T>> wrap(Corpus<T> master, int distance)
	{
		List<EveryN<T>> list = new ArrayList<EveryN<T>>(distance);
		
		for(int offset : series(distance))
			list.add(new EveryN<T>(master, distance, offset));
		
		return list;		
	}
	
}
