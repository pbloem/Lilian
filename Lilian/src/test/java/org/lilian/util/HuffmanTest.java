package org.lilian.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;


public class HuffmanTest {

	@Test
	public void testRatio() 
	{
		List<String> a = Arrays.asList("a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "b", "c", "a", "a", "a", "a", "a", "a", "a", "a");
		List<String> b = Arrays.asList("a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "b", "c", "b", "b", "b", "b", "b", "a", "b", "a");
		List<String> c = Arrays.asList("a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a");
		
		Compressor<String> comp = new HuffmanCompressor<String>();
		
		assertTrue(comp.ratio(a.toArray()) < comp.ratio(b.toArray()));
		assertTrue(comp.ratio(b.toArray()) < comp.ratio(c.toArray()));
	}

}
