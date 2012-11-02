package org.lilian.data.real;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.List;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;

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
	
	public static Generator<Point> rossler()
	{
		return rossler(0.2, 0.2, 5.7, 0.001);
	}
	
	public static Generator<Point> rossler(double a, double b, double c, double h)
	{
		Generator<Point> rossler = new RK4(new RosslerDerivative(a, b, c), 0.001, new ArrayRealVector(3));
		
		rossler.generate(1000000);
		
		return rossler;
	}
	
	private static class RosslerDerivative implements RK4.Derivative
	{
		double a, b, c;
		
		public RosslerDerivative(double a, double b, double c)
		{
			this.a = a;
			this.b = b;
			this.c = c;
		}
		
		@Override
		public RealVector derivative(RealVector in, double t)
		{
			double x = in.getEntry(0),
			       y = in.getEntry(1),
			       z = in.getEntry(2);
			
			return new ArrayRealVector(new double[]{
				- y - z,
				x + a * y,
				b + z * (x - c)
			}, 0, 3);
		}
	}
	
	public static Generator<Point> mapped(Generator<Point> master, Map map)
	{
		return new MappedGenerator(map, master);
	}
	
	public static class MappedGenerator implements Generator<Point>
	{
		private Map map;
		private Generator<Point> master;

		public MappedGenerator(Map map, Generator<Point> master)
		{
			this.map = map;
			this.master = master;
		}

		@Override
		public Point generate()
		{
			Point p = master.generate();
			return map.map(p);
		}

		@Override
		public List<Point> generate(int n)
		{
			List<Point> p = master.generate(n);
			return map.map(p);
		}
	}
}
