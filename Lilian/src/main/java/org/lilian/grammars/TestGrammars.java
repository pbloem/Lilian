package org.lilian.grammars;

import java.util.*;
import org.lilian.util.*;
import org.lilian.util.trees.Tree;


/**
 * Provides some relatively simple hardcoded grammars to test stuff with  
 *
 * TODO: move to a utility class Grammars.java
 */

public class TestGrammars
{

	public static PCFGrammar<String> adriaansVervoort()
	{
		
		PCFGrammar<String> g = new PCFGrammar<String>();
		
		addRule("S NP Vi ADV", 1.0, g);
		addRule("S NPa VPa", 1.0, g);
		addRule("S NPa Vs that S", 1.0, g);
		
		addRule("NP NPa", 1.0, g);
		addRule("NP NPp ", 1.0, g);
		
		addRule("VPa Vt NP", 1.0, g);
		addRule("VPa Vt NP P NPp", 1.0, g);
		
		addRule("NPa john", 1.0, g);
		addRule("NPa mary", 1.0, g);
		addRule("NPa the Nh", 1.0, g);
		
		addRule("NPp the Nt", 1.0, g);
		
		addRule("N Nh", 1.0, g);
		addRule("N Nt", 1.0, g);
		
		// human nouns
		addRule("Nh man", 1.0, g);
		addRule("Nh child", 1.0, g);
		
		// thing nouns
		addRule("Nt car", 1.0, g);
		addRule("Nt city", 1.0, g);
		addRule("Nt house", 1.0, g);
		addRule("Nt shop", 1.0, g);
		
		addRule("P with", 1.0, g);
		addRule("P near", 1.0, g);
		addRule("P in", 1.0, g);
		addRule("P from", 1.0, g);
		
		addRule("Vi appears", 1.0, g);
		addRule("Vi is", 1.0, g);
		addRule("Vi seems", 1.0, g);
		addRule("Vi looks", 1.0, g);
		
		addRule("Vs thinks", 1.0, g);
		addRule("Vs hopes", 1.0, g);
		addRule("Vs tells", 1.0, g);
		addRule("Vs says", 1.0, g);
		
		addRule("Vt knows", 1.0, g);		
		addRule("Vt likes", 1.0, g);
		addRule("Vt misses", 1.0, g);
		addRule("Vt sees", 1.0, g);
		
		addRule("ADV large", 1.0, g);
		addRule("ADV small", 1.0, g);
		addRule("ADV ugly", 1.0, g);
		addRule("ADV beautiful", 1.0, g);
		
		return g;
	}
	
