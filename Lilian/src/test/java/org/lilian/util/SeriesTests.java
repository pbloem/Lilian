package org.lilian.util;

import static org.junit.Assert.*;
import static org.lilian.util.Functions.*;
import static org.lilian.util.Series.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.lilian.util.ranges.Range;

public class SeriesTests
{

	public static List<Integer> empty = Collections.emptyList();
	public static List<Double> emptyD = Collections.emptyList();	
	
	@Test
	public void testSeriesInt()
	{
		compare(
				Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
				series(10));
		compare(empty,
				series(0));
		compare(
				Arrays.asList(0, -1, -2, -3, -4, -5, -6, -7, -8, -9),
				series(-10));
	}
	
	@Test
	public void testSeriesIntInt()
	{
		compare(
				Arrays.asList(-3, -2, -1, 0, 1, 2),
				series(-3, 3));
		compare(
				Arrays.asList(3, 2, 1, 0, -1, -2),
				series(3, -3));
		compare(
				empty,
				series(3, 3));
	}

	@Test
	public void testSeriesIntIntInt()
	{
		compare(
				Arrays.asList(-3, -1, 1),
				series(-3, 2, 3));
		compare(
				Arrays.asList(3, 1, -1),
				series(3, -2, -3));		
		compare(
				empty,
				series(3, 1, 3));	
		
		boolean caught = false;
		try
		{
			series(3, 2, -3);
		} catch(IllegalArgumentException e)
		{
			caught = true;
		}
		assertTrue(caught);
		
		caught = false;
		try
		{
			series(3, -2, -3).get(3);
		} catch(IndexOutOfBoundsException e)
		{
			caught = true;
		}
		assertTrue(caught);
		
		caught = false;
		try
		{
			series(3, -2, -3).get(-1);
		} catch(IndexOutOfBoundsException e)
		{
			caught = true;
		}
		assertTrue(caught);		
		
	}
	
	/**
	 * Performs a range of tests comparing an expected to an actual series
	 * 
	 */
	public void compare(List<Integer> expected, List<Integer> actual)
	{
		try {
			// * test size
			assertEquals((long)expected.size(), (long)actual.size());
			
			// * compare isEmpty		
			assertEquals(expected.isEmpty(), actual.isEmpty());		
			
			// * test contains 
			for(Integer e : expected)
				assertTrue("contains("+e+") should be true", actual.contains(e));
			
			for(int i = -100; i < 100; i++)
				assertEquals(
						"Failed for integer " + i, 
						expected.contains(i), 
						actual.contains(i));
			
			// * compare iterators
			Iterator<Integer> expectedIterator = expected.iterator();
			Iterator<Integer> actualIterator = actual.iterator();
			while(expectedIterator.hasNext())
			{
				assertEquals(expectedIterator.hasNext(), actualIterator.hasNext());
				assertEquals(expectedIterator.next(), actualIterator.next());			
			}
			
			// * test toString
			assertEquals(expected.toString(), actual.toString());
			
		} catch(AssertionError e)
		{
			System.out.println("Failed for expected: _"+expected+"_ and actual: _"+actual+"_ ");
			throw e;
		}
	}

