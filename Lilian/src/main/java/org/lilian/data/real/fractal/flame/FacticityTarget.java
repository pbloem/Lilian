package org.lilian.data.real.fractal.flame;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.lilian.Global;
import org.lilian.search.evo.Target;
import org.lilian.util.Functions;

/**
 * This target is simply the size of the resulting image when compressed in JPEG
 *  
 * @author peter
 *
 */
public class FacticityTarget implements Target<Flame>
{
	private long steps;
	private int xRes;
	private int yRes;
	private int osa;
	private boolean wrap;
	private double[] xRange;
	private double[] yRange;
	
	private double max;
	
	public FacticityTarget(long steps, int xRes, int yRes, int osa, boolean wrap,
			double[] xRange, double[] yRange)
	{
		super();
		this.steps = steps;
		this.xRes = xRes;
		this.yRes = yRes;
		this.osa = osa;
		this.wrap = wrap;
		this.xRange = xRange;
		this.yRange = yRange;
		
		// * Calculate max (The size of a completely random image)
		BufferedImage image = 
			new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_RGB);

		for(int i = 0; i < xRes; i++)
			for(int j = 0; j < yRes; j++)
			{
				Color colorObj  = new Color(Global.random.nextFloat(), Global.random.nextFloat(), Global.random.nextFloat(), Global.random.nextFloat());
				image.setRGB(i, yRes - j - 1, colorObj.getRGB());					
			}
		max = size(image);
	}

	@Override
	public double score(Flame flame)
	{
		double c = size(flame.draw(steps, xRes, yRes, xRange, yRange, osa, wrap));
		
		double entropy = c / max; // * normalized entropy
		double rd = (max - c)/ max; // * normalized randomness deficiency
		
		return 4.0 * entropy * rd;		
	}
	
	public double max()
	{
		return max;
	}
	
	public static int size(BufferedImage image)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(1); 
		try
		{
			ImageIO.write(image, "JPG", out);
		} catch (IOException e)
		{
			// * Doesn't happen since we're writing to ByteArrayoutputStream 
		}
		
		return out.toByteArray().length;
	}
}
