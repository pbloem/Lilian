package org.lilian.grammars.automata;

/**
 * Combines two measures of complexity into 
 * a single complexity for the combination of the model and the data 
 * by summing them 
 *
 */
public class MDLComplexity<T> implements Complexity<T>
{
	private Complexity<T> modelComplexity;
	private Complexity<T> dataComplexity;
	
	public MDLComplexity(Complexity<T> modelComplexity, Complexity<T> dataComplexity)
	{
		this.modelComplexity = modelComplexity;
		this.dataComplexity  = dataComplexity; 
	}
	
	public double get(Automaton<T> a)
	{
		return modelComplexity.get(a) + dataComplexity.get(a);
	}

}