	@Test
	public void testSeriesDouble()
	{
		compareD(
				Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0),
				series(10.0));
		compareD(emptyD,
				series(0.0));
		compareD(
				Arrays.asList(0.0, -1.0, -2.0, -3.0, -4.0, -5.0, -6.0, -7.0, -8.0, -9.0),
				series(-10.0));
	}

	@Test
	public void testSeriesDoubleDouble()
	{
		compareD(
				Arrays.asList(-3.0, -2.0, -1.0, 0.0, 1.0, 2.0),
				series(-3.0, 3.0));
		compareD(
				Arrays.asList(3.0, 2.0, 1.0, 0.0, -1.0, -2.0),
				series(3.0, -3.0));
		compareD(
				emptyD,
				series(3.0, 3.0));
	}

	@Test
	public void testSeriesDoubleDoubleDouble()
	{
		compareD(
				Arrays.asList(-3.0, -1.0, 1.0),
				series(-3.0, 2.0, 3.0));
		compareD(
				Arrays.asList(3.0, 1.0, -1.0),
				series(3.0, -2.0, -3.0));		
		compareD(
				emptyD,
				series(3.0, 1.0, 3.0));	
		
		boolean caught = false;
		try
		{
			series(3.0, 2.0, -3.0);
		} catch(IllegalArgumentException e)
		{
			caught = true;
		}
		assertTrue(caught);
		
		caught = false;
		try
		{
			series(3.0, -2.0, -3.0).get(3);
		} catch(IndexOutOfBoundsException e)
		{
			caught = true;
		}
		assertTrue(caught);
		
		caught = false;
		try
		{
			series(3.0, -2.0, -3.0).get(-1);
		} catch(IndexOutOfBoundsException e)
		{
			caught = true;
		}
		assertTrue(caught);			
	}
	
	/**
	 * Performs a range of tests comparing an expected to an actual series
	 * 
	 */
	public void compareD(List<Double> expected, List<Double> actual)
	{
		try {
			// * test size
			assertEquals((long)expected.size(), (long)actual.size());
			
			// * compare isEmpty		
			assertEquals(expected.isEmpty(), actual.isEmpty());		
			
			// * test contains 
			for(Double e : expected)
				assertTrue("contains("+e+") should be true", actual.contains(e));
			
			for(double i = -100.0; i < 100.0; i += 0.1)
				assertEquals(
						"Failed for double " + i, 
						expected.contains(i), 
						actual.contains(i));
			
			// * compare iterators
			Iterator<Double> expectedIterator = expected.iterator();
			Iterator<Double> actualIterator = actual.iterator();
			while(expectedIterator.hasNext())
			{
				assertEquals(expectedIterator.hasNext(), actualIterator.hasNext());
				assertEquals(expectedIterator.next(), actualIterator.next());			
			}
			
			// * test toString
			assertEquals(expected.toString(), actual.toString());
			
		} catch(AssertionError e)
		{
			String message = "Failed for expected: _"+expected+"_ and actual: _"+actual+"_ ";
			System.out.println(message);
			
			throw e;			
		}
	}	
	
	@Test
	public void performance()
	{
		int n = 10000000;
		long z;
		
		tic();
		z = 0;
		for(int a = 0; a < n; a++)
		{
			z++;
			for(int b = 0; b < 10; b++)
				z += a * b; 
		}
		System.out.println("Regular for loops finished in " + toc() + " seconds ("+z+")");

		tic();
		z = 0;
		for(int a = 0; a < n; a++)
		{
			z++;
			for(int b : series(10))
				z += a * b; 
		}
		System.out.println("Ranged for loops finished in " + toc() + " seconds ("+z+")");

		tic();
		List<Integer> s = series(10);
		z = 0;
		for(int a = 0; a < n; a++)
		{
			z++;
			for(int b : s)
				z += a * b; 
		}
		System.out.println("Ranged for loops (single range) finished in " + toc() + " seconds ("+z+")");		
	}
	
	public void performanceAnalog()
	{
		int n = 1000000;
		long z;		
		
		List<Integer> list = new ArrayList<Integer>(series(100));
		
		tic();
		z = 0;
		for(int a = 0; a < n; a++)
		{
			z++;
			for(int b = 0; b < list.size(); b++)
				z += a * list.get(b); 
		}
		System.out.println("Regular for loop finished in " + toc() + " seconds ("+z+")");

		tic();
		z = 0;
		for(int a = 0; a < n; a++)
		{
			z++;
			Iterator<Integer> it = list.iterator();
			while(it.hasNext())
				z += a * it.next(); 
		}
		System.out.println("Iterator old fashioned finished in " + toc() + " seconds ("+z+")");

		tic();
		List<Integer> s = series(10);
		z = 0;
		for(int a = 0; a < n; a++)
		{
			z++;
			for(int b : list)
				z += a * b; 
		}
		System.out.println("Foreach loop finished in " + toc() + " seconds ("+z+")");		
	}
	
	@Test
	public void rangesTest()
	{
		assertEquals(
				Arrays.asList(new Range(1.0, 2.0)),
				Series.ranges(Arrays.asList(1, 2))
		);		
		
		assertEquals(
				Arrays.asList(
						new Range(Double.NEGATIVE_INFINITY, 1.0),
						new Range(1.0, 2.0),						
						new Range(2.0, Double.POSITIVE_INFINITY)
						),
				Series.ranges(Arrays.asList(1, 2), true)
		);
	}
}
