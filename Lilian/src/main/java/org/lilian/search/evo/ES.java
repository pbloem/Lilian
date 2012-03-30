package org.lilian.search.evo;

import java.util.*;
import java.io.*;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.Global;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Rotation;
import org.lilian.data.real.fractal.IFS;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

/**
 * <p>
 * Implements the evolutionary strategies algorithm.\
 * </p><p>
 * Evolutionary strategies is an evolutionary algorithm for agents represented 
 * by a fixed number of decimal values. Each agent evolves, in addition to the 
 * object to be optimized, a multivariate normal distribution which determines
 * how it will mutate. 
 * </p><p>
 * In this sense. Agents are able to influence their own 
 * evolution and adapt to the structure of the fitness landscape.
 * </p>
 * @author peter
 *
 * @param <P>
 * @param <T>
 */
public class ES<P extends Parametrizable> implements Serializable
{
	private static final long serialVersionUID = 3586222214435236218L;

	public static enum CrossoverMode {GLOBAL, LOCAL, UNIFORM};
	public static final int NUM_PARENTS = 2;
	public static final int OFFSPRING = 2;
	public static final int MAX_LIFESPAN = 0;
	public static final CrossoverMode MODE = CrossoverMode.UNIFORM;	
	
	private List<Agent> population;
	private Builder<P> builder;
	private Target<P> target;
	private int n;
	private int dimension;

	private int numParents;
	private int offspringSize;
	private int maxAge;
	
	private CrossoverMode objectMode;
	private CrossoverMode scalesMode;
	private CrossoverMode anglesMode;
	
	// * TODO Parametrize these
	private int mutationDimensions = 0;
	private double angleMutationVar = 0.08;
	private double convergenceSpeed = 0.001;
	
	// * Standard deviation for initializing the parameters, scales and angles
	private double initialStdDevObject = 0.0;
	private double initialStdDevScales = 0.0;
	private double initialStdDevAngles = 0.0;	
	
	private double mutationProbabilityAngles = 1.0;	
	private double mutationProbabilityScales = 1.0;	
	private double mutationProbabilityObject = 1.0;	
	
	public ES(Builder<P> builder, Target<P> target, Collection<List<Double>> initialPop)
	{
		this(builder, target, initialPop, NUM_PARENTS, initialPop.size() * OFFSPRING, MAX_LIFESPAN, MODE);
	}	
	
	public ES(
			Builder<P> builder,
			Target<P> target,
			Collection<List<Double>> initialPop,
			int numParents, 
			int offspringSize,
			int maxAge,
			CrossoverMode mode)
	{
		this(
				builder, target, initialPop, 
				numParents, offspringSize, maxAge, mode,
				0.0001, 0.08);
	}	
	
	public ES(
			Builder<P> builder,
			Target<P> target,
			Collection<List<Double>> initialPop,
			int numParents, 
			int offspringSize,
			int maxAge,
			CrossoverMode mode,
			double convergenceSpeed,
			double angleMutationVar)
	{
		this.target = target;
		this.builder = builder;
				
		n = initialPop.size();
		dimension = initialPop.iterator().next().size();
		
		population = new ArrayList<Agent>(initialPop.size());
		for(List<Double> params : initialPop)
			population.add(new Agent(params));
		
		this.numParents = numParents;
		this.offspringSize = offspringSize;
		this.maxAge = maxAge;
		
		this.objectMode = mode;
		this.scalesMode = mode;
		this.anglesMode = mode;	
		
		this.convergenceSpeed = convergenceSpeed;
		this.angleMutationVar = angleMutationVar;
	}
	
	/**
	 * Create the next generation of agents
	 */
	public void breed()
	{
		List<Agent> nextPopulation = new ArrayList<Agent>(offspringSize);

		// * Add all the agents that haven't exceeded the max lifespan
		//   to the new population
		for(Agent agent : population)
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
			List<Agent> parents = new ArrayList<Agent>(numParents);
			for(int j = 0; j < numParents; j++)
				parents.add(population.get(Global.random.nextInt(population.size()))); 
			
			nextPopulation.add( parents.get(0).procreate(parents));						
		}
		
