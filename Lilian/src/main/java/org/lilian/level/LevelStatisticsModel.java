package org.lilian.level;

import java.io.*;
import java.util.*;
import static java.lang.Math.*;

import org.lilian.models.*;
import org.lilian.models.old.FrequencyTable;
import org.lilian.corpora.*;

import JSci.maths.statistics.NormalDistribution;

/**
 * This model implements the statistical techniques described in the paper
 * <i> Level statistics of words: finding keywords in literary texts and 
 * 	   symbolic sequences
 * </i> (P Carpena et al. 2009)
 * 
 * The methods are useful for keyword extraction and extraction of relevant 
 * sequences of symbols.
 * 
 * The basic idea of the method is that words that are not specifically relevant 
 * to the text are distributed according to a simple uniform distribution
 * (a geometric distribution, the discrete variant of the Poisson distribution)
 * whereas keywords show a distinct clustering pattern.
 * 
 * @author peter
 *
 * @param <T>
 */
public class LevelStatisticsModel<T>
{
	public FrequencyTable<T> table = new FrequencyTable<T>();
	protected int total = 0;
	
	protected int minFreq = 2;
	
	// * The sum of nearest neighbour distances for a given token
	private Map<T, Double> sumD = new HashMap<T, Double>();
	//* The sum of squared nearest neighbour distances for a given token	
	private Map<T, Double> sumDSquared = new HashMap<T, Double>();
	//* The number of distance seen for this token
	private Map<T, Double> numDistances = new HashMap<T, Double>();	
	//* The index for the last time this token was added
	//  (This collection is also used to iterate over the tokens with freq > 1);
	private Map<T, Double> lastIndices = new LinkedHashMap<T, Double>();

	public LevelStatisticsModel()
	{
	}	
	
	public LevelStatisticsModel(Corpus<T> corpus)
	{
		add(corpus);
	}
	
	public void add(Corpus<T> corpus)
	{
		for(T token : corpus)
			add(token);
	}
	
	public void add(T token)
	{
		table.addToken(token);
		
		if(isKnown(token))
		{
			double distance = total - lastIndices.get(token);
			
			if(!sumD.containsKey(token))
				sumD.put(token, 0.0);
			if(!sumDSquared.containsKey(token))
				sumDSquared.put(token, 0.0);
			if(!numDistances.containsKey(token))
				numDistances.put(token, 0.0);
			
			sumD.put(token, 
					sumD.get(token) + distance);
			sumDSquared.put(token, 
					sumDSquared.get(token) + distance * distance);
			numDistances.put(token, 
					numDistances.get(token) + 1);
		}
		
		lastIndices.put(token, (double)total);

		total++;
	}
	
	/**
	 * Whether the token has been encountered. Note that if a token has been 
	 * encountered only once, there will be no distance information.
	 * @param token
	 * @return
	 */
	public boolean isKnown(T token)
	{
		return lastIndices.containsKey(token);
	}
	
	/**
	 * @param token
	 * @return The average distance from an occurrence of the word to its 
	 * nearest neighbour. 0.0 if the token is not known.
	 */
	public double avgDistance(T token)
	{
		if(!isKnown(token))
			return 0.0;
		
		return sumD.get(token) / numDistances.get(token);
	}
	
	public double avgSquaredDistance(T token)
	{
		if(!isKnown(token))
			return 0.0;
		
		return sumDSquared.get(token) / numDistances.get(token); 		
	}
	
	public double frequency(T token)
	{
		return table.getFrequency(token);
	}
	
	public double probability(T token)
	{
		return table.probability(token);
	}
	
	/**
	 * Relevance as determined by the sigma-measure described in the paper 
	 * <pre>
	 * 	sigma(t) = sqrt(avg(d^2) - avg(d)^2)/avg(d)
	 * </pre>
	 * 
	 * The average is over all the distances <em>d</em> for the word. 
	 * 
	 * @param token
	 * @return A positive double expressing the relevance of the token (higher 
	 * is more relevant). NaN if the token occurs less than twice.
	 */
	public double relevanceSigma(T token)
	{
		if(! isKnown(token) || table.getFrequency(token) < 2)
			return Double.NaN;
		
		double avg = avgDistance(token);
		double sd = sqrt(avgSquaredDistance(token) - avg * avg);
		
		return sd/avg;
	}

