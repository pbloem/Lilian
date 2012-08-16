package org.lilian.data.real;

import static org.junit.Assert.*;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.junit.Test;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

public class MVNTest
{

	@Test
	public void test()
	{
		MVN mvn3 = new MVN(3);
		
        // * matlab:
		// cov = [0.4, 0.6; 0.2, 0.8]; 
		// cov2 = cov * cov';
		// x = [-0.5735, -0.3305; 2.1280,  1.6938; -0.1329, -0.0663]	
		
		RealMatrix rot = MatrixTools.toMatrix("0.4, 0.6; 0.2, 0.8");
		RealVector trans = MatrixTools.toVector(1.0, -1.0);
		MVN mvn2 = new MVN(new AffineMap(rot, trans));
		
		Point x1 = new Point(-0.5735 + 1.0, -0.3305 - 1.0);
		Point x2 = new Point( 2.1280 + 1.0,  1.6938 - 1.0);
		Point x3 = new Point(-0.1329 + 1.0, -0.0663 - 1.0);
		
		double 	t1 = 0.3394,
	    		t2 = 0.0010,
	    		t3 = 0.7529;
				
		assertEquals(mvn2.density(x1), t1, 0.0001);
		assertEquals(mvn2.density(x2), t2, 0.0001);
		assertEquals(mvn2.density(x3), t3, 0.0001);
		
		// m4 = [0.050000, 0.000000; 0.000000, 0.050000]; 
		// mean = [0.500, -0.500]; p = [-0.989, -0.921]; mvnpdf(p, mean, mat*mat')
		RealMatrix rot4 = MatrixTools.toMatrix("0.050000, 0.000000; 0.000000, 0.050000");
		RealVector trans4 = MatrixTools.toVector(0.500, -0.500);
		MVN mvn4 = new MVN(new AffineMap(rot4, trans4));
		
		Point x4 = new Point(-0.989, -0.921);
		
		double t4 = 6.7958e-207;
		
		assertTrue(Math.abs(mvn4.density(x4) - t4) < 0.00001);		
	}
	
	@Test
	public void findSphericalTest()
	{
		MVN source = new MVN(new Point(5.0, 1.0), MatrixTools.identity(2).scalarMultiply(0.1));
		
		MVN rec = MVN.findSpherical(source.generate(300));
		System.out.println(rec);
	}
	
	@Test
	public void mapTest()
	{
		int dim = 2;
		MVN mvn = new MVN(new Point(-0.5, -0.5), 0.1);
		
		System.out.println(mvn.map());
		
		MVN mvn2 = MVN.find(mvn.generate(1000));
		
		System.out.println(mvn2.map());
		
		for(int i : Series.series(AffineMap.numParameters(dim)))
			assertEquals(mvn.map().parameters().get(i), mvn2.map().parameters().get(i), 0.02);
	}

	@Test
	public void mapTest2()
	{
		int dim = 2;
		Generator<Point> gen = Datasets.three();
		
		MVN mvnFrom = MVN.find(gen.generate(3000));
		MVN mvnTo = MVN.find(new MVN(new Point(-0.5, -0.5), 0.01).generate(1000));
		
		System.out.println(mvnFrom.map());
		System.out.println(mvnTo.map());
		
		
//		for(int i : Series.series(AffineMap.numParameters(dim)))
//			assertEquals(mvn.map().parameters().get(i), mvn2.map().parameters().get(i), 0.02);
	}	
	
}
