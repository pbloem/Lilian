package org.lilian.corpora;

import java.util.Iterator;

public abstract class AbstractCorpusIterator<T> implements Iterator<T> {

	@Override
	public abstract boolean hasNext();

	@Override
	public abstract T next();

	@Override
	public void remove() {
			throw new UnsupportedOperationException(); 
	}
}
