package org.lilian.util;

/**
 * A mutable Integer, a class that stores an integer (like java's 
 * own Integer class), but allows the value to be changed.
 *
 * TODO:
 *  - hashcode() and other stuff (see MDouble)
 *  - With boxing and unboxing, these offer no improvements anymore. 
 *    They should be discarded 
 *  
 * @author Peter Bloem 0168491
 */
public class MInteger implements Comparable<MInteger>{
	private int value;

	/**
	 * Set the value of this Integer
	 *
	 * @param v the value to set the integer to
	 */
	public void setValue(int v){
		value = v;
	}

	/**
	 * get the value of this Integer
	 */
	public int getValue(){
		return value;
	}

	/**
	 * Creates an integer with value v
	 * @param v the initial value this Integer represents
	 */
	public MInteger(int v){
		setValue(v);
	}

	/**
	 * Increase the value of this integer by 1
	 */
	public void increment(int inc){
		value += inc;
	}

	public String toString()
	{
		return Integer.toString(value);
	}


	/**
	 * Compares this Integer to another. This only works
	 * with other MutableInteger objects.
	 *
	 * @param o the object to compare to
	 */
	public int compareTo(MInteger m)
	{
		return value - m.value;
	}
}
