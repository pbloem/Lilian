package org.lilian.grammars;

import java.io.*;
import java.util.*;
import org.lilian.util.*;
import org.lilian.util.trees.Tree;
import org.lilian.models.*;


/**
 * This class represents a Probability based grammar in Chomsky Normal Form.
 * 
 * Contrary to PCFGrammer, this grammar stores probabilities directly, rather
 * than frequencies from which probabilities are then derived.
 * 
 * TODO: 
 * <ul>
 *  <li> The method of storing probabilities in a Map<CNFRule, MDouble> doesn't 
 *  work very well. For each probability request, two CNFRule objects are 
 *  created</li> 
 * </ul>
 * 
 * OPTIMIZATION:
 * <ul>
 * <li/> Use ArrayLists instead of vectors.
 * </ul>
 */

public class CNFProbabilityGrammar<T> implements Grammar<T>
{

	/* maps constituents to all rules that have them on the right as the single
	 * constituent (these have to be terminal rules) */
	protected LinkedHashMap<T, Set<CNFRule>> oneSymbolMap;
	
	/* maps constituents to rules where they occur as the left 'to' symbol */
	protected LinkedHashMap<T, Set<CNFRule>> leftSymbolMap;
	
	/* maps constituents to rules where they occur as the right 'to' symbol */	
	protected LinkedHashMap<T, Set<CNFRule>> rightSymbolMap;
	
	/* maps two constituents to all rules that have them on the right */
	protected LinkedHashMap<Pair<T, T>, Set<CNFRule>> twoSymbolMap;
	
	/* maps constituents to all rules that have them on the left */
	protected LinkedHashMap<T, Set<CNFRule>> fromMapCNF;
	
	/* Store the frequencies of rules and constituents*/
	protected Map<CNFRule, MDouble> probabilities;

	private Set<T> emptySet = Collections.emptySet();
	
	public CNFProbabilityGrammar()
	{
		/* Clear the current CNF version of the grammar */
		oneSymbolMap = new LinkedHashMap<T, Set<CNFRule>>();
		twoSymbolMap = new LinkedHashMap<Pair<T, T>, Set<CNFRule>>();
		leftSymbolMap  = new LinkedHashMap<T, Set<CNFRule>>();
		rightSymbolMap = new LinkedHashMap<T, Set<CNFRule>>();
		fromMapCNF = new LinkedHashMap<T, Set<CNFRule>>();
		probabilities = new HashMap<CNFRule, MDouble>();
	}

	@Override
	public void addRule(T from, Collection<? extends T> to)
	{
		if(to.size() < 1 || to.size() > 2)
			throw new IllegalArgumentException("Rule not in Chomsky Normal Form. Right-hand side ("+to+") must have 1 or 2 symbols. ");
		
		if(to.size() == 2)
		{
			Iterator<? extends T> it = to.iterator();
			addRule(from, it.next(), it.next());
		}
		
		if(to.size() == 1)
			addRule(from, to.iterator().next());
	}

	@Override
	public void addRule(T from, Collection<? extends T> to, double probability)
	{
		if(to.size() < 1 || to.size() > 2)
			throw new IllegalArgumentException("Rule not in Chomsky Normal Form. Right-hand side ("+to+") must have 1 or 2 symbols. ");
		
		if(to.size() == 2)
		{
			Iterator<? extends T> it = to.iterator();
			addRule(from, it.next(), it.next(), probability);
		}		
		
		if(to.size() == 1)
			addRule(from, to.iterator().next(), probability);
	}

	@Override
	public void setRule(T from, Collection<? extends T> to, double probability)
	{
		if(to.size() < 1 || to.size() > 2)
			throw new IllegalArgumentException("Rule not in Chomsky Normal Form. Right-hand side ("+to+") must have 1 or 2 symbols. ");
		
		if(to.size() == 2)
		{
			Iterator<? extends T> it = to.iterator();
			setRule(from, it.next(), it.next(), probability);
		}		
		
		if(to.size() == 1)
			setRule(from, to.iterator().next(), probability);
	}
	
