package org.lilian.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;


public class GZIPTest 
{

	@Test
	public void testRatio() 
	{
		List<String> a   = Arrays.asList("a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "b", "c", "a", "a", "a", "a", "a", "a", "a", "a");
		List<String> ab  = Arrays.asList("a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "b", "c", "b", "b", "b", "b", "b", "a", "b", "a");
		List<String> abc = Arrays.asList("a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a");
		List<String> rnd = new ArrayList<String>(abc);
		Collections.shuffle(rnd);  
		
		Compressor<String> comp = new GZIPCompressor<String>();
		
		System.out.println(comp.ratio(a));
		System.out.println(comp.ratio(ab));
		System.out.println(comp.ratio(abc));
		System.out.println(comp.ratio(rnd));		
		
		assertTrue(comp.ratio(a.toArray()) < comp.ratio(ab.toArray()));
		assertTrue(comp.ratio(abc.toArray()) < comp.ratio(rnd.toArray()));		

	}
	
	@Test
	public void testRatioShort() 
	{
		List<String> a   = Arrays.asList("a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a");
		List<String> ab  = Arrays.asList("a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a", "b", "a");
		List<String> abc = Arrays.asList("a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b", "c", "a", "b");
		List<String> rnd = new ArrayList<String>(abc);
		Collections.shuffle(rnd);  
		
		Compressor<String> comp = new GZIPCompressor<String>();
		
		System.out.println(comp.ratio(a));
		System.out.println(comp.ratio(ab));
		System.out.println(comp.ratio(abc));
		System.out.println(comp.ratio(rnd));		
		
		assertTrue(comp.ratio(a.toArray()) < comp.ratio(ab.toArray()));
		assertTrue(comp.ratio(abc.toArray()) < comp.ratio(rnd.toArray()));		

	}
	

}


