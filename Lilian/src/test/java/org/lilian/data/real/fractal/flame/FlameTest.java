//package org.lilian.data.real.fractal.flame;
//
//import static org.junit.Assert.*;
//
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import javax.imageio.ImageIO;
//
//import org.junit.Test;
//import org.lilian.Global;
//import org.lilian.data.real.AffineMap;
//import org.lilian.data.real.Ellipse;
//import org.lilian.data.real.fractal.IFS;
//import org.lilian.data.real.fractal.IFSs;
//import org.lilian.search.Builder;
//import org.lilian.util.Functions;
//import org.lilian.util.Series;
//
//public class FlameTest
//{
////
////	private static final long STEPS = (long)2E9;
////	private static final int XRES = 1920;
////	private static final int YRES = 1080;
////	private static final double[] X_RANGE = new double[]{-16.0/9.0, 16.0/9.0};
////	private static final double[] Y_RANGE = new double[]{-1.0, 1.0};	
////	private static final int OSA = 3;
////	private static int K = 2;
////	private static int FRAMES = 360;
////	private static double VAR = 0.1;
////	private static double VAR_PERTURB = 0.0001;
////	private static boolean WRAP = false;
////	
////	@Test
////	public void testFlame()
////	{
////		String name = "gutenberg7";
////		File dir = new File("/home/peter/Documents/PhD/output/flame/" + name + "/");
////		dir.mkdirs();
////		
////		// long seed = 5692776151140015461l;
////		// long seed = -2654647857798219730l;
////		long seed = new Random().nextLong();
////		System.out.println("seed: " + seed);
////		
////		Global.random = new Random(seed);
////		
////		Builder<Flame> builder = Flame.builder(K);
////		List<Double> change = new ArrayList<Double>(builder.numParameters());
////		List<Double> params = new ArrayList<Double>(builder.numParameters());		
////		
////		for(int i : Series.series(builder.numParameters()))
////		{
////			change.add(0.0);
////			params.add(Global.random.nextDouble() < VAR ? 1.0 : 0.0);			
////		}
////		
////		Flame flame = builder.build(params);
////		for(int i = 0; i < FRAMES; i++)
////		{
////			perturb(change, Math.pow((FRAMES - i)/(double)FRAMES, 10) * VAR_PERTURB);
////			add(params, change);
////			
////			flame = builder.build(params);			
////			
////			Functions.tic();
////			BufferedImage image = flame.draw(STEPS, XRES, YRES, X_RANGE, Y_RANGE, OSA, WRAP);
////			System.out.println(i + ": " + Functions.toc() + "seconds");
////			
////			try
////			{
////				ImageIO.write(image, "PNG", new File(dir, String.format("out%04d.png", i)));
////			} catch (IOException e)
////			{
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
////		}	
////	}
////	
////	// @Test
////	public void testFlameEllipse()
////	{
////		String name = "ellipse4";
////		File dir = new File("/home/peter/Documents/PhD/output/flame/" + name + "/");
////		dir.mkdirs();
////		
////		// long seed = 5692776151140015461l;
////		// long seed = -2654647857798219730l;
////		long seed = new Random().nextLong();
////		System.out.println("seed: " + seed);
////		
////		Global.random = new Random(seed);
////		
////		Builder<Flame> builder = Flame.builder(K);
////		Ellipse ellipse = Ellipse.random(builder.numParameters(), VAR);
////				
////		System.out.println(builder.numParameters());
////		System.exit(0);
////		
////		int i = 0;
////		for(double a = 0.0; a < 2*Math.PI; a += (2*Math.PI)/FRAMES)
////		{
////			Flame flame = builder.build(ellipse.point(a));
////			
////			Functions.tic();
////			BufferedImage image = flame.draw(STEPS, XRES, YRES, X_RANGE, Y_RANGE, OSA, WRAP);
////			System.out.println(i + ": " + Functions.toc() + "seconds");
////			
////			try
////			{
////				ImageIO.write(image, "PNG", new File(dir, String.format("out%04d.png", i)));
////			} catch (IOException e)
////			{
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
////			
////			i++;
////		}	
////	}	
////	
////	private static void add(List<Double> in, List<Double> change)
////	{
////		for(int i : Series.series(in.size()))
////			in.set(i, in.get(i) + change.get(i));
////	}	
////	
////	private static void perturb(List<Double> in, double var)
////	{
////		for(int i : Series.series(in.size()))
////			in.set(i, in.get(i) + Global.random.nextGaussian() * var);
////	}
//
//}
