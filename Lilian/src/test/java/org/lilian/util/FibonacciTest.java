package org.lilian.util;

import static org.junit.Assert.*;
import static org.lilian.util.Functions.tic;
import static org.lilian.util.Functions.toc;
import static org.nodes.util.Fibonacci.isFibonacci;

import java.util.Random;

import org.junit.Test;
import org.lilian.Global;
import org.nodes.util.Fibonacci;

public class FibonacciTest
{

	@Test
	public void testIsFibonacci()
	{
		assertTrue(isFibonacci(0));
		assertTrue(isFibonacci(1));
		assertTrue(isFibonacci(1));
		assertTrue(isFibonacci(2));
		assertTrue(isFibonacci(3));
		assertTrue(isFibonacci(5));
		assertTrue(isFibonacci(8));
		assertTrue(isFibonacci(13));
		assertTrue(isFibonacci(21));
		assertTrue(isFibonacci(34));
		assertTrue(isFibonacci(55));
		assertTrue(isFibonacci(89));
		assertTrue(isFibonacci(144));
		assertTrue(isFibonacci(233));

		assertTrue(isFibonacci(196418));
		assertTrue(isFibonacci(5702887));
		assertTrue(isFibonacci(14930352));
		assertTrue(isFibonacci(39088169));

		assertFalse(isFibonacci(6));
		assertFalse(isFibonacci(3298));
		assertFalse(isFibonacci(837));
		assertFalse(isFibonacci(111));
	}

	@Test
	public void testGet()
	{
		long a = 0, b = 1;
		
		System.out.println(Long.MAX_VALUE);
		for(int i = 0; i < Fibonacci.MAX_INDEX+1; i++)
		{
			long c = Fibonacci.get(i);
			System.out.println(i + " " + c);
			assertEquals(c, a);
			a = b;
			b = b + c;
		}
	}
	
	@Test
	public void testIndexSpeed()
	{
		Global.random = new Random();
		
		int m = 10000000;
		
		tic();
		for(int i : Series.series(m))
		{
			int index = Global.random.nextInt(Fibonacci.MAX_INDEX+1);
			long n = Fibonacci.get(index);
			Fibonacci.getIndexApprox(n);
		}
		System.out.println(toc());

		tic();
		for(int i : Series.series(m))
		{
			int index = Global.random.nextInt(Fibonacci.MAX_INDEX+1);
			long n = Fibonacci.get(index);
			Fibonacci.getIndex(n);
		}
		System.out.println(toc());
		
		tic();
		for(int i : Series.series(m))
		{
			int index = Global.random.nextInt(Fibonacci.MAX_INDEX+1);
			long n = Fibonacci.get(index);
		}
		System.out.println(toc());
	}
	
	@Test
	public void testGetIndex()
	{
		long a = 0, b = 1, c = -1;
		
		for(int i = 0; i < Fibonacci.MAX_INDEX+1; i++)
		{
			c = a + b;
			System.out.println(i + " " + a + " " + Fibonacci.getIndex(a) + " " + Fibonacci.getIndexApprox(a));
			
			if(a != 1)
				assertEquals(i, Fibonacci.getIndex(a));
			
			a = b;
			b = c;
		}
	}
	
	@Test
	public void testPrev()
	{
		long a = 0, b = 1, c = -1;
		long prev = -1;
		
		for(int i = 0; i < Fibonacci.MAX_INDEX+1; i++)
		{
			c = a + b;
			System.out.println(i + " " + a);
			
			if(prev != -1 && a != 1)
				assertEquals(prev, Fibonacci.previous(a));
			
			prev = a;
			a = b;
			b = c;
		}
	}
	
	@Test
	public void testPrevSpeed()
	{
		Global.random = new Random();
		
		int m = 1000000;
		
		tic();
		for(int i : Series.series(m))
		{
			int index = Global.random.nextInt(Fibonacci.MAX_INDEX+1);
			long n = Fibonacci.get(index);
		}
		System.out.println(toc());
		
		tic();
		for(int i : Series.series(m))
		{
			int index = Global.random.nextInt(Fibonacci.MAX_INDEX+1);
			long n = Fibonacci.get(index);
			Fibonacci.previous(n);
		}
		System.out.println(toc());
	}	

}
