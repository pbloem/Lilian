package org.lilian.data.real.fractal;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Draw;
import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
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
			
			Builder<IFS<AffineMap>> builder = IFS.builder(3, AffineMap.builder(2));
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
		
			File dir = new File("/home/peter/Documents/PhD/perturb12HD/");
			dir.mkdirs();
			ImageIO.write(image, "PNG", new File(dir, name + ".png") );
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(name + ": " + Functions.toc() + " seconds");
	}

}
