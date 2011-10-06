package org.lilian.models.old;

import java.util.*;
import java.io.*;

import org.lilian.corpora.*;
import org.lilian.util.*;

/**
 * This class is a subclass of NGramTable, that takes sentence ends into account. Before
 * every sentence n marker tokens are inserted (where n is the order of the table), and n 
 * after each sentence.
 */

public class MarkeredTable extends NGramTable<String> {
	
	private String startBase = "start";
	private String endBase = "end";

	public MarkeredTable(int order)
	{
		super(order);
	}
	
	public MarkeredTable(SequenceCorpus<String> corpus, int order)
	throws IOException
	{
		this(order);
		add(corpus);
	}
	
	public void add(SequenceCorpus<String> corpus)
	throws IOException
	{
		sentenceStart();
		
		SequenceIterator<String> si = corpus.iterator();
		
		while(si.hasNext())
		{
			if(si.atSequenceEnd())
				setSentenceBreak();
			add(si.next());
		}
		
		sentenceEnd();
	}
	
	/**
	 * When adding tokens using add(String token), this method
	 * can be used to signal that there is a sentence break between 
	 * the last token and the next.
	 */
	public void setSentenceBreak()
	{
		sentenceStart();
		sentenceEnd();
	}
	private void sentenceStart()
	{
		for(int i = 1; i <= order; i++)
			add(startBase + "_" + i);
	}
	private void sentenceEnd()
	{
		for(int i = order; i > 0; i--)
			add(endBase + "_" + i);
	}
}
