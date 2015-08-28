package org.lilian.util;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.exp;
import static java.lang.Math.floor;
import static java.lang.Math.log;
import static org.apache.commons.math3.util.ArithmeticUtils.binomialCoefficientLog;
import static org.lilian.util.Functions.logFactorial;
import static org.lilian.util.Series.series;

import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.*;
import java.text.*;

import org.apache.commons.math3.util.ArithmeticUtils;
import org.lilian.Global;
import org.lilian.corpora.OVISCorpus;
import org.lilian.data.real.Point;
import org.lilian.util.trees.Tree;
import org.lilian.util.trees.Tree.Node;

/**
 * This class contains several static functions (mainly mathematical)
 * That are used throughout the library.
 * 
 * @author Peter Bloem
  */
public class Functions
{
	public static int factCache = 100;
	public static double[] factorials = new double[factCache + 1];
	public static boolean factsInited = false;
	public static NumberFormat nf = NumberFormat.getNumberInstance();
	
	/**
	 * Approximation of the machine epsilon.
	 */
	public static final double EPS;
	
	static {
		double machEps = 1.0f;
	 
        do
           machEps /= 2.0;
        while ((float) (1.0 + (machEps / 2.0)) != 1.0);
	 
	    EPS = machEps;
	}
	
	/**
	 * Return the natural logarithm of the gamma function for the nonnegative integer x.
	 * 
	 * (see "Numerical recipes in c")
	 * @param x The integer to return ln(gamma(x)) for. x Can't   
	 * @return
	 */
		
	public static double logGamma(double in)
	{
		if(in < 0)
			throw new IllegalArgumentException("Cant derive log gamma for negative integer " + in + ".");
		
		double x, y, tmp, ser;
		
		double[] cof = new double[6];
		cof[0] = 76.18009172947146; 
		cof[1] = -86.50532032941677;			
		cof[2] = 24.01409824083091;
		cof[3] = -1.231739572450155;
		cof[4] = 0.1208650973866179e-2;
		cof[5] = -0.5395239384953e-5;

		x = in;
		y = in;

		tmp = x+5.5;
		tmp -= (x+0.5)* Math.log(tmp);
		ser = 1.000000000190015;
		
		for(int j=0;j<=5;j++) 
			ser += cof[j]/++y;
		
		return -tmp + Math.log(2.5066282746310005 * ser / x);
	}
	
	public static double logFactorial(int n, double base)
	{
		return logFactorial(n) / Math.log(base);
	}
	
	
	/**
	 * Calculates the log of the factorial of an integer n. Uses gamma
	 * functions for n > 100;
	 * @param n
	 * @return
	 */
	public static double logFactorial(int n)
	{
		//initialize the lookup table for values < 101 
		if(!factsInited)
		{
			factorials[0] = 1.0;
			for(int i = 1; i <= factCache; i++)
				factorials[i] = i * factorials[i-1];
						
			factsInited = true;
		}			
		
		if (n <= factCache) 
			return Math.log(factorials[n]);
		else 
			return logGamma(n + 1.0);
	}
	
	/**
	 * Calculates the log of the factorial of an integer n. Uses gamma
	 * functions.
	 * @param n 
	 * @return
	 */
	public static double logFactorial(double n)
	{
		if(n < 0.0) throw new IllegalArgumentException("Parameter n ("+n+")should be positive");
		return logGamma(n + 1.0);
	}
	
	
	/**
	 * Calculates a factorial of an integer n. Uses gamma functions for factorials > 100;
	 * @param n
	 * @return
	 */
	public static double factorial(int n)
	{
		//initialize the lookup table for values < 101 
		if(!factsInited)
		{
			factorials[0] = 1.0;
			for(int i = 1; i <= factCache; i++)
				factorials[i] = i * factorials[i-1];
						
			factsInited = true;
		}			

		if (n <= factCache) 
			return factorials[n];
		else 
			return Math.exp(logGamma(n + 1.0));
	}
	
	/**
	 * Calculates the set overlap between two sets.
	 * 
	 * @param a the first set
	 * @param b the second set
	 * @return The the number of elements present in both sets. 
	 */
	public static int overlap(Collection<?> a, Collection<?> b)
	{
		Collection<?> smallest;
		Collection<?> largest;
		if(a.size() >= b.size())
		{
			largest = a;
			smallest = b;
		}else
		{
			largest = b;
			smallest = a;
		}
		
		int overlap = 0;
		Iterator<?> it = smallest.iterator();
		while(it.hasNext())
		{
			if(largest.contains(it.next()))
				overlap++;			
		}
		
		return overlap;
	}
	
	
	public static String matrixToString(int[][] n)
	{
		
		if(n == null) return "null";
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < n.length; i++)
		{
			if(i > 0)
				sb.append("\n");
			for(int j = 0; j < n[i].length;j++)
			{
				sb.append(n[i][j]).append("\t");
			}			
		}
		
