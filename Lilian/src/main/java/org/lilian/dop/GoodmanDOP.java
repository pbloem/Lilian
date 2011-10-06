package org.lilian.dop;

import java.util.*;
import java.io.*;

import org.lilian.*;
import org.lilian.corpora.*;
import org.lilian.corpora.wrappers.*;
import org.lilian.grammars.*;
import org.lilian.util.*;
import org.lilian.util.trees.Tree;

/**
 * An implementation of the DOP model, using Goodman Reduction.
 *
 * TODO and NOTES:
 * <ul>
 * <li /> At the moment, the implementation ignores POS rules (NOUN -> 'man') since
 *        we'll be parsing sentences of POS tags. There should be an option to
 *        include POS rules (ie. parse sentences of words instead of sentences of
 *        POS tags).
 * <li /> Information is lost in the conversion to  CNF (see ToCNFWrapper) see if 
 *        this is avoidable.
 * <li /> This class should be generalized properly to non-CNF trees. (The user 
 *        can always use the CNF wrapper from the outside).<br />
 *        The proper way to deal with unary nodes is described in the Master's 
 *        Thesis of Frederico Sangati (http://www.illc.uva.nl/Publications/ResearchReports/MoL-2007-23.text.pdf).<br />
 *        The proper way to deal with ternary and higher branching nodes seems
 *        to be analogous to the binary method used by Goodman. To use ternary 
 *        and higher rules the PCFG should do its own CNF-conversion, properly 
 *        implemented. 
 * <li /> The includeLeaves parameter does not refer to removal of all leaves,
 * 		  just leaves that have no siblings (like words nodes that have a POS 
 *        tag for a parent, like inmost parse trees). Note that this doesn't 
 *        just affect the nodes included in the model, but also how a sentence 
 *        to be parsed is placed in the parse chart. If leaves are included, the 
 *        parser searches for all pos tags that could've produced the word, and
 *        places those in the bottom row of the parse chart. If leaves are not
 *        included, the parser places the tokens from the sentence to be parsed
 *        directly in the bottom of the parse chart. Needless to say, this should
 *        be cleaned up.
 * <li /> This implementation and the two UDOP implementations do not check 
 * 		  specifically that parses are rooted in a general top sentence symbol.
 *        Implementing that behavior should improve performance considerably.
 * </ul>
 *   
 */
public class GoodmanDOP<T> implements Parser<T>
{
	/* The underlying PCFG */
	protected GoodmanCNF grammar = new GoodmanCNF();
	
	/* A set of all the encountered root-symbols (representing a sentence-level 
	 * constituent)
	 */
	protected Set<Constituent> rootSymbols = new HashSet<Constituent>();
	
	/* Whether to build a model for POS tags or for words */
	private boolean includeLeaves = false;
	
	//FIXME: delete this after testing to save on memory
	protected Parse<Constituent> lastParse;

	protected int uniqueConstituents = 0;

	/* Used to count the number of subtrees headed by some non-unique symbol
	 */
	protected Map<Constituent, MDouble> fromMap =
		new HashMap<Constituent, MDouble>();

	/**
	 * Emtpy DOP model.
	 */
	public GoodmanDOP()
	{
	}

	public GoodmanDOP(boolean includeLeaves)
	{
		this.includeLeaves = includeLeaves;
	}
	
	/**
	 * Creates a DOP model from a corpus.
	 */
	public GoodmanDOP(SequenceIterator<Tree<T>> corpus) throws IOException
	{
		add(corpus);
	}
	
	/**
	 * Creates a DOP model from a corpus.
	 */
	public GoodmanDOP(SequenceIterator<Tree<T>> corpus, boolean includeLeaves) 
		throws IOException
	{
		this(includeLeaves);
		add(corpus);
	}

	/**
	 * Walks through a corpus and adds all parsetrees to this model.
	 */
	public void add(SequenceIterator<Tree<T>> corpus) throws IOException
	{
		while (corpus.hasNext()) {
			add(corpus.next());
		}
	}

	/**
	 * Adds a parsetree to this model.
	 */
	public void add(Tree<T> tree)
	{
		//* Add the root symbol to the set of root symbols 
		rootSymbols.add(new Constituent(tree.getRoot().getValue()) );
		
		//* Convert the tree to CNF.
		Tree<ToCNF.Token<T>> cnfTree = ToCNF.toCNF(tree, true);
		
		//* Derive all Goodman rules and add them to the grammar 
		addGoodmanRules(cnfTree.getRoot(), null);
	}

	public GoodmanParse parse(Collection<? extends T> sentence)
	{
		Vector<Constituent> constituentSentence =
			new Vector<Constituent>(sentence.size());

		for (T token : sentence)
			constituentSentence.add(new Constituent(token));
		
		lastParse = includeLeaves ? 
			grammar.parse(constituentSentence):
			grammar.parsePOS(constituentSentence);

		return new GoodmanParse(lastParse);
	}
	
