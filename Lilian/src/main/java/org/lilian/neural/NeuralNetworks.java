package org.lilian.neural;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.lilian.data.real.Point;

public class NeuralNetworks
{
	
	public static List<Point> orbit(ThreeLayer nn, Point initial, int n)
	{
		List<Point> result = new ArrayList<Point>(n);
		
		Point p = initial;
		
		for(int i : series(n))
		{
			nn.set(p);
			nn.step();
			
			p = new Point(nn.out());
			result.add(p);
		}
		
		return result;
	}

}
