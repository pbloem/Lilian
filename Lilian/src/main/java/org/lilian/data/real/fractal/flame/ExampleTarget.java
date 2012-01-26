package org.lilian.data.real.fractal.flame;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

import org.lilian.search.evo.Target;
import org.lilian.util.Compressor;
import org.lilian.util.GZIPCompressor;
import org.lilian.util.distance.CompressionDistance;

/**
 * Determines the score as the distance to a target image.
 * 
 * @author peter
 */
public class ExampleTarget implements Target<Flame>
{
	private RenderedImage target;
	private JPGCompressor comp = new JPGCompressor();
//	private GZIPCompressor<RenderedImage> comp = new GZIPCompressor<RenderedImage>(1);
	private CompressionDistance<RenderedImage> distance = new CompressionDistance<RenderedImage>(comp);
	
	private long steps;
	private int xRes;
	private int yRes;
	private int osa;
	private boolean wrap;
	private double[] xRange;
	private double[] yRange;	

	public ExampleTarget(RenderedImage target, long steps, int xRes, int yRes, int osa, boolean wrap,
			double[] xRange, double[] yRange)
	{
		this.target = target;
		
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
		BufferedImage rendered = flame.draw(steps, xRes, yRes, xRange, yRange, osa, wrap);
		
		return score(rendered);
	}
	
	
	public double score(RenderedImage rendered)
	{		
		
		return - distance.distance(rendered, target);
	}
	
	private static class JPGCompressor implements Compressor<RenderedImage> 
	{
		GZIPCompressor<String> master = new GZIPCompressor<String>();

		@Override
		public double compressedSize(Object... objects)
		{
			try
			{			
				ByteArrayOutputStream bos = new ByteArrayOutputStream(1);
				for(Object object : objects)
				{
					RenderedImage im = (RenderedImage)object;
					ImageIO.write(im, "JPG", bos);
				}
				
				bos.close();
				
				return master.compressedSize(bos.toString());
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public double ratio(Object... object)
		{
			// TODO Auto-generated method stub
			return 0;
		}
	}

}
