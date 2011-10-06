package org.lilian.corpora;

import java.util.*;
import java.util.regex.*;
import java.io.*;

/**
 * This class represents a corpus based on plain western text. 
 * 
 * It reads a unicode text file, and assumes western text. Whitespace delimits 
 * tokens, Period, exclamation mark and question mark delimit sentences. It 
 * attempts to recognize where they do not (inside quotes, periods at the end 
 * of abbreviations). This corpus will usually be only approximately correct.
 * 
 * TODO:
 *   - This is not yet implemented properly. The method used should be much more 
 *     elegant (especially when dealing with quoted text), and tested more thoroughly
 * 
 * @author Peter Bloem
 *
 */

public class WesternCorpus 
	extends AbstractCorpus<String> 
	implements SequenceCorpus<String> 
{
	private int bufferSize = 15;
	private File sourceFile;
	private boolean caseSensitive; 
	
	public WesternCorpus(File sourceFile, boolean caseSensitive)
	{
		this.sourceFile = sourceFile;
		this.caseSensitive = caseSensitive;
	}

	@Override
	public SequenceIterator<String> iterator() {
		return new WesternIterator();
	}
	
	private class WesternIterator 
		extends	AbstractCorpusIterator<String> 
		implements SequenceIterator<String> 
	{
		private Vector<Token> buffer;
		// the file reader 
		private BufferedReader in;
		
		private boolean atSentenceEnd;
		private boolean atFileEnd;
		
		/**
		 * Creates a corpus of this western text document, tokenized by words.
		 * 
		 * @param sourceFile The text file to interpret
		 * @param caseSensitive If this is true, all letter will be left as is, 
		 * 						if false, everything will be converted to lowercase. 
		 * @throws IOException
		 */
		public WesternIterator()
		{
			init();
		}
		
		/**
		 *
		 */
		public void init()
		{	
			buffer = new Vector<Token>(bufferSize);
			try {
				in = new BufferedReader(new FileReader(sourceFile)); }
			catch (IOException e) {
				throw new RuntimeException(e); }
			
			atFileEnd = false;
			atSentenceEnd = false;
		}	
	
		/**
		 * 
		 */
		public boolean atSequenceEnd() 
		{
			return atSentenceEnd;
		}
	
		/**
		 * 
		 */
		public boolean hasNext() 
		{
			return !(atFileEnd && buffer.size() == 0);
		}
	
		/**
		 *
		 */
		public String next() 
		{
			while(buffer.size() < bufferSize && !atFileEnd)
				try {
					fillBuffer(); }
				catch (IOException e) {
					throw new RuntimeException(e); }
				
			
			if(buffer.get(0).isLastInSentence())
				atSentenceEnd = true;
			else
				atSentenceEnd = false;
	
			String result = buffer.remove(0).getValue();
			
			if(!caseSensitive)
				result = result.toLowerCase();
			
			return result; 
		}
		
		private void fillBuffer() throws IOException
		{
			int start = buffer.size();
			String line = in.readLine();
			if(line ==  null)
			{
				atFileEnd = true;
			}else
			{
				// tokenize and add to the buffer
				StringTokenizer st = new StringTokenizer(line);
				while(st.hasMoreTokens())
					buffer.add(new Token(st.nextToken()));
				
				// check for and interpret sentence delimiters and other 
				// non-word characters
				Token token;
				String value;
							
				if(start > 0)
					start = start - 1;
				for(int i = start; i <  buffer.size() - 1; i++)
				{
					token = buffer.get(i);
					value = token.getValue();
					
	//System.out.print('!' + value + '!');
					
					if(value.length() == 1 && !Character.isLetterOrDigit(value.charAt(0)))
					{	
						// check if the are just sentence delimiters				
						if(value.equals("!") || value.equals("?"))
							buffer.get(i-1).setLastInSentence(true);
						else if(value.equals("."))
							if(i+1 < buffer.size() && 
									Character.isUpperCase(buffer.get(i+1).getValue().charAt(0)))
								buffer.get(i-1).setLastInSentence(true);
						
						buffer.remove(i);
					}else
					{
						char lastChar = value.charAt(value.length()-1);
						if(lastChar == '?' || lastChar =='!')
						{
							value = value.substring(0, value.length()-1);
							token.setValue(value);
							token.setLastInSentence(true);
						}else if(lastChar == '.')
						{
							value = value.substring(0, value.length()-1);
							token.setValue(value);
							if(i+1 < buffer.size() && 
									Character.isUpperCase(buffer.get(i+1).getValue().charAt(0)))
								token.setLastInSentence(true);
						}
						
						//trim any non
						value = trim(value);
						token.setValue(value);
						
						if(value.length() < 1)
							buffer.remove(i);
					}
				}
			}
			
			// if we've found the file end, trim the last token in the buffer
			if(atFileEnd && buffer.size() > 0)
			{
				Token token = buffer.get(buffer.size()-1);
				String value = token.getValue();
				
				if(value.length() == 1 && !Character.isLetterOrDigit(value.charAt(0)))
					buffer.remove(buffer.size()-1);
				else
					token.setValue(trim(value));							
			}			
		}
		
		/**
		 * Trims a string of its whitespace and non-alphanumeric characters
		 */
		private String trim(String in)
		{
			//System.out.println(in);
			if(in.length() == 0)
				return in;
	
			int start = 0;
			int end = in.length() - 1;
			char current;
	
			current = in.charAt(start);
			//read up to the first non trimmable character
			while(	start < in.length() - 1 &&
		 			!Character.isLetterOrDigit(current)
			)
			{
				start++;
				current = in.charAt(start);
			}
	
	
	
			current = in.charAt(end);
			//read back to the last non trimmable character
			while(	end > start &&
		 			!Character.isLetterOrDigit(current)
			)
			{
					end--;
					current = in.charAt(end);
			}
			
			return in.substring(start, end + 1);
		}
	
		private class Token
		{
			private String token;
			private boolean lastInSentence = false;
			
			public Token(String token)
			{
				this.token = token;
			}
	
			public void setValue(String token)
			{
				this.token = token;			
			}
			
			public String getValue()
			{
				return token;
			}
			
			public void setLastInSentence(boolean lastInSentence)
			{
				this.lastInSentence = lastInSentence;
			}
			
			public boolean isLastInSentence()
			{
				return lastInSentence;
			}
		}
	}
}
