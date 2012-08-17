package org.lilian.grammars.automata;

import java.io.PrintStream;
import java.util.*;

import org.lilian.util.*;
import org.lilian.*;

/**
 * This class represents a basic automaton, a directed graph with each edge
 * labeled with a character. Vertices are called states, edges are called
 * transitions.
 * 
 * This file also contains the main class and the algorithm (in mergeStates)
 * that finds the smallest automaton by merging states. The main class calls
 * this algorithm for the automaton prescribed by the assignment, and outputs
 * the results.
 * 
 * For the sake of brevity and simplicity, I haven't included any user friendly
 * way of passing parameters. Some simple modifications to the main class can be
 * made to give the automaton different examples, or to make the algorithm
 * determinitsic.
 * 
 * With it's current settings, the algorithm is random and generates different
 * automata each time it's run. The smallest one (which has three states) gets
 * found about one in every five runs.
 */

public class Automaton<T>
{
	// used to give each new state a unique id
	private int stateCounter = 0;

	// the states of the automaton
	private Vector<State> states = new Vector<State>();

	// maps state id's to states
	private Map<Integer, State> idToState = new HashMap<Integer, State>();

	// the starting state
	private State startingState;

	// the accepting states
	private Collection<State> acceptingStates = new LinkedHashSet<State>();
	
	// a set of transition labels
	private Set<T> labels = new LinkedHashSet<T>();

	private Set<List<T>> positive = new LinkedHashSet<List<T>>();
	private Set<List<T>> negative = new LinkedHashSet<List<T>>();

	/**
	 * Constructs a deep copy of an original automaton
	 * 
	 * @param aut The automaton to copy from
	 */
	public Automaton(Automaton<T> aut)
	{
		positive.addAll(aut.positive);
		negative.addAll(aut.negative);

		for(State state : aut.states)
			new State(state.id);

		// copy the out transitions
		// (in transitions are automatically created)
		for(State state : aut.states)
		{
			State newState = idToState.get(state.getId());
			
			for(T label : state.transitionsOut.keySet())
				for(State destination : state.transitionsOut.get(label))
					newState.addTransition(label, getState(destination.getId()));
		}

		// copy the accepting states and the starting state
		for(State state : aut.acceptingStates)
			acceptingStates.add(getState(state.getId()));

		startingState = getState(aut.startingState.getId());
	}

	/**
	 * Constructs an initial automaton based on positive and negative examples.
	 * The initial automaton is an MCA.
	 * 
	 * @param positive
	 *            A collection of strings that represent the positive examples
	 * @param negative
	 *            A collection of strings that represent the negative examples
	 */
	public Automaton(Collection<List<T>> positive, Collection<List<T>> negative)
	{
		startingState = new State();

		// copy all positive and negative examples
		this.positive.addAll(positive);
		this.negative.addAll(negative);

		State oldState, newState;

		// iterate over all possible examples, create a branch from
		// the starting state for each example
		for(List<T> example : positive)
		{
			// * start with the starting state
			oldState = startingState;

			// * if the example is empty, the branch ends with the starting 
			// state as well
			newState = startingState;

			// * iterate over all characters in the example
			for (T label : example)
			{
				// * for each character, create a new transition from the
				// old state to a new one
				newState = new State();
				oldState.addTransition(label, newState);
				oldState = newState;
			}

			// * add the last state of the branch representing this example to
			// the list of accepting states.
			acceptingStates.add(newState);
		}

	}

	/**
	 * @return This automaton's starting state
	 */
	public State getStartingState()
	{
		return startingState;
	}

	/**
	 * Returns those states in this automaton that are accepting (ie. when
	 * reached, can represent the end of the string)
	 * 
	 * @return An unmodifiable collection of states.
	 */
	public Collection<State> getAcceptingStates()
	{
		return Collections.unmodifiableCollection(acceptingStates);
	}

	/**
	 * @return A collection containing all the states in this automaton.
	 */
	public List<State> getStates()
	{
		return Collections.unmodifiableList(states);
	}
	
	public Collection<T> getLabels()
	{
		return Collections.unmodifiableSet(labels);
	}

	public State getState(int id)
	{
		Integer idInt = new Integer(id);
		if (idToState.containsKey(idInt))
			return idToState.get(idInt);
		return null;
	}

	/**
	 * Return whether the automaton accepts this example. An automaton accepts a
	 * sample if it can find a path from the starting state to an accepting
	 * state such that all transitions passed match the characters of the
	 * example in order.
	 * 
	 * @param in
	 *            The example to test
	 * @return true if the automaton accepts the example, false if not.
	 */
	public boolean member(List<T> example)
	{
		return member(startingState, example, 0);
	}
	