	public GoodmanParse parse(	Collection<? extends T> sentence, 
								int beamWidth, 
								boolean useRootSymbols)
	{
		Vector<Constituent> constituentSentence =
			new Vector<Constituent>(sentence.size());

		for (T token : sentence)
			constituentSentence.add(new Constituent(token));
		
		
		if(useRootSymbols)
			lastParse = includeLeaves ? 
					grammar.parse(constituentSentence, beamWidth, rootSymbols):
					grammar.parsePOS(constituentSentence, beamWidth, rootSymbols);
		else
			lastParse = includeLeaves ? 
					grammar.parse(constituentSentence, beamWidth):
					grammar.parsePOS(constituentSentence, beamWidth);

		return new GoodmanParse(lastParse);
	}

	/**
	 * For a node in a parse tree, this method generates all eight Goodman rules
	 * and adds them to the model.
	 */
	private void addGoodmanRules(Tree<ToCNF.Token<T>>.Node node, UniqueConstituent parentUC)
	{
		/* Make sure the node has two children or less. */
		if (node.getChildren().size() > 2)
			throw new RuntimeException("Node with more than two children encountered: " + node);
		
		if (node.getChildren().size() == 2)
		{
			addGoodmanRulesBinary(node, parentUC);
		} else 
			if(node.getChildren().size() == 1)
			{	
				if (node.getChildren().get(0).isLeaf() )
					addGoodmanRulesLeaf(node, parentUC);
				else
					//* TODO: 	we can add these rules once the parser can deal with them.
					//* 		for now, they are removed in the CNF process
					//* addGoodmanRulesUnary(node, parentUC);
					throw new RuntimeException("Node with unary production encountered. " + node);
			}
	}
	
	private void addGoodmanRulesBinary(Tree<ToCNF.Token<T>>.Node node, UniqueConstituent parentUC)
	{
		/* These determine which of the symbols to make unique, they will iterate
		 * through true and false like a binary string of length 3 */
		boolean		uniqueFrom = false,
					uniqueTo1  = false,
					uniqueTo2  = false;

		/* The probability of the rule created*/
		double prob;
		/* The tree symbols of the rule */
		Constituent from, to1, to2;
		
		if(parentUC == null) parentUC = 
									new UniqueConstituent(node.getValue());
		UniqueConstituent to1UC = 	new UniqueConstituent(
										node.getChildren().get(0).getValue());
		UniqueConstituent to2UC = 	new UniqueConstituent(
										node.getChildren().get(1).getValue());	

		for (int i = 1; i <= 8; i++)
		{
			if (uniqueFrom)	from = parentUC;
			else 			from = new Constituent(node.getValue());

			if (uniqueTo1)	to1 = to1UC;
			else 			to1 = new Constituent(node.getChildren().get(0).getValue());

			if (uniqueTo2)	to2 = to2UC;
			else 			to2 = new Constituent(node.getChildren().get(1).getValue());

			/* Calculate the probability of this based on the number of subtrees
			 * of the various rules */
			prob = 1.0;

			if (uniqueTo1)	 prob *= node.getChildren().get(0).getSubtreeCount();
			if (uniqueTo2)	 prob *= node.getChildren().get(1).getSubtreeCount();
			if (uniqueFrom)
				prob /= node.getSubtreeCount();
			else
			{
				MDouble freq;
				if (fromMap.containsKey(from))
				{
					freq = fromMap.get(from);
					freq.increment(node.getSubtreeCount());
				} else
				{
					freq = new MDouble(node.getSubtreeCount());
					fromMap.put(from, freq);
				}
			}

			/* Rules with a non-unique 'from' symbol should have their
			 * probability divided by the frequency of that symbol. The grammar
			 * object takes care of that */
			grammar.setRule(from, to1, to2, prob);

			/* To iterate over all possible combinations of these three,
			 * we flip uniqueTo1 every iteration, uniqueTo2 every second iteration
			 * and uniqueFrom every fourth iteration */
			uniqueTo2 = !uniqueTo2;
			if (i % 2 == 0)
				uniqueTo1 = !uniqueTo1;
			if (i % 4 == 0)
				uniqueFrom = !uniqueFrom;
		}
		
		/* Recurse */
		addGoodmanRules(node.getChildren().get(0), to1UC);
		addGoodmanRules(node.getChildren().get(1), to2UC);		
	}
	
