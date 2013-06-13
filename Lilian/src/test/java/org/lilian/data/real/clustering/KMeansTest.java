package org.lilian.data.real.clustering;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.util.Functions;
import org.lilian.util.Series;
import org.lilian.util.distance.EuclideanDistance;

public class KMeansTest
{

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
		
		KMeans km = new KMeans(data, 3);
		km.iterate(20);
		
		System.out.println(km.clustered().classes());
	}

}
