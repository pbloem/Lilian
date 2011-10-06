package org.lilian.grammars;

import java.util.*;
import java.io.*;

import org.lilian.util.*;
import org.lilian.util.trees.Tree;

/**
 * This interface describes a class that a Grammar returns to parse a sentence.
 *
 * The parse object is created by Grammar<T>.parse(Collection<T> sentence). The object
 * can then be used to check if the sentence is a member of the grammar. The object
 * can also be used to generate the best parse for thios sentence, or a collection of
 * all possible parses.
 *
 * This class is necessary because some parsers (like CYK parsers) create a datastructure
 * first, that represents the parse, and from that, generate additional information. This
 * approach allows a user to check for set membership and then generate all parses,
 * without having to redo the first stage of the parsing process.
 */
public interface Parse<T>
{
	/**
	 * Checks if the sentence that generated this parse is a member of the underlying
	 * grammar.
	 *
	 * @return  <code>true</code> if the sentence is a member of the underlying
	 *          grammar, <code>false</code> otherwise.
	 */
	public boolean isMember();

	/**
	 * Returns the best parse for this sentence (optional operation).
	 *
	 * If the underlying grammar has some definition that can be used to rate
	 * parses (eg, a probabilistic grammar can assign a parse a probability, or
	 * other might prefer the shallowest parse tree) this returns the best
	 * parse. If the grammar has no such measure, this method can just return
	 * any parse (perhaps the one that is fastest to generate) or it can leave
	 * this method unimplemented.
	 */
	public Pair<Tree<T>, Double> bestParse();

	/**
	 * Returns all possible parses for the sentence, according to this grammar.
	 */
	public Collection<Pair<Tree<T>, Double>> allParses();

	/**
	 * Writes lengthy info to files.
	 */
	public void write(File directory, String base) throws IOException;
}
