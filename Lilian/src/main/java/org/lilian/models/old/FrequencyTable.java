package org.lilian.models.old;

import java.util.*;
import java.io.*;

import org.lilian.*;
import org.lilian.corpora.*;
import org.lilian.util.*;


/**
 * This class provides a table of Tokens and their frequencies, based
 * on a corpus.
 *
 * The tokens used should have decent hashCode(), equals() and compareTo()
 * implementations.
 *
 * Not to be confused with FrequencyMap, which is only used to interpolate
 * frequencies in smoothing.
 *
 * @author Peter Bloem
 */
public class FrequencyTable<T>
{
	// the number of words in the corpus (non distinct).
	protected double tokens = 0.0;
	// the number of distinct tokens in the corpus (size of the hashmap)
	protected int distinctTokens = 0;

	//threshold used for smoothing
	private int k;
	// the frequency for tokens not in the table (0.0 if unsmoothed)
	private MDouble zeroFrequency = new MDouble(0.0);
	private boolean isSorted = false;

	// used as a buffer to read ahead in the corpus
	private Vector<T> tokenQueue;
	/** Holds the ngrams and their frequencies */
	public Map<T, MDouble> table = new LinkedHashMap<T, MDouble>();
	/** Holds the sorted ngrams and frequencies */
	private List<Map.Entry<T, MDouble>> sorted;

	/**
	 * Constructs a frequency table with a default smoothing threshold of 7.
	 */
	public FrequencyTable()
	{
		this(7);
	}

	/**
	 * Constructs a frequency table.
	 *
	 * @param smoothingThreshold Frequencies below this value are smoothed
	 *        when smooth() is called.
	 */
	public FrequencyTable(int smoothingThreshold)
	{
		this.k = smoothingThreshold;
	}

	/**
	 * Returns the frequency for some token
	 *
	 * @param token The token for which to get the frequency.
	 * @return The tokens frequency according to this table
	 */
	public double getFrequency(T token)
	{
		MDouble frequency = table.get(token);

		if(frequency== null)
			return zeroFrequency.getValue();

		return frequency.getValue();
	}
	
	
	public double probability(T token)
	{
		return getFrequency(token)/getNumberOfTokens();
	}

	/**
	 * Returns the number of non-distinct words encountered
	 * @return The number of tokens non-distinct tokens encountered (this should
	 *         equal the total of all frequencies of all tokens)
	 */
	public double getNumberOfTokens(){
		return tokens;
	}
	
	/**
	 * Returns the number of distinct tokens encountered
	 * @return The number of distinct tokens encountered
	 */
	public int size(){
		return distinctTokens;
	}	

	/**
	 * Writes out the entire table to a csv file.
	 * @param filename the base filename to use (the method tags on an extension)
	 */
	public void writeResults(File directory, String base) throws IOException
	{
		// Iterates backwards over the sorted table, (it's sorted in
		// ascending order).
		sort();

		BufferedWriter out = new BufferedWriter(new FileWriter(new File(directory, base + ".frequencies.csv")));

		ListIterator<Map.Entry<T, MDouble>> it = sorted.listIterator(sorted.size());

		out.write("total number of tokens in corpus: " + tokens + "\n");
		out.write("number of distinct tokens: " + table.size() + "\n");
		out.write("\n");

		Map.Entry<T, MDouble> entry;
		while(it.hasPrevious()){
			entry = it.previous();
			out.write(entry.getKey() + ", " + entry.getValue() + "\n");
		}

		out.flush();
		out.close();
	}

	/**
	 * Returns a regular iterator over the entries in this frequency table
	 * including their frequencies
	 *
	 * The iterator does not allow modification.
	 */
	public Iterator<Map.Entry<T, MDouble>> iterator()
	{
		return Collections.unmodifiableSet(table.entrySet()).iterator();
	}

	/**
	 * Returns a regular iterator over just the tokens in this frequency table.
	 *
	 * The iterator does not allow modification.
	 */
	public Iterator<T> tokenIterator()
	{
		return Collections.unmodifiableSet(table.keySet()).iterator();
	}
	
