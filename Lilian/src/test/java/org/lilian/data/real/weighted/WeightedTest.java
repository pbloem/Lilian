package org.lilian.data.real.weighted;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.lilian.util.Series;

public class WeightedTest
{

	@Test
	public void test()
	{
		Weighted<String> w = WeightedLists.combine(
				Arrays.asList("a", "b", "c"), Arrays.asList(1.0, 0.1, 0.01));
		
		for(int i : Series.series(25))
			System.out.println(w.choose());
		
	}

}