	private void addGoodmanRulesLeaf(
			Tree<ToCNF.Token<T>>.Node node, 
			UniqueConstituent parentUC)
	{
		if(! includeLeaves)
			return;
		
		Constituent to = new Constituent(node.getChildren().get(0).getValue());
		
		Constituent from = new Constituent(node.getValue());
		if(parentUC == null) parentUC =	new UniqueConstituent(node.getValue());
		
		MDouble freq;
		
		if (fromMap.containsKey(from))
		{
			freq = fromMap.get(from);
			freq.increment(node.getSubtreeCount());
		} else
		{
			freq = new MDouble(node.getSubtreeCount());
			fromMap.put(from, freq);
		}
		
		grammar.setRule(from, to, 1.0);
		grammar.setRule(parentUC, to, 1.0);		
	}	
	

	/**
	 * A normal constituent, which maps to a T token.
	 */
	protected class Constituent
	{
		ToCNF.Token<T> value;
		
		public Constituent(T value)
		{
			this.value = new ToCNF.RegularToken<T>(value);
		}
		
		public Constituent(ToCNF.Token<T> value)
		{
			this.value = value;
		}

		public ToCNF.Token<T> getValue()
		{
			return value;
		}

		public int hashCode()
		{
			return 31 * value.hashCode();
		}

		public String toString()
		{
			return value.toString();
		}

		public boolean equals(Object o)
		{
			return (o instanceof GoodmanDOP<?>.Constituent) &&
			       ((Constituent)o).value.equals(this.value);
		}
	}

	/**
	 * The Goodman reduction creates unique versions of each constituent it
	 * encounters. For instance, for a node NP in a parse tree, it will create
	 * NP@i as a unique node, where i increments globally. This class represents
	 * those nodes.
	 *
	 * Since all constituents may need this treatment, it takes a constituent
	 * as input and uses uniqueConstituents to make it unique within this DOP model.
	 */
	protected class UniqueConstituent extends Constituent
	{
		int index;

		public UniqueConstituent(ToCNF.Token<T> value)
		{
			super(value);			
			index = ++uniqueConstituents;
		}
		
		public UniqueConstituent(ToCNF.Token<T> value, int id)
		{
			this(value);
			index = id;
		}

		public int hashCode()
		{
			return 31 * index + super.hashCode();
		}

		public String toString(){
			return super.toString() + "_" + index;
		}

		public boolean equals(Object o)
		{
			return (o instanceof GoodmanDOP<?>.UniqueConstituent) &&
                   ((UniqueConstituent)o).index == this.index;
		}
	}

	/**
	 * This extends CNFProbabilityGrammar, to store probabilities, and parse with
	 * them, while allowing us to override the probability function
	 */
	protected class GoodmanCNF extends CNFProbabilityGrammar<Constituent>
	{
		@Override
		public double getProbability(Constituent from, Constituent to1, Constituent to2)
		{
			double prob = super.getProbability(from, to1, to2);

			/*
			 * If the first symbol is non-unique, we store the probability
			 * non-normalized. Otherwise the stored probability can be returned
			 * directly.
			 */
			if (!(from instanceof GoodmanDOP.UniqueConstituent))
			{
				if (fromMap.containsKey(from))
					prob = prob / fromMap.get(from).getValue();
				else /* division by zero is zero today */
					prob = 0.0;
			}

			return prob;
		}
		
		public int size()
		{
			return super.probabilities.size();
		}
	}

	/**
	 * We have to translate the internal grammar's Parse<Constituent>
	 * to a Parse<T> object, so the outside world'll understand it.
	 */
	public class GoodmanParse implements Parse<T>
	{
		public Parse<Constituent> parent;
		
		private Collection<Pair<Tree<Constituent>, Double>> derivations = null;
		
		/** The collection of reconstructed parse (sums of derivations). 
		 */
		private List<Pair<Tree<T>, Double>> parses = null; 

		public GoodmanParse(Parse<Constituent> parent)
		{
			this.parent = parent;
		}

		public Collection<Pair<Tree<T>, Double>> allDerivations()
		{
			initDerivations();
			
			Vector<Pair<Tree<T>, Double>> result = 
				new Vector<Pair<Tree<T>, Double>>(derivations.size());
			
			for(Pair<Tree<Constituent>, Double> pair : derivations)
				result.add(new Pair<Tree<T>, Double>(
						ToCNF.reconstruct(reconstruct(pair.getFirst())), 
						pair.getSecond()));

			return result;
		}
		
		public Collection<Pair<Tree<T>, Double>> allParses()
		{
			initParses();
			return Collections.unmodifiableList(parses);						
		}

		@Override
		public Pair<Tree<T>, Double> bestParse()
		{
			return mostProbableParse();
		}
		
