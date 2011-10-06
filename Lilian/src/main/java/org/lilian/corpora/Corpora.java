package org.lilian.corpora;

import java.io.*;
import java.util.*;

import org.lilian.util.*;

/**
 * Static helper functions for corpus-classes
 * 
 */
public class Corpora
{
	/**
	 * A helper method for quickly creating a corpus of strings. 
	 * 
	 * The input string is tokenized by periods (.) to retrieve 
	 * the sentences and by whitespace to retrieve the tokens.
	 */
	public static SequenceCorpus<String> quickCorpus(String in)
	{
		String sentence;
		List<List<String>> coll = new ArrayList<List<String>>();  
		
		StringTokenizer sentenceTokenizer = new StringTokenizer(in, ".");
		while(sentenceTokenizer.hasMoreTokens())
		{
			sentence = sentenceTokenizer.nextToken();
			coll.add(Functions.sentence(sentence));
		}
		
		return new CollectionCorpus<String>(coll);
	}

	/** 
	 * Writes a corpus to a text file, one token per line.
	 * 
	 * @param markSentenceEnds 
	 * 		Whether to insert an extra newline character
	 * 		for sentence ends.
	 */
	public static <T> void writeLineCorpus(SequenceCorpus<T> in, File outFile, boolean markSentenceEnds)
		throws IOException
	{
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
		
		SequenceIterator<T> i = in.iterator();
		while(i.hasNext())
		{
			out.write(i.next() + "\n");
			if(i.atSequenceEnd() && markSentenceEnds)
				out.write("\n");
		}
		
		out.close();
	}
	
	/**
	 * Returns a pair of corpora--copies of the input--to be used in
	 * crossvalidation.
	 * 
	 * The entire contents of the in corpus are copied to memory, so this method 
	 * should be used with care, where large corpora are concerned.
	 * 
	 * @param in
	 *            The corpus to split in to two parts
	 * @param folds
	 *            The number of folds. The first corpus will contain
	 *            <code>folds-1</code> folds and the second corpus will contain
	 *            one.
	 * @param testFold
	 *            Which section of the fold will be the second corpus (ie. the
	 *            testfold), with the first fold having index 0.
	 * @returns A Pair of corpora. The first is the larger corpus, generally
	 *          used for training, the second is the smaller corpus, generally
	 *          used for testing.
	 */
	public static <T> Pair<SequenceCorpus<T>, SequenceCorpus<T>> crossValidationPair(
				SequenceCorpus<T> in, int folds, int testFold)
			throws IOException			
	{
		if(testFold >= folds || testFold < 0)
			throw new RuntimeException("testFold index ("+testFold+") out of bounds");
		
		//* length in sentences
		int length = numberOfSentences(in);
	
		if(folds > length)
			throw new RuntimeException("Number of folds cannot exceed number of sentences in corpus.");
		
		int foldSize = length/folds;
		//* start of the small corpus
		int markOne = (int)Math.round((length/(double)folds) * testFold); 
		//* end of the small corpus
		int markTwo = (int)Math.round((length/(double)folds) * (testFold+1));		
	
		ArrayList<ArrayList<T>> largeCollection = new ArrayList<ArrayList<T>>(foldSize * (folds-1));
		ArrayList<ArrayList<T>> smallCollection = new ArrayList<ArrayList<T>>(foldSize);
		ArrayList<ArrayList<T>> collection = largeCollection;		
		
		ArrayList<T> sentence = new ArrayList<T>();
		int index = 0;
		
		SequenceIterator<T> it = in.iterator();
		while(it.hasNext())
		{
			sentence.add(it.next());
			if(it.atSequenceEnd())
			{
				if(index >= markOne)
					collection = smallCollection;
				if(index >= markTwo)
					collection = largeCollection;

				collection.add(sentence);
			
				sentence = new ArrayList<T>();
				index++;
			}
		}
		
		SequenceCorpus<T> large = new CollectionCorpus<T>(largeCollection);
		SequenceCorpus<T> small = new CollectionCorpus<T>(smallCollection);
		
		Pair<SequenceCorpus<T>, SequenceCorpus<T>> pair = 
			new Pair<SequenceCorpus<T>, SequenceCorpus<T>>(large, small);
		
		return pair;
	}

	/**
	 * Determines the number of sentences in a corpus, by running through it.
	 */
	public static int numberOfSentences(SequenceCorpus<?> in)
	throws IOException
	{
		int result = 0;
		
		SequenceIterator<?> it = in.iterator();
		
		while(it.hasNext())
		{
			it.next();
			if(it.atSequenceEnd())
				result++;
		}

		return result;		
	}

	/**
	 * Returns a set of all distinct tokens in the corpus.
	 *  
	 * @param <T> The token type
	 * @param corpus The corpus from which to retrieve the tokens
	 * @return A {@link Set} containing all the tokens in the corpus. The set is 
	 * 	completely distinct from the corpus, but the tokens are not copied.
	 */
	public static <T> Set<T> tokenSet(Corpus<T> corpus) {
		Set<T> set = new TreeSet<T>();
		for(T token : corpus)
			set.add(token);
		
		return set;
	}
}
