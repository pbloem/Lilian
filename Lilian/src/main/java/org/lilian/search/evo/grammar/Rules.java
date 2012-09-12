package org.lilian.search.evo.grammar;

import static java.lang.Math.min;
import static org.lilian.util.Series.series;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.solvers.NewtonSolver;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.lilian.Global;
import org.lilian.data.real.Datasets;
import org.lilian.grammars.Grammar;
import org.lilian.grammars.PCFGrammar;
import org.lilian.search.evo.Crossover;
import org.lilian.search.evo.Mutator;
import org.lilian.search.evo.Target;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.HausdorffDistance;
import org.lilian.util.distance.LevenshteinDistance;

/**
 * Representation of a context free grammar for use in a genetic algorithm
 * @author Peter
 *
 */
public class Rules implements Serializable
{

	public static final String TOP = "top";
	public static final int MAX_DEPTH = 10;
	private List<Double> weights = new ArrayList<Double>();
	private List<List<String>> rules = new ArrayList<List<String>>();
	
	private Rules(){}
	
	public int size()
	{
		return rules.size();
	}
	
	public List<List<String>> rules()
	{
		return Collections.unmodifiableList(rules);
	}
	

	public List<Double> weights()
	{
		return Collections.unmodifiableList(weights);
	}
	
	
	public PCFGrammar<String> grammar()
	{
		PCFGrammar<String> grammar = new PCFGrammar<String>();
		
		for(int i : series(rules.size()))
		{
			List<String> rule = rules.get(i);
			double weight = weights.get(i);
		
			grammar.addRule(rule.get(0), rule.subList(1, rule.size()), weight);
		}

		return grammar;
	}
	
	public static Crossover<Rules> crossover()
	{
		return new RulesCrossover();
	}
	
	private static class RulesCrossover implements Crossover<Rules>
	{

		@Override
		public Rules cross(Rules first, Rules second)
		{
			Rules child = new Rules();
			
			for(int i : series(min(first.size(), second.size())))
			{
				Rules parent = Global.random.nextDouble() < 0.5 ? first : second;
				child.rules.add(parent.rules.get(i));
				child.weights.add(parent.weights.get(i));
			}
			
			return child;
		}
		
	}
	
	/**
	 * The mutator for rules works as follows: 
	 * - Either a single rule chosen uniformly at random is modified by changing a 
	 *   single nonterminal by incrementing or decrementing the index (the top 
	 *   symbol has index 0)
	 * - Or a single weight is scaled by the value 1-u or 1/(1 - u) where u is a 
	 * uniform random draw between 0 and mutationSize.
	 * 
	 * weightProb is the probability of a weight mutation rather than a rule 
	 * mutation.
	 * @author Peter
	 *
	 */	
	public static Mutator<Rules> mutator(double mutationSize, double weightProb)
	{
		return new RulesMutator(mutationSize, weightProb);
	}
	
	private static class RulesMutator implements Mutator<Rules>
	{
		private double weightMutation = 0.01; 
		private double weightProb = 0.5;
				
		public RulesMutator(double weightMutation, double weightProb)
		{
			this.weightMutation = weightMutation;
			this.weightProb = weightProb;
		}

		@Override
		public Rules mutate(Rules in)
		{
			
			int draw = Global.random.nextInt(in.size());

			if(Global.random.nextDouble() < weightProb)
			{
				double weightVar = 1.0 - Global.random.nextDouble();
				if(Global.random.nextDouble() < 0.5)
					weightVar = 1.0 / weightVar;
				
				in.weights.set(draw, in.weights.get(draw) * weightVar);
				
			} else 
			{
				List<String> rule = in.rules.get(draw);
				
				int index = rule.size() == 2 ? 0 : Global.random.nextInt(rule.size());
				String symbol = rule.get(index);
				int symbolInt = symbol.equals(TOP) ? 0 : Integer.parseInt(symbol.substring(1));
				
				if(symbolInt == 0 || Global.random.nextDouble() < 0.5)
					symbolInt ++;
				else
					symbolInt --;
				
				rule.set(index, symbolInt == 0 ? TOP : "x"+symbolInt);
			}			
			
			return in;

		}
		
	}
	
	public static Rules random(int numRules, double terminalProbability, List<String> terminals, double binomP, int binomN)
	{
		Rules rules = new Rules();
		
		for(int i : Series.series(numRules))
		{
			if(Global.random.nextDouble() < terminalProbability)
			{
				String nonTerminal = randSymbol(binomP, binomN, false);
				String terminal = terminals.get(Global.random.nextInt(terminals.size()));
				
				rules.rules.add(Arrays.asList(nonTerminal, terminal));
			} else
			{
				String symbol0 = randSymbol(binomP, binomN, true),
				       symbol1 = randSymbol(binomP, binomN, false),
				       symbol2 = randSymbol(binomP, binomN, false);
				       
				rules.rules.add(Arrays.asList(symbol0, symbol1, symbol2));
			}
			
			rules.weights.add(Global.random.nextDouble());
		}
		
		return rules;
	}
	
	/**
	 * Returns a random non-terminal symbol
	 * @param var
	 * @return
	 */
	public static String randSymbol(double p, int n, boolean canBeTop)
	{
		BinomialDistributionImpl binom = new BinomialDistributionImpl(n, p);
		binom.reseedRandomGenerator(Global.random.nextLong());
		
		int draw;
		try
		{
			draw = binom.sample();
		} catch (MathException e)
		{
			throw new RuntimeException(e);
		}
		
		if(canBeTop & draw == 0)
			return TOP;
		
		return "x"+(draw + (canBeTop ? 0 : 1));
	}
	
	public static List<String> terminals(List<List<String>> sentences)
	{
		Set<String> terminals = new HashSet<String>();
		
		for(List<String> sentence : sentences)
			for(String terminal : sentence)
				terminals.add(terminal);
		
		return new ArrayList<String>(terminals);
	}
	
	/**
	 * The target for a Rules object is to approximate a list of sentences
	 */
	private static class RulesTarget implements Target<Rules>
	{		
		private List<List<String>> data;
		private int sampleSize;
		private int repeats;
		
		private Distance<List<List<String>>> distance = new HausdorffDistance<List<String>>(new LevenshteinDistance<String>());

		public RulesTarget(List<List<String>> data, int sampleSize, int repeats)
		{
			this.data = data;
			this.sampleSize = sampleSize;
			this.repeats = repeats;
		}

		@Override
		public double score(Rules rules)
		{
			int sample = sampleSize == -1 ? data.size() : sampleSize;
			
			Grammar<String> grammar = rules.grammar();
			
			double sum = 0.0; 
			
			for(int r : series(repeats))
			{
				List<List<String>> dataSample = sampleSize == -1 ? data : Datasets.sample(data, sample);
				List<List<String>> grammarSample = new ArrayList<List<String>>(sample);
				for(int i : series(sample))
				{
					List<String> sentence = grammar.generateSentence(TOP, 0, MAX_DEPTH);
					if(sentence == null)
						return Double.NEGATIVE_INFINITY;
					grammarSample.add(sentence);
				}
				sum += distance.distance(dataSample, grammarSample);
				
			}
			
			return - (sum / repeats);
		}

	}

	public static Target<Rules> target(List<List<String>> data, int sampleSize, int repeats)
	{
		return new RulesTarget(data, sampleSize, repeats);
	}
}
