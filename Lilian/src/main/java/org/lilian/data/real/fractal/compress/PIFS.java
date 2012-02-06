package org.lilian.data.real.fractal.compress;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static org.lilian.util.Series.series;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.lilian.Global;


/**
 * An implementation of the simplest form of the PIFS fractal compression 
 * algorithm.
 * 
 * A given image is partitioned into squares of n pixels and squares of m pixels
 * (where m < n so that the first partition is coarser than the second). Then, 
 * the algorithm goes through all fine tiles and finds the coarse tile which is 
 * most similar to it. Each fine tile is then described as the affine 
 * transformation from the coarse to the fine tile.
 * 
 * The image is fully described by only these transformations. Starting with 
 * any image and applying the transformations iteratively will produce an 
 * approximation of the original image.
 * 
 * @author peter
 *
 */
public class PIFS
{

	private static int samples = 0;
	private BufferedImage image;
	
	private int sideHorTo;
	private int sideVerTo;
	
	private int sideHorFrom;
	private int sideVerFrom;
	
	private int delta;
	
	private List<Function> functions = new ArrayList<Function>();
	
	public PIFS(BufferedImage image, int[] sideFrom, int delta, int[] sideTo, int samples)
	{
		super();
		this.image = image;
		
		this.sideHorFrom = sideFrom[0];
		this.sideVerFrom = sideFrom[1];
		
		this.sideHorTo = sideTo[0];
		this.sideVerTo = sideTo[1];
		
		this.delta = delta;
		
		this.samples = samples;
	}

	public static List<Block> blocks(BufferedImage image, int sideHor, int sideVer, int delta)
	{
		return new OverlappingBlockList(image, sideHor, sideVer, delta);
	}
	
	public static List<Block> blocks(BufferedImage image, int sideHor, int sideVer)
	{		
		return new BlockList(image, sideHor, sideVer);
	}
	
	public BufferedImage reconstruct(int iterations, BufferedImage initial)
	{
		return reconstruct(iterations, initial, null);
	}
	
