package org.lilian.corpora.wrappers;


import org.lilian.util.*;
import org.lilian.util.trees.Tree;

import java.io.*;
import java.util.*;

import org.lilian.corpora.*;

public class TreeLength<T> 
	extends	CorpusWrapper<Tree<T>, Tree<T>>
{
	private int bufferSize = 10;
	
	private int minLeaves = -1;
	private int maxLeaves = -1;
	
	/**
	 * Creates a TreeLengthControlWrapper based on a master corpus and two
	 * bounds on the number of leaves
	 * 
	 * @param master
	 *            The corpus to get the trees from.
	 * @param maxLeaves
	 *            The maximum number of leaves. Trees with numLeaves > maxLeaves
	 *            are removed. Set -1 for no upper bound.
	 * @param minLeaves
	 *            The minimum number of leaves. Trees with numLeaves < minLength
	 *            are removed. Set -1 for no lower bound.
	 */	
	public TreeLength(
			Corpus<Tree<T>> master, int minLeaves, int maxLeaves)
	{
		super(master);		
		this.minLeaves = minLeaves;
		this.maxLeaves = maxLeaves;
		
	}
	
	@Override
	public Iterator<Tree<T>> iterator() {
		return new TLCIterator();
	}	

	public class TLCIterator
		extends WrapperIterator
	{
		private ArrayList<Tree<T>> buffer = new ArrayList<Tree<T>>(bufferSize);;
		
		@Override
		public boolean hasNext()
		{
			ensureBuffer();
			return ! buffer.isEmpty();
		}

		@Override
		public Tree<T> next()
		{
			ensureBuffer();
			
			if(buffer.isEmpty())
				throw new NoSuchElementException();
			
			return buffer.remove(0);			
		}

		private void ensureBuffer()
		{
			Tree<T> tree;
			int size;
			while(masterIterator.hasNext() && buffer.size() < bufferSize)
			{
				tree = masterIterator.next();
				size = tree.getLeaves().size(); 
				if(		(minLeaves == -1 || size > minLeaves) 
					&& 	(maxLeaves == -1 || size < maxLeaves))
					buffer.add(tree);
			}
		}
	}

}
