package org.lilian.util.ranges;

import static org.junit.Assert.*;

import org.junit.Test;

public class LinearRangeSetTest
{

	@Test
	public void testLinearRangeSetDouble()
	{
		RangeSet set = new LinearRangeSet(1.5, 0.5);
		
		assertEquals(new Range(0.5, 2.0), set.first(0.6));
		assertEquals(new Range(-1.0, 0.5), set.first(-0.6));
		
		assertEquals(new Range(0.5, 2.0), set.first(0.5));
		assertEquals(new Range(2.0, 3.5), set.first(2.0));
		assertEquals(new Range(-1.0, 0.5), set.first(-1.0));
	}

}
