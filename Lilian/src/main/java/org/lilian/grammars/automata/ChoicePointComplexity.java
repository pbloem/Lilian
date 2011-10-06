package org.lilian.grammars.automata;

import java.util.*;

/**
 * This class represents a complexity measure based on the number of choice points in
 * an automaton, given a dataset. 
 *
 */
public class ChoicePointComplexity<T> implements Complexity<T>
{
	private Vector<List<T>> data;
	/**
	 * Creates a ChoicePointComplexity class based on a dataset.
	 * @param data The positive examples that will determine the 
	 * 		complexity of the automaton. 
	 */
	public ChoicePointComplexity(Collection<List<T>> data)
	{
		this.data = new Vector<List<T>>(data.size());
		this.data.addAll(data);
	}

	/**
	 * 
	 * @throws IllegalArgumentException if the data is not consistent
	 * with this automaton
	 */
	public double get(Automaton<T> a)
	{
		double complexity = 0.0;
		Iterator<List<T>> it = data.iterator();
		
		while(it.hasNext())
			complexity += (double) ChoicePointComplexity.choicePoints(a, it.next());
		
		return complexity;
	}

	/**
	 * Rturns the number of choice points this automaton has for this word
	 * @param word
	 * @return
	 */
	public static <T> int choicePoints(Automaton<T> a, List<T> word)
	{
		return choicePoints(a, a.getStartingState(), word, 0);
	}
	
	/**
	 * Recursively count the number of choice points for a substring,
	 * from some state
	 */
	private static <T> int choicePoints(Automaton<T> a, Automaton<T>.State s0, List<T> example, int start)
	{
		// * If this is the end of the example
		if (start == example.size())
		{
			// we've reached the end. If we're not in an accepting state, return -1
			if(! a.getAcceptingStates().contains(s0))
				return -1;
			
			// otherwise return the number of transitions out of this state + 1
			// (the number of choices at this point is the number of ways out, plus 
			// the option to stop)
			return s0.getNumTransitionsOut() + 1;			
		}

		int choicePoints = 0;
		T label = example.get(start);
		
		// if this state has no out for this character, return -1
		if (!s0.transitionsOut.containsKey(label))
			return -1;
		
		// calculate the number of choicepoints for this state
		// (the number of transistions out)
		int thisChoicePoints = s0.getNumTransitionsOut();
		// if this state is accepting, that's an extra choicepoint
		if(a.getAcceptingStates().contains(s0))
			thisChoicePoints++;
		
		choicePoints += thisChoicePoints;	

		Iterator<Automaton<T>.State> it = s0.transitionsOut.get(label).iterator();
		Automaton<T>.State s1;
		int s1ChoicePoints;
		// * True if one of the transitions from this state is successful
		boolean success = false;
		
		while (it.hasNext())
		{
			s1 = it.next();
			s1ChoicePoints = choicePoints(a, s1, example, start + 1);

			if(s1ChoicePoints != -1)
			{
				success = true;
				choicePoints += s1ChoicePoints;
			}
		}
		
		if(success)
			return choicePoints;
		return -1;
	}	
}
