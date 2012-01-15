package org.lilian.data.real.fractal.flame;

import static java.lang.Math.log;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lilian.Global;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Draw;
import org.lilian.data.real.Point;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.util.Functions;

public class Flame implements Parametrizable  
{
	public static final int INITIAL = 20;
	public static double GAMMA = 2.2;
	
	// * initial point and color
	private Point point;
	private Point color;
	
	private List<Function>  functions = new ArrayList<Function>();
	private List<Double>	weights   = new ArrayList<Double>();
	private double weightSum = 0.0;
	
	public Flame(Function first)
	{
		add(first);
		init();		
	}
	
	public void add(Function function)
	{
		functions.add(function);
		weights.add(function.weight());
		weightSum += function.weight();
	}

	public void init()
	{
		// * initial point and color
		point = new Point(
				Global.random.nextDouble() * 2.0 - 1.0,
				Global.random.nextDouble() * 2.0 - 1.0);			
		color = new Point(3);
	}
	
	public void step()
	{
		// * draw a random function
		Function function = functions.get(Functions.draw(weights, weightSum));
		
		point = function.map(point);
		Point c = function.color();
		
		for(int i = 0; i < 3; i++)
			color.set(i, (color.get(i) + c.get(i)) / 2.0);
	}
	
	/**
	 * 
	 * @param steps The number of samples
	 * @param xRes Horizontal resolution
	 * @param yRes Vertical resolution
	 * @param osa oversampling level
	 * @param wrap Whether to wrap the orbits of the point around so
	 * 		the resulting image will tile. This also means that divergent 
	 * 		attractors will produce images.  
	 * @return
	 */
	public BufferedImage draw(long steps, int xRes, int yRes, double[]xRange, double[] yRange, int osa, boolean wrap)
	{
		//* Create the histogram
		double[][][] histogram = new double[xRes * osa][][];
		
		// * the number of points outside the viewing area
		long outside = 0;
		
		//* Fill the histogram
		for(int i = 0; i < histogram.length; i++)
		{
			histogram[i] = new double[yRes * osa][];			
			for(int j = 0; j < histogram[i].length; j++)
			{
				histogram[i][j] = new double[4];
				for(int k = 0; k < 4; k++) 
					histogram[i][j][k] = 0.0;
			}
		}
		
		for(int i = 0 ; i < INITIAL; i++)
			step();
		for(int i = 0; i < steps; i++)
		{
			step();
			
			int x = Draw.toPixel(point.get(0), xRes * osa, xRange[0], xRange[1]), 
				y = Draw.toPixel(point.get(1), yRes * osa, yRange[0], yRange[1]);
			
			if(wrap)
			{
				if(x >= xRes * osa)	 	
					x = x % (xRes * osa);
				if(x < 0)
					x = (xRes * osa - 1) + (x % (xRes * osa)); 
				if(y >= yRes * osa)
					y = y % (yRes * osa);
				if(y < 0)
					y = (yRes * osa - 1) + (y % (yRes * osa));
				
				for(int c = 0; c < 3; c++)
					histogram[x][y][c] += color.get(c);
				histogram[x][y][3] ++;
				
			} else
			{		
				if(x >= 0 && x < xRes*osa && y >= 0 && y < yRes*osa)
				{
					for(int c = 0; c < 3; c++)
						histogram[x][y][c] += color.get(c);
					histogram[x][y][3] ++;
				} else
					outside++;
			}			
		}
		
		// * Histogram finished, produce the image
		
		double[] avg;
		
		BufferedImage image = 
			new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_RGB);
		
		float[] color;
		double max= 0.0;
		for(int i = 0; i< xRes * osa; i++)
			for(int j = 0; j < yRes * osa; j++)
					max = Math.max(histogram[i][j][3] , max);

		for(int i = 0; i < xRes; i++)
			for(int j = 0; j < yRes; j++)
			{
				color = color(i, j, histogram, osa, max);
				try{
					Color colorObj  = new Color(color[0], color[1], color[2], color[3]);
					image.setRGB(i, yRes - j - 1, colorObj.getRGB());					
				} catch(Exception e)
				{
					System.out.println();
					System.out.println("max:" + max);					
					System.out.println("col: " + Arrays.toString(color));
				}
			}
		
		return image;		
	}
	
	private float[] color(int x, int y, double[][][] histogram, int osa, double max)
	{
		float[] sum = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
		
		int n = 0;
		for(int i = 0; i < osa; i++)
			for(int j = 0; j < osa; j++)
			{
				n++;
				for(int k = 0; k < 4; k++)
					if(x*osa+i < histogram.length && y*osa+j <  histogram[0].length)
						sum[k] += colorSingle(x*osa+i, y*osa+j, k, histogram, max);
			}

		
		for(int c = 0; c < 4; c++)
			sum[c] = sum[c]/(float)n;
		
		return sum;
	}	
	
	private double colorSingle(int i, int j, int k, double[][][] histogram,
			double max)
	{
		if(histogram[i][j][3] == 0.0)
			return 0.0;
		
		double alpha = log(histogram[i][j][3])/log(max);
		
		if(alpha < 0.0 || Double.isNaN(alpha) || Double.isInfinite(alpha))
		{
			System.out.println(histogram[i][j][3]);
			System.out.println(max);
			System.out.println(alpha);
			System.out.println();
		}
	
		// System.out.println(alpha);
		
		// return (histogram[i][j][k] / histogram[i][j][3]) * Math.pow(alpha, 1.0/GAMMA);
		return (histogram[i][j][k] / histogram[i][j][3]) * alpha;		
		// return alpha;
		// return histogram[i][j][3]/max;
		// return 1.0;
	}

	@Override
	public List<Double> parameters() 
	{
		List<Double> p = new ArrayList<Double>();
		for(Function function : functions)
			p.addAll(function.parameters());
		
		return p;
	}
	
	public static Builder<Flame> builder(int k)
	{
		return new FlameBuilder(k);
	}
	
	private static class FlameBuilder implements Builder<Flame>
	{
		// * The number of components
		private int k;
		private Builder<Function> builder = Function.builder();
		
		public FlameBuilder(int k)
		{
			this.k = k;
		}

		@Override
		public Flame build(List<Double> parameters)
		{
			int n = parameters.size();			
			if(n % k != 0)
				throw new IllegalArgumentException("Number of parameters ("+n+") must be a multiple of number of functions ("+k+")");

			int per = n / k;
			Flame flame = null;
			for(int i = 0; i + per <= n; i++)
			{
				Function function = builder.build(parameters.subList(i, i + per));
				if(flame == null)
					flame = new Flame(function);
				else
					flame.add(function);
			}
			
			return flame;
		}

		@Override
		public int numParameters()
		{
			return k * builder.numParameters();
		}
	}

}
