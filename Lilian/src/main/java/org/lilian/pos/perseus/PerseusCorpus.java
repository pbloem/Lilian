package org.lilian.pos.perseus;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import org.lilian.corpora.AbstractFileSequenceCorpus;
import org.lilian.corpora.SequenceIterator;
import org.lilian.pos.Tag;
import org.lilian.pos.TagSet;
import org.lilian.pos.TaggedCorpus;
import org.lilian.pos.TaggedWord;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class PerseusCorpus extends AbstractFileSequenceCorpus<TaggedWord>
	implements TaggedCorpus
{

	private TagSet tagSet = new PerseusTagSet();
	
	public PerseusCorpus(File... files) {
		super(files);
	}	

	@Override
	protected SequenceIterator<TaggedWord> singleIterator(File file)
	{
		return new PerseusIterator(file);
	}
	
	private class PerseusIterator implements SequenceIterator<TaggedWord>
	{	
		private static final int BUFFER_SIZE = 5;

		private File file;		
		private Deque<Token> buffer = new LinkedList<Token>();
		
		private Document document;
		private NodeList sentences;
		
		private boolean last = false;
		
		private int lastSentence = -1;
		
		public PerseusIterator(File file)
		{
			this.file = file;

			DOMParser parser = new DOMParser();			
			try 
			{
				parser.parse(file.getCanonicalPath());				
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			} catch(SAXException e)
			{
				throw new RuntimeException(e);
			}
			
		    document = parser.getDocument();
		    sentences = document.getElementsByTagName("sentence");
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
			if(lastSentence+1 >= sentences.getLength())
				return;
			
			lastSentence++;			
			Element sentence = (Element) sentences.item(lastSentence);
			NodeList words = sentence.getElementsByTagName("word");
			for(int i = 0; i < words.getLength(); i++)
			{
				Element word = (Element) words.item(i);
				
				String wordString = word.getAttribute("form");
				String tagString = word.getAttribute("postag");
				
				TaggedWord tw = 
					new TaggedWord(wordString, tagSet.getTag(tagString));
				
				buffer.add(new Token(tw));				
			}
			
			if(words.getLength() > 0)
				buffer.peekLast().makeLast();
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
			
			public Token(TaggedWord word) {
				this.word = word;
			}

			@Override
			public String toString() {
				return word + (this.last ? "!" : "");
			}
			
			public void makeLast()
			{
				last = true;
			}
		}		
	}

	@Override
	protected boolean ignore(File file)
	{
		// Anything that isn't xml
		if((! file.isDirectory()) && ! file.getName().toLowerCase().endsWith(".xml"))
			return true;		
		
		// Any readme file
		if(file.getName().toLowerCase().startsWith("readme"))
			return true;
		
		// The metadata
		if(file.getName().toLowerCase().startsWith("ldt"))
			return true;		
		
		return false;	
	}

	@Override
	public TagSet tagSet()
	{
		return tagSet;
	}

}
