package org.lilian.data.real.fractal;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Draw;
import org.lilian.data.real.Generator;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Map;
import org.lilian.data.real.Maps;
import org.lilian.data.real.Point;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class SimilarityHashingTest
{
	private static final int DATA_SIZE = 100000;
	private static final int SAMPLES = 1000000;
	private static final double LAMBDA = 10E-2;
	
	@Test
	public void test()
	{
		long seed = new Random().nextLong();
		Global.random = new Random(seed);
		System.out.println(seed);
		
		File dir = new File("/Users/Peter/Documents/PhD/simple/");
		dir.mkdirs();

		// Generator<Point> g = IFSs.random(2, 4, 0.4).generator();
		Generator<Point> g = new MVN(2);
		
		List<Point> in = g.generate(DATA_SIZE);
//		Map m1 = ifs.get(0);
//		List<Point> out = m1.map(m1.map(in));
	
		// Generator im = IFSs.cantorA().generator();
		Generator<Point> gen = new SSGenerator(in, in);
		
		try
		{
			BufferedImage image = Draw.draw(in, 1000, true);
			ImageIO.write(image, "PNG", new File(dir, "in.png"));
//			image = Draw.draw(out, 1000, true);
//			ImageIO.write(image, "PNG", new File(dir, "out.png"));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try
		{
			BufferedImage image = Draw.draw(
					gen, SAMPLES, 
					new double[]{.0, 1.0}, new double[]{.0, 1.0},
					250, 250, true);
			ImageIO.write(image, "PNG", new File(dir, "density.png"));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double l = 0.1;
		double lc = 1.0 - l;
		Point p = new Point(2);
		for(int i : series(100000))
		{
			Point c = gen.generate();
			p = new Point(p.get(0) * lc + c.get(0) * l, p.get(1) * lc + c.get(1) * l);
		}
		
		System.out.println(p);
		
//		for(Bin bin :top(
//				gen, SAMPLES, 
//				new double[]{.0, 1.0}, new double[]{.0, 1.0},
//				50, 50).subList(0, 100))
//			System.out.println(bin);	
		
	}
	
	/**
	 * Returns the length of the line between two points
	 * @param x
	 * @param y
	 * @return
	 */
	public static double length(Point x, Point y)
	{
		double l0 = x.get(0) - y.get(0);
		double l1 = x.get(1) - y.get(1);
		
		return Math.sqrt(l0*l0 + l1*l1);
	}
	

	public static double scale(Point[] a, Point[] b, int i)
	{
		double la = a[1].get(i) - a[0].get(i);
		double lb = b[1].get(i) - b[0].get(i);
		
		return Math.min(la/lb, lb/la);
	}

	/**
	 * Returns the angle that the line (x,y) makes with the horizontal axis
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static double angle(Point x, Point y)
	{
		double l0 = y.get(0) - x.get(0);
		double l1 = y.get(1) - x.get(1);
		
		return Math.atan( l1 / l0 );
	}
	
	public static class SSGenerator implements Generator<Point>
	{		
		private static final int BUFFER_SIZE = 5000;
		
		private List<Point> masterIn;
		private List<Point> masterOut;
		
		public SSGenerator(List<Point> in, List<Point> out)
		{
			this.masterIn = in;
			this.masterOut = out;
		}
		
		private Point[] drawShort(int i, List<Point> master)
		{
			Point[] a = new Point[]{
					master.get(Global.random.nextInt(master.size())), 
					master.get(Global.random.nextInt(master.size()))};
			if(i == 0)
				return a;
			
			Point[] b =	drawShort(i - 1, master);
			
			double la = length(a[0], a[1]);
			double lb = length(b[0], b[1]);
			
			if(la > lb)
				return b;
			return a;
		}
		
		public Point[] discretize(Point[] in, double lambda) 
		{
			return new Point[] {
				discretize(in[0], lambda),
				discretize(in[1], lambda)};
		}
		
		public Point discretize(Point in, double lambda) 
		{
			return new Point(
				discretize(in.get(0), lambda),
				discretize(in.get(1), lambda));
		}
		
		public double discretize(double in, double lambda)
		{
			return in - in % lambda;
		}

		@Override
		public Point generate()
		{
			double weight = 0.9;
			double[] c = generateBase().getBackingData();
			
			for(int i = 0; i < 0; i++)
			{
				double[] p = generateBase().getBackingData();
				
				c[0] = c[0]*weight + p[0]*(1.0 - weight);
				c[1] = c[1]*weight + p[1]*(1.0 - weight);
			}
			
			return new Point(c);
		}
		
		public Point generateBase()
		{
			Point[] a = discretize(drawShort(0, masterIn), LAMBDA);
			Point[] b = discretize(drawShort(0, masterIn), LAMBDA);;
			
			double la = length(a[0], a[1]);
			double lb = length(b[0], b[1]);
//			System.out.println(la + " " + lb);
			
			double scale = min(la / lb, lb / la);
			
			double aa = angle(a[0], a[1]);
			double ab = angle(b[0], b[1]);
			
			double angle = Math.abs(aa - ab) % Math.PI;	
			// * Normalize
			angle = angle / Math.PI;
			
			double[] s = {
					scale(a, b, 0),
					scale(a, b, 1)
			};
			
			double[] t = {
						abs(a[0].get(0) - b[0].get(0)),
						abs(a[0].get(1) - b[0].get(1))
					};
					
//			return new Point(scale, angle);
//			AffineMap map = Maps.findMap(
//					Arrays.asList(a0, a1),
//					Arrays.asList(b0, b1));
			
//			return new Point(
//					Math.abs(map.getTranslation().getEntry(0)),
//					Math.abs(map.getTranslation().getEntry(1))					
//				);
			
			return new Point(
					scale,
					angle);
		}

		@Override
		public List<Point> generate(int n)
		{
			List<Point> points = new ArrayList<Point>(n);
			for(int i : Series.series(n))
				points.add(generate());
			
			return points;			
		}
	}
	
	/**
	 * Finds the top n bins in a 2D histogram
	 */
	public static List<Bin> top(Generator<Point> generator,
											int samples,
											double[] xrange, 
											double[] yrange, 
											int xRes,
											int yRes)
	{
		// * size of the image in coordinates
		double 	xDelta = xrange[1] - xrange[0],
				yDelta = yrange[1] - yrange[0];
		
		// * coordinate distance per pixel
		double xStep = xDelta/(double) xRes;
		double yStep = yDelta/(double) yRes;		
		
		// int xRes = (int) (xDelta / xStep);
		// int yRes = (int) (yDelta / yStep);

		float max = Float.NEGATIVE_INFINITY;
		float min = 0.0f;		
		float[][] matrix = new float[xRes][];
		for(int x = 0; x < xRes; x++)
		{
			matrix[x] = new float[yRes];
			for(int y = 0; y < yRes; y++)
				matrix[x][y] = 0.0f;				
		}
		
		int xp, yp;
		for(int i = 0; i < samples; i++)
		{
			Point point = generator.generate();
			
			xp = Draw.toPixel(point.get(0), xRes, xrange[0], xrange[1]); 
			yp = Draw.toPixel(point.get(1), yRes, yrange[0], yrange[1]);
			if(xp >= 0 && xp < xRes && yp >= 0 && yp < yRes)
				matrix[xp][yp] ++;
		}
		
		BufferedImage image = 
			new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_ARGB);
		
		List<Bin> bins = new ArrayList<Bin>(xRes*yRes);
		for(int i = 0; i < xRes; i++)
			for(int j = 0; j < yRes; j++)
				bins.add(
						new Bin(i,j, 
								Draw.toCoord(i, xRes, xrange[0], xrange[1]),
								Draw.toCoord(j, yRes, yrange[0], yrange[1]), 
								matrix[i][j]));
		
		Collections.sort(bins);
		return bins;	
	}
	
	private static class Bin implements Comparable<Bin>
	{
		int i, j;
		double x, y;
		float freq;
		
		public Bin(int i, int j, double x, double y, float freq)
		{
			super();
			this.i = i;
			this.j = j;
			this.x = x;
			this.y = y;
			
			this.freq = freq;
		}



		@Override
		public int compareTo(Bin other)
		{
			return - Float.compare(freq, other.freq);
		}
	
		public double x()
		{
			return x;
		}
		
		public double y()
		{
		 return y;
		}

		public String toString()
		{
			return String.format("(%.3f, %.3f)\t[%d, %d]\t%d", x, y, i, j, (int)freq);
		}
	}
}
