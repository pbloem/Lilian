package org.lilian.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.matrix.DenseMatrix2D;

import static java.lang.Math.*;

public class MatrixTools
{
	/**
	 *  Helper function to quickly create a matrix. Handy if you 
	 *  have a set of values you want entered into a matrix.  
	 *  
	 *  Rows are separated by a semicolon, values in rows by commas. Whitespace 
	 *  is ignored. 
	 *  "1, 2, 3; 4, 5, 6" represents the matrix 
	 *  <pre>
	 *  1 2 3
	 *  4 5 6
	 *  </pre>
	 */ 
	public static Matrix toMatrix(String string)
	{
		// TODO Handle syntax problems and give helpful errors
		
		int numRows, numCols;
				
		String[] rows = string.split(";");
		
		numRows = rows.length;
		numCols = rows[0].split(",").length;
		String [] strVals;
		
		DenseMatrix2D matrix = DenseMatrix2D.factory.zeros(numRows, numCols);
		
		for(int i = 0; i < numRows; i++)
		{
			strVals = rows[i].split(",");
			for(int j = 0; j < numCols; j++)
			{
				double v = Double.valueOf(strVals[j].trim());
				matrix.setAsDouble(v, i, j);
			}
		}
		
		return matrix;
	}	
	
	public static Matrix diag(Matrix v)
	{
		Matrix m = DenseMatrix2D.factory.eye(v.getSize(0));
		diag(v, m);
		
		return m;
	}
	
	/**
	 * Sets the values of vector v on the diagonal of matrix m
	 * @param v
	 * @param m
	 */
	public static void diag(Matrix v, Matrix m)
	{
		for(int i = 0; i < v.getSize(0); i ++)
			m.setAsDouble(v.getAsDouble(i), i, i);
	}
	
	public static Matrix toVector(List<Double> values)
	{
		Matrix v = DenseMatrix2D.factory.zeros(values.size(), 1);
		
		for(int i = 0; i < values.size(); i++)
			v.setAsDouble(values.get(i), i);
		
		return v;
	}
	

	/**
	 * Transforms a given set of angles to a rotation matrix.  
	 * 
	 * To represent arbitrary rotations in dimension d, <code>(d^2-d)/2</code> 
	 * angles are required. Therefore, when this function is provided with a 
	 * angles, it assumes a dimension of (1+sqrt(1+8a))/2.  
	 * 
	 * @param angles A list of angles 
	 * @param dimension The target dimension of the rotation transformation 
	 * 					represented by the resulting matrix 
	 * @return A transformation matrix constructed for the given angles
	 */
	public static Matrix toRotationMatrix(List<Double> angles)
	{
		// calculate the dimension
		double dimDouble = (1.0 + sqrt(1.0 + 8.0 * angles.size()))/2.0;
		int dim = (int)Math.floor(dimDouble);
		
		Matrix result, left, right;
		
		double[] cosa = new double[angles.size()];
		double[] sina = new double[angles.size()];		
		for(int i = 0; i < angles.size(); i++)
		{
			cosa[i] = cos(angles.get(i));
			sina[i] = sin(angles.get(i));
		}
		
		int k;

		result = null;
		left = DenseMatrix2D.factory.eye(dim, dim);
		result = DenseMatrix2D.factory.zeros(dim, dim);
		right = DenseMatrix2D.factory.zeros(dim, dim);
		
		for(int i = 0; i < dim-1; i++)
			for(int j = i+1; j < dim; j++)
			{
				// Reset the elementary rotation matrix (this should be faster
				// than generating a new eye(dim) )
				right.clear();
				for(int m = 0; m < dim; m++)
					right.setAsDouble(1.0, m, m);

				k = (((2 * dim - i - 1) * (i + 2))/2) - 2 * dim + j; 
				// note that (2d-i-1)(i+2) is always an even number
				// so we can keep the above equation in integers

				right.setAsDouble( cosa[k], i, i);
				right.setAsDouble( cosa[k], j, j);				
				right.setAsDouble(-sina[k], i, j);
				right.setAsDouble( sina[k], j, i);
	
				// Multiply
				left = left.mtimes(Ret.NEW, true, right); // Ret.NEW doesn't seem to work...
			}
	
		return left;
	}
	
	/**
	 * Checks whether two matrices are equal, given a certain margin of error. 
	 * Two matrices are considered equal if they have the same number of rows 
	 * and columns and (m1.get(i, j) - m2.get(i,j)) &lquot; margin for all i 
	 * and j. 
	 * 
	 * @param m1
	 * @param m2
	 * @return
	 */	
	public static boolean equals(Matrix m1, Matrix m2, double margin)
	{
		if(m1.getSize(0) != m2.getSize(0) || m1.getSize(1) != m2.getSize(1))
			return false;

		long numRows = m1.getSize(0);
		long numColumns = m1.getSize(1);
		
		for(long i = 0; i < numRows; i++)
			for(long j = 0; j < numColumns; j++)
				if( abs(m1.getAsDouble(i, j) - m2.getAsDouble(i, j)) >= margin)
					return false;
		
		return true;
	}
	
}