	public static PCFGrammar<String> ta1()
	{
		
		PCFGrammar<String> g = new PCFGrammar<String>();
		
		addRule("S prec np2 vp ptag", 1.0, g);
		addRule("S frec np2 vp ftag", 1.0, g);
		addRule("S frec iv6 iv55", 1.0, g);
		addRule("S that np2 iv5 iv6 iv4 np2", 1.0, g);
		
		addRule("np art noun", 1.0, g);
		addRule("np propn", 1.0, g);
		
		addRule("np2 the noun", 1.0, g);
		addRule("np2 propn", 1.0, g);
		
		addRule("propn p vp2", 1.0, g);
		addRule("propn p", 1.0, g);
		
		addRule("pp p and p vp6", 1.0, g);
		addRule("pp p p and p vp6", 1.0, g);
		
		addRule("vp iv and com", 1.0, g);
		addRule("vp2 who tv np", 1.0, g);
		
		addRule("com np iv2", 1.0, g);
		
		addRule("rec p vp5 that rec", 1.0, g);
		addRule("rec p vp5 that", 1.0, g);
		
		addRule("frec pf vp5 that rec", 1.0, g);
		
		addRule("ftag , doesn't she", 1.0, g);
		addRule("prec pp that rec", 1.0, g);
		
		addRule("ptag , don't they", 1.0, g);
		addRule("iv5 is iv5-ex", 1.0, g);
		addRule("iv55 is iv55-ex", 1.0, g);
		addRule("iv6 to iv6-ex", 1.0, g);
		
		addRule("art the", 1.0, g);
		addRule("art a", 1.0, g);
		
		addRule("noun cat", 1.0, g);
		addRule("noun dog", 1.0, g);
		addRule("noun horse", 1.0, g);
		addRule("noun cow", 1.0, g);
		addRule("noun rabbit", 1.0, g);
		addRule("noun bird", 1.0, g);
		
		addRule("p Joe", 1.0, g);
		addRule("p Beth", 1.0, g);
		addRule("p Jim", 1.0, g);
		addRule("p Cindy", 1.0, g);
		addRule("p Pam", 1.0, g);
		addRule("p George", 1.0, g);
		

		addRule("pf Beth", 1.0, g);
		addRule("pf Cindy", 1.0, g);
		addRule("pf Pam", 1.0, g);
		
		addRule("vp5 believes", 1.0, g);
		addRule("vp5 thinks", 1.0, g);
		
		addRule("vp6 believe", 1.0, g);
		addRule("vp6 think", 1.0, g);
		
		addRule("iv meows", 1.0, g);
		addRule("iv barks", 1.0, g);
		
		addRule("iv2 laughs", 1.0, g);
		addRule("iv2 jumps", 1.0, g);
		addRule("iv2 flies", 1.0, g);
		
		addRule("iv5-ex easy", 1.0, g);
		addRule("iv5-ex tough", 1.0, g);
		addRule("iv5-ex eager", 1.0, g);
		
		addRule("iv55-ex tough", 1.0, g);
		addRule("iv55-ex easy", 1.0, g);
		
		addRule("iv6-ex please", 1.0, g);
		addRule("iv6-ex read", 1.0, g);
		
		addRule("iv4 annoys", 1.0, g);
		addRule("iv4 worries", 1.0, g);
		addRule("iv4 disturbs", 1.0, g);
		addRule("iv4 bothers", 1.0, g);
		
		addRule("tv scolds", 1.0, g);
		addRule("tv loves", 1.0, g);
		addRule("tv adores", 1.0, g);
		addRule("tv worships", 1.0, g);
		
		return g;
	}
	
	public static PCFGrammar<String> simple()
	{
		PCFGrammar<String> g = new PCFGrammar<String>();
		
		addRule("S B B", 1.0, g);
		addRule("B C C", 1.0, g);		
		
		addRule("C D0 D0", 1.0, g);
		addRule("C D0 D1", 1.0, g);
		addRule("C D1 D0", 1.0, g);		
		addRule("C D1 D1", 1.0, g);
		
		addRule("D0 0", 1.0, g);
		addRule("D1 1", 1.0, g);
	
		return g;
	}
	
	/**
	 * Adds a rule to a string grammar
	 * the string "NP the N" represents the rule
	 * NP -> the N 
	 */
	public static void addRule(String r, double f, Grammar<String> g)
	{
		StringTokenizer st = new StringTokenizer(r);
		String from = st.nextToken();
		Vector<String> to = new Vector<String>();
		while(st.hasMoreTokens())
			to.add(st.nextToken());
		
		g.addRule(from, to, f);
	}
	
	/**
	 * Tokenizes a string into a sequence f words. This tokenizes by
	 * whitespace only.
	 * 
	 * @param r
	 * @return
	 */
	public static Collection<String> toSentence(String r)
	{
		StringTokenizer st = new StringTokenizer(r);
		Vector<String> sentence = new Vector<String>();
		while(st.hasMoreTokens())
			sentence.add(st.nextToken());
		
		return sentence;
	}	
	
	public static void main(String[] args)
	{
		Grammar<String> g = simple();
		
		for(int i = 0; i < 20; i++)
		{
			Tree<String> tree = g.generateTree("S", 0, 0, null);
			
			System.out.println(tree.getLeaves());
		}
	}
}
