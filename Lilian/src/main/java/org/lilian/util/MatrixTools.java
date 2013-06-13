package org.lilian.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

import static java.lang.Math.*;
import static org.lilian.util.Series.series;

/**
 * Some basic static matrix functions to provide functionality that Apache
 * Commons Math lacks.
 * 
 * @author peter
 *
 */
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
	public static RealMatrix toMatrix(String string)
	{
		// TODO Handle syntax problems and give helpful errors
		
		int numRows, numCols;
				
		String[] rows = string.split(";");
		
		numRows = rows.length;
		numCols = rows[0].split(",").length;
		String [] strVals;
		
		RealMatrix matrix = new Array2DRowRealMatrix(numRows, numCols);
		
		for(int i = 0; i < numRows; i++)
		{
			strVals = rows[i].split(",");
			for(int j = 0; j < numCols; j++)
			{
				double v = Double.valueOf(strVals[j].trim());
				matrix.setEntry(i, j, v);
			}
		}
		
		return matrix;
	}	
	
	public static RealMatrix identity(int dim)
	{
		RealMatrix m = new Array2DRowRealMatrix(dim, dim);
		
		for(int i = 0; i < dim; i++)
			m.setEntry(i, i, 1.0);
		
		return m;
	}
	
	public static RealVector base(int d, int i)
	{
		RealVector v = new ArrayRealVector(d);
		v.setEntry(i, 1.0);
		
		return v;
	}
	
	public static RealMatrix diag(RealVector v)
	{
		RealMatrix m = identity(v.getDimension());
		diag(m, v);
		
		return m;
	}
	
	/**
	 * Sets the values of vector v on the diagonal of matrix m
	 * @param v
	 * @param m
	 */
	public static void diag(RealMatrix m, RealVector v)
	{
		for(int i = 0; i < v.getDimension(); i ++)
			m.setEntry(i, i, v.getEntry(i));
	}
	
	/**
	 * Retrieves the values of the diagonal of m as a vector.
	 * 
	 * @param v
	 * @param m
	 */
	public static RealVector diag(RealMatrix m)
	{
		RealVector result = new ArrayRealVector(m.getColumnDimension());
		for(int i = 0; i < m.getColumnDimension(); i ++)
			result.setEntry(i, m.getEntry(i, i));
		return result;
	}

	public static RealVector toVector(double... values)
	{
		RealVector result = new ArrayRealVector(values.length);
		for(int i = 0; i < values.length; i++)
			result.setEntry(i, values[i]);
		
		return result;
	}
	
	public static RealVector toVector(List<Double> values)
	{
		RealVector result = new ArrayRealVector(values.size());
		for(int i = 0; i < values.size(); i++)
			result.setEntry(i, values.get(i));
		
		return result;
	}
	
	public static void zero(RealMatrix in)
	{
		// this can be optimized
		for(int i = 0; i < in.getColumnDimension(); i++)
			for(int j = 0; j < in.getRowDimension(); j++)
				in.setEntry(i, j, 0.0);
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
	public static boolean equals(RealMatrix m1, RealMatrix m2, double margin)
	{
		if(m1.getColumnDimension() != m2.getColumnDimension() || m1.getRowDimension() != m2.getRowDimension())
			return false;

		int numRows = m1.getRowDimension();
		int numColumns = m1.getColumnDimension();
		
		for(int i = 0; i < numRows; i++)
			for(int j = 0; j < numColumns; j++)
				if( abs(m1.getEntry(i, j) - m2.getEntry(i, j)) >= margin)
					return false;
		
		return true;
	}
	
	public static boolean isInvertible(RealMatrix in)
	{
		return new LUDecompositionImpl(in).getSolver().isNonSingular();
	}
	
	public static double getDeterminant(RealMatrix in)
	{
		return new LUDecompositionImpl(in).getDeterminant();
	}

	public static RealMatrix inverse(RealMatrix in) 
	{
		return new LUDecompositionImpl(in).getSolver().getInverse();
	}
	
	public static boolean isSingular(RealMatrix in)
	{
		return ! new LUDecompositionImpl(in).getSolver().isNonSingular();
	}
	
	/**
	 * Returns the matrix a * b^T
	 * @param a
	 * @param b
	 * @return
	 */
	public static RealMatrix outer(RealVector a, RealVector b)
	{
		RealMatrix matrix =
				new Array2DRowRealMatrix(a.getDimension(), b.getDimension());
		
		for(int row : series(a.getDimension()))
			for(int column : series(b.getDimension()))
				matrix.setEntry(row, column, a.getEntry(row) * b.getEntry(column));
		
		return matrix;
	}

	public static String toString(RealMatrix s)
	{
		return toString(s, 1);
	}

	public static String toString(RealMatrix s, int dec)
	{
		String result = "";
		for(int i : series(s.getRowDimension()))
		{
			for(int j : series(s.getColumnDimension()))
				result += String.format("%."+dec+"f\t", s.getEntry(i, j));
			result += "\n";
		}
			
		return result;
	}

}
