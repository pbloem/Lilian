package org.lilian.search.evo;

import static java.lang.Math.*;
import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.util.BitString;
import org.lilian.util.BitStrings;
import org.lilian.util.Series;

public class GATest
{

	@Test
	public void test()
	{
		Global.random = new Random();
		int n = 100, g = 100;
		
		List<BitString> pop = new ArrayList<BitString>();
		for(int i : series(n))
			pop.add(BitString.random(63));
		
		GA<BitString> ga = new GA<BitString>(pop,new BitStringCrossover(), new BitStringMutator(), new DoubleTarget(), 0.1);
		
		for(int i : series(g))
		{
			ga.breed();
			// System.out.println(BitStrings.toDouble(ga.best().genes()) + " " + ga.best().fitness());
		}
		assertEquals(BitStrings.toDouble(ga.best().genes()), 1.0, 0.00000000000001);
	}
	

	/**
	 * 
	 * @author Peter
	 *
	 */
	private class DoubleTarget implements Target<BitString>
	{
		double base = 3.0;
		
		@Override
		public double score(BitString object)
		{
			double o = BitStrings.toDouble(object);
			if(Double.isNaN(o))
				return Double.NEGATIVE_INFINITY;
			// double base = Global.random.nextGaussian();
			
			double score = -abs(pow(base, o) - base);
			// System.out.println(base + " " + o + " " + score);
			return score;
		}
		
	}
}
