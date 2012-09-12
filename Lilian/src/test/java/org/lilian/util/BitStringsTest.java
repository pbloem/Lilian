package org.lilian.util;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;
import org.lilian.Global;

public class BitStringsTest
{

	@Test
	public void testLong()
	{
		Global.random = new Random();
		long l = Global.random.nextLong();
		
		l = Math.abs(l);
		BitString bs = BitStrings.fromLong(l);
		
		assertEquals(l, BitStrings.toLong(bs));
		
	}
	
	@Test
	public void testDouble()
	{
		Global.random = new Random();
		double d = Double.longBitsToDouble(Global.random.nextLong());
		
		BitString bs = BitStrings.fromDouble(d);
		
		assertEquals(d, BitStrings.toDouble(bs), 0.0);
	}	

}
