package org.lilian.data.real;

import static org.junit.Assert.*;

import org.junit.Test;

public class PointTest {

	@Test
	public void testPointInt() 
	{
		Point p = new Point(9);
		
		for(int i = 0; i < 9; i++)
			assertEquals(0.0, p.get(i), 0.0);

	}

	@Test
	public void testDistance() 
	{
		Point a = new Point(2, 3, 4),
		      b = new Point(3, 4);
		

		assertEquals(a.distance(a), 0.0, 0.0);		
		assertEquals(b.distance(b), 0.0, 0.0);
		
		assertEquals(a.distance(b), b.distance(a), 0.0);
		assertEquals(a.distance(b), Math.sqrt(18.0), 0.0);

	}

}
