package org.lilian.corpora.wrappers;

import org.lilian.corpora.Corpus;
import org.lilian.util.*;
import org.lilian.util.trees.Tree;

import java.io.*;
import java.util.Iterator;

public class StripLeaves<T> 
	extends CorpusWrapper<Tree<T>, Tree<T>> 
{
	public StripLeaves(Corpus<Tree<T>> master) 
	{
		super(master);
	}

	@Override
	public Iterator<Tree<T>> iterator() {
		return new SLIterator();
	}

	private class SLIterator 
		extends WrapperIterator
	{
		public Tree<T> next()
		{
			Tree<T> tree = masterIterator.next();
			String treeString = tree.toString();
			try {
				Functions.removeLeaves(tree);
			} catch(Exception e)
			{
				System.out.println(treeString);
				System.out.println(tree);
			}
			return tree;
		}
	
		public String toString()
		{
			return "Leaf nodes strippped from: ["+master.toString()+"]";
		}
	}
}
