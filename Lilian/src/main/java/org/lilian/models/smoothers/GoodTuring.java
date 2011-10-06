package org.lilian.models.smoothers;

import static java.util.Collections.reverseOrder;

import java.io.PrintStream;
import java.util.*;

import org.lilian.*;
import org.lilian.models.*;

/**
 * An implementation of the Good-Turing smoothing algorithm.
 * 
 * Note that the object created is independent of the original frequency model.
 *  
 * Note that the Good-Turing smoothing smmothing algorithm requires an estimate
 * of the number of unseen tokens. In the case of n-grams the most common 
 * estimate is to take the number of encountered unigrams <tt>v</tt>, and using
 * <tt>v^n - v</tt>.   
 *  
 * @param <T>
 */
public class GoodTuring<T> implements FrequencyModel<T> {
	
	// * The unsmoothed frequency model
	private BasicFrequencyModel<T> master;
	// * The frequencies of frequencies
	private IntHistogram<Double> meta = new IntHistogram<Double>();
	
	private int k;
	private double n0;
	
	/**
	 * 
	 * @param master
	 * @param tokens
	 * @param n0 The number of unseen tokens (see above) 
	 */
	public GoodTuring(FrequencyModel<T> master, Collection<T> tokens, double n0)
	{
		this(master, tokens, 5, n0);
	}	
	
	/**
	 * 
	 * @param master
	 * @param tokens
	 * @param k
	 * @param n0 The number of unseen tokens (see above)
	 */
	public GoodTuring(FrequencyModel<T> master, Collection<T> tokens, int k, double n0)
	{
		this.master = new BasicFrequencyModel<T>(master, tokens);
		this.k = k;
		this.n0 = n0;
		
		// * Tally the metafrequencies
		for(T token : this.master.tokens())
			meta.add(this.master.frequency(token));
		
	}

	@Override
	public double distinct() {
		return master.distinct();
	}

	@Override
	public double frequency(T token) {
		double c = master.frequency(token);
		
		double count;
		if (c <= k)
		{
			// * Smooth counts
			double nC = meta(c),
			       nCPlusOne = meta(c + 1.0),
			       nKPlusOne = meta(k+1),
			       nOne = meta(1);
			
			double a = (c + 1.0) * (nCPlusOne/nC),
			       b = (k + 1.0) * (nKPlusOne/nOne);
			       
			count = (a - c * b)/(1.0 - b);
		} else	 
		{
			// * Large counts are unsmoothed
			count = c;
		}
		
		return count;
	}
	
	private double meta(double frequency)
	{
		if(frequency == 0.0)
			return (double)n0;
		
		return meta.frequency(frequency);
	}

	@Override
	public double total() {
		return master.total();
	}

	@Override
	public double probability(T token) {
		return master.probability(token);
	}
	
	public Collection<T> tokens()
	{
		return master.tokens();
	}
	
	/**
	 * Calculates the entropy
	 * 
	 * @return A non-negative finite value representing the entropy of the
	 * probability model.
	 */
	public double entropy()
	{
		return Models.entropy(this, tokens()); 
	}
	
	/**
	 * Prints an extensive multiline summary of the model to an outputstream
	 * 
	 * @param out The printstream to print to. Use {@link System.out} for
	 *            printing to the console.
	 */
	public void print(PrintStream out)
	{
		out.printf("total:    %.0f \n", total());
		out.printf("distinct: %.0f \n", distinct());
		out.printf("entropy:  %.3f \n", entropy()); 
		out.println("tokens: ");		
		
		// * Create a list of key, sorted by probability/frequency 
		List<T> keys = new ArrayList<T>(tokens());
		Collections.sort(keys, reverseOrder(new ProbabilityModel.Comparator<T>(this)));
		
		for(T key : keys)
			out.println("  " + key + ", " + frequency(key));
		
		List<Double> freqs = new ArrayList<Double>(meta.tokens());
		Collections.sort(freqs);
		out.println("frequencies: ");		
		out.println("  0, " + n0);		
		for(Double frequency : freqs)
			out.println("  " + frequency + ", " + meta.frequency(frequency));
	}

	@Override
	public double logProbability(T token) {
		return Math.log(probability(token));
	}	

}
