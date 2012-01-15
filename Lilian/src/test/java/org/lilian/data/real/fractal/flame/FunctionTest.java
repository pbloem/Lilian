package org.lilian.data.real.fractal.flame;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.lilian.data.real.Point;
import org.lilian.search.Builder;

public class FunctionTest
{

	@Test
	public void testFunction()
	{
		Builder<Function> builder = Function.builder();
		
		// * The identity function
		Function f = builder.build(Arrays.asList(
				1.0,0.0, 0.0,1.0, 0.0,0.0,
				1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				1.1, 1.2, 1.3,
				1.0));
		
		System.out.println(f);
		
		Point in = new Point(4.0, 5.0);
		assertEquals(in, f.map(in));
	}
}
