package org.lilian.util.models.markov;

import org.junit.Test;

import org.lilian.corpora.Corpora;
import org.lilian.models.markov.*;
import org.lilian.util.Series;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.lilian.util.Functions.*;

import java.util.*;

public class TrieMarkovModelTest {

	@Test
	public void testBasics() 
	{
		
		MarkovModel<String> mm = new TrieMarkovModel<String>(3);

		for(String token : sentence("a b a a b b"))
			mm.add(token);
		mm.cut();
		
		for(String token : sentence("b a b b b"))
			mm.add(token);
		mm.cut();

		for(String token : sentence("b b b a b b"))
			mm.add(token);
		mm.cut();
		
		assertEquals(2, (int)mm.distinct(1));
		assertEquals(4, (int)mm.distinct(2));
		assertEquals(7, (int)mm.distinct(3));

		assertEquals(17, (int)mm.total(1));
		assertEquals(14, (int)mm.total(2));
		assertEquals(11, (int)mm.total(3));		
		
		assertEquals(5, (int)mm.frequency(sentence("a")));
		assertEquals(12, (int)mm.frequency(sentence("b")));
		
		assertEquals(1, (int)mm.frequency(sentence("a a")));
		assertEquals(4, (int)mm.frequency(sentence("a b")));
		assertEquals(3, (int)mm.frequency(sentence("b a")));
		assertEquals(6, (int)mm.frequency(sentence("b b")));
		
	}
	
	@Test @SuppressWarnings("unchecked")	
	public void testTokens() 
	{
		
		MarkovModel<String> mm = new TrieMarkovModel<String>(3);
		mm.add(Corpora.quickCorpus("a b b b a c d. c b a b a b b c. a b c b b a a"));

		List<List<String>> tokens;
		tokens = mm.sortedTokens(1);

		assertEquals(asList(asList("b"),asList("a"), asList("c"), asList("d")), tokens);
		
		System.out.println(mm.sortedTokens(3));
			
	}
}
