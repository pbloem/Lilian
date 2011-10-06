package org.lilian.models.old;

import java.io.*;
import java.util.*;

import org.lilian.corpora.*;
import org.lilian.util.*;

/**
 * This class stores a table of ngrams and their frequencies.
 * 
 * It moves through a corpus, counting all occurrences of n tokens. Subclasses 
 * are available for smoothing.
 * 
 * This class does not include the option to insert marker tokens at the 
 * beginning and end of sentences. For that purpose a subclass of the type
 * NGramTable<String> is available.
 *  
 * The table can be initialized with a corpus, or a corpus can be added later, but 
 * it's also possible to feed an NGramTable separate words. These are expected to be 
 * part of a sequence. For instance if a NGramTable<String> n of order 3 is created, 
 * and the following code is executed:
 * <pre>
 *   n.add("one");
 *   n.add("two");
 *   n.add("three");
 *   n.add("four");
 * </pre>
 * then the trigram ["one, "two", "three"] and ["two", "three", "four"] are counted.
 * 
 *
 */
public class NGramTable<T> {
	
	// used as a buffer to read ahead in the corpus
	private Vector<T> tokenQueue;
	
	// will hold the ngrams and their frequencies
	protected Map<List<T>, MDouble> table = new LinkedHashMap<List<T>, MDouble>();;
	protected List<Map.Entry<List<T>, MDouble>> sorted;
		
	// the number of tokens per ngram
	protected int order;
	protected int modCount = 0;
	//total number of (nondistinct) tokens encountered.
	protected int total = 0;
	private int sortedModCount = modCount - 1;
	
	public NGramTable(int order)
	{
		this.order = order;

		tokenQueue = new Vector<T>(this.order + 3);
	}

	public NGramTable(int order, Corpus<T> corpus)
	{
		this(order);
		add(corpus);
	}
	
	public void add(Corpus<T> corpus)
	{
		for(T token : corpus)
			add(token);
	}
	
	public void add(T token)
	{
		//System.out.println("Adding " + token);
		tokenQueue.add(token);
		
		if(tokenQueue.size() >= order)
		{
			while(tokenQueue.size() > order)
				tokenQueue.remove(0);
			
			if(table.containsKey(tokenQueue))
			{
				table.get(tokenQueue).increment(1.0);
			}else
			{
				ArrayList<T> ngram = new ArrayList<T>(tokenQueue);
				MDouble one= new MDouble(1.0);
				table.put(ngram, one);
				total++;
			}
		}
		
		//System.out.println(table);

		modCount++;
		total++;
	}
	
	public double get(List<T> ngram)
	{
		if(ngram.size() != order)
			throw new IllegalArgumentException("ngram size ("+ngram.size()+") must match this table's order ("+order+")");
		
		if(table.containsKey(ngram))
			return table.get(ngram).getValue();
		
		return 0.0;	
	}
	
	/**
	 * @return The number of distinct ngrams in the model
	 */
	public double getTotalDistinct()
	{
		return (double)(table.size());
	}
	
	/**
	 * @return 	The number of non-distinct ngrams in the model. Ie. 
	 * 			the number of ngrams encountered in the corpus.
	 */	
	public double getTotalEncountered()
	{
		return (double)(total);
	}
	
	/**
	 * Checks if the sorted verion of the table is out of date, and 
	 * creates a new one if it is. 
	 */
	protected void sort()
	{
		if(sortedModCount == modCount)
			 return;
		
		// create sorted with an initial capacity of ngram.size
		sorted = new ArrayList<Map.Entry<List<T>, MDouble>>();

		//copy all the entries from table into sorted
		Map.Entry<List<T>, MDouble> ent;
		Iterator<Map.Entry<List<T>, MDouble>> it = table.entrySet().iterator();
		while(it.hasNext())
		{
			ent = it.next();
			sorted.add(ent);
		}

		//sort sorted
		Comparator<Map.Entry<List<T>, MDouble>> c = new EntryComparator();
		Collections.sort(sorted, c);
		
		sortedModCount = modCount;
		
		// System.out.println(sorted);
	}
	
	/**
	 * Writes a csv file containing a sorted list  (descending) of the ngrams
	 * by frequency). 
	 */
	public void writeResults(File directory, String base)
	throws IOException 
	{
		// create a sorted version of the table if necessary
		sort();
		
		File file = new File(directory, base + ".csv");
		BufferedWriter out = new BufferedWriter(new FileWriter(file));

		ListIterator<Map.Entry<List<T>, MDouble>> it = sorted.listIterator(sorted.size());

		out.write("total number of tokens in corpus: " + total + "\n");
		out.write("number of distinct tokens: " + table.size() + "\n");
		out.write("\n");

		Map.Entry<List<T>, MDouble> entry;
		List<T> ngram;
		Iterator<T> ngramIt;

		while(it.hasPrevious()){
			entry = it.previous();
			ngram = entry.getKey();
			
			ngramIt = ngram.iterator();
			while(ngramIt.hasNext())
				out.write(ngramIt.next() + ", ");
			out.write(entry.getValue() + "\n");
		}

		out.flush();
		out.close();
	}
	
	/**
	 * A private inner class that defines how to compare our Map.Entry objects.
	 *
	 * Using this, we can create Arraylist sorted (containing Map.Entry
	 * objects) with Collections.sort(). It compares the entries (containing
	 * tokens as the keys and frequencies as the values) by value, instead of
	 * by key (which is what java would normally do).
	 */
	protected class EntryComparator
	implements Comparator<Map.Entry<List<T>, MDouble>>
	{

		/* Compare two Map.entry objects based on the MutableIntegers they have as values
		*/
		public int compare(Map.Entry<List<T>, MDouble> ent1,
		 Map.Entry<List<T>, MDouble> ent2)
		{
			return ent1.getValue().compareTo(ent2.getValue());
		}
	}
}
