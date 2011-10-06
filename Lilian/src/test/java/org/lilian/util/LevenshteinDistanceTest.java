package org.lilian.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.HammingDistance;
import org.lilian.util.distance.LevenshteinDistance;

public class LevenshteinDistanceTest
{

	@Test
	public void testDistance()
	{
		List<String> a = Arrays.asList("1", "1", "1", "0"),
                     b = Arrays.asList("1", "1", "1", "2", "1");

		Distance<List<String>> distance = new LevenshteinDistance<String>();

		assertEquals(2.0, distance.distance(a, b), 0.0);
	}

}
