package org.lilian.data.real.clustering;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.util.Series;
import org.lilian.util.distance.EuclideanDistance;

public class KMedioidsTest
{

	@Test
	public void testSample()
	{
		for(int i : Series.series(100))
		{
			List<Integer> sample = KMedioids.sample(50, 100);
			Set<Integer> set = new HashSet<Integer>(sample);
			
			System.out.println(sample);
			assertEquals(sample.size(), set.size());
		}
		
		
	}
	
	@Test
	public void testCluster()
	{
		List<Point> data = new ArrayList<Point>(30);
		
		MVN mvn;
		
		mvn = new MVN(new Point(10, 10));
		data.addAll(mvn.generate(10));
		
		mvn = new MVN(new Point(-10, 10));
		data.addAll(mvn.generate(10));
		
		mvn = new MVN(new Point(-10, -10));
		data.addAll(mvn.generate(10));
		
		KMedioids<Point> km = new KMedioids<Point>(data, new EuclideanDistance(), 3);
		km.iterate(20);
		
		System.out.println(km.clustered().classes());
	}

}
