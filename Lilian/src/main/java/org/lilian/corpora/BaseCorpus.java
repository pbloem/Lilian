package org.lilian.corpora;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/** 
 * A very simple corpus reader. One sequence per line, tokens
 * separated by whitespace.
 * 
 * @author peter
 *
 */
public class BaseCorpus extends AbstractFileSequenceCorpus<String>
{
	public BaseCorpus(File... files)
	{
		super(files);
	}

	@Override
	protected SequenceIterator<String> singleIterator(File file)
	{
		return new BaseIterator(file);
	}

	public class BaseIterator implements SequenceIterator<String>
	{
		private File file;
		private BufferedReader reader = null;
		private List<Token> buffer = new LinkedList<Token>();
		
		private boolean atLineEnd = false;
		private boolean finished = false;
				
		public BaseIterator(File file)
		{
			this.file = file;
			// -- Note that it's important not to init the FileReader in 
			//    the constructor, but only when next() or hasNext() is 
			//    first called. This way file is only opened when necessary,
			//    and we can create as many corpus objects and iterators as 
			//    we like, without getting a "too many open files" error 
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String next()
		{
			read();
			
			if(buffer.isEmpty())
				throw new NoSuchElementException();
			
			atLineEnd = buffer.get(0).last; 
			
			return buffer.remove(0).token;
		}

		@Override
		public boolean hasNext()
		{
			read();
			
			return ! buffer.isEmpty();
		}
		
		private void read()
		{
			if(reader == null)
			{
				try
				{
					reader = new BufferedReader(new FileReader(file));
				} catch (FileNotFoundException e)
				{
					throw new RuntimeException(e);
				}
			}
			
			
			while(buffer.size() < 5 && ! finished) {
				
				String line;
				
				try
				{
					line = reader.readLine();
					if(line == null)
					{
						reader.close();
						finished = true;
						return;					
					}					
				} catch (IOException e)
				{
					throw new RuntimeException(e);
				}
					
				for(String token : line.split("\\s"))
					buffer.add(new Token(token));
						
				buffer.get(buffer.size() - 1).last = true;
			}
				
		}

		@Override
		public boolean atSequenceEnd()
		{
			return atLineEnd;
		}
		
		private class Token {
			String token;
			boolean last;
			
			public Token(String token)
			{
				this.token = token;
				this.last = false;
			}
		}
	}

	@Override
	protected boolean ignore(File file)
	{
		return false;
	}
}