	public void addRule(T from, T to1)
	{
		if(from == null || to1 == null)
			throw new IllegalArgumentException("Input arguments can't be null.");
			
		addCNFRule(new CNFRule(from, to1, null), 1.0, true);		
	}	
	
	public void addRule(T from, T to1, T to2)
	{
		if(from == null || to1 == null || to2 == null)
			throw new IllegalArgumentException("Input arguments can't be null.");		
		
		addCNFRule(new CNFRule(from, to1, to2), 1.0, true);		
	}

	public void addRule(T from, T to1, double prob)
	{
		if(from == null || to1 == null)
			throw new IllegalArgumentException("Input arguments can't be null.");
			
		addCNFRule(new CNFRule(from, to1, null), prob, true);		
	}		
	
	public void addRule(T from, T to1, T to2, double prob)
	{
		if(from == null || to1 == null || to2 == null)
			throw new IllegalArgumentException("Input arguments can't be null.");		
		
		addCNFRule(new CNFRule(from, to1, to2), prob, true);		
	}

	public void setRule(T from, T to1, double prob)
	{
		if(from == null || to1 == null)
			throw new IllegalArgumentException("Input arguments can't be null.");
			
		addCNFRule(new CNFRule(from, to1, null), prob, false);		
	}		
	
	public void setRule(T from, T to1, T to2, double prob)
	{
		if(from == null || to1 == null || to2 == null)
			throw new IllegalArgumentException("Input arguments can't be null.");		
		
		addCNFRule(new CNFRule(from, to1, to2), prob, false);		
	}
	
	public double getProbability(T from, Collection<? extends T> to)
	{
		if(to.size() < 1 || to.size() > 2)
			throw new IllegalArgumentException("Rule not in Chomsky Normal Form. Right-hand side (" + to + ") must have 1 or 2 symbols. ");
		
		Iterator<? extends T> it = to.iterator();
		return getProbability(from, it.next(), it.next()); 
	}

	public double getProbability(T from, T to1, T to2)
	{
	
		CNFRule rule = new CNFRule(from, to1, to2);

		if (! probabilities.containsKey(rule) )
			return 0.0;
		
		return probabilities.get(rule).getValue(); 

	}
	
	private double getProbability(CNFRule rule)
	{
		/* 
		 * The reason this method doesn't check the hashmap directly with its 
		 * input is because getProbability cannot be bypassed, as that would break 
		 * classes that extend this class. See TODO above */ 
		return getProbability(rule.getFrom(), rule.getTo1(), rule.getTo2()); 
	}
	
	public Collection<T> generateSentence(T topSymbol,int minDepth,  int maxDepth)
	{
		throw new UnsupportedOperationException();		
	}

	public Collection<T> generateSentence(int minDepth, int maxDepth)
	{
		throw new UnsupportedOperationException();
	}

	public Parse<T> parse(Collection<? extends T> sentence)
	{
		Vector<T> newSentence = new Vector<T>(sentence.size());
		
		for(T token : sentence)
			newSentence.add(token);
		
		return new CNFParse(newSentence);
	}
	
	public Parse<T> parsePOS(Collection<? extends T> sentence)
	{
		Vector<T> newSentence = new Vector<T>(sentence.size());
		
		for(T token : sentence)
			newSentence.add(token);
		
		return new CNFParse(newSentence, true);
	}

	public Parse<T> parse(Collection<? extends T> sentence, int beamWidth)
	{
		Vector<T> newSentence = new Vector<T>(sentence.size());
		
		for(T token : sentence)
			newSentence.add(token);
		
		return new CNFParse(newSentence, false, beamWidth, emptySet);
	}
	
	public Parse<T> parsePOS(Collection<? extends T> sentence, int beamWidth)
	{
		Vector<T> newSentence = new Vector<T>(sentence.size());
		
		for(T token : sentence)
			newSentence.add(token);
		
		return new CNFParse(newSentence, true, beamWidth, emptySet);
	}
	
	public Parse<T> parse(	Collection<? extends T> sentence, 
							int beamWidth, 
							Set<T> rootSymbols )
	{
		Vector<T> newSentence = new Vector<T>(sentence.size());
		
		for(T token : sentence)
			newSentence.add(token);
		
		return new CNFParse(newSentence, false, beamWidth, rootSymbols);
	}
	