		/**
		 * Takes a parse tree and returns the tree with the constituent objects 
		 * turned into basic tokens.
		 *
		 * @param in A Goodmen reduced parse tree with unique constituents
		 * @return A Tree with those constituents removed
		 */
		private Tree<ToCNF.Token<T>> reconstruct(Tree<Constituent> in)
		{
			Constituent inRoot = in.getRoot().getValue();

			Tree<ToCNF.Token<T>> out = 
				new Tree<ToCNF.Token<T>>(inRoot.getValue());

			for (Tree<Constituent>.Node child : in.getRoot().getChildren()) {
				reconstruct(child, out.getRoot());
			}

			return out;
		}

		private void reconstruct(Tree<Constituent>.Node in, Tree<ToCNF.Token<T>>.Node out)
		{
			List<Tree<Constituent>.Node> inChildren = in.getChildren();

			Tree<ToCNF.Token<T>>.Node outChild;

			Constituent constituent = in.getValue();

			// * Create a new node in the out tree and recurse with that.
			outChild = out.addChild(constituent.getValue());
			
			for(Tree<Constituent>.Node child : inChildren)
				reconstruct(child, outChild);
		}

		@Override
		public boolean isMember()
		{
			return parent.isMember();
		}

		@Override
		public void write(File directory, String base) throws IOException
		{
			/* From symbol frequencies */
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(directory, base + ".fromSymbols.csv")));
			for(Constituent from : fromMap.keySet())
				out.write(from + "\t " + fromMap.get(from) + "\n");
			
			out.flush();
			out.close();			

			/* All parses */
			out = new BufferedWriter(new FileWriter(new File(directory, base + ".parses.csv")));

			Collection<Pair<Tree<T>, Double>> v = allParses();

			for(Pair<Tree<T>, Double> pair : v) 
				out.write(pair.getFirst() + ",\t" + pair.getSecond() + "\n");

			out.flush();
			out.close();
			
			parent.write(directory, "derivations." + base);
		}
		
		public Pair<Tree<T>, Double> mostProbableParse()
		{
			initParses();
			if(parses.size() == 0) 
				return null;
			return parses.get(0);
		}
		
		public Pair<Tree<T>, Double> mostProbableDerivation()
		{
			Pair<Tree<Constituent>, Double> in = parent.bestParse();
			
			if(in == null) return null;
			
			Pair<Tree<T>, Double> result = 
				new Pair<Tree<T>, Double>(
						ToCNF.reconstruct( reconstruct(in.getFirst())), 
						in.getSecond()); 
	
			return result;			
		}
		
		private void initDerivations()
		{
			if(derivations != null)
				return;
			
			derivations = new ArrayList<Pair<Tree<Constituent>, Double>>();

			for(Pair<Tree<Constituent>, Double> pair : parent.allParses())
				if(isGoodmanDerivation(pair.getFirst()))
					derivations.add(pair);
		}
		
		/* Initialize the parses object.
		 */
		private void initParses()
		{
			if(parses != null)
				return;
			
			initDerivations();
			
			Map<Tree<T>, MDouble> parsesMap = new LinkedHashMap<Tree<T>, MDouble>();
			Tree<T> reconstructedTree;
			
			for(Pair<Tree<Constituent>, Double> pair : derivations)
			{
				if(isGoodmanDerivation(pair.getFirst()))
				{
					reconstructedTree = ToCNF.reconstruct(
							reconstruct(pair.getFirst()));
					
					if(parsesMap.containsKey(reconstructedTree))
						parsesMap.get(reconstructedTree).increment(pair.getSecond());
					else
						parsesMap.put(reconstructedTree, 
								new MDouble(pair.getSecond()));
				}
			}
			
			parses = new ArrayList<Pair<Tree<T>, Double>>(parsesMap.size());
			for(Map.Entry<Tree<T>, MDouble> entry : parsesMap.entrySet())
				parses.add(new Pair<Tree<T>, Double>(entry.getKey(), entry.getValue().getValue()));
			
			// * Sort the parses by probability in descending order 
			Collections.sort(parses, Collections.reverseOrder(new TreeDoubleComp()));
		}
		
		public String toString()
		{
			return parent.toString();
		}
		
		private class TreeDoubleComp implements Comparator<Pair<Tree<T>, Double>>
		{
			public int compare(Pair<Tree<T>, Double> arg0,
					Pair<Tree<T>, Double> arg1)
			{
				return Double.compare(arg0.getSecond(),arg1.getSecond());  
			}
		}
	}
	
	public boolean isGoodmanDerivation(Tree<Constituent> tree)
	{
		if(tree.getRoot().getValue() instanceof GoodmanDOP.UniqueConstituent)
			return false;
		
		for(Constituent constit : tree.getLeaves())
			if(constit instanceof GoodmanDOP.UniqueConstituent)
				return false;
			
		return true;
	}
}
