package org.lilian.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.lilian.util.distance.FrequencyCosineDistance;
import org.lilian.util.distance.Distance;

public class CosineDistanceTests
{

	@Test
	public void testDistance()
	{
		List<Integer> features = Arrays.asList(0, 1),
		              a = Arrays.asList(0, 1, 0, 0, 1),
		              b = Arrays.asList(1, 1, 1, 0, 0, 0, 1,0);
		
		Distance<List<Integer>> distance = new FrequencyCosineDistance<Integer>(features);
		
		
		double expected = Math.cos(0.25 * Math.PI - Math.atan(2.0/3.0));
		assertEquals(expected, distance.distance(a, b), 0.0000000001);
	}

}
