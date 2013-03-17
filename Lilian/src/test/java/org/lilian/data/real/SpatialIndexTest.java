package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class SpatialIndexTest
{

	@Test
	public void test()
	{
		assertEquals(Arrays.asList(1, 0, 0), SpatialIndex.code(new Point(0.0000001), 3));
		
		assertEquals(Arrays.asList(3, 3, 3), SpatialIndex.code(new Point(0.99999, 0.99999), 3));

		assertEquals(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1), SpatialIndex.code(new Point(.99999999999,-.9999999999, -.9999999999), 10));

	}

}