	public Parse<T> parsePOS(	Collection<? extends T> sentence, 
								int beamWidth,
								Set<T> rootSymbols )
	{
		Vector<T> newSentence = new Vector<T>(sentence.size());
		
		for(T token : sentence)
			newSentence.add(token);
		
		return new CNFParse(newSentence, true, beamWidth, rootSymbols);
	}	

	public void write(File directory, String base) throws IOException
	{
		
	}

	/**
	 * Adds a CNF rule to the model. This is the core addition method. All other
	 * methods for adding rules call this method. 
	 * 
	 * For most extensions of this class it will suffice to override this rule, 
	 * and keep the superclass versions of the various addRule() methods. 
	 * 
	 * @param rule
	 * @param freq
	 * @param increment Whether to increment the frequency. If false, the existing 
	 * 	frequency for this rule is replaced by the given frequencys 
	 */
	protected void addCNFRule(CNFRule rule, double probability, boolean increment)
	{
		if (probabilities.containsKey(rule))
		{
			MDouble prob = probabilities.get(rule);
			if(increment) 	prob.increment(probability);
			else			prob.setValue(probability);
		} else	
		{
			probabilities.put(rule, new MDouble(probability));
		}
		
		/* Add the rule to the 'from' map */
		Set<CNFRule> rules;
		if (fromMapCNF.containsKey(rule.getFrom()))
		{
			fromMapCNF.get(rule.getFrom()).add(rule);			
		} else
		{
			rules = new LinkedHashSet<CNFRule>();
			rules.add(rule);
			fromMapCNF.put(rule.getFrom(), rules);						
		}
		
		if(rule.getTo2() == null)
		{
			/* Add the rule to the map for one symbol */
			if(oneSymbolMap.containsKey(rule.getTo1()))
			{
				oneSymbolMap.get(rule.getTo1()).add(rule);			
			}else
			{
				rules = new LinkedHashSet<CNFRule>();
				rules.add(rule);
				oneSymbolMap.put(rule.getTo1(), rules);						
			}
		}else
		{
			/* Add the rule to the map for two symbols */
			Pair<T, T> key = 
				new Pair<T, T>(rule.getTo1(), rule.getTo2());
			if( twoSymbolMap.containsKey(key) ){
				twoSymbolMap.get(key).add(rule);			
			}else
			{
				rules = new LinkedHashSet<CNFRule>();
				rules.add(rule);
				twoSymbolMap.put(key, rules);						
			}
			
			/* Add the rule to the map for its left 'to' symbol */ 
			if(leftSymbolMap.containsKey(rule.getTo1()))
			{
				leftSymbolMap.get(rule.getTo1()).add(rule);			
			}else
			{
				rules = new LinkedHashSet<CNFRule>();
				rules.add(rule);
				leftSymbolMap.put(rule.getTo1(), rules);						
			}
			
			/* Add the rule to the map for its right 'to' symbol */ 
			if(rightSymbolMap.containsKey(rule.getTo2()))
			{
				rightSymbolMap.get(rule.getTo2()).add(rule);			
			}else
			{
				rules = new LinkedHashSet<CNFRule>();
				rules.add(rule);
				rightSymbolMap.put(rule.getTo2(), rules);						
			}			
		}		
	}	

	/**
	 * Represents a rule in Chomsky Normal Form.
	 * 
	 * These are production rules of the form A -> B, C. These symbols are 
	 * referred to as 'from', 'to1' and 'to2' from left to right. 
	 */
	protected class CNFRule implements Comparable<CNFRule>
	{
		private T from;
		private T to1;
		private T to2;
	
		/**
		 * Constructs a rule based on three constituent objects 
		 * 
		 * @param from	The production symbol (on the left of the arrow)
		 * @param to1	The leftmost symbol on the right of the arrow 
		 * @param to2	The rightmost symbol on the right of the arrow
		 */
		public CNFRule(T from, T to1, T to2)
		{
			if(from == null || to1 == null)
				throw new IllegalArgumentException("from or to1 cannot be null");
			
			this.from = from;
			this.to1 = to1;
			this.to2 = to2;
		}
	
