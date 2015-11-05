package org.lilian.data.real.fractal;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class EMTest
{

	@Test
	public void testIndexOf()
	{
		List<Integer> empty = Collections.emptyList();

		int k;
		
		k= 2;
		assertEquals(0,  EM.indexOf(empty, k));
		assertEquals(1,  EM.indexOf(Arrays.asList(0), k));
		assertEquals(2,  EM.indexOf(Arrays.asList(1), k));
		assertEquals(3,  EM.indexOf(Arrays.asList(0, 0), k));
		assertEquals(10, EM.indexOf(Arrays.asList(1, 1, 0), k));
		assertEquals(24, EM.indexOf(Arrays.asList(1, 0, 0, 1), k));		
		
		k = 3;
		assertEquals(0,  EM.indexOf(empty, k));
		assertEquals(1,  EM.indexOf(Arrays.asList(0), k));
		assertEquals(2,  EM.indexOf(Arrays.asList(1), k));
		assertEquals(3,  EM.indexOf(Arrays.asList(2), k));
		assertEquals(10, EM.indexOf(Arrays.asList(0, 2), k));
		assertEquals(24, EM.indexOf(Arrays.asList(2, 0, 1), k));
		
		k = 16;
		assertEquals(0,  EM.indexOf(empty, k));
		assertEquals(1,  EM.indexOf(Arrays.asList(0), k));
		assertEquals(2,  EM.indexOf(Arrays.asList(1), k));
		assertEquals(16, EM.indexOf(Arrays.asList(15), k));
		assertEquals(18, EM.indexOf(Arrays.asList(1, 0), k));
	}
	
	@Test
	public void testCode()
	{		
		List<Integer> empty = Collections.emptyList();

		int k;
		
		k= 2;
		assertEquals(empty,            EM.code(0, k));
		assertEquals(asList(0),          EM.code(1, k));
		assertEquals(asList(1),          EM.code(2, k));
		assertEquals(asList(0, 0),       EM.code(3, k));
		assertEquals(asList(1, 0, 0, 1), EM.code(24, k));		

		k = 3;
		assertEquals(empty,  EM.code(0, k));
		assertEquals(asList(0),  EM.code(1, k));
		assertEquals(asList(1),  EM.code(2, k));
		assertEquals(asList(2),  EM.code(3, k));
		assertEquals(asList(0, 2), EM.code(10, k));
		assertEquals(asList(2, 0, 1), EM.code(24, k));
		
		k = 16;
		assertEquals(empty,  EM.code(0, k));
		assertEquals(asList(0),  EM.code(1, k));
		assertEquals(asList(1),  EM.code(2, k));
		assertEquals(asList(15), EM.code(16, k));
		assertEquals(asList(1, 0), EM.code(18, k));
	}

}
