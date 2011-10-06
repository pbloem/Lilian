package org.lilian.dop;

import org.lilian.corpora.*;
import org.lilian.corpora.wrappers.ToCNF;
import org.lilian.util.*;

import java.util.*;
import java.io.*;

public class FastUDOP<T> extends GoodmanDOP<T> {
	
	/* the number of charts to keep in memory */
	private int cacheSize = 10;
	
	/* The number of chart nodes */
	private int chartNodes = 0;	
	
	/* Used as a constituent symbol in the constructed binary trees */
	protected T tempSymbol;
	/* Used as the root of the constructed binary trees */
	protected T rootSymbol = null;
	
	/* A cache of parse charts used to construct binary trees */
	protected ArrayList<Chart> chartCache = new ArrayList<Chart>(30);
	
	/* A queue to determine which chart to delete first from the cache
	 */
	protected ArrayList<Integer> cacheQueue = new ArrayList<Integer>(cacheSize);
	
	/* Do not allow construction without a specified tempSymbol */
	@SuppressWarnings("unused")
	private FastUDOP(){}
	
	/**
	 * Constructs a new UDOP model with the specified token as a temporary 
	 * symbol and a cache size of 10. 
	 * 
	 * @param tempSymbol
	 */
	public FastUDOP(T tempSymbol, boolean includeLeaves)
	{
		super(includeLeaves);
		this.tempSymbol = tempSymbol;
		
		this.rootSymbol = tempSymbol;
	}	
	
	/**
	 * Constructs a new UDOP model with the specified token as a temporary 
	 * symbol and the specified cache size.
	 * 	 
	 * @param cacheSize The number of parse charts to keep in memory.
	 * @param tempSymbol
	 */
	public FastUDOP(int cacheSize, T tempSymbol, T rootSymbol, boolean includeLeaves)
	{
		super(includeLeaves);
		this.tempSymbol = tempSymbol;
		this.rootSymbol = rootSymbol;		
		this.cacheSize = cacheSize;
	}
	
	/**
	 * Constructs a new UDOP model with the specified token as a temporary 
	 * symbol and the specified cache size.
	 * 	 
	 * @param cacheSize The number of parse charts to keep in memory.
	 * @param tempSymbol
	 * @param rootSymbol 
	 * @param corpus A corpus to be added to the model
	 */
	public FastUDOP(	int cacheSize, 
						T tempSymbol, 
						T rootSymbol, 
						SequenceIterator<T> corpus, 
						boolean includeLeaves)
		throws IOException
	{
		super(includeLeaves);
		
		this.tempSymbol = tempSymbol;
		this.rootSymbol = rootSymbol;
		this.cacheSize = cacheSize;
		
		addTrees(corpus);
	}	
	
	public void addTrees(SequenceIterator<T> in)
	throws IOException
	{	
		List<T> sentence = new ArrayList<T>();
		
		while(in.hasNext())
		{	
			sentence.add(in.next());
			
			if(in.atSequenceEnd())
			{
				addSentence(sentence);
				sentence.clear();				
			}
		}
		

	}
	
	public void addSentence(List<T> sentence)
	{
		int n = sentence.size();
		
		ensureChart(n);
		chartCache.get(n).setSentence(sentence);
		chartCache.get(n).addGoodmanRules();
		clearCache();		
	}
	
	/**
	 * Make sure that a parse chart of a given size exists. 
	 * 
	 * @param size The size of the required parse chart (as the length of the 
	 * 		sentence it will represent)
	 */
	private void ensureChart(int size)
	{
		/* make sure chartCache is big enough */
		while(chartCache.size() <= size)
			chartCache.add(null);
		
		if(chartCache.get(size) != null)
			return;
		
		Chart chart = new Chart(size);
		
		chartCache.set(size, chart);
		
		cacheQueue.add(size);
	}
	
	/**
	 * If the cache has grown too large, clear the charts that are least recent.
	 * 
	 * The GC will have to take care of actually clearing memory.  
	 */
	private void clearCache()
	{
		while(cacheQueue.size() > cacheSize)
			chartCache.set(cacheQueue.remove(0), null);
	}
	
