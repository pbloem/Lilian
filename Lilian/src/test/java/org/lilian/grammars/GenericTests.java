package org.lilian.grammars;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.experiment.Resources;
import org.lilian.experiment.Tools;
import org.lilian.search.evo.grammar.Rules;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.HausdorffDistance;
import org.lilian.util.distance.LevenshteinDistance;

public class GenericTests
{

	@Test
	public void test()
	{
		for(int i : series(25))
			System.out.println(TestGrammars.mirror().generateSentence("S", 0, 25));
		
		List<List<String>> data = Resources.toyGrammarData("mirror", 1000);
		
		List<String> terminals = Rules.terminals(data);
		Rules rules = Rules.random(20, 0.66, terminals, 0.1, 10);

		for(int i : series(rules.size()))
			System.out.println(rules.rules().get(i) + "---" + rules.weights().get(i));
		
		Grammar<String> grammar = rules.grammar();
		for(int i : series(25))
		{
			System.out.println(grammar.generateSentence(Rules.TOP, 0, 25));
			
		}
	}
	
	@Test
	public void testDistance()
	{
		Global.random = new Random();
		int n = 100;
		int r = 1000;
		int rr = 10;
		
		List<Double> values = new ArrayList<Double>();
		for(int k : series(rr))
		{		
			double sum = 0.0;

			for(int j : series(r))
			{
				Distance<List<List<String>>> distance = new HausdorffDistance<List<String>>(new LevenshteinDistance<String>());
				
				Grammar<String> grammar = Resources.toyGrammar("mirror");
				
				List<List<String>> sample1 = new ArrayList<List<String>>(), sample2 = new ArrayList<List<String>>();
				
				for(int i : series(n))
				{
					sample1.add(grammar.generateSentence("S", 0, 25));
					sample2.add(grammar.generateSentence("S", 0, 25));
				}
				
		//		for(int i : series(n))
		//			System.out.println(sample1.get(i));
		//
		//		for(int i : series(n))
		//			System.out.println(sample2.get(i));
				
				
				sum += distance.distance(sample1, sample2);
			}
			values.add(sum/r);
		}
			
		
		System.out.println(Tools.mean(values));
		System.out.println(Tools.standardDeviation(values));
	}

}
