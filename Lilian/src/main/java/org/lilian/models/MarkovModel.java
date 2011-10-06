package org.lilian.models;

import org.lilian.corpora.*;
import org.lilian.util.Functions;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Creates a markov model of a corpus.
 * 
 * NOTE: To include markers for the end of a sentence, use a special corpus 
 * wrapper (to be built) that inserts special sentence marker symbols.
 * 
 * NOTE: The calculation of the entropy should be changed once smoothing is
 * 	implemented. 
 */
public class MarkovModel<T> implements Serializable
{
	
	private Vector<T> queue;
	private int order;
	private Node root = new Node(null, null);
	private int modifications = 0;
	
	private double lambda = 0.001; 
	
	// Totals for the number of ngrams encountered
	private List<Integer> totals = new ArrayList<Integer>();
	
	public MarkovModel(int order)
	{
		if(order < 1)
			throw new IllegalArgumentException("Order ("+order+")  cannot be less than 1"); 
		
		this.order = order;
		queue = new Vector<T>(order + 1);
		
		while(totals.size() < order + 2)
			totals.add(0);
	}
	
	public MarkovModel(int order, Corpus<T> corpus)
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
		if(token == null)
			throw new IllegalArgumentException("Token can't be null");
		
		modifications ++;
		
		queue.add(token);
		
		for(int i = 0; i < queue.size(); i++)
			totals.set(i, totals.get(i) + 1);
			
		if(queue.size() < order)
			return;
		
		if(queue.size() > order)
			queue.remove(0);
	