	public BufferedImage reconstruct(int iterations, BufferedImage initial, File dir)
	{
		BufferedImage result = initial, next;
		
		int width = initial.getWidth();
		int height = initial.getHeight();
		
		for(int i = 0; i < iterations; i++)
		{
			if(dir != null)
			{
				File file = new File(dir, String.format("out%03d.bmp", i));
				try	{
					ImageIO.write(result, "BMP", file);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			
			next = new BufferedImage(width, height, result.getType());
			
			Graphics2D g = next.createGraphics();
		    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			
			for(Function function : functions)
			{
				BufferedImage tile = 
						transform(
							result,
							function.from()[0], function.from()[1], function.from()[2],	
							function.red(), function.green(), function.blue()); 

				g.drawImage(tile, function.to().x(), function.to().y(), null);
			}
			g.dispose();
			
			result = next;
		}
		
		return result;
	}
	
	private BufferedImage transform(
			BufferedImage image, 
			Block redBlock,	Block greenBlock, Block blueBlock, 
			Transform red, Transform green, Transform blue)
	{
		int w = sideHorTo;
		int h = sideVerTo;
		
		BufferedImage result = 
				new BufferedImage(w, h, this.image.getType());
		
		BufferedImage redFrom   = image.getSubimage(redBlock.x(),   redBlock.y(),   sideHorFrom, sideVerFrom),
		              greenFrom = image.getSubimage(greenBlock.x(), greenBlock.y(), sideHorFrom, sideVerFrom),
		              blueFrom  = image.getSubimage(blueBlock.x(),  blueBlock.y(),  sideHorFrom, sideVerFrom);

	
		
		redFrom   = scale(sideHorTo, sideVerTo, redFrom);
		greenFrom = scale(sideHorTo, sideVerTo, greenFrom);
		blueFrom  = scale(sideHorTo, sideVerTo, blueFrom);
		
		for(int i = 0; i < w; i++)
			for(int j = 0; j < h; j++)
			{
				Color redColor   = new Color(redFrom.getRGB(i, j));
				Color greenColor = new Color(greenFrom.getRGB(i, j));
				Color blueColor  = new Color(blueFrom.getRGB(i,  j));
				
				float  r = redColor.getRed()   / 255.0f, 
				       g = greenColor.getGreen() / 255.0f, 
				       b = blueColor.getBlue()  / 255.0f;
				
				r = r * red.contrast()   + red.brightness();
				g = g * green.contrast() + green.brightness();
				b = b * blue.contrast()  + blue.brightness();
				
				Color color = new Color(sq(r), sq(g), sq(b));
				
				result.setRGB(i, j, color.getRGB());
			}
		
		return result;
	}

	public void search()
	{
		functions.clear();
		File dir = new File("/Users/Peter/Documents/PhD/output/pifs/tiles/");
		dir.mkdirs();
		
		for(PIFS.Block to : PIFS.blocks(image, sideHorTo, sideVerTo))
		{
			Block[] best = new Block[]{null, null, null};
			Transform[] bestTransform = new Transform[]{null, null, null};
			double[] di = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
			
			for(PIFS.Block from : PIFS.blocks(image, sideHorFrom, sideVerFrom, delta))
			{
				Transform[] current = new Transform[]{
						new Transform(from.tile(), to.tile(), Channel.RED),
						new Transform(from.tile(), to.tile(), Channel.GREEN),
						new Transform(from.tile(), to.tile(), Channel.BLUE)};				
												
				for(int i = 0; i < 3; i++)
					if(current[i].error() < di[i])
					{
						di[i] = current[i].error();
						best[i] = from;
						bestTransform[i] = current[i];
					}
			}
			
			System.out.println("* " + Arrays.toString(di));

			functions.add(new Function(
					bestTransform[0], bestTransform[1], bestTransform[2], best, to));
			
			BufferedImage res = comparison(to, best, bestTransform);

			try
			{
				File file = new File(dir, 
						String.format("tile%04d.%04d.png", to.x(), to.y()));
				ImageIO.write(res, "PNG", file);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	private static class BlockList extends AbstractList<Block>
	{
		private int sideHor;
		private int sideVer;
		private BufferedImage master;
		
		private int i = 0; // horizontal counter
		private int j = 0; // vertical counter
		
		private int iMax;
		private int jMax;
		
		private int width, height;
				
		public BlockList(BufferedImage master, int sideHor, int sideVer)
		{
			super();
			this.sideHor = sideHor;
			this.sideVer = sideVer;
			this.master = master;
			
			width = master.getWidth(null);
			height = master.getHeight(null);
			
			if(width % sideHor != 0)
				throw new IllegalArgumentException("Horizontal block size ("+sideHor+") should divide image width ("+width+").");
			if(height % sideVer != 0)
				throw new IllegalArgumentException("Vertical block side ("+sideVer+") should divide image height ("+height+").");
			
			iMax = (width/sideHor) - 1;
			jMax = (height/sideVer) - 1;			
		}

		@Override
		public Block get(int index)
		{
			int x = sideHor * i, y = sideVer * j;
			BufferedImage tile = master.getSubimage(x, y, sideHor, sideVer);
			Block block = new Block(master, tile, x, y);
			
			increment();
			
			return block;
		}
		
		/**
		 * Increments the counters for the current block
		 */
		private void increment()
		{
			i++;
			if(i > iMax)
			{
				i = 0;
				j++;
			}
		}

		@Override
		public int size()
		{
			return (jMax + 1) * (iMax + 1);
		}
	}
	
	private static class OverlappingBlockList extends AbstractList<Block>
	{
		private int sideHor;
		private int sideVer;
		private int delta;
		
		private BufferedImage master;
		
		private int x = 0; // horizontal counter
		private int y = 0; // vertical counter
		
		private int xMax;
		private int yMax;
		
		private int width, height;
				
		public OverlappingBlockList(BufferedImage master, int sideHor, int sideVer, int delta)
		{
			super();
			this.delta = delta;
			
			this.sideHor = sideHor;
			this.sideVer = sideVer;
			this.master = master;
			
			width = master.getWidth(null);
			height = master.getHeight(null);
			
			if(width % sideHor != 0)
				throw new IllegalArgumentException("Horizontal block size ("+sideHor+") should divide image width ("+width+").");
			if(height % sideVer != 0)
				throw new IllegalArgumentException("Verticak block side ("+sideVer+") should divide image height ("+height+").");
			
			xMax = width - sideHor;
			yMax = height - sideVer;			
		}

		@Override
		public Block get(int index)
		{
			BufferedImage tile = master.getSubimage(x, y, sideHor, sideVer);
			Block block = new Block(master, tile, x, y);
			
			increment();
			
			return block;
		}
		
		/**
		 * Increments the counters for the current block
		 */
		private void increment()
		{
			x += delta;
			if(x > xMax)
			{
				x = 0;
				y += delta;
			}
		}

		@Override
		public int size()
		{
			return (int)floor(
					ceil(((xMax + 1)/(double)delta)) * 
					ceil(((yMax + 1)/(double)delta)));
		}
	}
	
	/**
	 * The optimal brightness/contrast transform between to images, and the 
	 * resulting error measure
	 * 
	 * @author peter
	 */
	public static class Transform 
	{
		private Channel channel;
		
		BufferedImage from;
		BufferedImage to;
		
		// * various averages
		private double f  = 0, 
				       t  = 0,
				       ff = 0, 
				       ft = 0, 
				       tt = 0;
		
		// * number of pixels
		private double n;
		
		public Transform(BufferedImage from, BufferedImage to, Channel channel)
		{
			
			this.channel = channel;
			
			if(from.getWidth() == to.getWidth() && from.getHeight() == to.getHeight())
			{
				this.from = from;
				this.to = to;
			} else
			{
			
				// * Find the number of pixels for each and determine the 
				//   largest image				
				int sizeFrom = from.getWidth() * from.getHeight();
				int sizeTo = to.getWidth() * to.getHeight();
				
				if(sizeFrom > sizeTo)
				{
					this.from = scale(to.getWidth(), to.getHeight(), from);
					this.to = to;
					
				} else {
					this.from = from;
					this.to = scale(from.getWidth(), from.getHeight(), to);				
				}
			}
			
			n = this.to.getWidth() * this.to.getHeight();				
			
			if(samples == 0)
			{
				// * calculate the averages
				for(int i = 0; i < this.from.getWidth(); i++)
					for(int j = 0; j < this.from.getHeight(); j++)
					{
						Color pixelFrom = new Color(this.from.getRGB(i, j));
						Color pixelTo = new Color(this.to.getRGB(i, j));
						
						double channelFrom = channel(pixelFrom) / 255.0; 
						double channelTo = channel(pixelTo) / 255.0;
						
						f += channelFrom;
						t += channelTo;
						tt += channelTo * channelTo;
						ft += channelTo * channelFrom;
						ff += channelFrom * channelFrom;
					}
				
				f = f/n;
				t = t/n;
				tt = tt/n;
				ft = ft/n;
				ff = ff/n;
			} else
			{
				// * estimate the averages
				for(int c = 0; c < samples; c++)
				{
					int i = Global.random.nextInt(this.from.getWidth()),
					    j = Global.random.nextInt(this.from.getHeight());
					
					Color pixelFrom = new Color(this.from.getRGB(i, j));
					Color pixelTo = new Color(this.to.getRGB(i, j));
					
					double channelFrom = channel(pixelFrom) / 255.0; 
					double channelTo = channel(pixelTo) / 255.0;
					
					f += channelFrom;
					t += channelTo;
					tt += channelTo * channelTo;
					ft += channelTo * channelFrom;
					ff += channelFrom * channelFrom;
				}
				
				f = f/(double)samples;
				t = t/(double)samples;
				tt = tt/(double)samples;
				ft = ft/(double)samples;
				ff = ff/(double)samples;
			}
		}
		
		public BufferedImage from()
		{
			return from;
		}

		public BufferedImage to()
		{
			return to;
		}

		public float contrast()
		{
			double cov = ft - f * t;
			double var = ff - f * f;
			
			if(Math.abs(var) < 10E-10)
				return 0.0f;
			
			return (float)(cov/var);
		}
		
		public float brightness()
		{
			return (float)(t - contrast() * f);
		}
		
		public double error()
		{
			double s = contrast();
			double o = brightness();
			
			return o*n*n     + 2.0*s*o*n*f +
			       2.0*o*n*t + s*s*n*ff +
			       2*s*n*ft  + n*tt;
		}
		
		private double channel(Color color)
		{
			if(channel == Channel.BLUE)
				return color.getBlue();
			
			if(channel == Channel.GREEN)
				return color.getGreen();
			
			return color.getRed();
		}
	}
	
	public class Function 
	{
		Transform red;
		Transform green;
		Transform blue;
		
		Block[] from;
		Block to;
		int sideFrom;
		int sideTo;
		public Function(Transform red, Transform green, Transform blue,
				Block[] from, Block to)
		{
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.from = from;
			this.to = to;
		}
		
		public Transform red()
		{
			return red;
		}
		public Transform green()
		{
			return green;
		}
		public Transform blue()
		{
			return blue;
		}
		public Block[] from()
		{
			return from;
		}
		public Block to()
		{
			return to;
		}
	}
	
	/**
	 * Stitches together from, to and the transformed from.
	 *  
	 * @return
	 */
	public BufferedImage comparison(Block to, Block[] from, 
			Transform[] transforms)
	{
		int w = to.tile().getWidth();
		int h = to.tile().getHeight();
		
		BufferedImage image = new BufferedImage(
				w*5, h, 
				BufferedImage.TYPE_INT_RGB);
		
		Image fin = transform(this.image, from[0], from[1], from[2], 
				transforms[0], transforms[1], transforms[2]);		
				
		Graphics2D graphics = image.createGraphics();

		graphics.drawImage(to.tile(),
				0, 0, w, h, 
				0, 0, w, h, null);
		graphics.drawImage(from[0].tile(), 
				w, 0, w*2, h,
				0, 0, sideHorFrom, sideVerFrom, null);
		graphics.drawImage(from[1].tile(), 
				w*2, 0, w*3, h,  
				0, 0, sideHorFrom, sideVerFrom, null);
		graphics.drawImage(from[2].tile(), 
				w*3, 0, w*4, h, 
				0, 0, sideHorFrom, sideVerFrom, null);

		graphics.drawImage(fin, w*4, 0, null);			
		
		graphics.dispose();	
		
		return image;			
	}	
	
	private static float sq(float in)
	{
		if(in > 1.0)
			return 1.0f;
		if(in < 0.0)
			return 0.0f;
		return in;		
	}
	
	private static BufferedImage scale(int width, int height, BufferedImage original)
	{
		BufferedImage resized = new BufferedImage(width, height, original.getType());
		
	    Graphics2D g = resized.createGraphics();
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	    
	    g.drawImage(original, 0, 0, width, height, 0, 0, original.getWidth(), original.getHeight(), null);
	    g.dispose();
	    
	    return resized;
	}	
	
	public static class Block
	{
		private BufferedImage master;
		private BufferedImage tile;
		private int x, y;
		
		public Block(BufferedImage master, BufferedImage tile, int x, int y)
		{
			super();
			this.master = master;
			this.tile = tile;
			this.x = x;
			this.y = y;
		}
		public BufferedImage master()
		{
			return master;
		}
		public BufferedImage tile()
		{
			return tile;
		}
		public int x()
		{
			return x;
		}
		public int y()
		{
			return y;
		}
		@Override
		public String toString()
		{
			return "["+x+", "+y+"]";
		}
		
		
	}

	public enum Channel {RED, GREEN, BLUE}
}