		// * We're done with the old population now
		population = nextPopulation;
		
		// * Sort the population by fitness		
		Collections.sort(population);

		// * Reduce the size of the population back to the original, throwing 
		//   out the least fit individuals
		while(population.size() > n)
			population.remove(population.size() -1);
	}
	
	public Agent best()
	{
		return population.get(0);
	}
	
	public List<Agent> population()
	{
		return Collections.unmodifiableList(population);
	}
	
	public void print(PrintStream out)
	{
		out.println("population");
		for(Agent agent : population)
			out.println(agent.fitness() + "\t" + agent);
		
		out.println();		
	}
	
	public class Agent implements Comparable<Agent>, Serializable
	{
		protected P instance;
		protected List<Double> params;
		
		// * Strategy parameters: each agent carries double values in its genome
		//   to determine how mutations are distributed. These parameters 
		//   determine an MVN in the space of parameters. 
		// ** Scaling parameters for the strategy
		protected List<Double> strategyScales; 
		// ** Rotation parameters for the strategy 
		protected List<Double> strategyAngles; 		
		
		protected int age = 0;
		// * The last time the fitness was computed
		protected int fitnessAge = -1;
		protected double fitness = Double.NEGATIVE_INFINITY;		

		public Agent(List<Double> params)
		{
			this.params = params;
			this.instance = builder.build(params);
			
			int numScales = mutationDimensions;
			if(numScales == 0) numScales = dimension;
			
			// Initial standard devs
			double si = initialStdDevScales;
			double ai = initialStdDevAngles;			
			
			strategyScales = new ArrayList<Double>(numScales);
			for(int i = 0; i < numScales; i++)
				strategyScales.add(Global.random.nextGaussian() * si);

			//int numAngles = (int)Math.floor((dimension - (numScales/2.0)) * (numScales - 1.0));
			int numAngles = (int)Math.floor((numScales*numScales - numScales)/2.0);
			strategyAngles = new ArrayList<Double>(numAngles);
			for(int i = 0; i < numAngles; i++)
				strategyAngles.add(Global.random.nextGaussian() * ai);			
		}
		
		public Agent(List<Double> params, List<Double> scales,
				List<Double> angles)
		{
			this.params = params;
			this.strategyScales = scales;
			this.strategyAngles = angles;
					
			this.instance = builder.build(params);
		}

		/**
		 * The fitness of this agent, higher is better
		 * @return
		 */
		public double fitness()
		{
			// Recalculate the fitness if this is a new generation
			if(age != fitnessAge)
			{				
				fitness = target.score(instance);
				fitnessAge = age;
			}
			
			return fitness;			
		}
		
		public int compareTo(Agent other)
		{
			return -Double.compare(this.fitness(), other.fitness());
		}		
		
		public String toString()
		{
			return "["+params+", "+instance+"]";
		}
		
		public int age()
		{
			return age;
		}
		
		public void incrementAge() 
		{
			age++;
		}
		
		public Agent procreate(List<Agent> parents)
		{
			// * Recombine
			List<List<Double>> parentParams = new ArrayList<List<Double>>();
			List<List<Double>> parentScales = new ArrayList<List<Double>>();
			List<List<Double>> parentAngles = new ArrayList<List<Double>>();
			
			for(Agent parent : parents)
			{
				parentParams.add(parent.parameters());
				parentScales.add(parent.scales());			
				parentAngles.add(parent.angles());
			}
			
			// * Generate new, unmutated parameters
			List<Double> childParams = newParams(parentParams, objectMode);
			List<Double> childScales = newParams(parentScales, scalesMode);
			List<Double> childAngles = newParams(parentAngles, anglesMode);

			// * Mutate 
			// the angles, ...
			if(Global.random.nextDouble() < mutationProbabilityAngles)
				for(int i = 0; i < childAngles.size(); i ++)
					childAngles.set(i, 
							childAngles.get(i) + 
							Global.random.nextGaussian() * angleMutationVar);
			
			// ... the scales, ...
			if(Global.random.nextDouble() < mutationProbabilityScales)
				for(int i = 0; i < childScales.size(); i ++)
					childScales.set(i, childScales.get(i) + Global.random.nextGaussian() * convergenceSpeed);
		
			// ... and the object parameters.
			if(Global.random.nextDouble() < mutationProbabilityObject)
			{
				// - draw a standard normal 
				RealVector draw = new ArrayRealVector(childScales.size());
				for(int i = 0; i < childScales.size(); i ++)
					draw.setEntry(i, Global.random.nextGaussian());
				
				// - scale it
				for(int i = 0; i < childScales.size(); i++)
					draw.setEntry(i, draw.getEntry(i) * childScales.get(i));

				// - rotate it
				RealMatrix rot;
				if(params.size() == 1)
					rot = MatrixTools.identity(1);				
				else
					rot = Rotation.toRotationMatrix(childAngles);
				
				draw = rot.operate(draw);
				
				// - 'draw' is now a random draw form the MVN described by the 
				//   strategy parameters
				
				for (int i = 0; i < draw.getDimension(); i++)
					childParams.set(i, childParams.get(i) + draw.getEntry(i));
				
				// - this takes care of mutation in the left over dimension when 
				//   not all dimensions are covered by the strategies.
				//
				//   In that case the last value of the draw functions as
				//   the variance of a straightforward mutation of the rest of 
				//   the dimensions. 
				for (int i = draw.getDimension(); i < childParams.size(); i++)
					childParams.set(i, 
							childParams.get(i) 
							+ Global.random.nextGaussian() 
							* childScales.get(draw.getDimension() - 1));
				
			}
			
			Agent child = new Agent(childParams, childScales, childAngles); 

			return child;
		}
		
		private List<Double> newParams(List<List<Double>> parents, CrossoverMode mode)
		{
			int n = parents.get(0).size();
			List<Double> childParams = new ArrayList<Double>(n);		
			
			for(int i = 0; i < n; i++)
			{
				switch (mode) {
				case GLOBAL:
					double sum = 0.0;
					
					for(List<Double> parent : parents)
						sum += parent.get(i);

					childParams.add(sum/parents.size());
					
					break;
				case LOCAL:
			
					List<Double> mother, father;
					mother = parents.get(Global.random.nextInt(parents.size()));
					father = parents.get(Global.random.nextInt(parents.size()));				
					
					childParams.add(0.5 * (mother.get(i) + father.get(i)));
					
					break;
				case UNIFORM:
		
					List<Double> parent = parents.get(
							Global.random.nextInt(parents.size()));			
					
					childParams.add(parent.get(i)); 
					
					break;
				default:
					throw new IllegalStateException("crossover mode " + mode + " not recognized.");
				}
			}	
			
			return childParams;
		}
		
		public List<Double> parameters()
		{
			return params;
		}
		
		public List<Double> scales()
		{
			return strategyScales;
		}
		
		public List<Double> angles()
		{
			return strategyAngles;
		}

		public P instance()
		{
			return instance;		
		}			
	}
	
	
	/**
	 * Generates an initial population of lists of doubles, each drawn from 
	 * a normal distribution with the given variance.
	 * 
	 * @param size
	 * @param dimension
	 * @param var
	 * @return
	 */
	public static List<List<Double>> initial(int size, int dimension, double var)
	{
		List<List<Double>> initial = new ArrayList<List<Double>>(size);
		for(int i : Series.series(size))
		{
			List<Double> params = new ArrayList<Double>(dimension);
			for(int j : Series.series(dimension))
				params.add(Global.random.nextGaussian() *  var);
			initial.add(params);		
		}
	
		return initial;
	}
	
	/**
	 * Creates an initial population where the parameters are either zero or one
	 * with a given probability.
	 * 
	 * @param size
	 * @param dimension
	 * @param prob the probability of a one.
	 * @param var
	 * @return
	 */
	public static List<List<Double>> initialZeroOne(int size, int dimension, double prob)
	{
		List<List<Double>> initial = new ArrayList<List<Double>>(size);
		for(int i : Series.series(size))
		{
			List<Double> params = new ArrayList<Double>(dimension);
			for(int j : Series.series(dimension))
				params.add(Global.random.nextFloat() < prob ? 1.0 : 0.0);
			initial.add(params);		
		}
	
		return initial;
	}
}
