package org.lilian.util;

import static org.junit.Assert.*;
import static org.lilian.util.Functions.mod;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FunctionsTest
{

	@Test
	public void testMod()
	{
		assertEquals(5, mod(15, 10));
		assertEquals(9, mod(-1, 10));

	}
	
	@Test
	public void logs()
	{
		List<Double> vals = Arrays.asList(-269647.432, -231444.981, -231444.699); 
		
		List<Double> res = Functions.normalizeLog(vals, Math.E);
		System.out.println(res);
		
		assertEquals(0.0, res.get(0), 0.01);
		assertEquals(0.430, res.get(1), 0.01);
		assertEquals(0.570, res.get(2), 0.01);
	}
	
	@Test
	public void logs2()
	{
		List<Double> vals = Arrays.asList(-269647.432, -231444.981, -231444.699, Double.NEGATIVE_INFINITY); 
		
		List<Double> res = Functions.normalizeLog(vals, Math.E);
		System.out.println(res);
		
	}

}
