package org.lilian.adios;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.corpora.Corpora;
import org.lilian.corpora.SequenceCorpus;

public class MexFunctionsTest {

	@Test
	public void testCheckWrongSpaces()
	{
		SequenceCorpus<String> candidate; 
		SequenceCorpus<String> gold     ; 
		
		candidate  = Corpora.quickCorpus("abc abc abc. abc abc."); 
		gold       = Corpora.quickCorpus("abc abc abc. abc abc."); 
		
		assertEquals(MexFunctions.checkWrongSpaces(candidate, gold), 0);

		candidate  = Corpora.quickCorpus("ab c abc abc. a bc abc."); 
		gold       = Corpora.quickCorpus("abc abc abc. abc abc."); 
		
		assertEquals(MexFunctions.checkWrongSpaces(candidate, gold), 2);
		assertEquals(MexFunctions.checkWrongSpaces(gold, candidate), 2);		
	}

}
