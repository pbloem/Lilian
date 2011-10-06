package org.lilian.models;

import java.io.*;
import java.util.*;

import org.lilian.corpora.Corpus;
import org.lilian.corpora.SequenceIterator;
import org.lilian.util.*;


/**
 * Creates a histogram of values for a corpus over numbers. It uses the 
 * toString() method to convert the token to a string.
 * 
 */
public class IntHistogram<T extends Number> extends BasicFrequencyModel<T> {

	protected double sum = 0;
	protected double min = Double.POSITIVE_INFINITY, 
	                 max = Double.NEGATIVE_INFINITY;

	public IntHistogram()
	{
	}

	public IntHistogram(Corpus<T> corpus) 
	{
		for(T token : corpus)
			add(token);
	}
	
	public void add(T token, double weight)
	{
		super.add(token, weight);
		
		double v = token.doubleValue();
		sum += v * weight;

		max = Math.max(max, v);
		min = Math.min(min, v);		
	}
	
	/**
	 * @return The mean value of the tokens encountered.
	 */
	public double mean()
	{
		return sum / total();
	}
	
	/**
	 * The maximum value encountered. 
	 * @return
	 */
	public double max()
	{
		return max;
	}
	
	/**
	 * @return The variance of the histogram.
	 */
	public double var()
	{
		return varSum()/total();
	}
	
	/**
	 * @return The sample variance of the histogram.
	 */	
	public double sampleVar()
	{
		return varSum()/(total() - 1.0);
	}	
	
	/**
	 * The sum part of the calculation of the variance.
	 */
	private double varSum()
	{
		double sum = 0.0;
		double mean = mean();
		
		for(T key : tokens())
		{
			double diff = (key.doubleValue() - mean);
			sum += frequency(key) * diff * diff;
		}
		
		return sum; 
	}
	
	/**
	 * The maximum value encountered. 
	 * @return
	 */
	public double min()
	{
		return min;
	}
	
	/**
	 * Prints an extensive multiline summary of the model to an outputstream
	 */
	public void print(PrintStream out)
	{
		out.printf("total:           %.0f \n", total());
		out.printf("distinct:        %.0f \n", distinct());
		out.printf("mean value:      %.3f \n", mean());
		out.printf("variance:        %.3f \n", var());		
		out.printf("sample variance: %.3f \n", var());		
		out.printf("min, max:        %.3f, %.3f \n", min(), max());
		out.printf("entropy:         %.3f \n", entropy()); 
		out.println();
		
		List<T> keys = new ArrayList<T>(tokens());
		Collections.sort(keys, new NumberComparator<T>());
		
		for(T key : keys)
			out.printf("%.1f:  %.1f\n", key.doubleValue() , frequency(key)); 
	}
	
	private static class NumberComparator<T extends Number> implements java.util.Comparator<T>
	{
		@Override
		public int compare(T first, T second) {
			return Double.compare(first.doubleValue(), second.doubleValue());
		}
		
	}
}