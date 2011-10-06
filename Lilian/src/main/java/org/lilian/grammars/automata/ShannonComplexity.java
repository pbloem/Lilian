package org.lilian.grammars.automata;

import java.util.*;

/**
 * This class represents a complexity measure based on the Shannon information measure
 *
 */
public class ShannonComplexity<T> implements Complexity<T>
{
	private static final double nlog2 = Math.log(2.0);
	
	private Vector<List<T>> data;
	
	/**
	 * Creates a ChoicePointComplexity class based on a dataset.
	 * @param data The positive examples that will determine the 
	 * 		complexity of the automaton. 
	 */
	public ShannonComplexity(Collection<List<T>> data)
	{
		this.data = new Vector<List<T>>(data.size());
		this.data.addAll(data);
	}

	public double get(Automaton<T> a)
	{
		double complexity = 0.0;
		
		for(List<T> token : data) 
			complexity += (double) ShannonComplexity.shannonComplexity(a, token);
		
		return complexity;
	}
	
	/**
	 * Returns the Shannon complexity of an automaton for just one datum 
	 */
	public static <T> double shannonComplexity(Automaton<T> a, List<T> word)
	{
		return shannonComplexity(a, a.getStartingState(), word, 0);
	}
	
	private static <T> double shannonComplexity(Automaton<T> a, Automaton<T>.State s0, List<T> example, int start)
	{
		// if this is the end of the example
		if (start == example.size())
		{
			// we've reached the end. If we're not in an accepting state, return -1.0
			if(! a.getAcceptingStates().contains(s0))
				return -1.0;
			
			// otherwise return the number of transitions out of this state + 1
			// (the number of choices at this point is the number of ways out, plus 
			// the option to stop)
			return log2(s0.getNumTransitionsOut() + 1);			
		}

		double information = 0.0;
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
		
		information += log2(thisChoicePoints);	
		
		double s1Information;
		// true if one of the transitions from this state is successful
		boolean success = false;
		
		for (Automaton<T>.State s1 : s0.transitionsOut.get(label))
		{
			s1Information = shannonComplexity(a, s1, example, start + 1);

			if(s1Information != -1.0)
			{
				success = true;
				information += s1Information;
			}
		}
		
		if(success)
			return information;
		return -1.0;		
	}
	
	/** 
	 * returns the 2log of r
	 */
	private static double log2(double r)
	{
		return Math.log(r)/nlog2;
	}
}
