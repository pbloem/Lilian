package org.lilian.util;

/**
 * A mutable double, class that stores a double (like java's 
 * own Double class), but allows the value to be changed
 *
 * TODO:
 * - make sure it works with java's Number in all ways (maybe even implement it)
 *  - With boxing and unboxing, these offer no improvements anymore. 
 *    They should be discarded
 *     
 * @author Peter Bloem 0168491
 */
public class MDouble implements Comparable<MDouble>{
	private double value;

	/**
	 * Set the value of this Integer
	 *
	 * @param v the value to set the integer to
	 */
	public void setValue(double v){
		value = v;
	}

	/**
	 * get the value of this Integer
	 */
	public double getValue(){
		return value;
	}

	/**
	 * Creates an integer with value v
	 * @param v the initial value this Integer represents
	 */
	public MDouble(double v){
		setValue(v);
	}

	/**
	 * Increase the value of this integer by 1
	 */
	public void increment(double inc){
		value += inc;
	}
	
	public int hashCode()
	{
		long v = Double.doubleToLongBits(value);
		return (int)(v^(v>>>32));		
	}

	public String toString()
	{
		return Double.toString(value);
	}
	
	/**
	 * Compares this MDouble to another.
	 *
	 * @param o the object to compare to
	 */
	public int compareTo(MDouble d)
	{
	 	return (int)Math.signum(value - (d.value));
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof MDouble)
			return ((MDouble)o).value == this.value;
		
		if(o instanceof Number)
			return ((Number)o).doubleValue() == this.value;
		
		return false;
	}
}