	/**
	 * Removes token from this FrequencyTable. It's frequency will become
	 * 0.0 and it will not be returned by the iterators.
	 * @param token
	 */
	public void remove(T token)
	{
		double f = this.getFrequency(token);
		tokens = tokens - f;
		
		table.remove(token);
		isSorted = false;
	}

	/**
	 * Adds a single token to the frequencymap (ie. increments the frequency of
	 * this token by 1).
	 *
	 * @param token The token to be added
	 */
	public void addToken(T token)
	{
		if(table.containsKey(token))
			table.get(token).increment(1.0);
		else
			table.put(token, new MDouble(1.0));
		
		tokens++;
		distinctTokens = table.size();
		isSorted = false;
	}
	
	/**
	 * Manually sets the frequency for some token. If the
	 * token already has a frequency in this map, it will be
	 * overwritten.
	 *
	 * @param token The token to be added
	 * @param frequency The frequency for this token
	 */
	public void addFrequency(T token, double frequency)
	{
		if(table.containsKey(token)){
			// replace some existing value
			MDouble d = table.get(token);
			tokens -= (int) d.getValue();
			d.setValue(frequency);
			tokens += frequency;
		}else{
			// enter new value
			table.put(token, new MDouble(frequency));
			tokens += frequency;
		}

		distinctTokens = table.size();
		isSorted = false;
	}

	/**
	 * Manually increments the frequency for some token. If the
	 * token doesn't exist in the table yet, it's created with an
	 * initial value of zero, and then incremented.
	 *
	 * @param token The token to be added
	 * @param frequency The amount to add
	 */
	public void incrementFrequency(T token, double frequency)
	{
		if(table.containsKey(token))
		{
			MDouble d = table.get(token);
			d.setValue(d.getValue() + frequency);
			tokens += frequency;
		}else{
			table.put(token, new MDouble(frequency));
			tokens += frequency;
		}

		distinctTokens = table.size();
		isSorted = false;
	}
	
	/**
	 * Draws a token, at random from this frequencytable. The distribution of
	 * the tokens is based on their frequencies.
	 * @return
	 */	
	public T randomToken()
	{
		double draw = Global.random.nextDouble() * tokens;
		double total = 0.0;
		
		Iterator<Map.Entry<T, MDouble>> it = table.entrySet().iterator();
		Map.Entry<T, MDouble> entry;
		MDouble freq;
		T token = null;
		while(it.hasNext() && total <= draw){
			entry = it.next();
			freq = entry.getValue();
			token = entry.getKey();
			
			total += freq.getValue();
		}
		
		return token;		
	}

	/**
	 * Reads through a corpus, adding all tokens to this frequencyTable.
	 *
	 * @param corpus. The corpus to use. The corpus is not reset. This method
	 *                will start reading, wherever the cursor is when it's passed.
	 *                It will return with the cursor at the end of the corpus.
	 */
	public void readCorpus(Corpus<T> corpus) throws IOException
	{
		for(T token : corpus)
			addToken(token);

		isSorted = false;
	}

