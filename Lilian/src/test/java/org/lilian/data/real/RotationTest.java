package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;

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
	
}