		/**
		 * Returns the constituent that defines the left hand part of this rule.
		 * 
		 * @return A Constituent representing the left hand part of this rul.
		 */
		public T getFrom()
		{
			return from;
		}
	
		public T getTo1()
		{
			return to1;
		}
		
		public T getTo2()
		{
			return to2;
		}
		
		public boolean isUnary()
		{
			return to2 == null;
		}
	
		/**
		 * Returns this rule's hashcode.
		 * 
		 * (Code is based on java's hashcode for collections)
		 */
		public int hashCode(){
			int hashCode = 1;
	  		hashCode = 31*hashCode + from.hashCode();
	  		hashCode = 31*hashCode + to1.hashCode();
	  		hashCode = 31*hashCode + (to2==null ? 0 : to2.hashCode());
	  		
	  		return hashCode;
		}
	
		public boolean equals(Object o){
			
			if(o instanceof CNFProbabilityGrammar<?>.CNFRule)
			{
				CNFRule r = (CNFRule)o;
				return 
					(r.from.equals(this.from) &&
					 r.to1.equals(this.to1) &&
					 r.to2 == null ? this.to2 ==null : r.to2.equals(this.to2) );
			}else
				return false;
		}
	
		/**
		 * Defines an ordering over rules based on the ordering of their string 
		 * representations
		 */
		public int compareTo(CNFRule r){
				return this.toString().compareTo(r.toString());
		}
	
		
		public String toString()
		{
			if(to2 == null) return from + " -> " + to1;
			else return from + " -> " + to1 + " " + to2;
		}
	} 	
	
	
	/**
	 * Represents a parse from this grammar. 
	 */
	
	private class CNFParse implements Parse<T>{
		
		/* The 3d vector that will contain the parse values */
		private Vector<Vector<Vector<Node>>> array;
		
		private Set<T> rootSymbols = null;
		
		/* The length of the sentence */
		private int n;
		
		/* Whether to prune (Viterbi) */
		private boolean prune = false;
		/* The number of nodes to preserve at each step */
		private int beamWidth;
		
		/**
		 * Constructs a parse from the given sentence, according to this grammar
		 * 
		 * @param sentence A sentence of words (not POS symbols).
		 */
		
		public CNFParse(Collection<T> sentence)
		{
			init(sentence, false);
		}

		/**
		 * Constructs a parse from the given sentence, according to this grammar
		 * 
		 * @param sentence The sentence to parse
		 * @param posSentence 
		 *   If true, the sentence is regarded as a sentence of 
		 *   POS symbols, that are inserted directly in the bottom row of the parse 
		 *   chart. If false, the sentence is taken to be a sentence of words, and 
		 *   the bottom row of the chart is filled with all symbols that could 
		 *   have produced these words.   
		 */
		public CNFParse(Collection<T> sentence, boolean posSentence)
		{
			init(sentence, posSentence);
		}
		
		public CNFParse(	Collection<T> sentence, 
							boolean posSentence, 	
							int beamWidth,
							Set<T> rootSymbols)
		{
			prune = true;
			this.beamWidth = beamWidth;
			
			this.rootSymbols = rootSymbols;
			
			init(sentence, posSentence);
		}
		
		private void init(Collection<T> sentence, boolean posSentence)
		{
			n = sentence.size();

			/* create the parse chart (as a n x n matrix of empty vectors) */
			initializeArray(n);

			/* ** Phase one: place terminal symbols in the bottom row ** */
			
			Set<CNFRule> rules;
			Node wordNode, node;

			int i = 0;
			
			if(rootSymbols == null)
				this.rootSymbols = emptySet;
			
			for(T word : sentence)
			{
				/* Create a parse node for the word. */
				wordNode = new Node(word, 1.0);

				if(posSentence)
				{
					/* add the word nodes directly to the parse chart */
					array.get(i).get(1).add(wordNode);
				} else
				{
					/* Find the rules that could have produced the word */
					if(oneSymbolMap.containsKey(word))
							rules = oneSymbolMap.get(word);
					else 	rules = Collections.emptySet();

					/* Create a node on the bottom row of the parse chart for all 
					 * rules that could have produced the word */
					for(CNFRule rule : rules) 
					{
						node = new Node(rule.getFrom(), getProbability(rule), wordNode);
						array.get(i).get(1).add(node);
					}
				}
				i++;
			}
			
			/* Fill the parse chart */
			fillChart();			
		}
		
