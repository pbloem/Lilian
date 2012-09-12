package org.lilian.grammars;

import java.util.List;

public class Grammars
{

	public static <T> double precision(Grammar<T> grammar, List<List<T>> data)
	{
		double accepted = 0.0;
		for(List<T> sentence : data)
			if(grammar.parse(sentence).isMember())
				accepted++;
		
		return accepted / data.size();
	}
}
