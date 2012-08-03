package org.lilian.adios;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.corpora.Corpora;
import org.lilian.corpora.Corpus;
import org.lilian.corpora.SequenceCorpus;

public class MexGraphTest {

	@Test
	public void testMexGraph() 
	{
		SequenceCorpus<String> corpus = Corpora.quickCorpus("a a b c. a a. b b b c.");
		
		MexGraph<String> graph = new MexGraph<String>(corpus);
		
		System.out.println(graph);
	}

}
