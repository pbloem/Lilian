package org.lilian.neural;

public interface Activation
{
	public double function(double in);
	
	/**
	 * The derivative in terms of the result of function
	 * @param in
	 * @return
	 */
	public double derivative(double fx);
}
