package org.lilian.graphs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lilian.graphs.random.FractalGenerator;
import org.lilian.util.Series;

public class FractalGeneratorTest
{

	@Test
	public void testGraph()
	{
		FractalGenerator gen = new FractalGenerator(2, 1, 0.0);
		
		for(int i : Series.series(4))
		{
			gen.iterate();
			System.out.println(gen.graph().size() + " " + gen.graph().numLinks());
			System.out.println(gen.graph());
		}
		
	}

}
