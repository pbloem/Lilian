package org.lilian.search.evo;

import org.lilian.Global;
import org.lilian.util.BitString;

public class BitStringMutator implements Mutator<BitString>
{

	@Override
	public BitString mutate(BitString in)
	{
		int d = Global.random.nextInt(in.size());
		in.set(d, ! in.get(d));
		return in;
	}

}
