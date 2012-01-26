package org.lilian.util;

import static org.junit.Assert.*;
import static org.lilian.util.Functions.mod;

import org.junit.Test;

public class FunctionsTest
{

	@Test
	public void testMod()
	{
		assertEquals(5, mod(15, 10));
		assertEquals(9, mod(-1, 10));

	}

}
