package org.lilian.grammars.automata;

/**
 * This interface defines a complexity measure over an automaton 
 *
 * @param <T>
 */
public interface Complexity<T>
{

	/**
	 * Calculates the complexity of an automaton
	 * @param a The automaton for which to calculate the complexity
	 * @return The complexity of the automaton.
	 */
	public double get(Automaton<T> a);
}
