package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.lilian.search.Builder;

public class AffineMapTest 
{

	@Test
	public void testInvertible() 
	{
		Map map;
		
		// the identity map is invertible
		map = AffineMap.identity(6);
		
		assertTrue(map.invertible());	
		
		// arbitrary invertible map
		map = new AffineMap(Arrays.asList(4.0, 3.0, 2.0, 1.0, 0.0, 0.0));
		
		assertTrue(map.invertible());
		
		// arbitrary non-invertible map
		map = new AffineMap(Arrays.asList(4.0, 3.0, 0.0, 0.0, 0.0, 0.0));
		
		assertFalse(map.invertible());		
	}

	@Test
	public void testDimension() 
	{
		
		Map map = new AffineMap(Arrays.asList(4.0, 3.0, 0.0, 0.0, 0.0, 0.0));
		assertEquals(2, map.dimension());		
	}

	@Test
	public void testIdentity() 
	{
		Map map;
		map = AffineMap.identity(6);
		
		Point in = new Point(1, 2 ,3 ,4 ,5, 6);
		assertEquals(in, map.map(in));
	}

	@Test
	public void testNumParameters() 
	{
		assertEquals(2,  AffineMap.numParameters(1));
		assertEquals(6,  AffineMap.numParameters(2));
		assertEquals(12, AffineMap.numParameters(3));
		assertEquals(20, AffineMap.numParameters(4));
		assertEquals(30, AffineMap.numParameters(5));		
	}
	
	@Test
	public void testDimensionStatic() 
	{
		assertEquals(1, AffineMap.dimension(2));
		assertEquals(2, AffineMap.dimension(6));
		assertEquals(3, AffineMap.dimension(12));
		assertEquals(4, AffineMap.dimension(20));
		assertEquals(5, AffineMap.dimension(30));		
	}	

	@Test
	public void testMapPoint() 
	{
		// arbitrary map
		Map map = new AffineMap(Arrays.asList(4.0, 3.0, 2.0, 1.0, 3.0, 3.0));

		assertEquals(new Point(58, 26), map.map(new Point(7, 9)));	
	}
	
	@Test
	public void testBuilder()
	{
		Builder<AffineMap> builder = AffineMap.builder(3);
		
		assertEquals(12, builder.numParameters());
		
		List<Double> params = Arrays.asList(1.0,0.0,0.0, 0.0,1.0,0.0, 0.0,0.0,1.0, 0.0,0.0,0.0);
		
		AffineMap result = builder.build(params);
		
		assertEquals(AffineMap.identity(3), result);
		
		AffineMap result2 = builder.build(result.parameters());
		
		assertEquals(result, result2);		
	}
}
