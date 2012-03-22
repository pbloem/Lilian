package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class MapModelTest {

	@Test
	public void testMapModel() 
	{
		MapModel<AffineMap> model = new MapModel<AffineMap>(AffineMap.identity(3), 1.0);
		
		model.addMap(AffineMap.identity(3), 1.0);
		model.addMap(AffineMap.identity(3), 1.0);
	}

	@Test
	public void testIsInvertible() 
	{
		MapModel<AffineMap> model = new MapModel<AffineMap>(AffineMap.identity(3), 1.0);
		
		model.addMap(AffineMap.identity(3), 1.0);
		model.addMap(AffineMap.identity(3), 1.0);
		
		assertTrue(model.invertible());
		
		// * add a noninvertible map
		model.addMap(new AffineMap(Arrays.asList(1.0,0.0,0.0, 0.0,0.0,0.0, 1.0,0.0,0.0, 0.0,0.0,0.0)), 1.0);
		
		assertFalse(model.invertible());
	}

	@Test
	public void testProbability() 
	{
		MapModel<AffineMap> model = new MapModel<AffineMap>(AffineMap.identity(3), 1.0);
		
		model.addMap(AffineMap.identity(3), 1.0);
		model.addMap(AffineMap.identity(3), 1.0);
		model.addMap(AffineMap.identity(3), 1.0);
		
		assertEquals(0.25, model.probability(0), 0.0);
	}

	@Test
	public void testParameters() 
	{
		MapModel<AffineMap> model = new MapModel<AffineMap>(AffineMap.identity(3), 1.0);
		
		model.addMap(AffineMap.identity(3), 1.0);
		model.addMap(AffineMap.identity(3), 1.0);
		model.addMap(AffineMap.identity(3), 1.0);
		
		assertEquals(model, MapModel.builder(4, AffineMap.affineMapBuilder(3)).build(model.parameters()));
	}

}
