package org.lilian.grammars.automata;

/**
 * This class defines the complexity of an automaton as the 
 * sum of the number of states, the number of accepting states 
 * and the number of transition labels. 
 */

public class SimpleComplexity<T> implements Complexity<T>
{

	public double get(Automaton<T> a)
	{
		return (double)(
				a.getStates().size() + 
				a.getAcceptingStates().size() +
				a.getLabels().size());
	}

}