	/**
	 * Recursively check whether the automaton, starting from state s0 accepts
	 * example from index start.
	 */
	private boolean member(State s0, List<T> example, int start)
	{
		// if this is the end of the example
		if (start == example.size())
			// true if we're in an accepting state, false otherwise
			return acceptingStates.contains(s0);

		T label = example.get(start);
		// if this state has no out for this character, return false
		if (!s0.transitionsOut.containsKey(label))
			return false;
		
		boolean accepts = false;
	
		for(State s1 : s0.transitionsOut.get(label))
		{
			accepts = member(s1, example, start + 1);
			if(accepts)
				break;
		}

		return accepts;
	}
	
	/**
	 * Return whether the automaton is consistent with its positive and negative
	 * examples
	 * 
	 * @return false if there is a negative example that the automaton accepts,
	 *         or a positive example that it doesn't accept. (In the current
	 *         context we don't need to check the positive examples, because we
	 *         only increase generality from the MCA, but it doesn't hurt to be
	 *         complete).
	 */
	public boolean isConsistent()
	{
		// check all positive examples
		for (List<T> example : positive)
			if (! member(example))
				return false;

		// check all positive examples
		for (List<T> example : negative)
			if (member(example))
				return false;
		
		return true;
	}
	
	public double coverage()
	{
		double covered = 0.0;
		for(List<T> example : positive)
			if(member(example))
				covered ++;
		
		return covered / positive.size(); 
	}	
	
	/**
	 * Merge two states in this automaton based on their id's
	 * 
	 * @param s1 The first state to be merged
	 * @param s2 The second state
	 */
	public void merge(int s1, int s2)
	{
		State state1 = getState(s1);
		State state2 = getState(s2);

		state1.mergeWith(state2);
	}

	/**
	 * Make this automaton deterministic. Non-deterministic states have multiple
	 * transitions for a single label. This method finds these states, takes two
	 * states that it transitions to with the same label, and merges them. If no
	 * more of these states can be found, the automaton is deterministic.
	 */
	public void makeDeterministic()
	{
		boolean deterministic = false;

		while (!deterministic)
		{
			// * find the first non-deterministic state
			deterministic = true;

			State state = null;
			for(State s : states)
				if (!s.isDeterministic())
				{
					deterministic = false;
					state = s;
					break;
				}

			// * if a non-deterministic state was found, start the merging process
			if (!deterministic)
			{
				// * find its non-deterministic transitions (ndTransitions is a 
				//   set of labels for which there are more than one transition)
				T label = state.ndTransitions.iterator().next();

				// take the set of states associated with this label,
				// get it's first two states
				State s1, s2;
				Iterator<State> ndStatesIt = state.transitionsOut.get(label).iterator();
				s1 = ndStatesIt.next();
				s2 = ndStatesIt.next();

				// merge the states
				s1.mergeWith(s2);
			}
		}
	}
	
	/**
	 * The number of states in the automaton
	 * 
	 * @return The number of states
	 */
	public int size()
	{
		return states.size();
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("["+states.size()+","+(this.isConsistent() ? "con" : "inc" )+"]");

		boolean first = true;
		for(State state : states)
		{
			if (first)
				first = false;
			else
				sb.append(" ");

			sb.append(state.toString());
		}
		
		return sb.toString();
	}
	
	public List<Integer> encode(List<T> word)
	{
		List<Integer> code = new LinkedList<Integer>();
		
		if(encode(word, 0, this.startingState, code))
			return code;
		
		return null;
	}
	
	private boolean encode(List<T> word, int index, State s0, List<Integer> code)
	{
		// Check if we've reached the end of the word
		if(index == word.size())
		{
			if(acceptingStates.contains(s0))
				return true;
			return false;
		}
		
		int i = 0; // the index of the transition
		for(T label :  s0.transitionsOut.keySet())
		{
			for(State s1 : s0.transitionsOut.get(label))
			{
				if(label.equals(word.get(index)))
				{ 
					code.add(i);
										
					// we return the first path found, not the best (for now)
					if(encode(word, index+1, s1, code)) 
						return true;
					else
						code.remove(code.size() - 1);					
				}
				
				i++;
			}
		}
		
		return false;
	}

	/**
	 * Inner class to represent states
	 */
	public class State
	{
		// This id identifies the State uniquely within this automaton
		private int id;

