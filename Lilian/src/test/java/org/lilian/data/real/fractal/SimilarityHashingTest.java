package org.lilian.data.real.fractal;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.data.real.Draw;
import org.lilian.data.real.Generator;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class SimilarityHashingTest
{
	// private static final int DATA_SIZE = 100000;
	private static final int SAMPLES = 1000000;
	@Test
	public void test()
	{
		long seed = new Random().nextLong();
		Global.random = new Random(seed);
		System.out.println(seed);
		
		File dir = new File("/Users/Peter/Documents/PhD/simhash/");
		dir.mkdirs();
		
		// Generator im = IFSs.sierpinski().generator();
		// Generator im = IFSs.cantorA().generator();
		// Generator im = new MVN(2);
		Generator im = IFSs.random(2, 4, 0.55).generator();
		Generator gen = new SSGenerator(im);
		
		try
		{
			BufferedImage image = Draw.draw(
					im, SAMPLES, 
					new double[]{-1.0, 1.0}, new double[]{-1.0, 1.0},
					1000, 1000, true);
			ImageIO.write(image, "PNG", new File(dir, "generator.png"));
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
					1000, 1000, false);
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
	
	public static class SSGenerator implements Generator
	{		
		private static final int BUFFER_SIZE = 5000;
		
		private Generator master;
		private List<Point> buffer = new ArrayList<Point>(BUFFER_SIZE);
	
		public SSGenerator(Generator master)
		{
			this.master = master;
			buffer();
		}
		
		private void buffer()
		{
			if(buffer.size() < 10)
			{
				for(int i : Series.series(BUFFER_SIZE))
					buffer.add(master.generate());
			
				Collections.shuffle(buffer);
			}
		}

		@Override
		public Point generate()
		{
			buffer();
			
			Point a0 = buffer.remove(0),
				  a1 = buffer.remove(0),
				  b0 = buffer.remove(0),
			      b1 = buffer.remove(0);
			
			double la = length(a0, a1);
			double lb = length(b0, b1);
			
			double scale = min(la / lb, lb / la);
			
			double aa = angle(a0, a1);
			double ab = angle(b0, b1);
			
			double angle = Math.abs(aa - ab) % Math.PI;	
			// * Normalize
			angle = angle / Math.PI;
					
			return new Point(scale, angle);
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
	public static List<Bin> top(Generator generator,
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
