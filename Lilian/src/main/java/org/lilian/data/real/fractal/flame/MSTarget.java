package org.lilian.data.real.fractal.flame;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lilian.search.evo.Target;
import org.lilian.util.Functions;

/**
 * This target is simply the size of the resulting image when compressed in JPEG
 *  
 * @author peter
 *
 */
public class MSTarget implements Target<Flame>
{
	private long steps;
	private int xRes;
	private int yRes;
	private int osa;
	private boolean wrap;
	private double[] xRange;
	private double[] yRange;
	
	public MSTarget(long steps, int xRes, int yRes, int osa, boolean wrap,
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
	}

	@Override
	public double score(Flame flame)
	{
		BufferedImage image = flame.draw(steps, xRes, yRes, xRange, yRange, osa, wrap);
				
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
