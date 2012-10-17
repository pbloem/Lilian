package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.lilian.util.Series;

public class PCATest
{

	@Test
	public void test()
	{
		List<Point> data = new MVN(3).generate(300);
		PCA pca = new PCA(data);
		
		List<Point> simplified = pca.simplify(3);
		for(int i : Series.series(data.size()))
		{
			System.out.println(data.get(i) + " " + simplified.get(i));
		}
		
		Point datum = new MVN(3).generate();
		Point simp = pca.simplify(datum, 3);
		System.out.println(datum + " " + pca.mapBack(simp));
	}

}
