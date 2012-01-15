package org.lilian.data.real;

import static java.lang.Math.PI;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class EllipseTest
{

	@Test
	public void testEllipse()
	{
		Ellipse e = Ellipse.builder(2).build(Arrays.asList(1.0,0.0, 0.0,1.0, 0.0,0.0));
		
		double margin = 1.0E-7;
		assertTrue(equals(new Point( 0.0, 1.0), e.point(0.0 * PI), margin));
		assertTrue(equals(new Point( 1.0, 0.0), e.point(0.5 * PI), margin));
		assertTrue(equals(new Point( 0.0,-1.0), e.point(1.0 * PI), margin));
		assertTrue(equals(new Point(-1.0, 0.0), e.point(1.5 * PI), margin));		
	}
	
	@Test
	public void testRandom()
	{
		Ellipse e = Ellipse.random(49, 0.3);
		
		e.point(0.0 * PI);
		e.point(0.5 * PI);
		e.point(1.0 * PI);
		e.point(1.5 * PI);	
		
//		for(double a = 0.0; a < 2.0 * PI; a += PI/10.0)
//			System.out.println(e.point(a));
	}
	
	public boolean equals(Point expected, Point actual, double margin)
	{
		if(expected.size() != actual.size())
			return false;
		
		for(int i = 0 ; i < expected.size(); i ++)
			if(Math.abs(expected.get(i) - actual.get(i)) > margin)
				return false;
		
		return true;
	}
}