		root.add(queue.iterator());		
	}
	
	/**
	 * Returns the number of times the token sequence that this collection 
	 * represents, has been seen by this markov model. The order of the tokens 
	 * Is determined by the order in which the tokens are returned by the 
	 * collection's iterator.
	 * 
	 * @throws IllegalArgumentException When the size of the collection passed is
	 * larger than the order of the model
	 */
	public double getCount(Collection<T> tokens)
	{
		if(tokens.size() > order)
			throw new IllegalArgumentException("Collection size ("+tokens.size()+") cannot exceed the order of the Markov Model");
		
		return root.getCount(tokens.iterator());
	}

	/**
	 * Returns the probability of the given n-gram
	 * 
	 * @throws IllegalArgumentException When the size of the collection passed is
	 * larger than the order of the model
	 * 
	 * TODO: Currently uses very quick hack with add-lambda smoothing. Replace this
	 *       with optional Smoother object to be passed to the model.
	 */	
	public double nGramProbability(Collection<T> tokens)
	{
		if(tokens.size() > order)
			throw new IllegalArgumentException("Collection size ("+tokens.size()+") cannot exceed the order of the Markov Model");
		
		return (root.getCount(tokens.iterator()) + lambda) / nGramTotal(tokens.size());
	}
	
	/**
	 * Returns the probability of a given sequence according to this model.
	 * 
	 * @return
	 */
	public double probability(Collection<T> tokens)
	{
		double prob = 1.0;
		List<T> ngram = new LinkedList<T>();
		
		for(T token : tokens)
		{
			ngram.add(token);
			if(ngram.size() > order)
				ngram.remove(0);
			
			prob *= nGramProbability(ngram);
		}

		return prob;
	}
	
	/**
	 * Returns the log probability of a given sequence according to this model.
	 * 
	 * @return
	 */
	public double logProbability(Collection<T> tokens)
	{
		double logProb = 0.0;
		List<T> ngram = new LinkedList<T>();
		
		double lp;
		for(T token : tokens)
		{
			ngram.add(token);
			if(ngram.size() > order)
				ngram.remove(0);
			
			lp = Math.log(nGramProbability(ngram));
			logProb += lp;
		}

		return logProb;
	}	
	
	/**
	 * Returns the total number of encountered ngrams of a given length 
	 * @param length
	 * @return
	 */
	public double nGramTotal(int length)
	{
		if(length > order)
			throw new IllegalArgumentException("NGram length ("+length+") cannot exceed the order of the Markov Model");
		
		return totals.get(length -1);
	}
	
	public double entropy()
	{	
		List<T> l = Collections.emptyList();
		return root.conditionalEntropy(l.iterator());
	}
	
	/**
	 * Returns the conditional entropy H(t|given) of a token, given a 
	 * sequence of tokens. 
	 *
	 */
	public double conditionalEntropy(Collection<T> given)
	{
		return root.conditionalEntropy(given.iterator());
	}
	
	/**
	 * Returns the entropy rate as determined by this Markov Model.
	 * 
	 *  Entropy rate is defined as <pre>E_i p(i) E_j p(j|i) E_k p(i|j, k) log_2 p(i|j, k)</pre>
	 *  for the third order and analogously for higher orders. (E_i means 'sum over all possible i').
	 */
	public double entropyRate(int order)
	{
		if(order < 1 || order > this.order)
			throw new IllegalArgumentException("Order ("+order+") can't be smaller than 0 or larger than the order of this model ("+this.order+")");

		return - entropyRateInner(root, order - 1);
	}
	
	/**
	 * Recursively moves through the tree to claculate the entropy rate 
	 */
	private double entropyRateInner(Node n, int depth)
	{
		double result = 0.0;
		
		Iterator<Node> it = n.getChildMap().values().iterator();
		Node child; double term;
		
		while(it.hasNext())
		{
			child = it.next();
			
			if(depth == 0)
				term = (child.getCount() / n.getCount()) * Functions.log2(child.getCount() / n.getCount());
			else
				term = (child.getCount() / n.getCount()) * entropyRateInner(child, depth - 1);

			result += term;
		}
		
		return result;
	}
	
	public void writeResults(File directory, String baseName)
			throws IOException
	{
	}
	
	/**
	 * Returns an iterator over all n grams 
	 */
	public Iterator<Collection<T>> iterator()
	{
		return iterator(order);
	}
	
	/**
	 * Returns an iterator over all encountered n-grams
	 */
	public Iterator<Collection<T>> iterator(int n)
	{
		return new MMIterator(n);		
	}
	
	public String toString()
	{
		return root.toString();
	}	
	
	private class Node implements Serializable
	{
		private double count = 0.0;
		private int depth;
		private T token;
		private Node parent = null;
		private Map<T, Node> children = new LinkedHashMap<T, Node>();		
		
		public Node(T token, Node parent)
		{
			if(parent != null)
				this.depth = parent.depth + 1;
			else 
				this.depth = 0;
			this.token = token;
			this.parent = parent;
		}
		
		/**
		 * Adds all the tokens from index onwards, to children of 
		 * this node. 
		 * 
		 * If tokens contains [a, b, c], a is added as a child of this 
		 * node (or incremented if it already exists) and b to a child 
		 * of that node and so on. 
		 */
		public void add(Iterator<T> iterator)
		{
			count++;
			if(!iterator.hasNext())
				return;
				
			Node child;
			T nextToken = iterator.next();
			
			if(children.containsKey(nextToken))
				child = children.get(nextToken);
			else
			{
				child = new Node(nextToken, this);
				children.put(nextToken, child);
			}

			child.add(iterator);
		}
		
		/**
		 * Returns the frequency of the ngram that this node represents
		 * 
		 * (Ie. the number of times the sequence of tokens from the root 
		 * to this node has been seen so far) 
		 * 
		 */
		public double getCount()
		{
			return count;
		}
		
		public Node getParent()
		{
			return parent;
		}
		
		/**
		 * This method recursively finds the frequency of the sequence 
		 * represented by the remainder of this iterator from this node, or
		 *  one of its children.
		 */		
		public double getCount(Iterator<T> iterator)
		{
			// If the iterator runs out, this node's count is the frequency of
			// the sequence, according to the model  
			if(! iterator.hasNext())
				return count;
			
			// if the node has no children (but the itrator hasn't run out)
			// the iterator's sequence hasn't been seen (so the count is 0)
			if(! hasChildren())
				return 0.0;

			T token = iterator.next();
			
			// if the node has no child for the next token, the sequence
			// hasn't been seen
			if(! children.containsKey(token))
				return 0.0;
				
			// recurse to the next token and the next node
			return children.get(token).getCount(iterator);
		}
		
		public double conditionalEntropy(Iterator<T> iterator)
		{
			// If the iterator runs out, this node's count is the frequency of
			// the sequence, according to the model  
			if(iterator.hasNext())
			{
				T nextToken = iterator.next();
				if(!hasChildren())
					return 0.0;
				else
					if(! children.containsKey(nextToken))
						return 0.0;
					else
					{
						// System.out.println(token + "_ " + children.get(token));
						return children.get(nextToken).conditionalEntropy(iterator);
					}
			}
			
			double sum = 0.0;
			double p;
			
			
			for(Map.Entry<T, Node> node: children.entrySet())
			{
				p = node.getValue().count/this.count;
				sum += p * Functions.log2(p);
				// System.out.println(node.getKey() + "_" + p + " " + sum);
			}
			
			if(sum == 0.0)
				sum = -0.0;
			
			return - sum;			
		}		
		
		public boolean hasChildren()
		{
			return (children.size() != 0);
		}
		
		public Map<T, Node> getChildMap()
		{
			return children;
		}
		
		/** 
		 * Retrieves the child of this node that sits at the end of the
		 * path represented by the iterator
		 */
		public Node getChild(Iterator<T> it)
		{
			if(! it.hasNext())
				return this;
				
			T token = it.next();
			if(!children.containsKey(token))
				return null;
			
			Node n = children.get(token);
			return n.getChild(it);			
		}
		
		public int getDepth()
		{
			return depth;
		}
		
		public T getToken()
		{
			return token;
		}
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			
			for(int i = 0; i < depth; i++) sb.append("-");
			sb.append(token + "\n");
			
			for(Map.Entry<T, Node> me : children.entrySet())
				sb.append(me.getValue());
						
			return sb.toString();
		}
	}
	
	/**
	 * This is the iterator that MarkovModel.iterator() returns. It moves 
	 * through the tree and returns all possible ngrams of a given order 
	 */
	private class MMIterator implements Iterator<Collection<T>>
	{
		
		private int depth;
		// used to check for concurrent modification 
		private int baseModifications = modifications;
		
		// node stack the node at index 0 is the one we're currently iterating over
		private Vector<Node> nodeStack = new Vector<Node>();
		// iterator stack. the iterator at index 0 iterates over the children of
		// nodeStack[0]
		private Vector<Iterator<Node>> itStack = new Vector<Iterator<Node>>();
		// tokenStack this will, at any time represent the path from the root
		// of the tree to the current symbol (and thus, the current ngram)
		private Vector<T> tokenStack = new Vector<T>();
				
		public boolean hasNext = true;
		
		public MMIterator(int depth)
		{
			if(depth < 1 || depth > order)
				throw new IllegalArgumentException("Cannot create an iterator for depth " + depth + ".");

			this.depth = depth;
			
			nodeStack.add(root);
			tokenStack.add(root.getToken());
			tokenStack.add(null);
			itStack.add(root.getChildMap().values().iterator());
		
			iterate();
		}
		
		public boolean hasNext()
		{
			checkMod();
			return hasNext;
		}
		
		public Collection<T> next()
		{
			checkMod();
			if(!hasNext)
				throw new NoSuchElementException();
				
			Vector<T> result = new Vector<T>(depth+1);
			result.addAll(tokenStack.subList(1,tokenStack.size()));
			
			iterate();
			return result;	
		}
		
		public void remove(){ throw new UnsupportedOperationException(); }
		
		private void iterate()		
		{
			tokenStack.remove(tokenStack.size()-1);
			
			// This loop takes care of walking depth-first along the graph.
			// It exits when all nodes are closed, or we're at a new leaf node
			while(true)
			{
				// stop conditions
				if(nodeStack.size() >= depth && itStack.get(0).hasNext())	break;
				if(nodeStack.size() <= 0) 									break;
				
				if(itStack.get(0).hasNext())
				{
					// move deeper
					
					Node n = itStack.get(0).next();
					
					nodeStack.insertElementAt(n, 0);
					itStack.insertElementAt(n.getChildMap().values().iterator(), 0);
					tokenStack.add(n.getToken());
					
				} else
				{
					// close node
		
					nodeStack.remove(0);
					itStack.remove(0);
					tokenStack.remove(tokenStack.size() - 1);
				}
			}
			
			if(nodeStack.size() <= 0)
				hasNext = false;
			else
			{
				Node n = itStack.get(0).next();
				tokenStack.add(n.getToken());
			}
		}
		
		private void checkMod()
		{
			if(baseModifications != modifications)
				throw new ConcurrentModificationException();
		}
	}
}

