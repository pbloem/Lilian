package org.lilian.grammars;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.util.List;

import org.junit.Test;
import org.lilian.util.Series;

public class TestTestGrammars
{

	@Test
	public void test()
	{

		Grammar<String> grammar = TestGrammars.adriaansVervoort();
		// Grammar<String> grammar = TestGrammars.ta1();
	
		for(int i : series(25))
		{
			List<String> sentence = grammar.generateSentence("S", 12, 24);
			Parse<String> parse = grammar.parse(sentence);
			
			assertTrue(parse.isMember());
		}
			
	}

}
