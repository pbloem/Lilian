package org.lilian.search.evo;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
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
import org.lilian.data.real.fractal.flame.FacticityTarget;
import org.lilian.data.real.fractal.flame.Flame;
import org.lilian.data.real.fractal.flame.MSTarget;
import org.lilian.search.Builder;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class ESFlameTest
{
	private static final int POP_SIZE = 2;	
	private static final double VAR = 0.01;
	private static final int GENERATIONS = 1000;
	
	private static final long STEPS = (long)1E6;
	private static final int XRES = 1920/4;
	private static final int YRES = 1080/4;
	private static final double[] X_RANGE = new double[]{-16.0/9.0, 16.0/9.0};
	private static final double[] Y_RANGE = new double[]{-1.0, 1.0};	
	private static final int OSA = 2;
	private static int K = 2;
	private static boolean WRAP = false;
	
	@Test
	public void testIFS()
	{
		String name = "madrigal_facticity_MS";
		File dir = new File("/home/peter/Documents/PhD/output/es_flame/" + name + "/");
		dir.mkdirs();
		
		MSTarget target = new MSTarget(STEPS, XRES, YRES, OSA, WRAP, X_RANGE, Y_RANGE); 
		// FacticityTarget target = new FacticityTarget(STEPS, XRES, YRES, OSA, WRAP, X_RANGE, Y_RANGE);
		// System.out.println("target max = " + target.max());
		
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

			if(i%(GENERATIONS/100) == 0)
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
				
				System.out.println("generation " + i + ": " + Functions.toc() + " seconds, fitness:" + es.best().fitness());
				Functions.tic();				
			}	
		}
	}
}
