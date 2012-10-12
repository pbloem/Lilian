package org.lilian.util.graphs;

import static org.junit.Assert.*;

import nl.peterbloem.powerlaws.Discrete;

import org.junit.Test;
import org.lilian.util.graphs.old.Graphs;
import org.lilian.util.graphs.old.algorithms.BAGenerator;

public class BAGeneratorTest
{

	@Test
	public void test()
	{
		BAGenerator generator = new BAGenerator(1, 3);
		
		generator.iterate(50);
		
		System.out.println(generator.graph().size());
		System.out.println(generator.graph());
		
		Discrete pl = Discrete.fit(Graphs.degrees(generator.graph())).fit();
		System.out.println(pl.exponent());
		System.out.println(pl.xMin());
		
		System.out.println(pl.significance(Graphs.degrees(generator.graph()), 0.1));
	}

}