		/**
		 * At the start of this method, the bottom row of the parse chart is 
		 * filled with the POS symbols. This method fills the rest of the chart.
		 */
		private void fillChart()
		{
			Node node;

			/* Vectors of nodes to join, and their iterators */
			Vector<Node> nodes1, nodes2; 	
			
			/* Symbols to join in a rule */
			T symbol1, symbol2;
			
			Pair<T, T> key;

			/* All rules joining two s1 and s2 */
			Set<CNFRule> ruleSet;
			
			/* To complete the parse chart, we iterate over three values:
			 */
			
			/* l -- length of the span */
			for(int l = 1; l <= n;l++)
			{
				/* s -- start of span */
				for(int s = 0; s <= (n-l); s++)
				{
					/* p -- partition of span */
					for(int p = 0; p < l; p++)
					{
						/* we now have two points in the array (s, p) and
						 * (s+p, l-p). We need to find all rules that connect
						 * a symbol at (s, p) to a symbol at (s+p, l-p).
						 */

						/* Get all nodes at the first location ... */ 
						nodes1 = array.get(s).get(p);
						/* and from the second */						
						nodes2 = array.get(s+p).get(l-p);

						/* Check all combinations, of these nodes */
						for(Node node1 : nodes1)
						{
					
							// if(node1.isDisabled())
							//	 break;								

							if(!node1.isDisabled())
							{
								symbol1 = node1.getSymbol();
	
								for(Node node2 : nodes2)
								{
	
									//if(node2.isDisabled())
									//	break;
									if(!node2.isDisabled())
									{
										symbol2 = node2.getSymbol();
		
										/* get all rules joining s1 and s2 */
										key = new Pair<T, T>(symbol1, symbol2);
										
										if(twoSymbolMap.containsKey(key))
											ruleSet = twoSymbolMap.get(key);
										else
											ruleSet = Collections.emptySet();
										
										/* create a new node for all rules found */
										for(CNFRule rule : ruleSet)
										{
											/* Calculate the 'running probability' at 
											 * this point in the chart. At the top symbol, 
											 * this represents the parse probability of the
											 * tree */
											double probability =
												getProbability(rule) * 
												node1.getProbability() * 
												node2.getProbability();
		
											node = new Node(rule.getFrom(), probability,	
												node1, node2);
											
											array.get(s).get(l).add(node);
										}
									}
								}
							}
						
						}
					}
				}
				
				//* all nodes for some span-length have been created. We can now
				//* prune them by probability.
				if(prune) prune(l);
			}
		}
		
		private void prune(int length)
		{
			ArrayList<Node> nodes = new ArrayList<Node>();
			
			for(int i = 0; i < n ; i++)
				for(Node node : array.get(i).get(length))
					nodes.add(node);
			
			Collections.sort(nodes, new NodeComparator());
			
			//* TODO: We can optimize this by creating all nodes disabled by 
			//* default (if pruning is enabled). Then we would only have to 
			//* enable the node, and we could end this loop earlier.
			int i = 0;
			for(Node node : nodes)
			{
				if(i > beamWidth)
				{
					node.disable();
				}
				i++;
			}

		}


		/**
		 * Check whether this parse is positive, ie. the sentence is a member
		 * of the grammar. If any symbol in the grammar can produce the sentence
		 * this method returns true (not just if S can produce it).
		 *
		 * @return true is the sentence is a member.
		 */
		public boolean isMember()
		{
			/* If the top-left cell in the parse chart is non-empty then at least 
			 * one parse has been found. */
			return (array.get(0).get(n).size() > 0);
		}


		/**
		 * Returns the number of parses found for the full sentence.
		 *
		 */
		public int numberOfParses()
		{
			/* For every parse found, there is a node in the top left cell of the
			 * parse chart, so the size of that vector represents the amount of 
			 * parses found*/
			return array.get(0).get(n).size();
		}

