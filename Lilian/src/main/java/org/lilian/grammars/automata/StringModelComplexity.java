package org.lilian.grammars.automata;

import org.lilian.util.GZIPCompressor;

/**
 * 
 */

public class StringModelComplexity<T> implements Complexity<T>
{
	private GZIPCompressor<T> gzip;
	
	public StringModelComplexity()
	{
		gzip = new GZIPCompressor<T>();
	}
	
	public double get(Automaton<T> a)
	{
		return gzip.compressedSize(a.toString());		
	}

}
