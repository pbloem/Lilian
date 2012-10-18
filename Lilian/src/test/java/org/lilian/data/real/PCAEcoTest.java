package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class PCAEcoTest
{

	@Test
	public void test()
	{
		int m = 10;
		
		List<Point> data = new MVN(20).generate(m);
		
		PCAEco pca = new PCAEco(data);
		
		Point point = new MVN(20).generate();
		
		System.out.println(point);
		System.out.println(pca.simplify(point, m));
		System.out.println(pca.mapBack(pca.simplify(point, 3)));
		
	}

}