		/**
		 * Generate all parses with their probabilities.
		 * 
		 * The collection is alway generated from the parse chart when this 
		 * method is called. Caching should be done outside this object. 
		 *
		 * @return A Vector of parse trees, sorted by probability, with the first 
		 * 	parse the most probable.
		 */
		public Collection<Pair<Tree<T>, Double>> allParses()
		{
			Vector<Node> topNodes = array.get(0).get(n);
			Vector<Pair<Tree<T>, Double>> result0 =
				new Vector<Pair<Tree<T>, Double>>(topNodes.size());

			/* The CNF parse tree */
			Tree<T> tree;
			
			double prob;

			/* Iterate over all nodes in the top cell of the parse chart, and 
			 * retrieve the trees that they represent 
			 */
			for(Node top : topNodes)
			{
				tree = new Tree<T>(top.getSymbol());

				/* Retrieve the tree from the parse chart */
				makeTree(top, tree.getRoot());
				
				prob = top.getProbability();
				
				result0.add(new Pair<Tree<T>, Double>(tree, new Double(prob)));
			}

			/* Sort by probability */ 
			Collections.sort(result0, new TreeDoubleComparator());
			
			return result0;
		}

		/**
		 * Generate all parses starting with a certain symbol, with their
		 * probabilities.
		 *
		 * @param symbol The symbol that the root node should have.
		 * @return A Vector containing parses (as Tree objects).
		 *         
		 */
		public Vector<Tree<T>> allParses(T topSymbol)
		{
			
			Vector<Node> topNodes = array.get(0).get(n);
			Vector<Pair<Tree<T>, Double>> result0 =
				new Vector<Pair<Tree<T>, Double>>(topNodes.size());
			Vector<Tree<T>> result =
				new Vector<Tree<T>>(topNodes.size());

			/* The CNF parse tree */
			Tree<T> tree;
			
			double prob;

			/* Iterate over all nodes in the top cell of the parse chart, and 
			 * retrieve the trees that they represent 
			 */
			for(Node top : topNodes)
			{
				if(top.getSymbol().equals(topSymbol))
				{
					tree = new Tree<T>(top.getSymbol());
	
					/* Retrieve the tree from the parse chart */
					makeTree(top, tree.getRoot());
					
					prob = top.getProbability();
					
					result0.add(new Pair<Tree<T>, Double>(tree, new Double(prob)));
				}
			}

			/* Sort by probability */ 
			Collections.sort(result0, new TreeDoubleComparator());

			for(Pair<Tree<T>, Double> pair : result0)
				result.add(pair.first());
			
			return result;
		}

		/**
		 * Returns the best parse, according to probability.
		 *
		 * Note: this method is O(n) with n being the total
		 * number of parses for this sentence.(see TODO)
		 *
		 * @return A pair, containing the parse (as a Tree object)
		 *         and the probability as a Double object. null if
		 *		   none are found.
		 */
		public Pair<Tree<T>, Double> bestParse()
		{
			Vector<Node> topNodes = array.get(0).get(n);

			/* the best node found */
			Node topTop = null;
			/* the probability of topTop */
			double prob;
			/* the probability of topProb */
			double topProb = -1.0;

			/* Go through all successful parses, remembering the best one. */
			for(Node top : topNodes)
			{
				prob = top.getProbability();
				if(prob > topProb)
				{
					topProb = prob;
					topTop = top;
				}
			}
			
			if(topTop == null)
				return null;

			Tree<T> tree = new Tree<T>(topTop.getSymbol());
			makeTree(topTop, tree.getRoot());
			
			return new Pair<Tree<T>, Double>(tree, topProb);
		}

		/**
		 * Returns the best parse, according to probability.
		 *
		 * Note: this method is O(n) with n being the total
		 * number of parses for this sentence.(see TODO)
		 *
		 * @param symbol The symbol that the root node should have.
		 * @return The best parse (as a Tree object). Returns null if none is 
		 * 		found
		 */
		public Tree<T> bestParse(T symbol)
		{
			Vector<Node> topNodes = array.get(0).get(n);

			/* the best node found */
			Node topTop = null;
			/* the probability of topTop */
			double prob;
			/* the probability of topProb */
			double topProb = -1.0;

			/* Go through all successful parses, remembering the best one. */
			for(Node top : topNodes)
			{
				if(top.getSymbol().equals(symbol))
				{
					prob = top.getProbability();
					if(prob > topProb)
					{
						topProb = prob;
						topTop = top;
					}
				}
			}
			
			if(topTop == null)
				return null;

			Tree<T> tree = new Tree<T>(topTop.getSymbol());
			makeTree(topTop, tree.getRoot());

			return tree;
		}