		// holds the transitions from this state
		protected Map<T, Set<State>> transitionsOut = new LinkedHashMap<T, Set<State>>();

		// holds the transitions to this state
		protected Map<T, Set<State>> transitionsIn = new LinkedHashMap<T, Set<State>>();

		// holds the characters for which there are multiple
		// transitions
		private Set<T> ndTransitions = new LinkedHashSet<T>();
		
		int numTransitionsOut = 0;
		int numTransitionsIn = 0;

		/**
		 * Creates an empty state
		 */
		public State()
		{
			id = stateCounter;

			states.add(this);
			idToState.put(new Integer(id), this);
			stateCounter++;
		}

		/**
		 * Creates an empty state
		 * 
		 * @param id
		 *            The id that this state should have. (Any new states will
		 *            be incremented from this id)
		 * @throws IllegalArgumentException
		 *             If there is already a state in this automaton with this
		 *             id.
		 * 
		 */
		public State(int id)
		{
			if (idToState.containsKey(new Integer(id)))
				throw new IllegalArgumentException("State id " + id
						+ " already exists.");

			this.id = id;
			stateCounter = Math.max(stateCounter, id);

			states.add(this);
			idToState.put(new Integer(this.id), this);

			stateCounter++;
		}

		/**
		 * Adds a transition from this state to another
		 * 
		 * @param label
		 *            The character that the transition represents
		 * @param destination
		 *            The state that the transition moves to
		 */
		public void addTransition(T label, State destination)
		{
			labels.add(label);
			
			if (!transitionsOut.containsKey(label))
				transitionsOut.put(label, new LinkedHashSet<State>());

			// add the transition
			// (if this precise transition already existed, nothing happens)
			if(transitionsOut.get(label).add(destination))
				numTransitionsOut++;
			
			// add the transition to the destination state
			destination.addInTransition(label, this);

			// if there are multiple transitions for this label,
			// it's non-deterministic
			if (transitionsOut.get(label).size() > 1)
				ndTransitions.add(label);
		}

		/**
		 * Removes a transition from this label
		 * 
		 * @param label
		 *            The transitions label
		 * @param destination
		 *            The transitions destination
		 */
		public void removeTransition(T label, State destination)
		{

			// removes the transition, if the transition did
			// not exist, nothing happens
			if(transitionsOut.get(label).remove(destination))
				numTransitionsOut--;
			
			if (transitionsOut.get(label).size() < 2)
				ndTransitions.remove(label);

			if (transitionsOut.get(label).size() == 0)
				transitionsOut.remove(label);

			destination.removeInTransition(label, this);
		}

		/**
		 * Adds a transition from another state to this one
		 */
		private void addInTransition(T label, State from)
		{
			if (!transitionsIn.containsKey(label))
				transitionsIn.put(label, new LinkedHashSet<State>());

			// add the transition
			if(transitionsIn.get(label).add(from))
				numTransitionsIn++;
		}

		/**
		 * Removes a transtion from another state to this one
		 */
		private void removeInTransition(T label, State from)
		{

			if(transitionsIn.get(label).remove(from))
				numTransitionsIn--;

			if (transitionsIn.get(label).size() == 0)
				transitionsIn.remove(label);
		}

		/**
		 * Check if this state is deterministic. A state is deterministic if it
		 * has no more than one transition for each label.
		 * 
		 * @return True if the state is deterministic, false otherwise
		 */
		public boolean isDeterministic()
		{
			return (ndTransitions.size() == 0);
		}

		/**
		 * Merge this state with another one. This basically removes one state,
		 * and rewires all transitions to the other state. The removed state is 
		 * always the one with the higher id. 
		 * 
		 * @param newState
		 *            The state to merge this state with
		 */
		public void mergeWith(State newState)
		{
			if(this.id < newState.id)
				newState.mergeWithInner(this);
			else
				this.mergeWithInner(newState);
		}
		
