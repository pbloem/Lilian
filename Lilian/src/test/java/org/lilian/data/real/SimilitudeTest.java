package org.lilian.data.real;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.lilian.util.Functions.tic;
import static org.lilian.util.Functions.toc;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.linear.Array2DRowFieldMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;
import org.lilian.Global;
import org.lilian.util.Functions;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

import com.itextpdf.text.log.SysoLogger;

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
	
	@Test
	public void testFind()
	{		
		Similitude map = new Similitude(0.5, asList(1.0, -2.5, 3.0), asList(0.5, 0.1, -0.6));
		
		List<Point> x = points(9, 3, 3.0);
		List<Point> y = map.map(x);
		
		x.add(new Point(1.0, 2.0, 3.0));
		y.add(new Point(3.0, 2.0, 1.0));
		
		System.out.println(Similitude.find(x, y));
		System.out.println(Similitude.find(x, y, asList(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0)));
		
		List<Point> xNew = new ArrayList<Point>(x);
		List<Point> yNew = new ArrayList<Point>(y);
		
		for(int i : series(14))
		{
			xNew.add(x.get(0));
			yNew.add(y.get(0));
		}
		
		List<Double> weights = asList(15.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
		
		System.out.println(Similitude.find(xNew, yNew));
		System.out.println(Similitude.find(x, y, weights));
	}

	@Test
	public void testFindCor()
	{		
		Global.random = new Random();
		
		Similitude map = new Similitude(0.5, asList(0.5, 1.0, 0.5), asList(0.5, 0.1, -0.6));
		
		List<Point> x = points(1000, 3, 3.0);
		List<Point> y = map.map(x);
	
		// * Add some noise to y
		for(int i : series(y.size()))
		{
			MVN mvn = new MVN(y.get(i), 0.001);
			y.set(i, mvn.generate());
		}
		
		// RealMatrix cor = new Array2DRowRealMatrix(new double[][]{new double[]{0.998, 0.001, 0.001}, new double[]{0.001, 0.998, 0.001}, new double[]{0.001, 0.998, 0.001}});
		RealMatrix cor = MatrixTools.identity(x.size()); 
		
		
		List<Point> xFull = new ArrayList<Point>(x.size() * y.size());
		List<Point> yFull = new ArrayList<Point>(x.size() * y.size());
		List<Double> weights = new ArrayList<Double>(x.size() * y.size());
		
		for(int xi : series(x.size()))
			for(int yi : series(y.size()))
			{
				weights.add(cor.getEntry(xi, yi));
				xFull.add(x.get(xi));
				yFull.add(y.get(yi));
			}
		
		Similitude sim;
		
		tic();
		sim = Similitude.find(x, y);
		System.out.println(sim + " " + toc());
		
		tic();
		sim = Similitude.find(x, y, cor, new HashMap<String, Double>());
		System.out.println(sim + " " + toc());
		
		tic();
		sim = Similitude.find(xFull, yFull, weights);
		System.out.println(sim + " " + toc());
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