		/**
		 * Turns the parse that can be found under parserNode into treenodes
		 * under tree node.
		 *
		 * When it enters the method, treeNode already represents parserNode.
		 */
		private void makeTree(Node parserNode, Tree<T>.Node treeNode)
		{
			Node backlink1, backlink2;
			Tree<T>.Node node1, node2;

			backlink1 = parserNode.getBackLink1();
			backlink2 = parserNode.getBackLink2();

			/* if the parsernode has no children, return and stop the recursion */
			if(backlink1 == null)
				return;

			node1 = treeNode.addChild(backlink1.getSymbol());
			makeTree(backlink1, node1);

			/* If the parse node has only one child, stop here. */
			if(backlink2 == null)
				return;

			node2 = treeNode.addChild(backlink2.getSymbol());
			makeTree(backlink2, node2);

			return;
		}

		/**
		 * Fills the parse array with empty stuff.
		 *
		 * @param n The size of the sentence
		 */
		private void initializeArray(int n)
		{
			array = new Vector<Vector<Vector<Node>>>(n);
			Vector<Vector<Node>> subarray;

			for(int i = 0; i < n; i++)
			{
				subarray = new Vector<Vector<Node>>(n+1);
				for(int j = 0; j < n + 1; j++)
					subarray.add(new Vector<Node>());
				array.add(subarray);
			}
		}

		/**
		 * Writes
		 *  - the parsematrix
		 *  - all reconstructed parses with probabilities (sorted)
		 *  - all reconstructed parses starting with "top" (sorted)
		 *
		 * Three csv files are created for this purpose.
		 *
		 * @param n the base filename
		 */
		public void write(File directory, String base) throws IOException
		{
			// parsematrix

			BufferedWriter out = new BufferedWriter(
					new FileWriter(new File(directory, base + ".parsematrix.csv")));

			out.write(this.toString());

			out.flush();
			out.close();

			// all parses
			out = new BufferedWriter(new FileWriter(new File(directory, base + ".parses.csv")));

			Collection<Pair<Tree<T>, Double>> v = allParses();

			for(Pair<Tree<T>, Double> pair : v) 
			{
				out.write(pair.first() + ",\t" + pair.second() + "\n");
			}

			out.flush();
			out.close();
			
			out = new BufferedWriter(
					new FileWriter(new File(directory, base + ".CNFRules.csv")));
			
			Iterator<CNFRule> ruleIt = probabilities.keySet().iterator();
			CNFRule rule;
			while(ruleIt.hasNext())
			{
				rule = ruleIt.next();
				out.write(rule.getFrom() + " -> " + rule.getTo1());
				if(rule.getTo2() != null)
					out.write(" " + rule.getTo2());
				out.write("\t " + getProbability(rule)+ "\n");
			}

			out.flush();
			out.close();
		}

		/**
		 * Returns the parse chart in string form.
		 */
		public String toString()
		{
			StringBuilder sb = new StringBuilder();

			Iterator<Vector<Vector<Node>>> it1 = array.iterator();
			Vector<Vector<Node>> v2;
			Iterator<Vector<Node>> it2;
			Vector<Node> v3;
			Iterator<Node> it3;

			boolean first1 = true,
			first2,
			first3;

			/* Loop over lines */
			while(it1.hasNext()) 
			{
				if(first1)
					first1 = false;
				else
					sb.append("\n");

				v2 = it1.next();
				it2 = v2.iterator();

				first2 = true;

				/* Loop over cells */
				while(it2.hasNext())
				{
					if(first2)
						first2 = false;
					else
						sb.append(',');

					v3 = it2.next();
					
					//if(v3.size() < 1) 
					//	sb.append("         ");
					//else
						sb.append(String.format("%04d [%04d] ", v3.size(), numEnabled(v3)));
					
					it3 = v3.iterator();

					first3 = true;
					
					/* Loop over cell values */
					/*
					while(it3.hasNext()) 
					{
						if(first3)
							first3 = false;
						else
							sb.append(' ');

						sb.append(it3.next());
					}
					*/
				}
			}

			return sb.toString();
		}
		
