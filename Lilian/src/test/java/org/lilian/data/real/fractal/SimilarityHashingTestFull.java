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

public class SimilarityHashingTestFull
{
	// private static final int DATA_SIZE = 100000;
	private static final int SAMPLES = 100000;
	private static final double LAMBDA = 0.09;
	
	@Test
	public void test()
	{
		long seed = new Random().nextLong();
		Global.random = new Random(seed);
		System.out.println(seed);
		
		File dir = new File("/Users/Peter/Documents/PhD/simhash_full_2/");
		dir.mkdirs();
		
		Generator<Point> im = IFSs.sierpinski().generator();
		// Generator im = IFSs.cantorA().generator();
		// Generator im = new MVN(2);
		// Generator im = IFSs.random(2, 4, 0.55).generator();
		Generator<Maps.MapResult> gen = new SSGenerator(im.generate(SAMPLES));
		
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

		List<Point> out = new ArrayList<Point>();	
		for(int i : series(SAMPLES))
		{
			Maps.MapResult map = gen.generate();
			out.add(new Point(
					Math.min(map.scale(), 1.0/map.scale()), 
					Math.abs(map.translation().getEntry(0))
				));
		}
		
		try
		{
			BufferedImage image = Draw.draw(
					out, 250, true);
			ImageIO.write(image, "PNG", new File(dir, "density.png"));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Convex combination of and b
	 * @author Peter
	 *
	 * 
	 * @param weight The weight of a
	 */
	public static List<Double> combine(List<Double> a, List<Double> b, double weight)
	{
		List<Double> result = new ArrayList<Double>(a.size());
		
		for(int i = 0; i < a.size(); i++)
			result.add(a.get(i) * weight + b.get(i) + (1.0 - weight));
		
		return result;
	}
	
	public static class SSGenerator implements Generator<Maps.MapResult>
	{		
		private static final int BUFFER_SIZE = 5000;
		
		private List<Point> data ;

		public SSGenerator(List<Point> data)
		{
			super();
			this.data = data;
		}

		@Override
		public Maps.MapResult generate()
		{			
			Point[] a = discretize(new Point[]{
					data.get(Global.random.nextInt(data.size())),
					data.get(Global.random.nextInt(data.size()))
			}, LAMBDA);
			Point[] b = discretize(new Point[]{
					data.get(Global.random.nextInt(data.size())),
					data.get(Global.random.nextInt(data.size()))
			}, LAMBDA);

			return Maps.findMapResult(Arrays.asList(a), Arrays.asList(b));
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
		public List<Maps.MapResult> generate(int n)
		{
			List<Maps.MapResult> points = new ArrayList<Maps.MapResult>(n);
			for(int i : Series.series(n))
				points.add(generate());
			
			return points;			
		}
	}
}
