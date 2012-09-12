package org.lilian.search.evo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lilian.Global;
import org.lilian.search.evo.ES.Agent;

/**
 * A basic implementation of a genetic algorithm for discrete structures. No 
 * attempt is made to generalize over all possible implementations of genetic 
 * algorithms, rather we choose a simple implementation that works well.
 * 
 * @author Peter
 *
 * @param <P>
 */
public class GA<P extends Serializable>
{
	private Crossover<P> crossover;
	private Mutator<P> mutator;
	private Target<P> target;
	private Comparator<P> comparator;
	
	private int maxAge = 0, offspringSize, numParents = 2, n;
	private double mutationProbability;
	
	private List<Agent> agents;
	
	public GA(List<P> initial, Crossover<P> crossover, Mutator<P> mutator, Target<P> target, double mutationProbability)
	{
		this.crossover = crossover;
		this.mutator = mutator;
		this.target = target;
		this.mutationProbability = mutationProbability;
		
		comparator = Targets.comparator(target);
		n = initial.size();
		offspringSize = initial.size() * 2;
		
		agents = new ArrayList<Agent>(initial.size());
		for(P gene : initial)
			agents.add(new Agent(gene));
		
		Collections.sort(agents);
	
	}

	public void breed()
	{
		List<Agent> nextPopulation = new ArrayList<Agent>(offspringSize);

		// * Add all the agents that haven't exceeded the max lifespan
		//   to the new population
		for(Agent agent : agents)
		{
			if(agent.age() <= maxAge || maxAge == 0)
			{
				agent.incrementAge();
				nextPopulation.add(agent);
			}
		}
		
		// * Add children to the new population by breeding within the old 
		//   population at random
		while(nextPopulation.size() < offspringSize)
		{
			// sample the parents (with replacement)
			Agent mother = agents.get(Global.random.nextInt(agents.size())),
			      father = agents.get(Global.random.nextInt(agents.size()));
			
			P newGene = crossover.cross(mother.genes(), father.genes());
			if(Global.random.nextDouble() < mutationProbability)
				newGene = mutator.mutate(newGene);
			nextPopulation.add(new Agent(newGene));
		}
		
		// * We're done with the old population now
		agents = nextPopulation;
		
		// * Sort the population by fitness		
		Collections.sort(agents);

		// * Reduce the size of the population back to the original, throwing 
		//   out the least fit individuals
		while(agents.size() > n)
			agents.remove(agents.size() -1);
	}
	
	public Agent best()
	{
		return agents.get(0);
	}
	
	public List<Agent> population()
	{
		return Collections.unmodifiableList(agents);
	}	

	public class Agent implements Comparable<Agent>, Serializable
	{	
		private static final long serialVersionUID = 1L;

		private P genes;
		
		protected int age = 0;
		// * The last time the fitness was computed
		protected int fitnessAge = -1;
		protected double fitness = Double.NEGATIVE_INFINITY;			
		
		public Agent(P genes)
		{
			this.genes = genes;
		}


		public P genes()
		{
			return genes;
		}
		
		public int compareTo(Agent other)
		{
			return -Double.compare(this.fitness(), other.fitness());
		}		
		
		public double fitness()
		{			
			// Recalculate the fitness if this is a new generation
			if(age != fitnessAge)
			{				
				fitness = target.score(genes);
				fitnessAge = age;
			}
			
			return fitness;	
		}
		
		public int age()
		{
			return age;
		}
		
		public void incrementAge() 
		{
			age++;
		}
		
		public String toString()
		{
			return genes() + " " + fitness(); 
		}
	}
}
