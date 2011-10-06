package org.lilian.grammars.automata;

import java.util.*;

import org.lilian.util.*;

public class StringDataComplexity<T> implements Complexity<T> {
	
	private Collection<List<T>> data;
	private Map<T, Integer> vocabulary = new HashMap<T, Integer>();
	
	public StringDataComplexity(Collection<List<T>> data)
	{
		this.data = data;
		
		Set<T> vocSet = new HashSet<T>();
		for(List<T> example : data)
			for(T label : example)
				vocSet.add(label);
		
		int i = 0;
		for(T label : vocSet)
		{
			vocabulary.put(label, i);
			i++;
		}
	}

	@Override
	public double get(Automaton<T> a) 
	{
		List<List<Integer>> encoded = new ArrayList<List<Integer>>(data.size());
		
		for(List<T> example : data)
			encoded.add(encoding(example, a));
		
		GZIPCompressor<T> gzip = new GZIPCompressor<T>();
		return gzip.compressedSize(encoded);
	}
	
	public List<Integer> encoding(List<T> example, Automaton<T> aut)
	{
		List<Integer> encoding =  aut.encode(example);
		if(encoding == null)
		{
			encoding = new ArrayList<Integer>(example.size());
			encoding.add(-1); // this signals that we've stored the word directly, 
			                  // not as a path on the automaton
			for(T label : example)
				encoding.add(vocabulary.get(label));
		}
		
		return encoding;
	}
}
