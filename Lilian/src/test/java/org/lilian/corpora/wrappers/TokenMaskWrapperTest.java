package org.lilian.corpora.wrappers;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.corpora.Corpora;
import org.lilian.corpora.SequenceCorpus;

public class TokenMaskWrapperTest {

	@Test
	public void testSetMask() {
		SequenceCorpus<String> 
			corpus = Corpora.quickCorpus("a b c a b c. b c b c b c. a b c a b c."),
			wrapped = TokenMask.mask(corpus, "a", "b");
		
		int aCount = 0, bCount = 0;
		for(String token : wrapped)
		{
			assertTrue(token.equals("a") || token.equals("b"));
			
			if(token.equals("a"))
				aCount++;
			
			if(token.equals("b"))
				bCount++;
		}
		
		assertEquals(4, aCount);
		assertEquals(7, bCount);
	}
}
