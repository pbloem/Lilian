package org.lilian.neural;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.util.Series;

public class NeuralNetworksTest
{

	@Test
	public void testOrbit()
	{
		int n = 3;
		FullNN nn = FullNN.random(n, 0.00001, Activations.sigmoid());
		
		for(int i : Series.series(n))
			nn.set(i, Global.random.nextDouble());
		
		for(int i : Series.series(15))
		{
			System.out.println(nn + " " + nn.size());
			nn.step();
		}
	}
	
	@Test
	public void testOrbit2()
	{
		int n = 2, h = 10;
		ThreeLayer nn = ThreeLayer.random(n, h, 0.00001, Activations.sigmoid());
		
		List<Point> points = NeuralNetworks.orbit(nn, new MVN(n).generate(), 15);
		
		for(Point p : points)
			System.out.println(p);
	}
}
