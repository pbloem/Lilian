package org.lilian.data.real;

import static org.junit.Assert.*;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.junit.Test;
import org.lilian.util.MatrixTools;

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

}
