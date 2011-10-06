package org.lilian.util;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.*;

import org.junit.Test;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.matrix.DenseMatrix2D;

public class MatrixToolsTest
{

	@Test 
	public void testToString()
	{
		Matrix m = MatrixTools.toMatrix("0.0, 0.0; 0.0, 0.0");
		assertEquals(DenseMatrix2D.factory.zeros(2, 2), m);

		m = MatrixTools.toMatrix("1.0, 0.0; 0.0, 1.0");
		assertEquals(DenseMatrix2D.factory.eye(2, 2), m);
		
	}

	@Test
	public void test()
	{	
		Matrix a = MatrixTools.toMatrix("1.0,  0.0; 0.0, 1.0");		
		Matrix b = MatrixTools.toMatrix("0.0, -1.0; 1.0, 0.0");
		
		System.out.println(a.mtimes(Ret.LINK, true, b));		
	}	
	
	@Test
	public void testToRotationMatrix1()
	{	
		List<Double> angles;

		Matrix expected = MatrixTools.toMatrix("0.0, -1.0; 1.0, 0.0");
		angles = new ArrayList<Double>();
		
		angles.add(0.5 * PI); // 90 deg
		
		Matrix result = MatrixTools.toRotationMatrix(angles);
		
		assertTrue(MatrixTools.equals(result, expected, 1.0E-10));
	}

	@Test
	public void testToRotationMatrix2()
	{	

		Matrix expected = MatrixTools.toMatrix("0.0, 0.0, 1.0; 0.0, -1.0, -0.0; 1.0, 0.0, 0.0");
		List<Double> angles = new ArrayList<Double>();
		for(int i = 0; i < 3; i ++)
			angles.add(0.5 * PI); // 90 deg
		
		Matrix result = MatrixTools.toRotationMatrix(angles);
		assertTrue(MatrixTools.equals(result, expected, 1.0E-10));
	}
	
	@Test
	public void testToRotationMatrix3()
	{	
 		
 		Matrix expected = MatrixTools.toMatrix("0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 1.0; 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -1.0, 0.0; 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0; 0.0, -0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, 0.0, 0.0, -0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.0; 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0; 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0; 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0");
 		List<Double> angles = new ArrayList<Double>();
 		for(int i = 0; i < 90; i ++)
 			angles.add(0.5 * PI); // 90 deg
 		
 		Matrix result = MatrixTools.toRotationMatrix(angles);
 		assertTrue(MatrixTools.equals(result, expected, 1.0E-10));
	}

	
	@Test
	public void testToRotationMatrix4()
	{	
		int n = 10;
		Matrix expected = DenseMatrix2D.factory.eye(n, n);
		
		List<Double> angles = new ArrayList<Double>();
		for(int i = 0; i < ((n*n)-n)/2; i ++)
			angles.add(0.0);
		
		Matrix result = MatrixTools.toRotationMatrix(angles);
		
		assertTrue(MatrixTools.equals(result, expected, 1.0E-10));
	}
	
	@Test
	public void testToRotationMatrix5()
	{	
		int n = 30;
		Matrix expected = DenseMatrix2D.factory.eye(n, n);
		
		List<Double> angles = new ArrayList<Double>();
		for(int i = 0; i < ((n*n)-n)/2; i ++)
			angles.add(0.5 * PI); 
		
		Matrix result = MatrixTools.toRotationMatrix(angles);
		
		for(int i : series(n))
			for(int j : series(n))
			{
				double d0 = Math.abs(0.0 - Math.abs(result.getAsDouble(i, j)));				
				double d1 = Math.abs(1.0 - Math.abs(result.getAsDouble(i, j)));
			
				assertTrue(d0 < 1.0E-10 || d1 < 1.0E-10);
			}
				
			
	}	

}
