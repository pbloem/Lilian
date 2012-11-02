package org.lilian.data.real;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Generators
{

	public static Generator<Point> henon()
	{
		return new Henon();
	}
	
	private static class Henon extends AbstractGenerator<Point>
	{
		private static final int PRE = 50;
		public double x = 0.0, y = 0.0, a, b;
		
		public Henon()
		{
			this(1.4, 0.3);
		}
		
		public Henon(double a, double b)
		{
			this.a = a;
			this.b = b; 
			
			generate(PRE);
		}
		
		@Override
		public Point generate()
		{
			double xx = y + 1 - a* x*x,
			       yy = b * x;
			
			x = xx;
			y = yy;
			
			return new Point(x, y);
		}
		
	}
	
	public static Generator<Point> ikeda()
	{
		return new Ikeda();
	}
	
	private static class Ikeda extends AbstractGenerator<Point>
	{
		private static final int PRE = 50;
		public double x, y, u;
		
		public Ikeda()
		{
			this(0.9);
		}
		
		public Ikeda(double u)
		{
			this.u = u;
			
			generate(PRE);
		}
		
		@Override
		public Point generate()
		{
			double t = 0.4 - 6.0 / (1.0 + x*x + y*y),
			       xx = 1 + u * (x * cos(t) - y * sin(t)),
			       yy = u * (x * sin(t) + y * cos(t));
			
			x = xx;
			y = yy;
			
			return new Point(x, y);
		}
		
	}	
	
}
