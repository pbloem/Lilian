package org.lilian.util;

import java.math.*;
import java.io.*;

/**
 * This class is used for calculating binomial coefficients. It draws a 
 * pascal's triangle, up to 33. For top values larger than 33, a log gamma 
 * function is used. 
 * 
 * It should be noted that at top = 1030, bottom = 500 the results can no 
 * longer be stored in a double, and get(top, bottom) begins returning positive 
 * infinity. 
 * 
 * @author Peter Bloem
 */
public class BinomialCoefficient implements Serializable{
	private int[][] triangle;
	private int capacity;
	private int height;
	
	public BinomialCoefficient(){
		capacity = 33;

		triangle = new int[capacity+1][];
		
		triangle[0] = new int[1];
		triangle[0][0] = 1;
		
		triangle[1] = new int[2];
		triangle[1][0] = 1;
		triangle[1][1] = 1;
		
		for(int i = 2; i <= capacity;i++)
		{
			triangle[i] = new int[i+1];
			for(int j = 0; j < i+1; j++){
				if(j == 0 || j == i)
					triangle[i][j] = 1;
				else
					triangle[i][j] = triangle[i-1][j-1] + triangle[i-1][j];
			}
			
		}
			
		
	}

	/**
	 * Calculates the binomial coefficient c(top, bottom). Values up to 33 are retrieved from a lookup table.
	 * Higher values are calculated with gamma functions.
	 *
	 * 
	 * @param top The top number in the binomial coefficient
	 * @param bottom The top number in the binomial coefficient
	 * @return The binomial coefficient top over bottom
	 */
	public double get(int top, int bottom){
		//if(top > 1000) throw new IllegalArgumentException("Top number cannot be larger than 1000");
		if(bottom > top) throw new IllegalArgumentException("bottom ("+bottom+") larger than top("+top+")");
		if(bottom < 0) throw new IllegalArgumentException("bottom ("+bottom+") smaller than 0");

		if(top > capacity)
			return getByGamma(top, bottom);
		else
			return (double) triangle[top][bottom];
	}
	
	/**
	 * Calculates the binomial coefficient c(top, bottom). Values are always 
	 * with gamma functions.
	 *
	 * 
	 * @param top The top number in the binomial coefficient
	 * @param bottom The top number in the binomial coefficient
	 * @return The binomial coefficient top over bottom
	 */
	public double get(double top, double bottom)
	{
		if(bottom > top) throw new IllegalArgumentException("bottom ("+bottom+") larger than top("+top+")");
		if(bottom < 0.0) throw new IllegalArgumentException("bottom ("+bottom+") smaller than 0");		
		return getByGamma(top, bottom);
	}
	
	private double getByGamma(double top, double bottom)
	{
		return Math.floor(
					0.5 + Math.exp(
							Functions.logFactorial(top) - Functions.logFactorial(bottom) 
							- Functions.logFactorial(top-bottom)
						)
					);
	}
	
	public static void main(String[] args){
		BinomialCoefficient bc = new BinomialCoefficient();

		int top,bottom;
		top = 0;
		while(true){
			System.out.println(top + ": ");
			bottom = 0;
			while(bottom <= top){
				//System.out.print(bc.get(top, bottom) + " ");
				if(bc.get(top, bottom) > Double.MAX_VALUE || bc.get(top, bottom) == Double.POSITIVE_INFINITY)
				{
					System.out.println("First infinity at top:" + top + " bottom:" + bottom);
					System.exit(9);
				}
				bottom++;
			}
			top++;
			//System.out.println();
		}
		
		//System.out.println(bc.get(1000, 450) + " " + bc.get(1000, 451) + " " + (bc.get(1000, 450) +  bc.get(1000, 451))); 
		//System.out.println(bc.get(1001, 451));

	}

}