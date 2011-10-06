package org.lilian.corpora;

import java.io.*;
import java.util.*;

/**
 * This class reads a corpus as defined by the official Adios implementation.
 * 
 * This corpus can read all files that the official Adios implemetation can 
 * read, however, not all files that this corpus can read can be read by Adios.
 * (ie. this corpus is more lenient than the official implementation)
 *  
 * The corpus contains sentences between '*' and '#'. All tokens are delimited
 * by whitespace. any characters between # and * are ignored.
 * 
 * @author Peter Bloem
 */

public class AdiosCorpus 
	extends AbstractCorpus<String> 
	implements SequenceCorpus<String> {
	
	private int bufferSize = 15;
	private File sourceFile;
	
	public AdiosCorpus(File sourceFile)
	{
		this.sourceFile = sourceFile;
	}

	@Override
	public SequenceIterator<String> iterator() 
	{
		return new AdiosIterator();
	}
	
	private class AdiosIterator 
		extends AbstractCorpusIterator<String> 
		implements SequenceIterator<String> 
	{
		private List<Token> buffer;
		// the file reader 
		private BufferedReader in;
		
		private boolean atSequenceEnd;
		private boolean atFileEnd;	
	
		public AdiosIterator()
		{
			buffer = new ArrayList<Token>(bufferSize);
			
			try {
				in = new BufferedReader(new FileReader(sourceFile));
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
			
			atFileEnd = false;
			atSequenceEnd = false;
		}
		
		
		public boolean hasNext()
		{
			return !(atFileEnd && buffer.size() == 0);
		}
	
		public String next()
		{
			while(buffer.size() < bufferSize && !atFileEnd)
				try {
					fillBuffer(); }
				catch(IOException e) {
					throw new RuntimeException(e); }
			
			if(buffer.get(0).isLastInSentence())
				atSequenceEnd = true;
			else
				atSequenceEnd = false;
	
			return buffer.remove(0).value();
		}
		
		public boolean atSequenceEnd()
		{
			return atSequenceEnd;
		}
		
		private void fillBuffer() throws IOException
		{
			int current = 0;
			// read until we find '*'
			while(current != (int)'*' && current != -1)
				current = in.read();
			
			if(current == -1)
			{
				atFileEnd = true;
				return;
			}
			
			StringBuilder sb = new StringBuilder();
			current = in.read();
			
			// read into stringbuilder until we find '#'
			while(current != (int)'#' && current != -1)
			{
				sb.append((char)current);
				current = in.read();
			}
			
			if(current == -1)
			{
				atFileEnd = true;
				// if we don't get the closing '#' before the file ends, 
				// we ignore the incomplete sentence
				return;
			}
			
			StringTokenizer st = new StringTokenizer(sb.toString());
			
			while(st.hasMoreTokens())
				buffer.add(new Token(st.nextToken()));
			
			buffer.get(buffer.size()-1).setLastInSentence(true);		
		}
		
		/**
		 * A small helper class. We wrap the string tokens in these and put these in
		 * the buffer, so that the sentence ends can be easily read from the buffer. 
		 */
		private class Token
		{
			private String token;
			private boolean lastInSentence = false;
			
			public Token(String token)
			{
				this.token = token;
			}
			
			public String value()
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
	
	/**
	 * Writes a corpus in ADIOs formatting, based on the input corpus 
	 * @param corpus The input corpus to write to a file in ADIOS formatting
	 */
	public static void write(SequenceIterator<?> in, File directory, String base)
	throws IOException
	{
		File f = new File(directory, base + ".corpus.txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		
		out.write(" *  ");		
		while(in.hasNext())
		{
			out.write(in.next().toString());
			if(in.atSequenceEnd())
				out.write("  # \n *  ");
			else
				out.write(" ");				
		}
		out.write("  # ");
		
		out.flush();
		out.close();		
	}	
}