	/**
	 * Adds eight rules to the pcfg and calculates their probabilities based 
	 * on subtree counts.
	 * 
	 * TODO: This method replicates essential code from GoodmanDOP. It also has
	 * ugly and confusing variable names. This should be rewritten into a single 
	 * core class in GoodmanDOP for adding rules to the grammar. 
	 */
	private void addGoodmanRule(T from, T to1, T to2, 
			int fromSubtrees, int to1Subtrees, int to2Subtrees,
			int fromId, int to1Id, int to2Id)
	{
		/* These determine which of the symbols to make unique, they will iterate
		 * through true and false like a binary string of length 3 */
		boolean		uniqueFrom = false,
					uniqueTo1  = false,
					uniqueTo2  = false;

		/* The probability of the rule created*/
		double prob;
		/* The tree symbols of the rule */
		Constituent		fromConst, to1Const, to2Const;		
		
		ToCNF.Token<T>	fromToken = new ToCNF.RegularToken<T>(from),
								to1Token  = new ToCNF.RegularToken<T>(to1),
								to2Token  = new ToCNF.RegularToken<T>(to2);
		
		Constituent			fromC	= new Constituent(fromToken),
							to1C	= new Constituent(to1Token),
							to2C	= new Constituent(to2Token);
		
		UniqueConstituent 	fromUC	= new UniqueConstituent(fromToken, fromId),
							to1UC	= new UniqueConstituent(to1Token, to1Id),
							to2UC	= new UniqueConstituent(to2Token, to2Id);	

		for (int i = 1; i <= 8; i++)
		{
			if (uniqueFrom)	fromConst = fromUC;
			else 			fromConst = fromC;

			if (uniqueTo1)	to1Const = to1UC;
			else 			to1Const = to1C;

			if (uniqueTo2)	to2Const = to2UC;
			else 			to2Const = to2C;

			/* Calculate the probability of this based on the number of subtrees
			 * of the various rules */
			prob = 1.0;

			if (uniqueTo1)	 prob *= to1Subtrees;
			if (uniqueTo2)	 prob *= to2Subtrees;
			if (uniqueFrom)
				prob /= fromSubtrees;
			else
			{
				MDouble freq;
				if (fromMap.containsKey(fromC))
				{
					freq = fromMap.get(fromC);
					freq.increment(fromSubtrees);
				} else
				{
					freq = new MDouble(fromSubtrees);
					fromMap.put(fromC, freq);
				}
			}

			/* Rules with a non-unique 'from' symbol should have their
			 * probability divided by the frequency of that symbol. The grammar
			 * object takes care of that */
			grammar.setRule(fromConst, to1Const, to2Const, prob);

			/* To iterate over all possible combinations of these three,
			 * we flip uniqueTo1 every iteration, uniqueTo2 every second iteration
			 * and uniqueFrom every fourth iteration */
			uniqueTo2 = !uniqueTo2;
			if (i % 2 == 0)
				uniqueTo1 = !uniqueTo1;
			if (i % 4 == 0)
				uniqueFrom = !uniqueFrom;
		}		
	}

	private class Chart
	{
		/* The 3d vector that will contain the parse values */
		private ArrayList<ArrayList<ArrayList<Node>>> array;
		
		private int size;
		private boolean sentenceSet = false; 

		@SuppressWarnings("unused")
		private Chart(){}
		
		/**
		 * Create a new chart of the given size. 
		 * 
		 * @param The length of a sentence for which this chart can generate all
		 * 	binary trees  
		 */
		public Chart(int size)
		{
			this.size = size;
			initializeArray();
			
			fillChart();
		}

		/**
		 * Fills the parse array with empty stuff.
		 *
		 * @param n The size of the sentence
		 */
		private void initializeArray()
		{
			array = new ArrayList<ArrayList<ArrayList<Node>>>(size);
			ArrayList<ArrayList<Node>> subarray;

			for(int i = 0; i < size; i++)
			{
				subarray = new ArrayList<ArrayList<Node>>(size+1);
				for(int j = 0; j < size + 1; j++)
					subarray.add(new ArrayList<Node>());
				
				array.add(subarray);
			}
		}	
		
		/**
		 * At the start of this method, the bottom row of the parse chart is 
		 * filled with the POS symbols. This method fills the rest of the chart.
		 */
		private void fillChart()
		{
			
			/* 
			 * ** Fill the first row with empty ValueNodes (to which the sentence 
			 * words are to be added) 
			 */
			
			ValueNode wordNode;

			for(int i = 0; i < size; i++)
			{
				/* Create an empty parse node for the word at this location */
				wordNode = new ValueNode(null);

				/* add the word nodes directly to the parse chart */
				array.get(i).get(1).add(wordNode);
			}
			
			/*
			 * ** Fill the rest of the chart with node connected to these initial 
			 * nodes or each other
			 */
			
			XNode newNode;

			/* Vectors of nodes to join, and their iterators */
			ArrayList<Node> nodes1, nodes2; 	
			
			Pair<T, T> key;
			
			/* To complete the parse chart, we iterate over three values:
			 */
			
			/* l -- length of the span */
			for(int l = 1; l <= size;l++)
			{
				/* s -- start of span */
				for(int s = 0; s <= (size-l); s++)
				{
					/* k -- partition of span */
					for(int p = 0; p < l; p++)
					{
						/* we now have two points in the array (s, p) and
						 * (s+p, l-p). We need to find all rules that connect
						 * a symbol at (s, p) to a symbol at (s+p, l-p).
						 */

						/* Get all nodes at the first location ... */ 
						nodes1 = array.get(s).get(p);
						/* and from the second */						
						nodes2 = array.get(s+p).get(l-p);

						/* Check all combinations of these nodes */
						for(Node node1 : nodes1)
						{
							for(Node node2 : nodes2)
							{
								if(l == size)
									newNode = new RootNode();
								else
									newNode = new XNode();
								newNode.setChildOne(node1);
								newNode.setChildTwo(node2);								
								
								array.get(s).get(l).add(newNode);
							}
						}
					}
				}
			}
		}
		
		
		
