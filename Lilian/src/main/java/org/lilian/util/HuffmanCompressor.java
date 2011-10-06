package org.lilian.util;

import java.io.*;
import java.util.*;
import static java.lang.Math.*;

import org.lilian.models.*;
import org.lilian.models.old.FrequencyTable;
import org.lilian.corpora.*;

/**
 * <p> 
 * NOTE: This class doesn't actually compress the strings given, it just uses 
 * Kraft's inequality to calculate the length that a huffman code would have
 * </p><p>
 * We use hypothetical non-integer bits (ie. the 2-log of the probability 
 * without rounding/clipping, so the result is likely to be non-integer and 
 * represents a kind of ideal compression which is approached as the length of 
 * the sequence increases.
 * </p><p>
 * We do not (yet) count the space required to map the keys back to codons so 
 * the length returned is less than the length of an actual compression of the 
 * sequence.
 * </p>  
 * 
 **/
public class HuffmanCompressor<T> implements Compressor<T> {

	@Override
	public double compressedSize(Object... objects) 
	{
		BasicFrequencyModel<Object> table = new BasicFrequencyModel<Object>();
		
		// * Create a statistical model
		for(Object object : objects) 
			table.add(object);
		
		double size = 0;
		
		// * Calculate the size required to encode the sequence
		for(Object object : objects) 
			size += length( table.probability(object) );
			
		return size;
	}
	
	/**
	 * Simple encoding using the same length keys for all codons. 
	 * 
	 * @param objects
	 * @return
	 */
	public double plainSize(Object[] objects) {
		Set<Object> table = new HashSet<Object>();
		
		for(Object object : objects) 
			table.add(object);
		
		double codonSize = length( 1.0 / (double)table.size() );
		
		return objects.length * codonSize;
	}
	
	private static double length(double probability)
	{
		return - Functions.log2(probability);
	}

	@Override
	public double ratio(Object... objects) {
		double compressed = compressedSize(objects),
		       plain = plainSize(objects);
		return compressed / plain;
	}

}
