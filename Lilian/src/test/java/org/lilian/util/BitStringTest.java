package org.lilian.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.lilian.*;
import org.lilian.util.*;

import java.util.*;

public class BitStringTest {
	
	public void setup()
	{
		Global.random = new Random();
	}
	
	@Test
	public void bitStringTest1()
	{
		int length = Global.random.nextInt(100);
		String str = "";
		for(int i = 0; i < length; i++)
			str += Global.random.nextBoolean() ? '0' : '1';
		
		BitString bs =  BitString.parse(str);
		
		assertEquals(str, bs.toString());
	}
	
	@Test
	public void bitStringTest2()
	{
		String in  = "101010101010101010101010101010",
		       out = "010010101010010010101000011111";
		BitString bs = BitString.parse(in);
		bs.set(0, false);
		bs.set(1, true);		
		bs.set(2, false);
		
		bs.set(12, false);
		bs.set(13, true);		
		bs.set(14, false);		

		bs.set(22, false);
		bs.set(23, false);
		bs.set(24, false);
		bs.set(25, false);
		bs.set(26, false);		
				
		bs.set(25, true);
		bs.set(26, true);
		bs.set(27, true);
		bs.set(28, true);
		bs.set(29, true);		
		
		assertEquals(out, bs.toString());
	}
	
	@Test
	public void paddingTest()
	{
		BitString bs = random(Global.random.nextInt(100));
		assertEquals(0, (bs.size() + bs.padding()) % 8);		
	}
	
	@Test
	public void zerosTest()
	{
		String exp = "0000000000000000000000000";
		BitString bs = BitString.zeros(exp.length());
		
		assertEquals(exp, bs.toString());
	}
	
	@Test
	public void toIntegerTest()
	{
		BitString string = new BitString(2);
		for(int i : Series.series(129))
			string.add(false);
		
		List<Integer> ints = string.toIntegers();
		assertEquals(5, ints.size());
	}

	@Test
	public void toIntegerTest2()
	{
		BitString string = new BitString(2);
		for(int i : Series.series(1000))
			string.add(Global.random.nextBoolean());
		
		List<Integer> ints = string.toIntegers();
		double sum = 0.0;
		for(int i : ints)
			sum += i;
		
		System.out.println(sum);
	}
	
	/** 
	 * Returns a random string
	 */
	public BitString random(int length)
	{
		String str = "";
		for(int i = 0; i < length; i++)
			str += Global.random.nextBoolean() ? '0' : '1';
		
		return BitString.parse(str);
	}
	
	


}
