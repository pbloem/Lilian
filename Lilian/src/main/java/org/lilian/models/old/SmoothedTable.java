package org.lilian.models.old;

import java.io.*;
import java.util.*;

import org.lilian.corpora.*;
import org.lilian.models.*;
import org.lilian.util.*;

/**
 * This class represents a smoothed version of an NGramTable. It slightly 
 * modifies the frequencies of the original table, to make sure that no 
 * ngram has frequency 0.0. This class can be used as a superclass, by 
 * simply instantiating it as any NGramTable, but it can also be used as a 
 * wrapper, by constructing it with an existing NGramTable. In the latter 
 * case, the backing NGramTable is accessible to the user as well, which 
 * can be usefull for comparing smoothed and unsmoothed frequencies.
 * 
 * If the SmoothedTable is used as a wrapper, it is backed by the original 
 * NGramTable. If that changes, this changes too. If the add() methods of 
 * this class are used, thebacking NGramTable is updated (
 * 
 * The smoothing algorithm used is Good-Turing.
 * 
 * TODO:
 *  - implement additional smoothing algorithms, and allow a choice at the constructor.
 *
 * @param <T>
 */
public class SmoothedTable<T> extends NGramTable<T> {
	
	protected NGramTable<T> master = null;
	public Map<MDouble, MDouble> smoothedValues = null;
	private List<Map.Entry<List<T>, MDouble>> thisSorted;
	
	private boolean wrapper;
	
	private int thisModCount = super.modCount - 1;
	private int sortedModCount = thisModCount - 1;
	private int k = 7;
	
	public SmoothedTable(int order)
	{
		super(order);
		wrapper = false;
		
		this.k = 7;
	}
	
	public SmoothedTable(int order, int k)
	{
		super(order);
		wrapper = false;
		
		this.k = k;
	}
	
	public SmoothedTable(Corpus<T> corpus, int order, int k)
	throws IOException
	{
		super(order, corpus);
		wrapper = false;
		
		this.k = k;		
	}
	
	public SmoothedTable(NGramTable<T> master, int k)
	{
		super(1);
		
		order = master.order;
		wrapper = true;
		
		thisModCount = master.modCount - 1;
		sortedModCount = thisModCount - 1;
		
		this.k = k;
	}
	
	public void add(Corpus<T> corpus)
	{
		if(wrapper) 
			master.add(corpus);
		else 		
			super.add(corpus);
		
		thisModCount--;
	}
	
	public void add(T token)
	{
		if(wrapper) master.add(token);
		else 		super.add(token);
		thisModCount--;		
	}
	
	public double get(List<T> ngram)
	{
		smooth();
		
		double freq;
		if(wrapper) freq = master.get(ngram);
		else		freq = super.get(ngram);
		
		if(smoothedValues.containsKey(new MDouble(freq)))
				return smoothedValues.get(new MDouble(freq)).getValue();
		
		return freq;
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

		ListIterator<Map.Entry<List<T>, MDouble>> it = thisSorted.listIterator(thisSorted.size());
// System.out.println(thisSorted);
// System.out.println(super.table);

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
	 * Checks if the sorted version of the table is out of date, and 
	 * creates a new one if it is. 
	 */
	protected void sort()
	{
		if(sortedModCount == thisModCount)
			return;
		
		// create sorted with an initial capacity of ngram.size
		thisSorted = new ArrayList<Map.Entry<List<T>, MDouble>>();

		//copy all the entries from table into sorted
		Map.Entry<List<T>, MDouble> ent;
		
		Iterator<Map.Entry<List<T>, MDouble>> it;
		if(wrapper)
			it = master.table.entrySet().iterator();
		else
			it = super.table.entrySet().iterator();
			
		while(it.hasNext())
		{
			ent = it.next();
			// System.out.println(this.get(ent.getKey()));
			ent.getValue().setValue(this.get(ent.getKey()));
			thisSorted.add(ent);
		}

		//sort sorted
		Comparator<Map.Entry<List<T>, MDouble>> c = new EntryComparator();
		Collections.sort(thisSorted, c);
		
		sortedModCount = thisModCount;
		
		// System.out.println(sorted);
	}
	
	private void smooth()
	{
		// check if we need to redo the smoothing, return if not. 
		if(smoothedValues != null)
		{
			if(wrapper)
			{
				if(thisModCount == master.modCount)	return;
			}else
			{
				if(thisModCount == super.modCount) return;
			}
		}
		
System.out.println("Smoothing:");			
		
		//sort the master
		if(wrapper) master.sort();
		else 		super.sort();
		
		smoothedValues = new LinkedHashMap<MDouble, MDouble>();

		//initialize frequencymap
		FrequencyMap frequencyMap = new FrequencyMap();
		
		Iterator<Map.Entry<List<T>, MDouble>> it;
		if(wrapper) it = master.sorted.iterator();
		else		it = super.sorted.iterator();
		
		Map.Entry<List<T>, MDouble> entry;
		MDouble frequency, metaFrequency;

		while(it.hasNext())
		{
			entry = it.next();
			frequency = entry.getValue();
			if(frequency.getValue() > (k + 2))
				break;

			frequencyMap.add(frequency.getValue());
		}

System.out.println("FrequencyMap filled.");		
// System.out.println(frequencyMap);
		
		int distinctTokens;
		int total;
		if(wrapper) 
		{
			distinctTokens = master.table.size();
			total = master.total;
		}else
		{
			distinctTokens = super.table.size();
			total = super.total;
		}
			
		// calculate the frequency of ngrams with frequency zero
		double nzero = Math.pow(distinctTokens, (double) order) - distinctTokens;

		frequencyMap.add(0.0, nzero);

		// go through all ngrams from those with frequency 1 through
		// those with frequency k and recalculate their frequency
		// frequency 1 is treated as 0

		it = super.sorted.listIterator(0);

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
			if(c == 1)
				c = 0;

			//frequencies larger than k aren't smoothed
			if(c > k)
				break;
			
			//if c has changed, recalculate smoothedc and add
			// it to the smoothedValues map.
			if(c != lastc)
			{
				nc = frequencyMap.get(c);
				cp1 = c + 1.0;
				ncp1 = frequencyMap.get(cp1);

				//calculate the smoothed frequency
				smoothedc  = cp1 * (ncp1/nc) - c * (kp1 * nkp1 / n1);
				smoothedc /= 1.0 - (kp1 * nkp1 / n1);

				lastc = c;
				
				smoothedValues.put(new MDouble(frequency.getValue()), new MDouble(smoothedc));
			}
		}

		// calculate the frequency for unseen ngrams (c = 0)
		double c0;
		c0  = 1.0 * (n1/nzero);
		c0 /= 1.0 - (kp1 * nkp1 /n1);

		smoothedValues.put(new MDouble(0.0), new MDouble(c0));
		
		if(wrapper)	thisModCount = master.modCount;
		else		thisModCount = super.modCount;
	}
}
