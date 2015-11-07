package org.lilian.util;

import static java.lang.Math.exp;
import static org.junit.Assert.*;
import static org.lilian.util.Functions.mod;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.nodes.util.Generators;

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
		
		
		vals = Arrays.asList(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY); 
		
		res = Functions.normalizeLog(vals, Math.E);
		System.out.println(res);
	}
	
	@Test
	public void testLogSum()
	{
		double a = -116326.21243521984;
		double b = -115293.85118645725 -71.48161875294834;
		
		System.out.println(b - a);
		System.out.println(1 + exp(b - a));
		
		System.out.println(Functions.logSumOld(a, b));
	}
	
	@Test
	public void testLogSum2()
	{
		double a = 1;
		double b = -812;
		
		System.out.println(a - b);
		System.out.println(1 + exp(a - b));
		
		System.out.println(Functions.logSumOld(a, b));
	}
	
	@Test
	public void testSort()
	{
		List<Integer> values = l(Arrays.asList(0, 2, 2, 8, 2, 2, 0, 3, 7, 9));
		List<Integer> l1 =     l(Arrays.asList(1, 2, 3, 4, 5, 5, 6, 7, 7, 9));
		List<Integer> l2 =     l(Arrays.asList(1, 2, 3, 4, 6, 0, 7, 8, 9, 10));
		Collections.reverse(l2);

		for(int i : series(values.size()))
			System.out.println(values.get(i) + "\t" + l1.get(i) + "\t" + l2.get(i));
		
		System.out.println();
		Comparator<Integer> comp = Functions.natural();
		Functions.sort(values, comp, l1, l2);
		
		for(int i : series(values.size()))
			System.out.println(values.get(i) + "\t" + l1.get(i) + "\t" + l2.get(i));

	}

	private <T> List<T> l(List<T> list)
	{
		return new ArrayList<T>(list);
	}
}
