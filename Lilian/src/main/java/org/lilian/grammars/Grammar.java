package org.lilian.grammars;

import java.util.*;
import java.io.*;

import org.lilian.util.*;
import org.lilian.util.trees.Tree;

/**
 * This class represents a grammar based on rewrite rules. Each rewrite rule 
 * has a symbol of type T on the left (from), and a collection of rules (arbitrary 
 * length) on the right (to). A rule dictates that 'from' may be rewritten into 'to'.
 * Applying these rewrite rules in the right order allows a top symbol to be rewritten
 * into a sequence of terminals (like a sentence).
 *
 * The generic T is the type of the symbols. It should be noted that both the terminals
 * (the tokens in the sequential dataset) and the nonterminal symbols should fall under
 * T. This interface does not specify how this should be accomplished. The simplest method
 * would be to use strings for both. A more elegant solution would be to create a class with
 * two subclasses, one for terminals, one for symbols.
 */
 
public interface Grammar<T> extends Parser<T> {

	/**
	 * Adds this rule to the grammar, telling it that 'from' can be rewritten into
	 * 'to'. Adding the same rule several times may be significant to some grammars
	 * (such as those tthat count frequencies)
	 *
	 * @param from
	 * @param to
	 *
	 */
	public void addRule(T from, Collection<? extends T> to);
	
	/**
	 * Adds a rule with a frequency (optional operation).
	 *
	 * Adds a rule with a frequency. If the rule already exists, the 
	 * frequency is added to tyhe existing frequency.
	 *
	 * @param from
	 * @param to
	 * @param freq
	 *
	 */
	public void addRule(T from, Collection<? extends T> to, double freq);

	/**
	 * Adds a rule with a frequency (optional operation).
	 * 
	 * If the rule already exists, its frequency is replaced by 
	 * this frequency.
	 */
	public void setRule(T from , Collection<? extends T> to, double  freq);
	
	/**
	 * Returns the symbols that can occur at the leaf nodes of a parse tree. Note
	 * that by our broad definition of a grammar, a symbol can be both a terminal and a
	 * non-terminal.
	 *  
	 * @return The symbols in this grammar that occur at the leaves of the grammar.
	 */
	// public Collection<T> terminals();

	/**
	 * Returns the symbols that can occur at the internal nodes of a parse tree. Note
	 * that by our broad definition of a grammar, a symbol can be both a terminal and a
	 * non-terminal.
	 *  
	 * @return The symbols in this grammar that occur at the leaves of the grammar.
	 */
	// public Collection<T> nonTerminals();
	
	/**
	 * Generates a random sentence, according to this grammar. (optional operation)
	 * If the grammar is probability based, that should affect the distribution of 
	 * the sentences.
	 *
	 * Since this type of grammar cannot distinguish terminal and leaf symbols,
	 * there is no enforced guarantee that the sentence contains only leaf nodes.
	 * However this method should not return a sentence with any symbol that can be 
	 * rewritten into anything else.
	 * 
	 * @param topSymbol The symbol to rewrite into a sentence.
	 * @param maxDepth The maximum depth for the generated sentence. 
	 * (The maximum number of rewrites for each symbol)
	 * @return A randomly generated sentence. null if the model can't generate 
	 *         any sentences yet, or none at this maxDepth.
	 */
	public List<T> generateSentence(T topSymbol, int minDepth, int maxDepth);
	
	public List<T> generateSentence(int minDepth, int maxDepth);
	
	public void write(File directory, String base) throws IOException;
	
	/**
	 * Parses this sentence, and returns the information in a parse object.
	 */		
	// public Parse<T> parse(Collection<? extends T> sentence);

	public Tree<T> generateTree(int minDepth, int maxDepth, Random random);
	
	public Tree<T> generateTree(T topSymbol, int minDepth, int maxDepth, Random random);	
}