		/**
		 * Set the given sentence at the bottom of this chart. 
		 *  
		 * @param sentence A sentence whose length is equal to the 
		 * 		size-attribute of this chart.
		 */
		public void setSentence(List<T> sentence)
		{
			for(int i = 0; i < size; i++)
				array.get(i).get(1).get(0).setValue(sentence.get(i));
			
			
			sentenceSet = true;
		}
		
		/**
		 * Walks through the parseChart, calling FastUDOP.addGoodmanRule() for
		 * each node.
		 * 
		 * @throws IllegalStateException If no sentence has been set yet.
		 */
		public void addGoodmanRules()
		{
			if(!sentenceSet) throw new IllegalStateException("Sentence hasn't been set yet.");
			
			for(ArrayList<ArrayList<Node>> column : array) 
				for(ArrayList<Node> cell : column)
					for(Node node : cell)
						node.addGoodmanRules();
		}

		public int getSize() {
			return size;
		}
		
		/**
		 * Returns the parse chart in string form.
		 */
		public String toString()
		{
			StringBuilder sb = new StringBuilder();

			Iterator<ArrayList<ArrayList<Node>>> it1 = array.iterator();
			ArrayList<ArrayList<Node>> v2;
			Iterator<ArrayList<Node>> it2;
			ArrayList<Node> v3;
			Iterator<Node> it3;

			boolean first1 = true,
			first2,
			first3;

			/* Loop over lines */
			while(it1.hasNext()) 
			{
				if(first1)	first1 = false;
				else		sb.append("\n");

				v2 = it1.next();
				it2 = v2.iterator();

				first2 = true;

				/* Loop over cells */
				while(it2.hasNext())
				{
					if(first2)	first2 = false;
					else		sb.append(',');

					v3 = it2.next();
					if(v3.size() < 1) sb.append("   ");
					it3 = v3.iterator();

					first3 = true;
					
					/* Loop over cell values */
					while(it3.hasNext()) 
					{
						if(first3)	first3 = false;
						else		sb.append(' ');

						sb.append(it3.next());
					}
				}
			}

			return sb.toString();
			//return size + "";
		}
		
		/**
		 * An abstract superclass for the two kinds of node. This empty class 
		 * gives us a generic type for the chart. As both types of node store 
		 * different things this should also minimize the memory used on empty 
		 * fields (I think).   
		 */
		private abstract class Node	
		{
			private int uniqueId = ++chartNodes;
			
			/**
			 * Returns the symbol that this node represents
			 */
			public abstract T getValue();
			
			/**
			 * (Optional operation)
			 * @param value
			 */
			public abstract void setValue(T value);
			
			public abstract void addGoodmanRules();
			
			public abstract int subtrees();
			
			/**
			 * TODO: Generate on the fly from position in parse chart 
			 * (reduces memory use)
			 **/
			public int uniqueId()
			{
				return uniqueId;
			}
			
			public String toString()
			{
				return getValue().toString();
			}			
		}
		
		/**
		 * A Node in the parse chart. Contains a symbol.
		 */
		private class ValueNode extends Node
		{
			private T value;
			
			public ValueNode(T value)
			{
				setValue(value);
			}

			public void setValue(T value) 
			{
				this.value = value;
			}

			public T getValue() 
			{
				return value;
			}
			
			public void addGoodmanRules()
			{ /* no children, no rules */
			}
			
			public int subtrees()
			{
				return 0;
			}
		}

		/** 
		 * A Node in the parse chart. Returns the tempSymbol from the enclosing
		 * FastUDOP object  
		 */
		private class XNode extends Node
		{
			private Node childOne = null;
			private Node childTwo = null;			

			public XNode()
			{
			}
			
			public T getValue()
			{
				return tempSymbol;				
			}
			
			public void setValue(T value)
			{
				throw new UnsupportedOperationException();
			}
			
			public Node getChildOne() {
				return childOne;
			}

			public void setChildOne(Node childOne) {
				this.childOne = childOne;
			}

			public Node getChildTwo() {
				return childTwo;
			}

			public void setChildTwo(Node childTwo) {
				this.childTwo = childTwo;
			}
			
			public void addGoodmanRules()
			{
				addGoodmanRule(
						getValue(), childOne.getValue(), childTwo.getValue(),
						subtrees(), childOne.subtrees(), childTwo.subtrees(),
						uniqueId(), childOne.uniqueId(), childTwo.uniqueId());
			}
			
			// TODO: Figure out if we can calculate this from the position in the 
			// chart. Otherwise, figure out if it's worth the memory expense to 
			// cache this value.
			public int subtrees()
			{
				return (childOne.subtrees() + 1) * (childTwo.subtrees() + 1);				
			}
		}
		
		/** 
		 * A root node in the parse chart. Returns the rootSymbol from
		 * the enclosing object  
		 */
		private class RootNode extends XNode
		{
			public T getValue()
			{
				return rootSymbol;				
			}
			
			public String toString()
			{
				return "R_" + super.toString();				
			}			
		}
	}
}
