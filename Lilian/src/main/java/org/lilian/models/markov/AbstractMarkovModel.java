package org.lilian.models.markov;

import java.util.*;

import org.lilian.corpora.Corpus;
import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;
import org.lilian.models.*;

/**
 * Provides base implementations for markov models. 
 * 
 * Implementations need only override add(List<T>), frequency(T), 
 * total(int) and distinct(int).
 * 
 * @author peter
 *
 * @param <T>
 */
public abstract class AbstractMarkovModel<T> implements MarkovModel<T> {
	
	private int order;
	private List<List<T>> queues;
	
	private long mods = 0;
	
	public AbstractMarkovModel(int order) {
		this.order = order;
		
		queues = new ArrayList<List<T>>(order);
		for(int i = 0; i < order; i++)
			queues.add(new LinkedList<T>());
	}
	
	/**
	 * Increments the mod counter
	 */
	protected void mod()
	{
		mods++;
	}

	@Override
	public void add(SequenceCorpus<T> corpus) 
	{		
		SequenceIterator<T> it = corpus.iterator();
		
		while(it.hasNext())
		{
			add(it.next());
			if(it.atSequenceEnd())
				cut();
		}	
	}
	
	@Override
	public void add(T token) {
		mod();
		
		// Add the token to all queues
		for(List<T> queue : queues)
			queue.add(token);
		
		// Trim queues where necessary
		ListIterator<List<T>> it = queues.listIterator();
		List<T> queue;
		while(it.hasNext())
		{
			queue = it.next();
			while(queue.size() > it.nextIndex())
				queue.remove(0);
		}
		
		// Pass on the ngrams (queues which have grown to the required length)
		it = queues.listIterator();
		while(it.hasNext())
		{
			queue = it.next();
			if(queue.size() == it.nextIndex())
				add(new ArrayList<T>(queue));
		}
		
		// Implementation notes:
		// * It is necessary to maintain a different queue for each order of 
		//   n-gram. We could get all the n-grams from a single queue, but it 
		//   would be impossible to make the cut method work properly.
		// * It very is necessary to copy the ngrams before passing them to 
		//   add(List<T>), because implementing methods need to use these 
		//   objects as keys and suchlike. 
	}
	
	/**
	 * The encountered nGrams are added one by one to this method, of the 
	 * implementing class to process
	 * @param token
	 */
	protected abstract void add(List<T> token);
	
	@Override
	public abstract double frequency(List<T> nGram);

	@Override
	public abstract double total(int order);
	
	@Override
	public abstract double distinct(int order);

	@Override
	public void cut() {
		mod();

		for(List<T> queue : queues)
			queue.clear();
	}

	@Override
	public long state() {
		return mods;
	}

	@Override
	public FrequencyModel<List<T>> model(int order) {
		return new FModel(order);
	}
	
	private class FModel extends AbstractFrequencyModel<List<T>>
	{
		private int order;

		public FModel(int order) {
			this.order = order;
		}

		@Override
		public double frequency(List<T> token) {
			if(token.size() !=order)
				return 0.0;
			
			return AbstractMarkovModel.this.frequency(token);
		}

		@Override
		public double total() {
			return AbstractMarkovModel.this.total(order);
		}

		@Override
		public double distinct() {
			return AbstractMarkovModel.this.distinct(order);
		}
	}
	
	public int order()
	{
		return order;
	}
	
	
	@Override
	public List<List<T>> sortedTokens(int order) 
	{
		Collection<List<T>> tokensIn = tokens(order);
		List<List<T>> tokens = new ArrayList<List<T>>(tokensIn.size());
		tokens.addAll(tokensIn);
		
		FrequencyModel<List<T>> model = this.model(order);
		Collections.sort(tokens, Collections.reverseOrder(
			new FrequencyModel.Comparator<List<T>>(model)
		));
		
		return tokens;
	}
	

	@Override
	public ProbabilityModel<List<T>> sequenceModel() {
		return sequenceModel(order());
	}	

	@Override
	public ProbabilityModel<List<T>> sequenceModel(int order) {
		return new SModel(order);
	}

	private class SModel implements ProbabilityModel<List<T>>
	{
		private int order;
		
		public SModel(int order) {
			this.order = order;
		}

		@Override
		public double probability(List<T> sequence) 
		{
			double prob = 1.0;
			
			List<T> ngram = new LinkedList<T>();
			
			for(T token : sequence)
			{
				ngram.add(token);
				if(ngram.size() > order)
					ngram.remove(0);
				
				prob *= AbstractMarkovModel.this.model(ngram.size()).probability(ngram);
			}

			return prob;			
		}

		@Override
		public double logProbability(List<T> sequence) {
			double prob = 0.0;
			
			List<T> ngram = new LinkedList<T>();
			
			for(T token : sequence)
			{
				ngram.add(token);
				if(ngram.size() > order)
					ngram.remove(0);
				
				prob += AbstractMarkovModel.this.model(ngram.size()).logProbability(ngram);
			}

			return prob;			
		}
	}
	
	/**
	 * Checks whether input argument is > 0 and <= order.
	 *  
	 * @param input some input that needs to satisfy the constraints of this 
	 * 		model, such as an order of the length of an ngram
	 * @throws IllegalArgumentException if the constraints are not satisfied
	 */
	protected void checkOrder(int input)
	{
		if(!(input > 0))
			throw new IllegalArgumentException("Input (was "+input+") must be larger than 0.");
		
		if(!(input <= order))
			throw new IllegalArgumentException("Input (was "+input+") must be less than or equal to the order of this model ("+order()+")");
	}
	
}
