package org.lilian.data.dimension;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.data.real.Point;
import org.lilian.util.distance.EuclideanDistance;

public class TakensTest
{

	@Test
	public void testGenerate()
	{
		Takens takens = new Takens(2.0, 2.0);
		
		int prop = 0, total = 0;
		double threshold = 1.0;
		
		for(double distance : takens.generate(100000))
		{
			if(distance < threshold)
				prop++;
			total++;
		}
		
		assertEquals(takens.cdf(threshold), prop / (double) total, 0.05);
	}

}
