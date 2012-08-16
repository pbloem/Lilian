package org.lilian.statistics;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class KSStatisticTest
{

	@Test
	public void testKs()
	{
		assertEquals(
				0.25,
				KSStatistic.ks(
				Arrays.asList(0., 1., 2., 3.), 
				Arrays.asList(.5, 1.3, 1.6, 2.5), 
				false), 0.00001);
		
		assertEquals(
				0.1,
				KSStatistic.ks(
				Arrays.asList(0., 1., 2., 3.), 
				Arrays.asList(0., 1., 1.5, 2., 3.), 
				false), 0.00001);

	}

}