		return sb.toString();  
	}
	
	public static String matrixToString(double[][] n)
	{
		nf.setMinimumFractionDigits(2);
		
		if(n == null) return "null";
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < n.length; i++)
		{
			if(i > 0)
				sb.append("\n");
			for(int j = 0; j < n[i].length;j++)
			{
				sb.append(nf.format(n[i][j])).append("\t");
			}			
		}
		
		return sb.toString();  
	}
	
	/**
	 * Calculates the mean and variance from a collection of numbers.
	 *  
	 * @param in
	 * @param sample If true, the sample variance is computed (dividing by n-1, instead of n)
	 * @returnA pair containing the mean first and the variance second 
	 */
	public static Pair<Double, Double> getMeanAndVariance(Collection<? extends Number> in, boolean sample)
	{
		Iterator<? extends Number> it = in.iterator();
		int n = 0;
		double total = 0.0;
		while(it.hasNext())
		{
			n++;
			total += it.next().doubleValue();
		}
		
		double mean;
		if(n == 0)
			mean = 0.0;				
		else		
			mean = total / (double)n;
 		
		it = in.iterator();
		total = 0.0;
		// compute variance
		while(it.hasNext())
			total += Math.pow((it.next().doubleValue() - mean), 2.0);
		
		double variance;
		if(sample)
			if(n == 1) variance = 0.0;
			else variance = total / (double)(n-1);
		else 		
			if(n == 0) variance = 0.0; 
			else variance = total / (double)(n);
		
		return new Pair<Double, Double>(new Double(mean), new Double(variance));
	}
	
	/**
	 * Tokenizes an input sentences by white space, and returns a vector of the 
	 * tokens. Useful for quick testing.
	 * 
	 * 
	 */
	public static List<String> sentence(String sentence)
	{
		StringTokenizer st = new StringTokenizer(sentence);
		
		Vector<String> result = new Vector<String>();
		
		while(st.hasMoreTokens())
			result.add(st.nextToken());
			
		return result;
	}
	
	/**
	 * Reports the number of nodes in this tree that match nodes in the
	 * gold tree. Matching nodes are nodes that, when expanded to their
	 * leaves represent the same sequence of leaves, and have the same
	 * value.
	 *
	 * If the goldTree doesn't represent the same sequence of leaves, the
	 * behavior is undefined (this is not checked).
	 *
	 * @param goldTree The tree to match this one against.
	 * @param checkLabels Whether labels (values) should match as well
	 * @param includeChildren Whether to include matched by leaf nodes
	 * @param includeRoot Whether to include the root nodes as a match 
	 * @return The number of matching nodes.
	 */
	public static <T> int matchingNodes(	Tree<T> candidate, 
											Tree<T> gold, 
											boolean checkLabels, 
											boolean includeLeaves, 
											boolean includeRoot	)
	{
		int sentenceLength = candidate.getLeaves().size();
		
		Set<Pair<Pair<Integer, Integer>, T>>
			treeSet = new LinkedHashSet<Pair<Pair<Integer, Integer>, T>>(),
			goldSet = new LinkedHashSet<Pair<Pair<Integer, Integer>, T>>();

		fillSet(0, candidate.getRoot(), treeSet, 
				checkLabels, includeLeaves, includeRoot, sentenceLength);

		fillSet(0, gold.getRoot(), goldSet,
				checkLabels, includeLeaves, includeRoot, sentenceLength);
		
		//* intersect the sets
		goldSet.retainAll(treeSet);
		return goldSet.size();
	}
	
	/**
	 * The total number of subsequences fo the sentence defines by a
	 * tree's nodes
	 */
	public static <T> int totalNodes(	Tree<T> candidate, 
										boolean includeLeaves, 
										boolean includeRoot	)
	{
		Set<Pair<Pair<Integer, Integer>, T>>
			set = new LinkedHashSet<Pair<Pair<Integer, Integer>, T>>();
		
		fillSet(0, candidate.getRoot(), set, 
			false, includeLeaves, includeRoot, 
			candidate.getLeaves().size());
		
		return set.size();		
	}

	/**
	 * Check what part of the sentence this node covers (start, length)
	 * and adds that to the provided set. It does the same for its children
	 * recursively
	 *
	 * @return the length of this nodes' span
	 */
	private static <T> int fillSet( int start, Tree<T>.Node node,
									Set<Pair<Pair<Integer, Integer>, T>> set,
									boolean checkLabels, 
									boolean includeLeaves, 
									boolean includeRoot, 
									int sentenceLength)
	{
		int length = 0;

		for (Tree<T>.Node child : node.getChildren())
			length += fillSet(length + start, child, set, 
					checkLabels, includeLeaves, includeRoot, sentenceLength);
		
		if(node.isLeaf()) length = 1;
		
		//* Add the range to the set if this node qualifies   
		if ( (includeLeaves ? true :!node.isLeaf()) &&
			 (includeRoot ? true : !(length == sentenceLength))	)
		{ 

			//* if you dislike generics, look away now
			Pair<Integer, Integer> intPair =
			           	new Pair<Integer, Integer>(
						   new Integer(start),
						   new Integer(length));

			Pair<Pair<Integer, Integer>, T> setPair =
						new Pair<Pair<Integer, Integer>, T>(
							intPair,
							checkLabels ? node.getValue() : null );

			set.add(setPair);
		}

		return length;
	}	

	/**
	 * Removes the children from this tree. If the tree only contains a root 
	 * node, an exception is thrown
	 * 
	 * @param <T>
	 */
	public static <T> void removeLeaves(Tree<T> tree)
	{
		List<Tree<T>.Node> nodeList = new ArrayList<Tree<T>.Node>();
		removeLeavesInner(tree.getRoot(), nodeList);	
		for(Tree<T>.Node node : nodeList)
			node.remove();
	}
	
	private static <T> void removeLeavesInner(Tree<T>.Node node, List<Tree<T>.Node> list)
	{
		if(node.isLeaf())
			list.add(node);
		else
			//* Don't replace this with any kind of Iterator based loop
			//* it's written this way to avoid concurrent modification
			for(int i = 0 ; i < node.getChildren().size(); i++)
				removeLeavesInner(node.getChildren().get(i), list);
	}
	
	private static ThreadLocal<Long> ticTime = new ThreadLocal<Long>();
	
	public static void tic()
	{
		ticTime.set(System.currentTimeMillis());
	}
	
	/** 
	 * Returns the number of seconds since tic was last called. <br/>
	 * <br/>
	 * Thread safe.
	 * 
	 * @return A double representing the number of seconds since the last call 
	 *         to tic(). 
	 */
	public static double toc()
	{
		if(ticTime.get() == null)
			throw new IllegalStateException("Tic has not been called yet");
		return (System.currentTimeMillis() - ticTime.get())/1000.0;
	}

	/**
	 *  2 log
	 */
	public static double log2(double x)
	{
		return Math.log10(x) / Math.log10(2.0);
	}

	public static double logChoose(double sub, double total, double base)
	{
		return logChoose(sub, total) / Math.log(2.0);
	}	
	
	
	/**
	 * The binary logarithm of the choose function (ie. the binomial coefficient)
	 */
	public static double logChoose(double sub, double total)
	{
		if(sub <= Integer.MAX_VALUE && total <= Integer.MAX_VALUE 
				&& sub == (int)sub && total == (int)total)
			return binomialCoefficientLog((int)total, (int)sub);
		
		double n = total, k = sub;
		return logFactorial(n) - logFactorial(k) - logFactorial(n - k); 
	}
	 
	/**
	 * Equals method for objects that takes into account null values
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equals(Object a, Object b)
	{
		if(a == null)
			return b == null;
		
		return a.equals(b);
	}
	
	/**
	 * Simple random choice from a collection
	 * 
	 * @param in
	 * @return
	 */
	public static <T> T choose(Collection<T> in)
	{
		return choose(in, Global.random);
	}
	
	/**
	 * Simple random choice from a collection
	 * 
	 * If the input is a list, processing time is constant, otherwise the 
	 * collection is traversed to the randomly drawn index.
	 * 
	 * @param in
	 * @return A random element from the collection
	 */
	public static <T> T choose(Collection<T> in, Random random)
	{
		int draw = random.nextInt(in.size());
		if(in instanceof List)
			return ((List<T>)in).get(draw);
		
		int c = 0;
		for(T t : in)
			if(c < draw)
				c++;
			else
				return t;
		
		// * Unreachable code to make the compiler happy
		assert(false); 
		return null; 
	}
	
	/**
	 * Returns a lightweight, reversed list, backed by the original.
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> reverse(List<T> list)
	{
		return new ReverseList<T>(list);
	}

	public static class ReverseList<T> extends AbstractList<T>
	{
		private List<T> master;
		
		public ReverseList(List<T> master)
		{
			this.master = master;
		}

		@Override
		public T get(int index)
		{
			return master.get((master.size()-1)-index);
		}

		@Override
		public int size()
		{
			return 0;
		}
	
	}
	
	public static int draw(Collection<Double> probabilities)
	{
		double sum = 0.0;
		for(double p : probabilities)
			sum += p;
		
		return draw(probabilities, sum);
	}
	
	/**
	 * Draws an integer according to specified probabilities.
	 * 
	 * Draws a random integer i such that i's probability of being drawn is
	 * equal to the i'th double returned by the collection's iterator.
	 * 
	 * @param probabilities A collection of doubles, summing to 1.0, whose n'th 
	 * 			element defines the probability of n being drawn (first 
	 * 			element is 0)
	 * @param sum The sum of the values in probabilities. This valeu is not 
	 * 				calculated from the collection for reasons of efficiency.  
	 */
	public static int draw(Collection<Double> probabilities, double sum)
	{
		return draw(probabilities, sum, Global.random);
	}
	
	public static int draw(Collection<Double> probabilities, double sum, Random rand)
	{
		// * select random element
		double draw = rand.nextDouble();
		double total = 0.0;
		int elem = 0;
		for(double probability : probabilities)
		{
			total += probability/sum;
			if(total > draw)
				break;
			
			elem++;
		}
		
		// account for floating point problems
		if(elem >= probabilities.size())
		{
			elem = probabilities.size()-1;
//			Global.logger.info("Index larger than probabilities.size(): draw = "+draw+", total = "+total+", sum = "+sum+". ");
		}

		return elem;
	}
	
	public static <L extends List<Double>> void toCSV(List<L> data, File csvFile)
			throws IOException
	{
		BufferedWriter out = new BufferedWriter(new FileWriter(csvFile));
		
		boolean first;
		for(L point : data)
		{
			first = true;
			for(Double val : point)
			{
				if(first) first = false;
				else out.write(", ");
				
				out.write(val.toString());				
			}
			out.write("\n");
		}
		
		out.close();
	}
	
	public static int mod(int x, int range)
	{
		int r = x % range;
		if(r >= 0)
			return r;
		
		return range + x;
	}
	
	public static Comparator<Number> numberComparator()
	{
	 return new NumberComparator();
	}

	public static class NumberComparator implements Comparator<Number>
	{
		@Override
		public int compare(Number n1, Number n2)
		{
			return Double.compare(n1.doubleValue(), n2.doubleValue());
		}
	
	}
	
	/**
	 * Probabilistically rounds the input. The result of this 
	 * method is at least floor(in), and is ceil(in) with probability
	 * (in - floor(in)) 
	 * 
	 * @param in
	 */
	public static double probRound(double in)
	{
		if(Global.random.nextDouble() < in - floor(in))
			return ceil(in);
		
		return(floor(in));
	}

	/**
	 * Samples k distinct values from the first 'size' natural numbers
	 * 
	 * @param k The number of natural numbers to return
	 * @param size The maximum integer possible + 1
	 * @return A uniform random choice from all sets of size k of distinct 
	 * 	integers below the 'size' parameter.
	 */
	public static List<Integer> sample(int k, int size)
	{
		if(0 > k || k > size)
			throw new IllegalArgumentException("Argument k ("+k+") must be non-negative and larger than size ("+size+")");
		// * The algorithm we use basically simulates having an array with the 
		//   values of 0 to n - 1 at their own indices, and for each i, choosing
		//   a random index above it and swapping the two entries.
		//
		//   Since we expect low k, most entries in this array will stay at 
		//   their original index and we only stores the values that deviate.
		
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
				
		for(int i : series(k))
		{
			// Sample a random integer above or equal to i and below 'size'
			int draw = Global.random.nextInt(size - i) + i;
			
			int drawValue = map.containsKey(draw) ? map.get(draw) : draw;
			int iValue = map.containsKey(i) ? map.get(i) : i; 
			
			// swap the values
			map.put(i, drawValue);
			map.put(draw, iValue);
		}
		
		List<Integer> result = new ArrayList<Integer>(k);
		for(int i : series(k))
			result.add(map.get(i));
		
		return result;
	}

	/**
	 * Uses natural logarithms
	 * @param logA
	 * @param logB
	 * @return
	 */
	public static double logSum(double logA, double logB)
	{
		double logMin, logMax;
		
		if(logA < logB)
		{
			logMin = logA;
			logMax = logB;
		} else {
			logMax = logA;
			logMin = logB;
		}
		
		if(logMin == Double.NEGATIVE_INFINITY && logMax != Double.POSITIVE_INFINITY)
			return logMax;
			
		double right = exp(logMax - logMin);
		double left = exp(logMin);
		
		if(left == 0.0 && right == Double.POSITIVE_INFINITY)
				return Double.NEGATIVE_INFINITY; // TODO: strictly speaking, we could get pos infty as well
						
		
		return logMin * Math.log1p(right);
	}
	
	public static double exp2(double x)
	{
		return Math.exp(x);
	}
	
    public static <T> Set<T> asSet(T... a) {
        Set<T> set = new LinkedHashSet<T>();
        for(T t : a)
        	set.add(t);
        
        return set;
    }
    
    /**
     * Takes a list of double values representing logs likelihoods: eg.
     * {log p_1, log p_2, ...} and returns a vector of normalized regular
     * likelihoods: {p_1/sum p, p_2/sum_p, ...}
     * 
     * Since the likelihoods would underflow when exponentiated, this operation 
     * requires some care to do most of the steps in log space until the values 
     * are large enough that they can be exponentiated 
     * 
     * @param logs
     * @param the base of the logarithm
     * @return
     */
    public static List<Double> normalizeLog(List<Double> logs, double base)
    {
    	double[] in = new double[logs.size()];
    	for(int i : series(logs.size()))
    		in[i] = logs.get(i);
    	
    	double[] res = normalizeLog(in, base);
    	
    	List<Double> out = new ArrayList<Double>(res.length);
    	for(double r : res)
    		out.add(r);
    	return out;
    }    
    
    public static double[] normalizeLog(double[] logs, double base)
    {    	    	
    	// We're implementing this algorithm:
    	// http://stats.stackexchange.com/questions/66616/converting-normalizing-very-small-likelihood-values-to-probability
    	
    	double[] ll = new double[logs.length];
    	for(int i : series(logs.length))
    		ll[i] = logs[i];
    	
    	double maxLog = Double.NEGATIVE_INFINITY;
    	for(double l : ll)
    		maxLog = Math.max(l, maxLog);
    	
    	if(maxLog == Double.NEGATIVE_INFINITY)
    		return uniform(logs.length);
    	
    	for(int i : series(ll.length))
    		ll[i] -= maxLog;
    	    	
    	for(int i : series(ll.length))
    		ll[i] = Math.pow(base, ll[i]);
    	
    	// * No need to check for underflow. Java sets to 0 automatically
    	
    	double sum = 0.0;
    	for(double l : ll)
    		sum += l;
    	
    	for(int i : series(ll.length))
    		ll[i] = ll[i]/sum;;
    	
    	return ll;
    }
    
    private static double[] uniform(int n)
    {
    	double[] res = new double[n];
    	for(int i : series(n))
    		res[i] = 1.0/n;
    	
    	return res;
    }
    
    public static <T extends Comparable<? super T>> Comparator<T> natural()
    {
    	return new NaturalComparator<T>();
    }

    /**
     * Sorts both lists by the corresponding values in V.
     *
     * @param things
     * @param values
     */
    public static <T, V> void sort(List<T> things, List<V> values, final Comparator<V> comp)
    {   
        class Comp implements Comparator<Pair<T, V>>
        {
			@Override
			public int compare(Pair<T, V> a, Pair<T, V> b)
			{
				return comp.compare(a.second(), b.second());
			}
        }
        
        int n = things.size();
        assert(things.size() == values.size());
        
        List<Pair<T, V>> pairs = new ArrayList<Pair<T, V>>(n);
        for(int i : series(n))
        	pairs.add(new Pair<T, V>(things.get(i), values.get(i)));
        
        Collections.sort(pairs, new Comp());
        
        things.clear();
        values.clear();
        
        for(Pair<T, V> pair : pairs)
        {
        	things.add(pair.first());
        	values.add(pair.second());
        }
    }
    
    /**
     * Sorts all lists (including the values) by the corresponding values in 'values'.
     *
     * if the values have equal order, the elements are sorted by the first list 
     * that contains elements comparable to one another. This behavior is 
     * ill-defined due to problems with generics. 
     *
     * @param things
     * @param values
     */
    public static <V> void sort(final List<V> values, final Comparator<V> comp, final List... lists)
    {   
    	int n = values.size();
    	for(int i : series(lists.length))
    		if(lists[i].size() != n)
    			throw new IllegalArgumentException("List "+i+" has size "+lists[i].size() +", but values has size "+ values.size()+". All lists should have the same size.");
    	
    	class Tuple
    	{
    		V value;
    		Object[] things;
    		
    		public Tuple(int i)
    		{
    			value = values.get(i);
    			
    			things = new Object[lists.length];
    			for(int j : series(lists.length))
    				things[j] = lists[j].get(i);
    		}
    		
    		public V value(){return value;}
    		public Object thing(int j){return things[j];}
    	}
    	
        class Comp implements Comparator<Tuple>
        {
			@Override
			public int compare(Tuple a, Tuple b)
			{
				int vcomp = comp.compare(a.value(), b.value());
				
				if(vcomp != 0)
					return vcomp;
				
				for(int j : series(lists.length))
				{
					Object oa = a.thing(j);
					Object ob = b.thing(j);
					
					if(oa instanceof Comparable<?>)
					{
						Comparable ca = (Comparable)oa;
						
						try {
							int comp = ca.compareTo(ob);
							if(comp != 0)
								return comp;
						} catch (Exception e)
						{
							// no problem, move on 
						}
					}	
				}
				
				return 0;
			}
        }
                
        List<Tuple> tuples = new ArrayList<Tuple>(values.size());
        for(int i : series(values.size()))
        	tuples.add(new Tuple(i));
        
        Collections.sort(tuples, new Comp());
        
        values.clear();
        for(int j : series(lists.length))
        	lists[j].clear();
        
        for(Tuple tuple : tuples)
        {
        	values.add(tuple.value());

            for(int j : series(lists.length))
            	(lists[j]).add(tuple.thing(j));
        }
    }
    
    
    
    public static class NaturalComparator<T extends Comparable<? super T>> 
    	implements Comparator<T> 
    {
	
	    @Override
	    public int compare(T first, T second) 
	    {
	        return first.compareTo(second);
	    }
    }
    
    /**
     * Test if n is a perfect square (ie. its square root is an integer).
     * 
     * Code from http://stackoverflow.com/questions/295579/fastest-way-to-determine-if-an-integers-square-root-is-an-integer
     * 
     * @param n
     * @return
     */
    public static boolean isSquare(long n)
    {
      // Quickfail
      if( n < 0 || ((n&2) != 0) || ((n & 7) == 5) || ((n & 11) == 8) )
        return false;
      if( n == 0 )
        return true;

      // Check mod 255 = 3 * 5 * 17, for fun
      long y = n;
      y = (y & 0xffffffffL) + (y >> 32);
      y = (y & 0xffffL) + (y >> 16);
      y = (y & 0xffL) + ((y >> 8) & 0xffL) + (y >> 16);
      if( bad255[(int)y] )
          return false;

      // Divide out powers of 4 using binary search
      if((n & 0xffffffffL) == 0)
          n >>= 32;
      if((n & 0xffffL) == 0)
          n >>= 16;
      if((n & 0xffL) == 0)
          n >>= 8;
      if((n & 0xfL) == 0)
          n >>= 4;
      if((n & 0x3L) == 0)
          n >>= 2;

      if((n & 0x7L) != 1)
          return false;

      // Compute sqrt using something like Hensel's lemma
      long r, t, z;
      r = start[(int)((n >> 3) & 0x3ffL)];
      do {
        z = n - r * r;
        if( z == 0 )
        	return true;
        if( z < 0 )
        	return false;
        t = z & (-z);
        r += (z & t) >> 1;
        if( r > (t  >> 1) )
        r = t - r;
      } while( t <= (1L << 33) );

      return false;
    }

    private static boolean[] bad255 =
    {
       false,false,true ,true ,false,true ,true ,true ,true ,false,true ,true ,true ,
       true ,true ,false,false,true ,true ,false,true ,false,true ,true ,true ,false,
       true ,true ,true ,true ,false,true ,true ,true ,false,true ,false,true ,true ,
       true ,true ,true ,true ,true ,true ,true ,true ,true ,true ,false,true ,false,
       true ,true ,true ,false,true ,true ,true ,true ,false,true ,true ,true ,false,
       true ,false,true ,true ,false,false,true ,true ,true ,true ,true ,false,true ,
       true ,true ,true ,false,true ,true ,false,false,true ,true ,true ,true ,true ,
       true ,true ,true ,false,true ,true ,true ,true ,true ,false,true ,true ,true ,
       true ,true ,false,true ,true ,true ,true ,false,true ,true ,true ,false,true ,
       true ,true ,true ,false,false,true ,true ,true ,true ,true ,true ,true ,true ,
       true ,true ,true ,true ,true ,false,false,true ,true ,true ,true ,true ,true ,
       true ,false,false,true ,true ,true ,true ,true ,false,true ,true ,false,true ,
       true ,true ,true ,true ,true ,true ,true ,true ,true ,true ,false,true ,true ,
       false,true ,false,true ,true ,false,true ,true ,true ,true ,true ,true ,true ,
       true ,true ,true ,true ,false,true ,true ,false,true ,true ,true ,true ,true ,
       false,false,true ,true ,true ,true ,true ,true ,true ,false,false,true ,true ,
       true ,true ,true ,true ,true ,true ,true ,true ,true ,true ,true ,false,false,
       true ,true ,true ,true ,false,true ,true ,true ,false,true ,true ,true ,true ,
       false,true ,true ,true ,true ,true ,false,true ,true ,true ,true ,true ,false,
       true ,true ,true ,true ,true ,true ,true ,true ,false,false,true ,true ,false,
       true ,true ,true ,true ,false,true ,true ,true ,true ,true ,false,false,true ,
       true ,false,true ,false,true ,true ,true ,false,true ,true ,true ,true ,false,
       true ,true ,true ,false,true ,false,true ,true ,true ,true ,true ,true ,true ,
       true ,true ,true ,true ,true ,false,true ,false,true ,true ,true ,false,true ,
       true ,true ,true ,false,true ,true ,true ,false,true ,false,true ,true ,false,
       false,true ,true ,true ,true ,true ,false,true ,true ,true ,true ,false,true ,
       true ,false,false,true ,true ,true ,true ,true ,true ,true ,true ,false,true ,
       true ,true ,true ,true ,false,true ,true ,true ,true ,true ,false,true ,true ,
       true ,true ,false,true ,true ,true ,false,true ,true ,true ,true ,false,false,
       true ,true ,true ,true ,true ,true ,true ,true ,true ,true ,true ,true ,true ,
       false,false,true ,true ,true ,true ,true ,true ,true ,false,false,true ,true ,
       true ,true ,true ,false,true ,true ,false,true ,true ,true ,true ,true ,true ,
       true ,true ,true ,true ,true ,false,true ,true ,false,true ,false,true ,true ,
       false,true ,true ,true ,true ,true ,true ,true ,true ,true ,true ,true ,false,
       true ,true ,false,true ,true ,true ,true ,true ,false,false,true ,true ,true ,
       true ,true ,true ,true ,false,false,true ,true ,true ,true ,true ,true ,true ,
       true ,true ,true ,true ,true ,true ,false,false,true ,true ,true ,true ,false,
       true ,true ,true ,false,true ,true ,true ,true ,false,true ,true ,true ,true ,
       true ,false,true ,true ,true ,true ,true ,false,true ,true ,true ,true ,true ,
       true ,true ,true ,false,false
    };

    private static int[] start =
    {
      1,3,1769,5,1937,1741,7,1451,479,157,9,91,945,659,1817,11,
      1983,707,1321,1211,1071,13,1479,405,415,1501,1609,741,15,339,1703,203,
      129,1411,873,1669,17,1715,1145,1835,351,1251,887,1573,975,19,1127,395,
      1855,1981,425,453,1105,653,327,21,287,93,713,1691,1935,301,551,587,
      257,1277,23,763,1903,1075,1799,1877,223,1437,1783,859,1201,621,25,779,
      1727,573,471,1979,815,1293,825,363,159,1315,183,27,241,941,601,971,
      385,131,919,901,273,435,647,1493,95,29,1417,805,719,1261,1177,1163,
      1599,835,1367,315,1361,1933,1977,747,31,1373,1079,1637,1679,1581,1753,1355,
      513,1539,1815,1531,1647,205,505,1109,33,1379,521,1627,1457,1901,1767,1547,
      1471,1853,1833,1349,559,1523,967,1131,97,35,1975,795,497,1875,1191,1739,
      641,1149,1385,133,529,845,1657,725,161,1309,375,37,463,1555,615,1931,
      1343,445,937,1083,1617,883,185,1515,225,1443,1225,869,1423,1235,39,1973,
      769,259,489,1797,1391,1485,1287,341,289,99,1271,1701,1713,915,537,1781,
      1215,963,41,581,303,243,1337,1899,353,1245,329,1563,753,595,1113,1589,
      897,1667,407,635,785,1971,135,43,417,1507,1929,731,207,275,1689,1397,
      1087,1725,855,1851,1873,397,1607,1813,481,163,567,101,1167,45,1831,1205,
      1025,1021,1303,1029,1135,1331,1017,427,545,1181,1033,933,1969,365,1255,1013,
      959,317,1751,187,47,1037,455,1429,609,1571,1463,1765,1009,685,679,821,
      1153,387,1897,1403,1041,691,1927,811,673,227,137,1499,49,1005,103,629,
      831,1091,1449,1477,1967,1677,697,1045,737,1117,1737,667,911,1325,473,437,
      1281,1795,1001,261,879,51,775,1195,801,1635,759,165,1871,1645,1049,245,
      703,1597,553,955,209,1779,1849,661,865,291,841,997,1265,1965,1625,53,
      1409,893,105,1925,1297,589,377,1579,929,1053,1655,1829,305,1811,1895,139,
      575,189,343,709,1711,1139,1095,277,993,1699,55,1435,655,1491,1319,331,
      1537,515,791,507,623,1229,1529,1963,1057,355,1545,603,1615,1171,743,523,
      447,1219,1239,1723,465,499,57,107,1121,989,951,229,1521,851,167,715,
      1665,1923,1687,1157,1553,1869,1415,1749,1185,1763,649,1061,561,531,409,907,
      319,1469,1961,59,1455,141,1209,491,1249,419,1847,1893,399,211,985,1099,
      1793,765,1513,1275,367,1587,263,1365,1313,925,247,1371,1359,109,1561,1291,
      191,61,1065,1605,721,781,1735,875,1377,1827,1353,539,1777,429,1959,1483,
      1921,643,617,389,1809,947,889,981,1441,483,1143,293,817,749,1383,1675,
      63,1347,169,827,1199,1421,583,1259,1505,861,457,1125,143,1069,807,1867,
      2047,2045,279,2043,111,307,2041,597,1569,1891,2039,1957,1103,1389,231,2037,
      65,1341,727,837,977,2035,569,1643,1633,547,439,1307,2033,1709,345,1845,
      1919,637,1175,379,2031,333,903,213,1697,797,1161,475,1073,2029,921,1653,
      193,67,1623,1595,943,1395,1721,2027,1761,1955,1335,357,113,1747,1497,1461,
      1791,771,2025,1285,145,973,249,171,1825,611,265,1189,847,1427,2023,1269,
      321,1475,1577,69,1233,755,1223,1685,1889,733,1865,2021,1807,1107,1447,1077,
      1663,1917,1129,1147,1775,1613,1401,555,1953,2019,631,1243,1329,787,871,885,
      449,1213,681,1733,687,115,71,1301,2017,675,969,411,369,467,295,693,
      1535,509,233,517,401,1843,1543,939,2015,669,1527,421,591,147,281,501,
      577,195,215,699,1489,525,1081,917,1951,2013,73,1253,1551,173,857,309,
      1407,899,663,1915,1519,1203,391,1323,1887,739,1673,2011,1585,493,1433,117,
      705,1603,1111,965,431,1165,1863,533,1823,605,823,1179,625,813,2009,75,
      1279,1789,1559,251,657,563,761,1707,1759,1949,777,347,335,1133,1511,267,
      833,1085,2007,1467,1745,1805,711,149,1695,803,1719,485,1295,1453,935,459,
      1151,381,1641,1413,1263,77,1913,2005,1631,541,119,1317,1841,1773,359,651,
      961,323,1193,197,175,1651,441,235,1567,1885,1481,1947,881,2003,217,843,
      1023,1027,745,1019,913,717,1031,1621,1503,867,1015,1115,79,1683,793,1035,
      1089,1731,297,1861,2001,1011,1593,619,1439,477,585,283,1039,1363,1369,1227,
      895,1661,151,645,1007,1357,121,1237,1375,1821,1911,549,1999,1043,1945,1419,
      1217,957,599,571,81,371,1351,1003,1311,931,311,1381,1137,723,1575,1611,
      767,253,1047,1787,1169,1997,1273,853,1247,413,1289,1883,177,403,999,1803,
      1345,451,1495,1093,1839,269,199,1387,1183,1757,1207,1051,783,83,423,1995,
      639,1155,1943,123,751,1459,1671,469,1119,995,393,219,1743,237,153,1909,
      1473,1859,1705,1339,337,909,953,1771,1055,349,1993,613,1393,557,729,1717,
      511,1533,1257,1541,1425,819,519,85,991,1693,503,1445,433,877,1305,1525,
      1601,829,809,325,1583,1549,1991,1941,927,1059,1097,1819,527,1197,1881,1333,
      383,125,361,891,495,179,633,299,863,285,1399,987,1487,1517,1639,1141,
      1729,579,87,1989,593,1907,839,1557,799,1629,201,155,1649,1837,1063,949,
      255,1283,535,773,1681,461,1785,683,735,1123,1801,677,689,1939,487,757,
      1857,1987,983,443,1327,1267,313,1173,671,221,695,1509,271,1619,89,565,
      127,1405,1431,1659,239,1101,1159,1067,607,1565,905,1755,1231,1299,665,373,
      1985,701,1879,1221,849,627,1465,789,543,1187,1591,923,1905,979,1241,181
    };
}