	/**
	 * Smooths the values in this frequency table, so the are no
	 * non-zero values. This function should be called once. Calling
	 * this function multiple times should work, but the results are
	 * not defined behavior (there is no quaranteed 'extra smoothing').
	 *
	 * The suggested use is adding all tokens, and then calling smooth().
	 * If tokens are added after smoothing, those frequencies will not be
	 * smoothed. And the count of total tokens will probably get messed up.
	 *
	 * The algorithm implemented is Good Turing smoothing with a threshold k.
	 * Unknown meta-frequencies are interpolated using the two nearest
	 * known frequencies.
	 *
	 * NB: The algorithm currently acts odd. This is probably due to inefficient
	 * estimation of the number of unseen tokens.
	 */
	public void smooth()
	{
		//initialize frequency table
		sort();

		FrequencyMap frequencyMap = new FrequencyMap();
		Iterator<Map.Entry<T, MDouble>> it = sorted.iterator();
		Map.Entry<T, MDouble> entry;
		MDouble frequency, metaFrequency;

		while(it.hasNext())
		{
			entry = it.next();
			frequency = entry.getValue();
			if(frequency.getValue() > (k + 2))
				break;

			frequencyMap.add(frequency.getValue());
		}

		// calculate the frequency of ngrams with frequency zero (estimated
		// linearly from N1 and N2). This estimate could be better,
		// currently, it rather underestimates the zero-frequency.
		double nzero = frequencyMap.get(0.0);

		// go through all ngrams from those with frequency 1 through
		// those with frequency k and recalculate their frequency
		// frequency 1 is treated as 0

		it = sorted.listIterator(0);

		// these represent the frequency c of the current ngram, c + 1, cp1
		// the metafrequency of c nc, the metafrequency of c + 1 ncp1
		// the metafrequency of 1 n1, k + 1 kp1, the metafrequency of k nk, and the
		// metafrequency of k+1 nkp1
		double 	c,
				cp1 = -1.0,
				nc = -1.0,
				ncp1 = -1.0,
				n1,	kp1, nk, nkp1;

		double lastc = -1.0;
		double smoothedc;

		kp1 = (double)(k + 1);

		n1 = frequencyMap.get(1.0);
		nk = frequencyMap.get((double) k);
		nkp1 = frequencyMap.get((double) (k+1));

		while(it.hasNext())
		{
			entry = it.next();
			frequency = entry.getValue();
			c = frequency.getValue();
			//treat c == 1 as c == 0
			if(c == 1.0)
				c = 0.0;

			//if c has changed, recalculate cp1, nc and ncp1
			if(c != lastc)
			{
				nc = frequencyMap.get(c);
				cp1 = c + 1.0;
				ncp1 = frequencyMap.get(cp1);
			}

			//frequencies larger than k aren't smoothed
			if(c > k)
				break;

			//calculate the smoothed frequency
			smoothedc  = cp1 * (ncp1/nc) - c * (kp1 * nkp1 / n1);
// System.out.println(smoothedc + " " + nc + " " + n1);
			smoothedc /= 1.0 - (kp1 * nkp1 / n1);

			frequency.setValue(smoothedc);

			lastc = c;
		}

		// calculate the frequency for unseen tokens (c = 0)
		double c0;
		c0  = 1.0 * (n1/nzero);
		c0 /= 1.0 - (kp1 * nkp1 /n1);

		zeroFrequency = new MDouble(c0);
	}

	/**
 	 * Copies all the values from HashTable table into ArrayList sorted and
	 * sorts it. If sorted is still current, it return immediately.
	 */
	private void sort()
	{
		// check if the map is still sorted
		if(isSorted)
			return;

		// create sorted with an initial capacity of ngram.size
		sorted = new ArrayList<Map.Entry<T, MDouble>>(table.size());

		//copy all the entries from table into sorted
		Map.Entry<T, MDouble> ent;
		Iterator<Map.Entry<T, MDouble>> it = table.entrySet().iterator();
		while(it.hasNext())
		{
			ent = it.next();
			sorted.add(ent);
		}

		//sort sorted
		Comparator<Map.Entry<T, MDouble>> c = new EntryComparator();
		Collections.sort(sorted, c);
		isSorted = true;
	}

	/**
	 * A private inner class that defines how to compare our Map.Entry objects.
	 *
	 * Using this, we can sort Arraylist sorted (containing Map.Entry
	 * objects) with Collections.sort(). It compares the entries (containing
	 * tokens as the keys and frequencies as the values) by value, instead of
	 * by key (which is what java would normally do).
	 */
	private class EntryComparator
	implements Comparator<Map.Entry<T, MDouble>>
	{

		/* Compare two Map.entry objects based on the MDoubles they have as values
		*/
		public int compare(Map.Entry<T, MDouble> ent1,
		 Map.Entry<T, MDouble> ent2)
		{
			return ent1.getValue().compareTo(ent2.getValue());
		}

		/* This ComparableEntry object is equal to obj if that's an EntryComparator object
		 * as well
		 */
		public boolean equals(Object obj)
		{
			try{
				EntryComparator e = (EntryComparator)obj;
			}
			catch(Exception e)
			{
				return false;
			}
			return true;
		}
	}

}