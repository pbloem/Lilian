package org.lilian.util.models.markov;

import org.junit.Test;

import org.lilian.corpora.Corpora;
import org.lilian.models.markov.*;
import org.lilian.util.Series;

import static org.junit.Assert.*;
import static org.lilian.util.Functions.*;

import java.util.*;

public class BasicMarkovModelTest {

	@Test
	public void testBasics() 
	{
		
		MarkovModel<String> mm = new BasicMarkovModel<String>(3);

		for(String token : sentence("a b a a b c"))
			mm.add(token);
		mm.cut();
		
		for(String token : sentence("b a c b b"))
			mm.add(token);
		mm.cut();

		for(String token : sentence("c b c a d c"))
			mm.add(token);
		mm.cut();
		
		assertEquals(4, (int)mm.distinct(1));
		assertEquals(10, (int)mm.distinct(2));
		assertEquals(11, (int)mm.distinct(3));

		assertEquals(17, (int)mm.total(1));
		assertEquals(14, (int)mm.total(2));
		assertEquals(11, (int)mm.total(3));		
		
	}
	
	@Test
	public void testTokens() 
	{
		
		MarkovModel<String> mm = new BasicMarkovModel<String>(3);

		mm.add(Corpora.quickCorpus("a b b b a c d ; d e f g h"));

		for(int i : Series.series(1, 3))
			for(List<String> ng : mm.sortedTokens(i))
				System.out.println(ng);
	}
	

}
