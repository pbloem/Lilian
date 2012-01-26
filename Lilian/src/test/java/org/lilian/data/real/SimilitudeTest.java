package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.lilian.util.Series;

public class SimilitudeTest
{

	@Test
	public void testSimilitudeListOfDouble()
	{
		Similitude s2 = new Similitude(Arrays.asList(0.5, 0.5, 0.5, Math.PI/2));
		Similitude s3 = new Similitude(Arrays.asList(0.1, 0.5, 0.5, - 0.4, Math.PI/2, Math.PI/2, Math.PI/2));
		Similitude s4 = new Similitude(Arrays.asList(0.01, 0.5, 0.5, - 0.4, -9.0, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2));
	}

	@Test
	public void testSimilitudeDoubleListOfDoubleListOfDouble()
	{
		Similitude s2 = new Similitude(
				0.5, 
				Arrays.asList(0.5, 0.5),
				Arrays.asList(Math.PI/2));
		Similitude s3 = new Similitude(
				0.1,
				Arrays.asList(0.1, 0.5, 0.5, - 0.4),
				Arrays.asList(Math.PI/2, Math.PI/2, Math.PI/2));
		Similitude s4 = new Similitude(
				0.01,
				Arrays.asList(0.5, 0.5, - 0.4, -9.0),
				Arrays.asList(Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2));
	}

	@Test
	public void testParameters()
	{
		List<Double> params = Arrays.asList(0.01, 0.5, 0.5, - 0.4, -9.0, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2);
		Similitude s4 = new Similitude(params);
		for(int i : Series.series(params.size()))
			assertEquals(params.get(i), s4.parameters().get(i), 0.0);
	}

	@Test
	public void testScalar()
	{
		List<Double> params = Arrays.asList(45.6, 0.5, 0.5, - 0.4, -9.0, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2, Math.PI/2);
		Similitude s4 = new Similitude(params);

		assertEquals(45.6, s4.scalar(), 0.0);
	}

	@Test
	public void testMapPoint()
	{
		List<Double> params = Arrays.asList(0.5, 0.0, 0.0, 0.0, 0.0, Math.PI, Math.PI, Math.PI, Math.PI, Math.PI, Math.PI);
		Similitude s4 = new Similitude(params);

		Point expected = new Point(-.5, -.5, -.5, -.5);
		Point actual = s4.map(new Point(1.0, 1.0, 1.0, 1.0));
		
		for(int i : Series.series(4))
			assertEquals(expected.get(i), actual.get(i), 0.0000001);
	}

	@Test
	public void testNumParameters()
	{
		assertEquals(1+1, Similitude.numParameters(1));
		assertEquals(1+2+1, Similitude.numParameters(2));
		assertEquals(1+3+3, Similitude.numParameters(3));
		assertEquals(1+4+6, Similitude.numParameters(4));
		assertEquals(1+5+10, Similitude.numParameters(5));
		assertEquals(1+6+15, Similitude.numParameters(6));
		assertEquals(1+7+21, Similitude.numParameters(7));
	}

	@Test
	public void testInvertible()
	{
		List<Double> params = Arrays.asList(0.5, 0.0, 0.0, 0.0, 0.0, Math.PI, Math.PI, Math.PI, Math.PI, Math.PI, Math.PI);
		Similitude s4 = new Similitude(params);
		assertTrue(s4.invertible());
		
		params = Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, Math.PI, Math.PI, Math.PI, Math.PI, Math.PI, Math.PI);
		s4 = new Similitude(params);
		assertFalse(s4.invertible());
	}

	@Test
	public void testInverse()
	{
		List<Double> params = Arrays.asList(0.5, 1.0, 2.0, 3.0, 4.0, Math.PI, Math.PI, Math.PI, Math.PI, Math.PI, Math.PI);
		Similitude s = new Similitude(params);
		Similitude sInv = s.inverse();

		for(int i : Series.series(20))
		{
			Point in = Point.random(4, 4.5);
			Point out = sInv.map(in);
			out = s.map(out);
			
			for(int j : Series.series(in.size()))
				assertEquals(in.get(j), out.get(j), 0.000001);
		}
	}

	@Test
	public void testDimension()
	{
		List<Double> params = Arrays.asList(0.5, 1.0, 2.0, 3.0, 4.0, Math.PI, Math.PI, Math.PI, Math.PI, Math.PI, Math.PI);
		Similitude s = new Similitude(params);
		Similitude sInv = s.inverse();
		
		assertEquals(4, s.dimension());
		assertEquals(4, sInv.dimension());
	}

}
