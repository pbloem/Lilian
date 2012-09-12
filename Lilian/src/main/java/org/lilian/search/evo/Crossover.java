package org.lilian.search.evo;

public interface Crossover<P>
{

	public P cross(P first, P second);
	
}
