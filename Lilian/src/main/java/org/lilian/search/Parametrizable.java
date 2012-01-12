package org.lilian.search;

import java.util.List;

/**
 * An object is parametrizable if it can be represented fully als a list of 
 * double values. The class in question should also contain a static function
 * which returns a {@link Builder}.
 * 
 * It may be wise to make the type of parameter generic so that boolean values
 * or integers can also be used as parameters.
 * 
 * @author Peter
 */
public interface Parametrizable
{
	
	/**
	 * Returns a a representation of this object as a list of double values.
	 * 
	 * @return
	 */
	public List<Double> parameters();

}