		private int numEnabled(List<Node> in)
		{
			int out = 0;
			for(Node node : in)
			{
				if(!node.isDisabled())
					out++;
			}
			
			return out;
		}


		/**
		 * Used to store backlinks and probabilities in the parse chart.
		 *
		 * Three nodes can be created: a node containing just a string (for
		 * the words), a node containg one backlink (for the unit production
		 * rules) or a node containing two backlinks for the regular rules
		 */
		private class Node
		{
			private T symbol;
			private double probability = 1.0;
			private Node bl1, bl2;
			private boolean disabled = false;

			/**
			 * Default probability = 1.0;
			 */
			public Node(T word)
			{
				this.symbol = word;
			}
			
			public Node(T word, double probability)
			{
				this.symbol = word;
				this.probability = probability; 
				
			}			

			public Node(T word, double probability, Node backlink)
			{
				this.symbol = word;
				this.probability = probability;
				this.bl1 = backlink;
			}

			public Node(T word, double probability, Node backlink1, Node backlink2)
			{
				this.symbol = word;
				this.probability = probability;
				this.bl1 = backlink1;
				this.bl2 = backlink2;
			}

			public T getSymbol()
			{
				return symbol;
			}

			public double getProbability()
			{
				return probability;
			}
			
			public boolean isDisabled()
			{
				return disabled;
			}
			
			public void disable()
			{
				disabled = true;
			}

			public Node getBackLink1()
			{
				return bl1;

			}

			public Node getBackLink2()
			{
				return bl2;
			}

			public String toString()
			{
				return symbol.toString() + "(" + probability + ", dis:" + disabled +")";
			} 
		}

		/**
		 * Used to sort the list of parses by probability. 
		 */
		private class TreeDoubleComparator
		implements Comparator<Pair<Tree<T>, Double>>
		{
			public int compare(Pair<Tree<T>, Double> p1, Pair<Tree<T>, Double> p2)
			{
				Double p1Prob = p1.second();
				Double p2Prob = p2.second();
				
				if(rootSymbols.contains(p1.first().getRoot().getValue()))
					p1Prob += 1.0;
				if(rootSymbols.contains(p2.first().getRoot().getValue()))
					p2Prob += 1.0;
				
				int c = p1Prob.compareTo(p2Prob);

				return -c;
			}
		}
		
		/**
		 * Defines an ordering over nodes by their running probability, 
		 * descending (highest first).
		 */
		private class NodeComparator implements Comparator<Node>
		{
			public int compare(Node one, Node two)
			{
				return -Double.compare(one.getProbability(), two.getProbability());
			}
		}
	}


	public static void main(String[] args)
	{
		CNFProbabilityGrammar<String> g = new CNFProbabilityGrammar<String>();
		
		g.addRule("S", "NP", "V", 0.9);
		g.addRule("NP", "D", "N", 0.9);
		g.addRule("V", "walks", 1.0);
		g.addRule("D", "the", 1.0);
		g.addRule("N", "man", 0.9);
		
		g.addRule("S", "D", "NP", 0.1);
		g.addRule("NP", "ADJ", "N", 0.1);
		g.addRule("ADJ", "man", 0.1);
		g.addRule("N", "walks", 0.1);
		
		Parse<String> p = g.parse(Functions.sentence("the man walks"), 2);
		
		Collection<Pair<Tree<String>, Double>> trees = p.allParses(); 
		
		for(Pair<Tree<String>, Double> pair : trees)
			System.out.println(pair.first());
	}

	// TODO: implement these (see PCFGrammar)
	@Override
	public Tree<T> generateTree(T topSymbol, int minDepth,  int maxDepth, Random random)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Tree<T> generateTree(int minDepth, int maxDepth, Random random)
	{
		throw new UnsupportedOperationException();
	}
}