		private void mergeWithInner(State newState)
		{
			// redo all the transitions from
			T label;
			State dest;

			// ** out transitions
			Iterator<T> labelIt = transitionsOut.keySet().iterator();
			Iterator<State> destIt;

			// * These will hold the transitions to be removed. We need to 
			//   collect them first, and then remove, to keep the iterators 
			//   happy
			Vector<T> tLabels;
			Vector<State> tStates;

			tLabels = new Vector<T>(transitionsOut.size());
			tStates = new Vector<State>(transitionsOut.size());

			while (labelIt.hasNext())
			{
				label = labelIt.next();
				destIt = transitionsOut.get(label).iterator();
				while (destIt.hasNext())
				{
					dest = destIt.next();

					tLabels.add(label);
					tStates.add(dest);

					// create a new transition
					newState.addTransition(label, dest);
				}
			}
			
			// remove the old transition
			for (int i = 0; i < tLabels.size(); i++)
				this.removeTransition(tLabels.get(i), tStates.get(i));

			// ** in transitions
			labelIt = transitionsIn.keySet().iterator();
			Iterator<State> fromIt;
			State from;

			tLabels = new Vector<T>(transitionsIn.size());
			tStates = new Vector<State>(transitionsIn.size());

			while (labelIt.hasNext())
			{
				label = labelIt.next();
				fromIt = transitionsIn.get(label).iterator();
				while (fromIt.hasNext())
				{
					from = fromIt.next();

					tLabels.add(label);
					tStates.add(from);

					// create a new transition
					from.addTransition(label, newState);
				}
			}

			for (int i = 0; i < tLabels.size(); i++)
			{
				// remove the old transition
				tStates.get(i).removeTransition(tLabels.get(i), this);
			}

			// if this was an accepting state, the newState becomes an accepting
			// state
			if (acceptingStates.contains(this))
				acceptingStates.add(newState);

			// remove the state from the Automaton's collections
			states.remove(this);
			idToState.remove(id);
			acceptingStates.remove(this);
			// if this was the starting state, newState becomes the starting
			// state.
			if (this.equals(startingState))
				startingState = newState;
		}

		/**
		 * @return The integer that uniquely identifies this state
		 */
		public int getId()
		{
			return id;
		}
		
		public int getNumTransitionsOut()
		{
			return numTransitionsOut;
		}
		
		public int getNumTransitionsIn()
		{
			return numTransitionsIn;
		}

		public int hashCode()
		{
			return getId();
		}

		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(id);
			if (startingState.equals(this))
				sb.append('!');
			if (acceptingStates.contains(this))
				sb.append('*');
			sb.append(':');

			Iterator<T> labelIt = transitionsOut.keySet().iterator();
			Iterator<State> stateIt;
			T label;
			State state;
			while (labelIt.hasNext())
			{
				label = labelIt.next();
				stateIt = transitionsOut.get(label).iterator();
				while (stateIt.hasNext())
				{
					state = stateIt.next();
					sb.append(label).append(state.getId());
					sb.append(".");
				}
			}
			
