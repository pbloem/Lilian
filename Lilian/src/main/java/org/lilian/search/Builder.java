package org.lilian.search;

import java.util.List;

public interface Builder<P extends Parametrizable> 
{
	/**
	 * Returns an instance for the given parameters
	 * 
	 * @param parameters
	 * @return
	 */
	public P build(List<Double> parameters);
	
	/**
	 * The number of parameters required to describe a single instance
	 */
	public int numParameters();
}
