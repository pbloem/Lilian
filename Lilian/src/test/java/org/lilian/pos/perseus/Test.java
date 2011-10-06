package org.lilian.pos.perseus;

import java.io.File;
import java.util.Iterator;

import org.lilian.corpora.SequenceIterator;
import org.lilian.pos.TaggedCorpus;
import org.lilian.pos.TaggedWord;

public class Test
{
	
	public static void main(String[] args)
	{
		TaggedCorpus corpus = new PerseusCorpus(new File("./data/perseus/data/"));
		
		SequenceIterator<TaggedWord> it = corpus.iterator();
		while(it.hasNext())
		{
			System.out.print(it.next() + " ");
			if(it.atSequenceEnd())
				System.out.println();
		}
		
	}

}
