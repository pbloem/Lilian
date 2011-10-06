package org.lilian.randomness;

import java.util.List;

import org.lilian.util.Compressor;

public class CompressionTest<T> implements RandomnessTest<T> 
{
	Compressor<T> comp;
	
	public CompressionTest(Compressor<T> comp) 
	{
		this.comp = comp;
	}

	@Override
	public double test(List<T> sequence) 
	{
		return 1.0/comp.ratio(sequence.toArray());
	}
}