	/**
	 * Relevance as determined by the normalized sigma-measure described in 
	 * the paper 
	 * 
	 * For token t;
	 * <pre>
	 * 	signorm(t) = sigma(t) / sqrt(1 -prob(t))
	 * </pre>
	 * 
	 * Where t is the straightforward relative frequency of the token in the text
	 * 
	 * @param token
	 * @return A positive double expressing the relevance of the token (higher 
	 * is more relevant). NaN if the token occurs less than twice.
	 */
	public double relevanceSigNorm(T token)
	{
		if(! isKnown(token) || table.getFrequency(token) < 2)
			return Double.NaN;
		
		double probability = table.probability(token);
		
		return relevanceSigma(token) / sqrt(1.0 - probability);		
	}

	/**
	 * Relevance as determined by the final C measure described in the paper  
	 * 
	 * We estimate the mean and standard deviation of signorm(t) as follows:
	 * <pre>
	 * 	sn_mn(t) = (2n(t) - 1)/(2n(t) + 1)
	 *  sn_sd(t) = 1/(sqrt(n(t))(1 + 2.8n(t)^-0.865))
	 * </pre>
	 * Where n(t) is the frequency of the token t in the text.
	 * 
	 * Which gives us the relevance measure
	 * <pre>
	 *  C(t) = (signorm(t) - sn_mn(t))/sn_sd(t)
	 * </pre>
	 * 
	 * @param token
	 * @return A positive double expressing the relevance of the token (higher 
	 * is more relevant). NaN if the token occurs less than twice.
	 */
	public double relevance(T token)
	{
		if(! isKnown(token) || table.getFrequency(token) < 2)
			return Double.NaN;
		
		double n = table.getFrequency(token);
		double mean = (2 * n - 1)/(2 * n + 1);
		double stdv = 1/(sqrt(n + 2.8 * pow(n, -0.65)));
	
		return (relevanceSigNorm(token) - mean)/stdv;
	}
	
	/**
	 * @return A list of al the encountered tokens. The list is not connected 
	 * to the model and can be freely modified.
	 */
	public List<T> tokens()
	{
		List<T> tokens = new ArrayList<T>(table.size());
		for(T token : lastIndices.keySet())
			tokens.add(token);

		return tokens;
	}
	
	public List<T> tokens(double minFreq)
	{
		List<T> tokens = new ArrayList<T>(table.size());
		for(T token : lastIndices.keySet())
			if(frequency (token) > minFreq)
				tokens.add(token);
		
		return tokens;
	}	
	
	/**
	 * @return A list of all the encountered tokens whose absolute relevance is 
	 * greater than minFrequency. The list is not connected to the model and can 
	 * be freely modified.
	 */
	public List<T> tokens(double minC, double minFreq)
	{
		List<T> tokens = new ArrayList<T>(table.size());
		for(T token : lastIndices.keySet())
			if(abs(relevance(token)) > minC && frequency (token) > minFreq)
				tokens.add(token);
		
		return tokens;
	}
	
	/**
	 * Finds the c value (relevance) which corresponds to a given level of 
	 * significance.
	 * 
	 * @param pValue
	 */
	public double cValue(T token, double pValue)
	{
		double n = table.getFrequency(token),
		       mean = (2 * n - 1)/(2 * n + 1),
		       stdv = 1/(sqrt(n + 2.8 * pow(n, -0.65)));
		
		NormalDistribution dist = new NormalDistribution(mean, stdv*stdv);
		
		return dist.inverse(1.0 - pValue);		
	}
	
	/**
	 * Sorts tokens by their sigma-relevance
	 * @author peter
	 *
	 * @param <T>
	 */
	public class SigmaComparator implements Comparator<T>
	{
		@Override
		public int compare(T first, T second) {
			return Double.compare(relevanceSigma(first), relevanceSigma(second));
		}
	}
	
	/**
	 * Sorts tokens by their sigma-relevance
	 * @author peter
	 *
	 * @param <T>
	 */
	public class SigNormComparator implements Comparator<T>
	{
		@Override
		public int compare(T first, T second) {
			return Double.compare(relevanceSigNorm(first), relevanceSigNorm(second));
		}
	}
	/**
	 * Sorts tokens by their c-relevance
	 * @author peter
	 *
	 * @param <T>
	 */
	public class RelevanceComparator implements Comparator<T>
	{
		@Override
		public int compare(T first, T second) {
			return Double.compare(relevance(first), relevance(second));
		}
	}
	
	public class FrequencyComparator implements Comparator<T>
	{
		@Override
		public int compare(T first, T second) {
			return Double.compare(frequency(first), frequency(second));
		}
	}
	public String out(T token)
	{
		return out(token, true);
	}
	
	public String out(T token, boolean printToken)
	{
		return 		(printToken ? "\"" + token.toString().replace('"', '_') + "\" , " : "") +
					frequency(token) + ", " + 
					probability(token) + ", " +					
					relevance(token) + ", " +
					relevanceSigNorm(token) + ", " +
					relevanceSigma(token) + "";
	}	
}
