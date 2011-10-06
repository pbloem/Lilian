package org.lilian.util.ranges;

import static org.junit.Assert.*;

import org.junit.Test;

public class RangeTest
{

	@Test
	public void testEqualsObject()
	{
		assertTrue( new Range(-0.7, -0.4).equals(new Range(-0.7, -0.4)) );
		assertTrue( new Range(-0.7, -0.4).equals(new Range(-7.0 / 10.0, -4.0 / 10.0)) );		
	}

}
