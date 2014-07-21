package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.search.Builder;
import org.lilian.search.Parameters;
import org.lilian.util.Series;

public class AffineMapTest 
{

	@Test
	public void testInvertible() 
	{
		Map map;
		
		// the identity map is invertible
		map = AffineMap.identity(6);
		
		assertTrue(map.invertible());	
		
		// arbitrary invertible map
		map = new AffineMap(Arrays.asList(4.0, 3.0, 2.0, 1.0, 0.0, 0.0));
		
		assertTrue(map.invertible());
		
		// arbitrary non-invertible map
		map = new AffineMap(Arrays.asList(4.0, 3.0, 0.0, 0.0, 0.0, 0.0));
		
		assertFalse(map.invertible());		
	}
	
	@Test
	public void testInverse() 
	{
		AffineMap m = random(3);
		
		for(Point p : new MVN(3).generate(20))
		{
			Point q = m.inverse().map(m.map(p));
			for(int i : Series.series(3))
				assertEquals(p.get(i), q.get(i), 0.00000001);
		}
		
	}	

	@Test
	public void testDimension() 
	{
		
		Map map = new AffineMap(Arrays.asList(4.0, 3.0, 0.0, 0.0, 0.0, 0.0));
		assertEquals(2, map.dimension());		
	}

	@Test
	public void testIdentity() 
	{
		Map map;
		map = AffineMap.identity(6);
		
		Point in = new Point(1, 2 ,3 ,4 ,5, 6);
		assertEquals(in, map.map(in));
	}

	@Test
	public void testNumParameters() 
	{
		assertEquals(2,  AffineMap.numParameters(1));
		assertEquals(6,  AffineMap.numParameters(2));
		assertEquals(12, AffineMap.numParameters(3));
		assertEquals(20, AffineMap.numParameters(4));
		assertEquals(30, AffineMap.numParameters(5));		
	}
	
	@Test
	public void testDimensionStatic() 
	{
		assertEquals(1, AffineMap.dimension(2));
		assertEquals(2, AffineMap.dimension(6));
		assertEquals(3, AffineMap.dimension(12));
		assertEquals(4, AffineMap.dimension(20));
		assertEquals(5, AffineMap.dimension(30));		
	}	

	@Test
	public void testMapPoint() 
	{
		// arbitrary map
		Map map = new AffineMap(Arrays.asList(4.0, 3.0, 2.0, 1.0, 3.0, 3.0));

		assertEquals(new Point(58, 26), map.map(new Point(7, 9)));	
	}
	
	@Test
	public void testBuilder()
	{
		Builder<AffineMap> builder = AffineMap.affineMapBuilder(3);
		
		assertEquals(12, builder.numParameters());
		
		List<Double> params = Arrays.asList(1.0,0.0,0.0, 0.0,1.0,0.0, 0.0,0.0,1.0, 0.0,0.0,0.0);
		
		AffineMap result = builder.build(params);
		
		assertEquals(AffineMap.identity(3), result);
		
		AffineMap result2 = builder.build(result.parameters());
		
		assertEquals(result, result2);		
	}
	
	@Test
	public void testCompose1()
	{
		AffineMap a = AffineMap.identity(3), b = AffineMap.identity(3);
		
		Map ab = a.compose(b);
		
		Point p = new Point(1, 2, 3);
		assertEquals(p.get(0), ab.map(p).get(0), 0.00001);
		assertEquals(p.get(1), ab.map(p).get(1), 0.00001);
		assertEquals(p.get(2), ab.map(p).get(2), 0.00001);		
	}
	
	@Test
	public void testCompose2()
	{
		AffineMap a = random(3), b = random(3);
		
		Map ab = a.compose(b);
		
		Point p = new Point(1, 2, 3),
		      ps = a.map(b.map(p));
		
		assertEquals(ps.get(0), ab.map(p).get(0), 0.00001);
		assertEquals(ps.get(1), ab.map(p).get(1), 0.00001);
		assertEquals(ps.get(2), ab.map(p).get(2), 0.00001);		
	}
	
	public AffineMap random(int dim)
	{
		Builder<AffineMap> builder = AffineMap.affineMapBuilder(dim);
		List<Double> p = Parameters.random(builder.numParameters(), 1.0);
		return builder.build(p);
	}

	
	@Test
	public void testFindMap1()
	{
		int dim = 5;
		
		AffineMap map = Parameters.random(AffineMap.affineMapBuilder(dim), 0.1);
		
		List<Point> x, y, yRes;
		x = points(30, dim, 0.01);
		y = map.map(x);
		
		AffineMap found = AffineMap.find(x, y);
		
		System.out.println("original map:" + map);
		System.out.println("       found:" + found);
		
		x = points(100, dim, 0.01);
		y = map.map(x);

		yRes = found.map(x);
		
		for(int i : Series.series(x.size()))
			for(int j : Series.series(dim))
				assertEquals(y.get(i).get(j), yRes.get(i).get(j), 0.0001);
	}
	
	@Test 
	public void testFindMap2()
	{
		List<Point> x, y;
		x = points(30, 5, 0.01);
		y = x;
		
		AffineMap map =AffineMap.find(x, y);
		
		assertTrue(map.equals(AffineMap.identity(5), 0.01));
	}
	
	public static List<Point> points(int num, int dim, double var)
	{
		Global.random = new Random();
		List<Point> points = new ArrayList<Point>(num);
		
		for(int i : Series.series(num))
		{
			Point point = new Point(dim);
			for(int j : Series.series(dim))
				point.set(j, Global.random.nextGaussian() * var);
			
			points.add(point);
		}
		
		return points;
	}
}
