package org.lilian.level;

import java.io.*;
import java.util.*;

import org.lilian.corpora.*;
import org.lilian.models.*;

public class SequenceModel<T> {
	
	private List<List<T>> buffers;
	public List<LevelStatisticsModel<List<T>>> models;
	private HashSet<T> alphabet= new HashSet<T>();
	
	private int min, max;
	
	public SequenceModel(int min, int max)
	{
		this.min = min;
		this.max = max;
		
		buffers = new ArrayList<List<T>>(max + 1);
		models  = new ArrayList<LevelStatisticsModel<List<T>>>(max + 1);		
		
		for(int i = min; i < max + 1; i++)
		{
			buffers.add(new LinkedList<T>());
			models.add(new LevelStatisticsModel<List<T>>());
		}		
	}
	
	public SequenceModel(Corpus<T> corpus, int min, int max)
		throws IOException
	{
		this(min, max);
		add(corpus);
	}
	
	public void add(Corpus<T> corpus)
		throws IOException
	{
		for(T token : corpus)
			add(token);
	}
	
	public void add(T token)
	{
		alphabet.add(token);
		
		// * Fill the buffers
		for(int i = 0; i < buffers.size(); i++)
		{
			List<T> buffer = buffers.get(i);
			
			buffer.add(token);              // push
			while(buffer.size() > i + min)  // pop
				buffer.remove(0);
		}
		
		// * Add the n-grams to the models
		List<T> superToken = new ArrayList<T>();
		for(int i = 0; i < models.size(); i++)
		{
			superToken = new ArrayList<T>();
			superToken.addAll(buffers.get(i));
			
			models.get(i).add(superToken);
		}
	}

	/**
	 * The shorter-by-one subtokens of this token
	 * @param token
	 * @return
	 */
	public List<List<T>> children(List<T> token)
	{
		List<List<T>> children = new ArrayList<List<T>>();
		
		children.add(token.subList(0, token.size()-1));
		children.add(token.subList(1, token.size()));		
		
		return children;
	}
	
	/**
	 * The longer-by-one supertokens of this token
	 * @param token
	 * @return
	 */	
	public List<List<T>> parent(List<T> token)
	{
		List<List<T>> parents = new ArrayList<List<T>>();
		
		List<T> parent;
		for(T t : alphabet)
		{
			parent = new ArrayList<T>(token.size() + 1);
			parent.add(t);
			parent.addAll(token);			
			parents.add(parent);

			parent = new ArrayList<T>(token.size() + 1);
			parent.addAll(token);			
			parent.add(t);
			parents.add(parent);
		}
		
		return parents;
	}

}
