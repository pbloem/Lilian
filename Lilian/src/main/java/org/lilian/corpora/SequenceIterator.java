package org.lilian.corpora;

import java.io.*;
import java.util.*;

/**
 * This interface determines the two methods required for corpora.
 *
 * Classes implementing this interface represent a corpus and feed
 * tokens to the program using them. They will usually be generated
 * from a corpus text file, but they could also generate tokens on
 * the fly (like a random stream of characters).
 *
 * It is suggested that any clas implementing this interface to read 
 * some specific file format, also have a static method
 * <pre> void generateCorpus(Corpus<T> in, File directory, String name) </pre> 
 * that creates a corpus file based on 'in', to be read by its class. For 
 * instance, AdiosCorpus has such a method that generates an Adios corpus 
 * file from any corpus.
 * 
 * Whether the token objects returned by the corpus are the same object each 
 * run or are regenerated after the corpus is reset, differs per corpus. The 
 * latter is recommended.
 */
public interface SequenceIterator<T> extends Iterator<T> {

	/**
	 * Returns the next token in the corpus.
	 * 
	 * @throws NoSuchElementException If method is called on a corpus with no 
	 * 		further elements to return.
	 */
	public T next();

	/**
	 * Returns whether the corpus can return more tokens
	 */
	public boolean hasNext();

	/**
	 * Returns whether the cursor is currently at the end of a sentence.
	 * (ie. the last token returned was the last in the sentence)
	 */
	public boolean atSequenceEnd();

}	
