package org.lilian.corpora;

import java.io.File;
import java.io.IOException;
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
public abstract class AbstractFileSequenceCorpus<T> extends AbstractCorpus<T> 
	implements SequenceCorpus<T> {
	
	protected List<File> files = new ArrayList<File>();
	
	public AbstractFileSequenceCorpus(File... files)
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
		if(ignore(file))
			return;
		
		if(file.isDirectory())
			for(File subFile : file.listFiles())
				addFile(subFile);
		else
			files.add(file);
	}
	
	
	@Override
	public SequenceIterator<T> iterator() {
		return new CompositeIterator();
	}
	
	private class CompositeIterator implements SequenceIterator<T>
	{
		private static final int BUFFER_SIZE = 5;
		
		Queue<File> fileQueue = new LinkedList<File>();
		
		File currentFile;
		SequenceIterator<T> current;
		
		boolean last = false;
		
		Queue<Token> buffer = new LinkedList<Token>();
		
		public CompositeIterator()
		{
			fileQueue.addAll(files);
			pop();
		}
		
		private void pop()
		{
			currentFile = fileQueue.poll(); 
			current = singleIterator(currentFile);
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
			
			Token t = buffer.poll();
			last = t.last;
			return t.token;
		}

		private void fillBuffer() {
			while(buffer.size() < BUFFER_SIZE)
				if(current.hasNext())
				{
					Token t = new Token(current.next());
					t.last = current.atSequenceEnd();
					buffer.add(t);					
				}
				else
				{
					if(fileQueue.isEmpty())
						break;
					else
						pop();
				}
		}

		@Override
		public void remove() 
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean atSequenceEnd()
		{
			return last;
		}
		
		public File currentFile()
		{
			return currentFile;
		}
		
		private class Token {
			T token;
			boolean last = false;
			
			public Token(T token) {
				super();
				this.token = token;
			}
		}
	}

	/**
	 * Returns an iterator that parses a single file.
	 * @return
	 */
	protected abstract SequenceIterator<T> singleIterator(File file);
	
	/** 
	 * <p>
	 * Files to ignore
	 * </p><p>
	 * The abstractfilesequenceiterator will pass any file it finds to this 
	 * method. If it returns true, the file will be ignored.
	 * </p><p>
	 * This can be helpful when things like license files or readmes must be
	 * included in the distribution of a corpus but shouldn't be parsed.
	 * </p><p>
	 * This implementation always returns false.
	 * </p>
	 */
	protected boolean ignore(File file)
	{
		return false;
	}
}
