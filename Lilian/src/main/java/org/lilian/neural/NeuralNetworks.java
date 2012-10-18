package org.lilian.neural;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.lilian.data.real.Point;

public class NeuralNetworks
{
	
	public static List<Point> orbit(Neural<Double> nn, Point initial, int n)
	{
		List<Point> result = new ArrayList<Point>(n);
		
		for(int i : series(nn.size()))
			nn.set(i, initial.get(i));
		
		for(int i : series(n))
		{
			nn.step();
			result.add(new Point(nn));
		}
		
		return result;
	}
	
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
