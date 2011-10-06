package org.lilian.util;

import java.util.*;

/**
 * Class representing a bitstring with simple getters and setters by index.
 * 
 * @author peter
 *
 */
public class BitString extends AbstractList<Boolean>
{
	protected byte[] array;
	protected int maxIndex = -1;
	
	/**
	 * 
	 */
	public BitString()
	{
		this(128);
	}
	
	/**
	 * @param capacity The initial capacity in bits.
	 */
	public BitString(int capacity)
	{
		int byteSize = (int)Math.ceil(capacity/8.0);
		array = new byte[byteSize];
	}
	
	/**
	 * Ensures that the array is large enough to accomodate a value at the 
	 * given index 
	 * 
	 * @param max
	 */
	private void ensureCapacity(int max)
	{
		int byteSize = (int)Math.ceil(max/8.0);
		if(byteSize <= array.length) 
			return;
		
		byte[] newArray = new byte[byteSize];
		
		for(int i = 0; i < array.length; i++)
			newArray[i] = array[i];
		
		array = newArray;
	}

	@Override
	public int size() 
	{
		return maxIndex+1;
	}
	
	@Override
	public boolean add(Boolean bit) {
		ensureCapacity(maxIndex + 1);
		maxIndex++;		
		set(maxIndex, bit);
	
		return true;
	}

	@Override
	public Boolean get(int index) {
		checkIndex(index);
		
		int whichByte = index/8,
		    whichBit  = index%8;
		
		byte b = array[whichByte];
		
		return bit(whichBit, b);
	}
	
	@Override
	public Boolean set(int index, Boolean bit) {
		checkIndex(index);
		
		int whichByte = index/8,
		    whichBit  = index%8;
		
		byte b = array[whichByte];
		byte mask = mask(whichBit);
		
		Boolean old = bit(whichBit, b);
		
		if(bit)
			array[whichByte] = (byte)(b |  mask);
		else
			array[whichByte] = (byte)(b & ~mask);
		
		return old;
	}
	
	private void checkIndex(int index)
	{
		if(index < 0 || index > maxIndex)
			throw new ArrayIndexOutOfBoundsException("Index ("+index+") must be in interval (0, "+maxIndex+")");
	}

	public String toString()
	{
		char[] ch = new char[this.size()];
		for(int i = 0; i < this.size(); i++) 
			ch[i] = this.get(i) ? '1': '0';
		
		return new String(ch);		
	}
	
	/**
	 * Returns a representation of this bitstring as a byte array, the closest
	 * we can get to a string of actual system bits. 
	 *
	 * The last byte is not part of the bitstring, but indicates (encoded as a 
	 * java int cast to a byte) the number of bits the second-to last byte has 
	 * been padded to make the number of bits a multiple of eight.
	 * 
	 * @return  An array of (floor(this.size()) / 8 + 2) bytes containing the 
	 * 			bitstring, with sufficient additional information to reconstruct
	 * 			it.
	 */
	public byte[] byteArray()
	{
		byte[] out = new byte[array.length + 1];
		System.arraycopy(array, 0, out, 0, array.length);
		
		out[array.length] = (byte) padding();
		
		return out;
	}
	
	/**
	 * The number of bits required to pad this bitstring out to a multiple of 
	 * eight.
	 * @return
	 */
	protected int padding()
	{
		return (8 - size() % 8) % 8;		
	}
	
	public static byte mask(int index)
	{
		switch (index) {
			case 0: return 1;			
			case 1: return 2;
			case 2: return 4;
			case 3: return 8;
			case 4: return 16;
			case 5: return 32;
			case 6: return 64;
			case 7: return -128;
			default: throw new IndexOutOfBoundsException(index + "");
		}
	}
	
	public static boolean bit(int index, byte b)
	{
		byte mask = mask(index);
		
		return (mask & b) == mask; 
	}
	
	/**
	 * Creates a bitstring of the given length with only zero bits as elements
	 * @param size
	 * @return
	 */
	public static BitString zeros(int size)
	{
		BitString out = new BitString(size);
		out.maxIndex = size - 1;
		return out;
	}	
	
	public static String toString(byte b)
	{
		char[] ch = new char[8];
		for(int i = 0; i < 8; i++)
			ch[0] = bit(i, b) ? '1': '0';
		
		return new String(ch);
	}
	
	/**
	 * Parses a character sequence of 1's and 0's into the corresponding 
	 * bitstring
	 * 
	 * If the char sequence contains any character not 0, it is interpreted 
	 * as 1.
	 * 
	 * @param in
	 * @return
	 */
	public static BitString parse(CharSequence in)
	{
		BitString out = new BitString(in.length());
		for(int i = 0; i < in.length(); i++)
			out.add(in.charAt(i) == '0' ? false : true);
		
		return out;
	}
}
