package org.lilian.data.real;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jfree.date.EasterSundayRule;
import org.junit.Test;
import org.lilian.Global;
import org.lilian.search.evo.ES;
import org.lilian.util.Series;

public class PCAIterativeTest
{

	@Test
	public void test()
	{
		Global.random = new Random(42); 
		
		// Ten 2d data points
		
		int d = 784;
		int n = 10;
		int k = 2;
		int its = 10;
		
		List<Point> data = new ArrayList<Point>(n);
		for(int i : series(n))
			data.add(Point.random(d, 2.0));
		
		System.out.println("*******************");
		
		PCAIterative pcai = new PCAIterative(data, k, its);
		PCA pca = new PCA(data);

		System.out.println(pcai.simplify(2));
		System.out.println(pca.simplify(2));
	}

}
