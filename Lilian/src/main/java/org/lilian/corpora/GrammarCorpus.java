package org.lilian.corpora;

import java.io.*;
import java.util.*;

import org.lilian.grammars.*;
import org.lilian.util.*;
import org.lilian.util.trees.Tree;
import org.lilian.*;

/**
 * This corpus randomly generates sentences from a given grammar. Since the
 * seed is fixed for the corpus, the Corpus is entirely deterministic.
 *    
 * @param <T>
 */

public class GrammarCorpus<T> 
	extends AbstractCorpus<Tree<T>> 
{
	private Random random;
	private Grammar<T> grammar;
	private T topSymbol = null;
	private int maxSentences;
	private int minDepth;
	private int maxDepth;
	private int sentences;
	private int seed;
	
	public GrammarCorpus(Grammar<T> grammar, int maxSentences, int minDepth, int maxDepth, int seed)
	{
		this.seed = seed;
		this.grammar = grammar;
		this.maxSentences = maxSentences;
		this.maxDepth = maxDepth;
		this.minDepth = minDepth;
	}

	public GrammarCorpus(Grammar<T> grammar, T topSymbol, int maxSentences, int minDepth, int maxDepth, int seed)
	throws IOException
	{
		this.seed = seed;
		this.grammar = grammar;
		this.topSymbol = topSymbol;
		this.maxSentences = maxSentences;
		this.maxDepth = maxDepth;
		this.minDepth = minDepth;
	}


	@Override
	public Iterator<Tree<T>> iterator() {
		return new GrammarCorpusIterator();
	}

	public String toString()
	{
		return "GrammarCorpus, based on " + grammar.getClass() + "-grammar, " + sentences + " sentences ";	
	}		

	public class GrammarCorpusIterator
		extends AbstractCorpusIterator<Tree<T>>
	{
		public GrammarCorpusIterator()
		{
			sentences = 0;
			random = new Random(seed);
		}
		
		public boolean hasNext()
		{
			return (sentences < maxSentences);
		}
	
		public Tree<T> next()
		{
			if(sentences >= maxSentences)
				throw new NoSuchElementException();
			
			sentences++;
			return topSymbol == null ? 
					grammar.generateTree(minDepth, maxDepth, random) :
					grammar.generateTree(topSymbol, minDepth, maxDepth, random);
		}
	}
}