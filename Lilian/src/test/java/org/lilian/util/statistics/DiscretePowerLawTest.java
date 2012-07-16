package org.lilian.util.statistics;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.util.Series;

public class DiscretePowerLawTest
{

	@Test
	public void testPInv()
	{
		DiscretePowerLaw dpl = new DiscretePowerLaw(5.0, 2.5);
		
		for(int i : Series.series(5, 100))
			assertEquals(i, dpl.pInv(dpl.p(i)));
		
	}

}
