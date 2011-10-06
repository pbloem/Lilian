package org.lilian.dop;

import org.lilian.util.*;
import org.lilian.util.trees.Tree;
import org.lilian.corpora.*;
import org.lilian.corpora.wrappers.AllBinaryTreesCorpusWrapper;
import org.lilian.grammars.*;

import java.io.*;
import java.util.*;

/**
 * A straight-forward implementation of Rens Bod's UDOP.
 * 
 */
public class SimpleUDOP<T> extends GoodmanDOP<T>
{
	protected T tempSymbol = null;
	protected T rootSymbol = null;
	
	/* Do not allow construction without a specified tempSymbol */
	@SuppressWarnings("unused")
	private SimpleUDOP(){}
	
	/**
	 * Constructs a new UDOP model with the specified token as a temporary 
	 * symbol and a cache size of 10. 
	 * 
	 * @param tempSymbol
	 */
	public SimpleUDOP(T tempSymbol, boolean includeLeaves)
	{
		super(includeLeaves);
		this.tempSymbol = tempSymbol;		
	}
	
	/**
	 * Constructs a new UDOP model with the specified token as a temporary 
	 * symbol and the specified cache size.
	 * 	 
	 * @param cacheSize The number of parse charts to keep in memory.
	 * @param tempSymbol
	 * @param corpus A corpus to be added to the model
	 */
	public SimpleUDOP(T tempSymbol, SequenceCorpus<T> corpus, boolean includeLeaves)
		throws IOException
	{
		super(includeLeaves);
		this.tempSymbol = tempSymbol;		
		
		addTrees(corpus);
	}
	
	/**
	 * Constructs a new UDOP model with the specified token as a temporary 
	 * symbol and a cache size of 10. 
	 * 
	 * @param tempSymbol
	 * @param rootSymbol (TODO) not used yet
	 */
	public SimpleUDOP(T tempSymbol, T rootSymbol, boolean includeLeaves)
	{
		super(includeLeaves);
		this.tempSymbol = tempSymbol;		
		this.rootSymbol = rootSymbol;
	}
	
	/**
	 * Constructs a new UDOP model with the specified token as a temporary 
	 * symbol and the specified cache size.
	 * 	 
	 * @param cacheSize The number of parse charts to keep in memory.
	 * @param tempSymbol
	 * @param rootSymbol (TODO) not used yet 
	 * @param corpus A corpus to be added to the model
	 */
	public SimpleUDOP(T tempSymbol, T rootSymbol, SequenceCorpus<T> corpus, boolean includeLeaves)
		throws IOException
	{
		super(includeLeaves);
		
		this.tempSymbol = tempSymbol;
		this.rootSymbol = rootSymbol;		
		
		addTrees(corpus);
	}		
	
	public void addTrees(SequenceCorpus<T> in)
	throws IOException
	{
		/* Create an AllBinaryTreesCorpusWrapper from in */
		Corpus<Tree<T>> binTreeCorpus =
			new AllBinaryTreesCorpusWrapper<T>(in, tempSymbol);
		
		/* Add the trees to super */
		for(Tree<T> tree : binTreeCorpus)
			add(tree);
	}
}
