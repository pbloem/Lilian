package org.lilian.search.evo;

import org.lilian.Global;
import org.lilian.util.BitString;

public class BitStringCrossover implements Crossover<BitString>
{

	@Override
	public BitString cross(BitString first, BitString second)
	{
		int n = Math.min(first.size(), second.size());
		BitString result = new BitString(n);
		for(int i = 0; i < n; i++)
			result.add(Global.random.nextDouble() < 0.5 ? first.get(i) : second.get(i));
		
		return result;
	}

}
