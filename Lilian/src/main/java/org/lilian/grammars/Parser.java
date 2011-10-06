package org.lilian.grammars;

import java.util.*;

public interface Parser<T>
{
	public Parse<T> parse(Collection<? extends T> sentence);
}
