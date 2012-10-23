package org.lilian.data.real.fractal;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Draw;
import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class IFSTest
{
	private static double var = 0.001;

		@Test
		public void testGenerator()
		{	
			// long seed = 5692776151140015461l;
			long seed = new Random().nextLong();
			System.out.println("seed: " + seed);
			
			Global.random = new Random(seed);
	//		IFS<AffineMap> sierpinski = IFSs.cantorB();
			IFS<AffineMap> sierpinski = IFSs.sierpinskiOff();
	//		IFS<AffineMap> sierpinski = IFSs.random(2, 3, 0.5);
			
			Builder<IFS<AffineMap>> builder = IFS.builder(3, AffineMap.affineMapBuilder(2));
			List<Double> change = new ArrayList<Double>(builder.numParameters());
			for(int i : Series.series(builder.numParameters()))
				change.add(Global.random.nextGaussian() * 0.0);
			
			int frames = 360;
			for(int i = 0; i < frames; i++)
			{
				write(sierpinski, String.format("out%04d", i));
				
				
				perturb(change, Math.pow((frames - i)/(double)frames, 10) * var);
				
				sierpinski = perturb(sierpinski, builder, change);
			}
		}
	
	public static <M extends Map & Parametrizable> IFS<M> perturb(
			IFS<M> in, Builder<IFS<M>> builder, List<Double> change)
	{
		List<Double> params = new ArrayList<Double>(in.parameters());
		
		// * perturb
		add(params, change);		
		
		return builder.build(params);
	}
	
	private static void add(List<Double> in, List<Double> change)
	{
		for(int i : Series.series(in.size()))
			in.set(i, in.get(i) + change.get(i));
	}	
	
	private static void perturb(List<Double> in, double var)
	{
		for(int i : Series.series(in.size()))
			in.set(i, in.get(i) + Global.random.nextGaussian() * var);
	}
	
	private static void scale(List<Double> in, double scalar)
	{
		for(int i : Series.series(in.size()))
			in.set(i, in.get(i) * scalar);
	}	
	
	private static double length(List<Double> in)
	{
		double l = 0.0;
		for(int i : Series.series(in.size()))
			l += in.get(i) * in.get(i);
		
		return Math.sqrt(l);
	}
	
	private void write(IFS<AffineMap> ifs, String name)
	{
		double[] xrange = new double[]{-3.0666, 1.2};
		double[] yrange = new double[]{-1.2, 1.2};
		
		Functions.tic();		
		
		BufferedImage image = Draw.draw(ifs.generator(), 1000000, xrange, yrange, 1920, 1080, true);
		
		try
		{
		
			File dir = new File("/Users/Peter/Documents/PhD/output/ifs");
			dir.mkdirs();
			ImageIO.write(image, "PNG", new File(dir, name + ".png") );
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(name + ": " + Functions.toc() + " seconds");
	}

	@Test
	public void testCompose()
	{
		IFS<AffineMap> ifs = IFSs.sierpinski();
		
		List<Integer> code = Arrays.asList(0, 2, 1, 0, 1, 1);
		Map m = ifs.compose(code);
		
		Point source = new Point(2), p = source;
		for(int i : code)
			p = ifs.get(i).map(p);
		
		System.out.println(p + " " + m.map(source));
		assertEquals(p, m.map(source));
	}
	
	@Test
	public void testCode()
	{
		int depth = 6;
		IFS<AffineMap> ifs = IFSs.sierpinski();
		
		for(int i : Series.series(100))
		{
			List<Integer> code = new ArrayList<Integer>(depth);
			for(int j : Series.series(depth))
				code.add(Global.random.nextInt(ifs.size()));
			
			Map m = ifs.compose(code);
			
			assertEquals(code, IFS.code(ifs, m.map(new Point(2)), code.size()));
		}
	}
	
	// @Test
	public void testCode2() throws IOException
	{
		int depth = 5;
		double[] xrange = new double[]{-3.0, 3.0};
		double[] yrange = new double[]{-3.0, 3.0};
		
		Functions.tic();		
		
		BufferedImage image = null;

		File dir = new File("/Users/Peter/Documents/PhD/output/ifs-codes-again");
		dir.mkdirs();

		IFS<Similitude> ifs = IFSs.sierpinskiOffSim();
		image = Draw.draw(ifs.generator(depth), 100000, 1000, true); 
		ImageIO.write(image, "PNG", new File(dir, "ifs.png") );
		
		image = Draw.drawCodes(ifs, xrange, yrange, 200, depth, -1);
		ImageIO.write(image, "PNG", new File(dir, "codes-again.png") );

		
		System.out.println("codes-again: " + Functions.toc() + " seconds");
	}
	
	@Test
	public void testDensities() throws IOException
	{
		Global.random = new Random();
		int depth = 9;
		double[] xrange = new double[]{-2.0, 2.0};
		double[] yrange = new double[]{-2.0, 2.0};
		
		Functions.tic();		
		
		BufferedImage image = null;

		File dir = new File("/Users/Peter/Documents/PhD/output/ifs-densities");
		dir.mkdirs();

		IFS<Similitude> ifs = IFSs.randomSimilitude(2, 2, 0.5);
		
		image = Draw.draw(ifs.generator(depth), 1000000, 1000, false); 
		ImageIO.write(image, "PNG", new File(dir, "ifs.png") );	
		
		image = Draw.drawDensities(ifs, xrange, yrange, 100, depth, false);
		ImageIO.write(image, "PNG", new File(dir, "densities.png") );
		
		image = Draw.drawDensities(ifs, xrange, yrange, 100, depth, true);
		ImageIO.write(image, "PNG", new File(dir, "approximations.png") );

		
		System.out.println("codes-again: " + Functions.toc() + " seconds");
	}	
	
	@Test
	public void testEndpoint()
	{
		IFS<Similitude> ifs = IFSs.sierpinskiSim();
		List<Integer> code = Arrays.asList(1, 2, 0, 2, 0, 1, 1, 1, 0, 2);
		
		Point point = IFS.endpoint(ifs, code);
		//System.out.println(point);
		//System.out.println(code + " " + IFS.code(ifs, point, code.size()));
		assertEquals(code, IFS.code(ifs, point, code.size()));
	}
	
	
}
