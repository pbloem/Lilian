package org.lilian.data.real;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;
import org.lilian.Global;
import org.lilian.util.Series;

public class RotationTest
{

	//@Test
	public void testFindAngles()
	{
		List<Double> angles = Arrays.asList(1.1, 1.0, 0.9);
		
		RealMatrix matrix = Rotation.toRotationMatrix(angles);
		List<Double> out = Rotation.findAngles(matrix, 2000, 400);
		
		for(double angle : out)
			System.out.println(
					(angle + Math.PI*10) % Math.PI
					);
	}
	
	// @Test
	public void testFindAngles3()
	{
		List<Double> angles = Arrays.asList(1.1, 1.0, 0.9);
		Rotation r = new Rotation(angles);
		List<Point> a = Arrays.asList(
				new Point(1.0, 0.0, 1.0),
				new Point(0.0, 1.0, 0.0),
				new Point(0.0, 0.0, 1.0));
		List<Point> b = r.map(a);
		
		List<Double> out = Rotation.findAngles(a, b, 2000, 6);
		
		System.out.println(out);
		System.out.println(b);
		System.out.println(new Rotation(out).map(a));
	}
	
	@Test
	public void testFindAngles4()
	{
		List<Double> angles = Arrays.asList(1.1);
		Rotation r = new Rotation(angles);
		List<Point> a = Arrays.asList(new Point(1.0, 1.0));
		List<Point> b = r.map(a);
		
		List<Double> out = Rotation.findAngles(a, b, 2000, 1);
		
		System.out.println(out);
		System.out.println(b);
		System.out.println(new Rotation(out).map(a));
	}


	// @Test
	public void testFindAngles2()
	{
		List<Double> angles = Arrays.asList(1.1);
		
		RealMatrix matrix = Rotation.toRotationMatrix(angles);
		List<Double> out = Rotation.findAngles(matrix, 10000, 4);
		
		for(double angle : out)
			System.out.println(
					(angle + Math.PI*10) % Math.PI
					);
	}	
	
	@Test
	public void testAngle()
	{
		Global.random = new Random();
		for(int i : series(200))
		{
			double hi = Global.random.nextGaussian() * 20;
			double hk = Global.random.nextGaussian() * 100;
			
			double a = Rotation.angle(hi, hk);
			assertEquals(0, Math.cos(a) * hi - Math.sin(a) * hk, 10E-10);
		}
	}
	
	@Test
	public void testFindAnglesClosed()
	{
		List<Double> angles, result;
		
		RealMatrix r;
//		
//		// * d = 2
//		angles = Arrays.asList(1.1);
//		r = Rotation.toRotationMatrix(angles);
//		result = Rotation.findAngles(r);
//		
//		for(int i : series(angles.size()))
//			assertEquals(angles.get(i), result.get(i), 10E-12);
		
		// * d = 3
		angles = Arrays.asList(-1.0, 1.0, 1.0);
		r = Rotation.toRotationMatrix(angles);
		System.out.println(r);
		result = Rotation.findAngles(r);
		
		System.out.println(angles);
		System.out.println(result);
//		
		System.out.println(r);		
		System.out.println(Rotation.toRotationMatrix(result));
//		
		for(int i : series(20))
		{
			System.out.println();
			
			Point p = Point.random(3, 3.0);
			Point exp = new Rotation(angles).map(p);
			Point res = new Rotation(result).map(p);
			
			for(int j : series(3))
				assertEquals(exp.get(j), res.get(j), 10E-6);
		}
	}
	
	@Test
	public void testFindAnglesClosed4()
	{
		List<Double> angles, result;
		
		RealMatrix r;		
		
		// *
		int d = 4;
		angles = Arrays.asList(-1.0, 1.0, 1.0, -6.0, 0.0, 1.0);
		r = Rotation.toRotationMatrix(angles);
		System.out.println(r);
		result = Rotation.findAngles(r);
		
		System.out.println(angles);
		System.out.println(result);
//		
		System.out.println(r);		
		System.out.println(Rotation.toRotationMatrix(result));
//		
		for(int i : series(20))
		{
			System.out.println();
			
			Point p = Point.random(d, 3.0);
			Point exp = new Rotation(angles).map(p);
			Point res = new Rotation(result).map(p);
			
			for(int j : series(d))
				assertEquals(exp.get(j), res.get(j), 10E-6);
		}
	}
}
