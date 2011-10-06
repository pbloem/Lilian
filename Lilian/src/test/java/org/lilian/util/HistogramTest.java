package org.lilian.util;

import static org.junit.Assert.*;
import static org.lilian.util.Series.ranges;
import static org.lilian.util.Series.series;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.models.Histogram;
import org.lilian.util.ranges.BoundedLinearRangeSet;
import org.lilian.util.ranges.LinearRangeSet;
import org.lilian.util.ranges.Range;
import org.lilian.util.ranges.RangeSet;

public class HistogramTest
{

	@Test
	public void testHistogram()
	{
		RangeSet ranges = new BoundedLinearRangeSet(-1.0, 0.3, 1.0, true);
		Histogram hist = new Histogram(ranges);
		
		for (int i : series(100000))
			hist.add( Global.random.nextGaussian() );
		
		hist.print(System.out);
		
		for (Range range : ranges(series(-1.0, 0.3, 1.0)))
			System.out.println(hist.probability(range));;
	}
}
