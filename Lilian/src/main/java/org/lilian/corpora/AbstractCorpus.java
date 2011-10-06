package org.lilian.corpora;

import java.util.*;

public abstract class AbstractCorpus<T> 
	extends AbstractCollection<T>
	implements Corpus<T>
{

	@Override
	public abstract Iterator<T> iterator();

	@Override
	@SuppressWarnings("unused")
	public int size() {
		int i = 0;
		for(T token: this)
			i++;
		
		return i;
	}

}
