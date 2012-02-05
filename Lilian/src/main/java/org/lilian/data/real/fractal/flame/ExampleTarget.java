package org.lilian.data.real.fractal.flame;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.lilian.Global;
import org.lilian.search.evo.Target;
import org.lilian.util.Compressor;
import org.lilian.util.GZIPCompressor;
import org.lilian.util.distance.CompressionDistance;
import org.lilian.util.distance.Distance;

/**
 * Determines the score as the distance to a target image.
 * 
 * @author peter
 */
public class ExampleTarget implements Target<Flame>
{
	private RenderedImage target;
	private JPGCompressor comp = new JPGCompressor();
	// private GZIPCompressor<RenderedImage> comp = new GZIPCompressor<RenderedImage>(1);
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
					ImageIO.write(im, "TIFF", bos);
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
	
	public static class IMGDistance implements Distance<BufferedImage>
	{
		private static final long serialVersionUID = 2514452650129731621L;
		private ImageWriter writer = ImageIO.getImageWritersBySuffix("jpeg").next();
		private int bufferSize = 640000000;
		
		public IMGDistance()
		{
			// ImageWriteParam iwp = writer.getDefaultWriteParam();
			
			// iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			// iwp.setCompressionQuality(1.0f);
		}
		
		@Override
		public double distance(BufferedImage a, BufferedImage b)
		{
			double 	cab = size(a, b),
					ca  = size(a),
					cb  = size(b);
			
			double big, small;
			if(ca > cb)
			{
				big = ca;
				small = cb;
			} else
			{
				big = cb;
				small = ca;
			}
			
			return (cab - small) / big; 			
		}
		
		private int size(BufferedImage im)
		{
			try
			{			
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				GZIPOutputStream goz = new GZIPOutputStream(bos, bufferSize);
				ImageOutputStream ios = new MemoryCacheImageOutputStream(goz);
				
				writer.setOutput(ios);
				writer.write(im);

				ios.close();
				goz.finish();
				goz.close();
				
				return bos.size();
				
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}			
		}
		
		private int size(BufferedImage a, BufferedImage b)
		{
			try
			{			
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				GZIPOutputStream goz = new GZIPOutputStream(bos, bufferSize);
				ImageOutputStream ios = new MemoryCacheImageOutputStream(goz);				
				
				writer.setOutput(ios);
				
				BufferedImage image = new BufferedImage(
						a.getWidth(), 
						a.getHeight() * 2, 
						BufferedImage.TYPE_INT_RGB);
				
				Graphics2D graphics = image.createGraphics();
				
				graphics.drawImage(a, 0, 0, null);
				graphics.drawImage(b, 0, a.getHeight(), null);				
				
				graphics.dispose();	
				
				writer.write(image);				
				
				ImageIO.write(image, "BMP", 
						new File("/home/peter/Documents/PhD/output/es_flame/example_1/stitched_"+Global.random.nextInt(200)+".bmp"));
				
				ios.close();
				goz.finish();
				goz.close();
				
				return bos.size();
				
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}			
		}		
		
		
	
	}
}
