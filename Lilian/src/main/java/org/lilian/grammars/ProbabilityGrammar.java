package org.lilian.grammars;

import java.io.*;
import java.util.*;

import org.lilian.util.trees.Tree;

public class ProbabilityGrammar<T> implements Grammar<T>
{

	@Override
	public void addRule(T from, Collection<? extends T> to)
	{

	}

	@Override
	public void addRule(T from, Collection<? extends T> to, double freq)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public List<T> generateSentence(T topSymbol, int minDepth, int maxDepth)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> generateSentence(int minDepth, int maxDepth)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Parse<T> parse(Collection<? extends T> sentence)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRule(T from, Collection<? extends T> to, double freq)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void write(File directory, String base) throws IOException
	{
	}

	@Override
	public Tree<T> generateTree(T topSymbol, int minDepth, int maxDepth, Random random)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Tree<T> generateTree(int minDepth,int maxDepth, Random random)
	{
		throw new UnsupportedOperationException();
	}

}
