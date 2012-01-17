package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.lilian.Global;
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
		
		AffineMap map = Maps.findMap(x, y);
		
		assertTrue(map.equals(AffineMap.identity(5), 0.01));
	}
	
	@Test
	public void testFindMap2()
	{
		int num = 2;
		
		Distance<List<Double>> d = new EuclideanDistance();
		Map map = AffineMap.builder(2).build(Parameters.random(6, 0.5));
		
		List<Point> x, y;
		x = points(num, 2, 3.4);
		y = map.map(x);
		
		AffineMap re = Maps.findMap(x, y);
	
		for(int i = 0; i < x.size(); i++)
		{
			// assertEquals(0.0, d.distance(y.get(i), re.map(x.get(i))), 0.01);
			System.out.println(x.get(i) + " " + y.get(i) + " " +  re.map(x.get(i)));
		}
	}
	
	@Test
	public void testFindMap3()
	{
		Distance<List<Double>> d = new EuclideanDistance();

		List<Point> x, y;
		x = new ArrayList<Point>();
		y = new ArrayList<Point>();
		
		x.add(new Point(1.0, 0.0));
		y.add(new Point(0.0, 1.0));
		
		x.add(new Point(0.0, 1.0));
		y.add(new Point(-1., 0.0));
				
		
		AffineMap re = Maps.findMap(x, y);
	
		for(int i = 0; i < x.size(); i++)
			assertEquals(0.0, d.distance(y.get(i), re.map(x.get(i))), 0.01);
			// System.out.println(x.get(i) + " " + y.get(i) + " " +  re.map(x.get(i)));
	}	
	
	@Test
	public void testFindMap4()
	{
		Distance<List<Double>> d = new EuclideanDistance();

		List<Point> x, y;
		x = new ArrayList<Point>();
		y = new ArrayList<Point>();
		
		x.add(new Point(1.0, 0.0));
		y.add(new Point(0.0, 1.0));
		
		x.add(new Point(0.0, 2.0));
		y.add(new Point(-2., 0.0));
				
		
		AffineMap re = Maps.findMap(x, y);
	
		for(int i = 0; i < x.size(); i++)
			assertEquals(0.0, d.distance(y.get(i), re.map(x.get(i))), 0.01);
			// System.out.println(x.get(i) + " " + y.get(i) + " " +  re.map(x.get(i)));
	}	
	
	@Test
	public void testFindMap5()
	{
		Distance<List<Double>> d = new EuclideanDistance();

		List<Point> x, y;
		x = new ArrayList<Point>();
		y = new ArrayList<Point>();
		
		x.add(new Point(0.0, 0.0));
		x.add(new Point(2.0, 0.0));
		
		y.add(new Point(0.0, 1.0));
		y.add(new Point(1.0, 2.0));
				
		AffineMap re = Maps.findMap(x, y);
	
		for(int i = 0; i < x.size(); i++)
			assertEquals(0.0, d.distance(y.get(i), re.map(x.get(i))), 0.01);
	}	
	
	@Test
	public void testFindMap6()
	{
		Distance<List<Double>> d = new EuclideanDistance();

		List<Point> x, y;
		x = new ArrayList<Point>();
		y = new ArrayList<Point>();
		
		x.add(new Point(0.0, 0.0));
		x.add(new Point(0.0, 2.0));
		
		y.add(new Point(1.0, 1.0));
		y.add(new Point(2.0, 2.0));
				
		AffineMap re = Maps.findMap(x, y);
	
		for(int i = 0; i < x.size(); i++)
			//System.out.println(x.get(i) + " " + y.get(i) + " " + re.map(x.get(i)));
			assertEquals(0.0, d.distance(y.get(i), re.map(x.get(i))), 0.01);
	}		
	
	public static List<Point> points(int num, int dim, double var)
	{
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
