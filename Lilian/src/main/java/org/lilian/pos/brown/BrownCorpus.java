package org.lilian.pos.brown;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lilian.corpora.AbstractCorpus;
import org.lilian.corpora.AbstractFileCorpus;
import org.lilian.corpora.AbstractFileSequenceCorpus;
import org.lilian.corpora.SequenceIterator;
import org.lilian.pos.Tag;
import org.lilian.pos.TagSet;
import org.lilian.pos.TaggedCorpus;
import org.lilian.pos.TaggedWord;


public class BrownCorpus extends AbstractFileSequenceCorpus<TaggedWord>
	implements TaggedCorpus {
	
	private Pattern tokenPattern = Pattern.compile("(.*)/([^/]*)");

	public BrownCorpus(File... files) {
		super(files);
	}

	@Override
	public SequenceIterator<TaggedWord> singleIterator(File file)
	{
		return new BrownIterator(file);
	}
	
	private class BrownIterator implements SequenceIterator<TaggedWord>
	{
		private static final int BUFFER_SIZE = 5;
		
		private Deque<Token> buffer = new LinkedList<Token>(); 
		private BufferedReader reader;
		
		// Whether the last token returned was the last in its sequence 
		private boolean last = false;
		
		private File file;

		public BrownIterator(File file) 
		{
			this.file = file;
			
			try {
				this.reader = new BufferedReader(new FileReader(file));
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public TaggedWord next() {
			fillBuffer();
			
			Token t = buffer.poll();
			last = t.last;
			return t.word;
		}
		
		private void fillBuffer() 
		{
			try 
			{
				while(buffer.size() < BUFFER_SIZE)
				{
					// Read the next line, and skip empty ones
					String line = "";					
					while(line != null && line.length() == 0)
						line = reader.readLine();
					
					// Stop if no more lines
					if(line == null)
						break;					
					
					// Parse the line, put tokens in buffer
					StringTokenizer st = new StringTokenizer(line);
					Token lastToken = null;
					while(st.hasMoreTokens())
					{
						String token = st.nextToken();
						Matcher matcher = tokenPattern.matcher(token);
						
						if(! matcher.matches())
							throw new RuntimeException("Can't parse token: " + token + " Does the token contain '/'? Current file " + file.getCanonicalPath());

						String word = matcher.group(1);
						String tag = matcher.group(2);						
						
						Tag t;
						try{
							t = tagSet().getTag(tag);
						} catch(IllegalArgumentException e)
						{
							throw new RuntimeException("Tag not recognized in token " + token + ". Current file " + file.getCanonicalPath(), e);
						}
						
						TaggedWord tw = new TaggedWord(word, t);
						
						lastToken = new Token(tw, false);
						buffer.add(lastToken);
					}
					
					// Last token of the line is the last token in the sequence
					if(lastToken != null) // This is true if the line contained no tokens
						lastToken.last = true;
				}	
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
			
		}

		@Override
		public boolean hasNext() {
			fillBuffer();
			
			return ! buffer.isEmpty();
		}

		@Override
		public boolean atSequenceEnd() {
			return last;			
		}
		
		private class Token {
			TaggedWord word;
			boolean last = false;
			
			public Token(TaggedWord word, boolean last) {
				this.word = word;
				this.last = last;
			}

			@Override
			public String toString() {
				return word + (this.last ? "!" : "");
			}
			
			
		}

	}
	
	@Override
	public TagSet tagSet() {
		return TAGSET;
	}
	
	private static final BrownTagSet TAGSET = new BrownTagSet();

	@Override
	protected boolean ignore(File file) 
	{
		// The readme file
		if(file.getName().toLowerCase().startsWith("readme"))
			return true;
		
		// The category file
		if(file.getName().toLowerCase().startsWith("cats"))
			return true;
		
		// The contents file
		if(file.getName().toLowerCase().startsWith("contents"))
			return true;		
		
		// Files that may be present if nltk generated te distribution
		if(file.getName().toLowerCase().endsWith(".pickle"))
			return true;		

		return false;
	}

}
