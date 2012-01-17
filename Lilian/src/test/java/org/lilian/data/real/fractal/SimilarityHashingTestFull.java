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
	private static final int SAMPLES = 1000000;
	@Test
	public void test()
	{
		long seed = new Random().nextLong();
		Global.random = new Random(seed);
		System.out.println(seed);
		
		File dir = new File("/Users/Peter/Documents/PhD/simhash_full/");
		dir.mkdirs();
		
		Generator<Point> im = IFSs.sierpinski().generator();
		// Generator im = IFSs.cantorA().generator();
		// Generator im = new MVN(2);
		// Generator im = IFSs.random(2, 4, 0.55).generator();
		Generator<AffineMap> gen = new SSGenerator(im);
		
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

		List<Double> p = new ArrayList<Double>(new Point(6));			
			
		for(int i : series(100000))
		{
			AffineMap c = gen.generate();
			p = combine(p, c.parameters(), 0.9);
			
			if(i % 1000 == 0)
				System.out.println(p);
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
	
	public static class SSGenerator implements Generator<AffineMap>
	{		
		private static final int BUFFER_SIZE = 5000;
		
		private Generator<Point> master;
		private List<Point> buffer = new ArrayList<Point>(BUFFER_SIZE);
	
		public SSGenerator(Generator<Point> master)
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
		public AffineMap generate()
		{
			buffer();
			
			Point a0 = buffer.remove(0),
				  a1 = buffer.remove(0),
				  b0 = buffer.remove(0),
			      b1 = buffer.remove(0);
					
			return Maps.findMap(Arrays.asList(a0, a1), Arrays.asList(b0, b1));
		}

		@Override
		public List<AffineMap> generate(int n)
		{
			List<AffineMap> points = new ArrayList<AffineMap>(n);
			for(int i : Series.series(n))
				points.add(generate());
			
			return points;			
		}
	}
}
