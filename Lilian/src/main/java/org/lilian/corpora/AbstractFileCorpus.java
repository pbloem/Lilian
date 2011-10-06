package org.lilian.corpora;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Abstract base implementation for a corpus that reads one or more files
 * 
 * Authors need only supply a corpusiterator for a single file.
 * 
 * @author peter
 *
 * @param <T>
 */
public abstract class AbstractFileCorpus<T> extends AbstractCorpus<T> {
	
	protected List<File> files = new ArrayList<File>(0);
	
	public AbstractFileCorpus(File... files)
	{
		for(File file : files)
			addFile(file);	
	}
	
	/**
	 * Adds a file to the list of files to be read by this corpus.
	 * 
	 * If the file is a directory, then all the files in it are added 
	 * recursively
	 *  
	 * @param file
	 */
	private void addFile(File file)
	{
		if(file.isDirectory())
			for(File subFile : file.listFiles())
				addFile(subFile);
		else
			files.add(file);
	}
	
	
	@Override
	public Iterator<T> iterator() {
		return new CompositeIterator();
	}
	
	private class CompositeIterator implements Iterator<T>
	{
		private static final int BUFFER_SIZE = 5;
		
		Queue<Iterator<T>> iterators = new LinkedList<Iterator<T>>();
		Iterator<T> current;
		Queue<T> buffer = new LinkedList<T>();
		
		public CompositeIterator()
		{
			for(File file : files)
				iterators.add(singleIterator(file));
			
			current = iterators.poll();
		}

		@Override
		public boolean hasNext() 
		{
			fillBuffer();
			
			return ! buffer.isEmpty();
		}

		@Override
		public T next() {
			fillBuffer();
			
			return buffer.poll();
		}

		private void fillBuffer() {
			while(buffer.size() < BUFFER_SIZE)
				if(current.hasNext())
					buffer.add(current.next());
				else
				{
					if(!iterators.isEmpty())
						current = iterators.poll();
					else
						break;
				}
		}

		@Override
		public void remove() 
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Returns an iterator that parses a single file.
	 * @return
	 */
	protected abstract Iterator<T> singleIterator(File file);
}