			sb.append("("+numTransitionsIn+","+numTransitionsOut+")");
			return sb.toString();
		}
	}
	
	/**
	 * Used to sort Automaton/Complexity pairs by their complexity 
	 *
	 */
	private static class PairComparator<T> implements Comparator<Pair<Automaton<T>, Double>>
	{
		public int compare(Pair<Automaton<T>, Double> p0, Pair<Automaton<T>, Double> p1)
		{
			return p0.second().compareTo(p1.second());
		}
	}

	/**
	 * Merges Automaton.States to produce a minimal automaton that accepts all positive
	 * examples and none of the negative ones.
	 * 
	 * @param a The automaton
	 * @return The input if no smaller automaton can be found. null if the input
	 *         is inconsistent (accepts negative examples). A smaller automaton
	 *         if the search is successful.
	 */
	public static <T> Automaton<T> mergeStates(
			Automaton<T> a, boolean random, Complexity<T> complexity)
	{
		return mergeStates(a, random, complexity, Double.MAX_VALUE);
	}

	private static <T> Automaton<T> mergeStates(
			Automaton<T> a, boolean random, 
			Complexity<T> complexity, double maxComplexity)
	{
		// * The method returns null for inconsistent automata, this makes the
		// recursion easier to implement
		if (!a.isConsistent())
			return null;
		
		// the method also returns null for automata with too high a complexity
		double thisComplexity = complexity.get(a);
		if(thisComplexity > maxComplexity)
			return null;
	
		// generate list of all pairs
		List<Pair<Automaton<T>.State, Automaton<T>.State>> pairs = 
			new ArrayList<Pair<Automaton<T>.State, Automaton<T>.State>>();
		
		ListIterator<Automaton<T>.State> it1 = a.getStates().listIterator();
		ListIterator<Automaton<T>.State> it2;
		
		int index;
		Automaton<T>.State state1, state2;
		while (it1.hasNext())
		{
			index = it1.nextIndex();
			state1 = it1.next();
			it2 = a.getStates().listIterator(index + 1);
			while (it2.hasNext())
			{
				state2 = it2.next();
				
				Pair<Automaton<T>.State, Automaton<T>.State> pair =				
					new Pair<Automaton<T>.State, Automaton<T>.State>(state1, state2);
	
				// * Insert the pair in a random place, or simply at the end
				if (!random)
					pairs.add(pair);
				else
				{
					int ind = Global.random.nextInt(pairs.size() + 1);
					pairs.add(ind, pair);
				}
			}
		}
	
		// * For all pairs, do the merge, make the automaton deterministic
		//   and call this function again.
		Automaton<T> newAutomaton, finalAutomaton = null;
	
		for(Pair<Automaton<T>.State, Automaton<T>.State> pair : pairs)
		{
			state1 = pair.first();
			state2 = pair.second();
	
			newAutomaton = new Automaton<T>(a);
			newAutomaton.merge(state1.getId(), state2.getId());
			newAutomaton.makeDeterministic();
	
			// * This is a recursive call, which means it returns
			//   the first automaton it can find from this automaton, that can't
			//   be compressed further.
			finalAutomaton = mergeStates(newAutomaton, random, complexity, thisComplexity);
	
			// * We're greedy, the first automaton we find, we pick
			if (finalAutomaton != null)
				break;
		}
	
		// * If finalAutomaton is still null at this point, all possible merges
		//   resulted in inconsistent automata, and the input is the best 
		//   automaton for this branch of the search. If it's not null, some 
		//   smaller automaton was found.
		if (finalAutomaton != null)
			return finalAutomaton;
		
		return a;
	}

	/**
	 * Searches the full space of all DFA's using a greedy heuristic for the one with 
	 * the lowest complexity
	 * 
	 * TODO:
	 *  - check for equal automata in the queue (this requires some definition
	 *    of isomorphism between automata, which is a pain)
	 *  - insert the automata in the right point in the queue, so
	 *    we don't have to sort all the time
	 */
	public static <T> Automaton<T> fullSearch(Automaton<T> start, Complexity<T> complexity, boolean requireConsistency)
	{
		return fullSearch(start, complexity, requireConsistency, null);
	}
	
	public static <T> Automaton<T> fullSearch(Automaton<T> start, Complexity<T> complexity, boolean requireConsistency, PrintStream out)
	{
		Comparator<Pair<Automaton<T>, Double>> comp = new PairComparator<T>();
		
		// sorted queue of states to try next
		Vector<Pair<Automaton<T>, Double>> queue =
			new Vector<Pair<Automaton<T>, Double>>();
		
		queue.add(new Pair<Automaton<T>, Double>(start, new Double(complexity.get(start))));
		Automaton<T> current;
		Automaton<T> top = start;
		Double currentComplexity;
		
		Automaton<T> best = start;
		double bestComplexity = Double.MAX_VALUE;
		
		int steps = 0;
		while(queue.size() > 0)
		{
			if(out != null && (steps % 1) == 0)
			{
				out.println("best in queue: " + top);
				out.println("   complexity: " + complexity.get(top));
				out.println("     coverage: " + top.coverage());
				
				out.println("best in queue: " + best);
				out.println("   complexity: " + complexity.get(best));
				out.println("     coverage: " + best.coverage());						
				out.println();
			}
			
			steps++;			
			
			
			// * Get the next automaton
			current = queue.get(0).first();
			currentComplexity = queue.get(0).second();
			queue.remove(0);
			
			// * If this is better than the current best, it becomes the current best
			if(bestComplexity > currentComplexity)
			{
				best = current;
				bestComplexity = currentComplexity;				
			}
			
			// * Add all automata that can be made by joining two states to the 
			// queue
			ListIterator<Automaton<T>.State> it1 = current.getStates().listIterator();
			ListIterator<Automaton<T>.State> it2;
			int index;
			Automaton<T>.State state1, state2;
			Automaton<T> newAutomaton;
			Double newComplexity;
			
			while (it1.hasNext())
			{
				index = it1.nextIndex();
				state1 = it1.next();
				it2 = current.getStates().listIterator(index + 1);
				while (it2.hasNext())
				{
					state2 = it2.next();
					newAutomaton = new Automaton<T>(current);
					newAutomaton.merge(state1.getId(), state2.getId());
					
					newAutomaton.makeDeterministic();
					if(!requireConsistency || newAutomaton.isConsistent())
					{
						newComplexity = complexity.get(newAutomaton);
						queue.add(new Pair<Automaton<T>, Double>(newAutomaton, newComplexity));
					}
				}			
			}
			
			Collections.sort(queue, comp);
			top = queue.get(0).first();
			
		}
		
		return best;
	}
}
