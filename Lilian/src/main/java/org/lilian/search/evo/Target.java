package org.lilian.search.evo;

import java.io.Serializable;
import java.util.Comparator;

import org.lilian.search.Parametrizable;

public interface Target<P> extends Serializable
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
