package org.lilian.search.evo;

import java.util.Comparator;

import org.lilian.search.Parametrizable;

public interface Target<P>
{
	/**
	 * Scores an object according to this target function.
	 * 
	 * the higher the score, the better the object
	 * 
	 * @param object
	 * @return
	 */
	public double score(P object);	

}
