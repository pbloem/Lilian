package org.lilian.search.evo;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Draw;
import org.lilian.data.real.Point;
import org.lilian.data.real.fractal.IFS;
import org.lilian.data.real.fractal.IFSTarget;
import org.lilian.data.real.fractal.IFSs;
import org.lilian.data.real.fractal.flame.ExampleTarget;
import org.lilian.data.real.fractal.flame.FacticityTarget;
import org.lilian.data.real.fractal.flame.Flame;
import org.lilian.data.real.fractal.flame.MSTarget;
import org.lilian.search.Builder;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class ESFlameTest
{
	private static final int POP_SIZE = 200;	
	private static final double VAR = 0.01;
	private static final int GENERATIONS = 1000;
	
	private static final long STEPS = (long)1E5;
	private static final int XRES = 1920/8;
	private static final int YRES = 1080/8;
	private static final double[] X_RANGE = new double[]{-16.0/9.0, 16.0/9.0};
	private static final double[] Y_RANGE = new double[]{-1.0, 1.0};	
	private static final int OSA = 1;
	private static int K = 2;
	private static boolean WRAP = false;
	
	@Test
	public void testIFS()
	{
		String name = "example_3";
		File dir = new File("/home/peter/Documents/PhD/output/es_flame/" + name + "/");
		dir.mkdirs();
		
		// MSTarget target = new MSTarget(STEPS, XRES, YRES, OSA, WRAP, X_RANGE, Y_RANGE); 
//		FacticityTarget target = new FacticityTarget(STEPS, XRES, YRES, OSA, WRAP, X_RANGE, Y_RANGE);
		
		RenderedImage targetImage = null;
		try
		{
			targetImage = ImageIO.read(new File("/home/peter/Documents/PhD/output/es_flame/example_1/jennie.jpg"));
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ExampleTarget target = new ExampleTarget(targetImage, STEPS, XRES, YRES, OSA, WRAP, X_RANGE, Y_RANGE);
//		System.out.println("target max = " + target.max());
		
		Builder<Flame> builder = Flame.builder(K);
		List<List<Double>> initial = ES.initial(POP_SIZE, builder.numParameters(), VAR);
		
		ES<Flame> es = new ES<Flame>(
				builder, target, initial, 
				2, initial.size() * 2, 0, 
				ES.CrossoverMode.UNIFORM);
		
		Functions.tic();		
		for(int i : Series.series(GENERATIONS))
		{
			es.breed();

			if(true)//i%(GENERATIONS/100) == 0)
			{
				Flame best = es.best().instance();
				
				BufferedImage image = best.draw(STEPS, XRES, YRES, X_RANGE, Y_RANGE, OSA, WRAP);
				try
				{
					ImageIO.write(image, "PNG", new File(dir, String.format("out%04d", i)));
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				
				if(i % (GENERATIONS/25) == 0 && i != 0)
				{
					image = best.draw((long)1e9, 1920, 1080, X_RANGE, Y_RANGE, 3, WRAP);
					try
					{
						ImageIO.write(image, "PNG", new File(dir, String.format("out%04d.big", i)));
					} catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				
				System.out.println("generation " + i + ": " + Functions.toc() + " seconds, fitness:" + es.best().fitness());
				Functions.tic();				
			}	
		}
	}
}
