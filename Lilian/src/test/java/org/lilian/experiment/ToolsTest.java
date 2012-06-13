package org.lilian.experiment;

import static org.junit.Assert.*;

import org.junit.Test;

public class ToolsTest
{

	@Test
	public void testCssSafe()
	{
		assertEquals("test-case", Tools.cssSafe("   	Te%%%s$$$$t	 cASE  "));
	}

}
