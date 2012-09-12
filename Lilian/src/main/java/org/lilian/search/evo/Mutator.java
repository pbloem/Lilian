package org.lilian.search.evo;

public interface Mutator<P>
{

	/**
	 * Mutates the original object. If possible, should manipulate the provided 
	 * object directly and return the result. 
	 * @param in
	 * @return
	 */
	public P mutate(P in);
}
