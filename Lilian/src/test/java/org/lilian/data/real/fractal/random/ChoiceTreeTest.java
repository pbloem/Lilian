package org.lilian.data.real.fractal.random;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class ChoiceTreeTest
{

	@Test
	public void test()
	{
		
		System.out.println(new ChoiceTree(Arrays.asList(0, 0, 0, 0, 0, 0, 0), 3, 2, 3));
		System.out.println(ChoiceTree.random(3, 2, 3));

	}

}
