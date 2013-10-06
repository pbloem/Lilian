package org.lilian.data.real;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Series;

public class GeneratorsTest
{

	@Test
	public void testExponential()
	{
		int n = 1000000;
		Generator<Integer> expo = Generators.exponential(0);
		
		BasicFrequencyModel<Integer> bfm = new BasicFrequencyModel<Integer>(expo.generate(n));
			
		assertEquals(0.5, bfm.probability(0), 0.01);
		assertEquals(0.25, bfm.probability(1), 0.01);
		assertEquals(0.125, bfm.probability(2), 0.01);

	}

}
