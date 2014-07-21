package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.data.real.Maps.FindSimilitudeResult;
import org.lilian.search.Parameters;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.EuclideanDistance;

public class MapsTest
{

	@Test
	public void testFindMap1()
	{
		List<Point> x, y;
		x = points(30, 5, 0.01);
		y = x;
		
		AffineMap map = Maps.findSimilitude(x, y);
		
		assertTrue(map.equals(AffineMap.identity(5), 0.01));
	}
	
	@Test
	public void testFindMapError()
	{
		List<Point> x, y, yC;
		x = points(3, 5, 0.01);
		y = points(3, 5, 0.01);
		
		FindSimilitudeResult result = Maps.findSimilitudeResult(x, y);
		AffineMap map = result.affineMap();
		
		yC = map.map(x);
		double e = 0.0;
		for(int i : Series.series(x.size()))
		{
			double d = yC.get(i).distance(y.get(i));
			e += d*d;
		}
		
		e /= x.size();
		
		assertEquals(e, result.error(), 1E-10);
	}

	
	// @Test TODO Why doesn't this work?
	public void testFindMap2()
	{
		int num = 30;
		
		Distance<Point> d = new EuclideanDistance();
		Map map = Similitude.affineMapBuilder(2).build(Parameters.random(4, 0.5));
		
		List<Point> x, y;
		x = points(num, 2, 3.4);
		y = map.map(x);
		
		AffineMap re = Maps.findSimilitude(x, y);
	
		for(int i = 0; i < x.size(); i++)
		{
			assertEquals(0.0, d.distance(y.get(i), re.map(x.get(i))), 0.01);
			// System.out.println(x.get(i) + " " + y.get(i) + " " +  re.map(x.get(i)));
		}
	}
	
	@Test
	public void testFindMap3()
	{
		Distance<Point> d = new EuclideanDistance();

		List<Point> x, y;
		x = new ArrayList<Point>();
		y = new ArrayList<Point>();
		
		x.add(new Point(1.0, 0.0));
		y.add(new Point(0.0, 1.0));
		
		x.add(new Point(0.0, 1.0));
		y.add(new Point(-1., 0.0));
				
		
		AffineMap re = Maps.findSimilitude(x, y);
	
		for(int i = 0; i < x.size(); i++)
			assertEquals(0.0, d.distance(y.get(i), re.map(x.get(i))), 0.01);
			// System.out.println(x.get(i) + " " + y.get(i) + " " +  re.map(x.get(i)));
	}	
	
	@Test
	public void testFindMap4()
	{
		Distance<Point> d = new EuclideanDistance();

		List<Point> x, y;
		x = new ArrayList<Point>();
		y = new ArrayList<Point>();
		
		x.add(new Point(1.0, 0.0));
		y.add(new Point(0.0, 1.0));
		
		x.add(new Point(0.0, 2.0));
		y.add(new Point(-2., 0.0));
				
		
		AffineMap re = Maps.findSimilitude(x, y);
	
		for(int i = 0; i < x.size(); i++)
			assertEquals(0.0, d.distance(y.get(i), re.map(x.get(i))), 0.01);
			// System.out.println(x.get(i) + " " + y.get(i) + " " +  re.map(x.get(i)));
	}	
	
	@Test
	public void testFindMap5()
	{
		Distance<Point> d = new EuclideanDistance();

		List<Point> x, y;
		x = new ArrayList<Point>();
		y = new ArrayList<Point>();
		
		x.add(new Point(0.0, 0.0));
		x.add(new Point(2.0, 0.0));
		
		y.add(new Point(0.0, 1.0));
		y.add(new Point(1.0, 2.0));
				
		AffineMap re = Maps.findSimilitude(x, y);
	
		for(int i = 0; i < x.size(); i++)
			assertEquals(0.0, d.distance(y.get(i), re.map(x.get(i))), 0.01);
	}	
	
	@Test
	public void testFindMap6()
	{
		Distance<Point> d = new EuclideanDistance();

		List<Point> x, y;
		x = new ArrayList<Point>();
		y = new ArrayList<Point>();
		
		x.add(new Point(0.0, 0.0));
		x.add(new Point(0.0, 2.0));
		
		y.add(new Point(1.0, 1.0));
		y.add(new Point(2.0, 2.0));
				
		AffineMap re = Maps.findSimilitude(x, y);
	
		for(int i = 0; i < x.size(); i++)
			//System.out.println(x.get(i) + " " + y.get(i) + " " + re.map(x.get(i)));
			assertEquals(0.0, d.distance(y.get(i), re.map(x.get(i))), 0.01);
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
	
	@Test 
	public void testUniformCentered()
	{
		List<Point> points = Arrays.asList(
			new Point(1.0, 10.0),
			new Point(10.0, 9.0)
		);
		
		System.out.println(Maps.centerUniform(points).map(points));
	}

}
