package org.lilian.data.real.fractal.compress;

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
	private BufferedImage image;
	private int side;
	private List<Function> functions = new ArrayList<Function>();
	
	public PIFS(BufferedImage image, int side)
	{
		super();
		this.image = image;
		this.side = side;
	}

	public static List<Block> blocks(BufferedImage image, int side)
	{
		return new BlockList(image, side);
	}
	
	public void search()
	{
		File dir = new File("/home/peter/Documents/PhD/output/pifs2/");
		dir.mkdirs();
		
		for(PIFS.Block to : PIFS.blocks(image, side/2))
		{
			Transform[] bestTransform = new Transform[]{};
			double di = Double.POSITIVE_INFINITY;
			
			for(PIFS.Block from : PIFS.blocks(image, side))
			{
				Transform red   = new Transform(from.tile(), to.tile(), Transform.Channel.RED);
				Transform green = new Transform(from.tile(), to.tile(), Transform.Channel.GREEN);
				Transform blue  = new Transform(from.tile(), to.tile(), Transform.Channel.BLUE);				
				
				double d = red.error() + green.error() + blue.error();
				
				if(d < di)
				{
					di = d;
					bestTransform = new Transform[] {red, green, blue};
				}
			}
			System.out.println("* " + di);
			
			BufferedImage res = comparison(bestTransform[0], bestTransform[1], bestTransform[2]);

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
		private int side;
		private BufferedImage master;
		
		private int i = 0; // horizontal counter
		private int j = 0; // vertical counter
		
		private int iMax;
		private int jMax;
		
		private int width, height;
				
		public BlockList(BufferedImage master, int side)
		{
			super();
			this.side = side;
			this.master = master;
			
			width = master.getWidth(null);
			height = master.getHeight(null);
			
			if(width % side != 0)
				throw new IllegalArgumentException("Block side ("+side+") should divide image width ("+width+").");
			if(height % side != 0)
				throw new IllegalArgumentException("Block side ("+side+") should divide image height ("+height+").");
			
			
			iMax = (width/side) - 1;
			jMax = (height/side) - 1;			
		}

		@Override
		public Block get(int index)
		{
			int x = side * i, y = side * j;
			BufferedImage tile = master.getSubimage(x, y, side, side);
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
	
	/**
	 * The optimal brightness/contrast transform between to images, and the 
	 * resulting error measure
	 * 
	 * @author peter
	 */
	public static class Transform 
	{
		public enum Channel {RED, GREEN, BLUE};
		
		private Channel channel;
		
		BufferedImage from;
		BufferedImage to;
		
		// * various sums
		private double f  = 0, 
				       t  = 0,
				       ff = 0, 
				       ft = 0, 
				       tt = 0;
		
		// * number of pixels
		private double n;
		
		public Transform(BufferedImage from, BufferedImage to, Channel channel)
		{
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
			
			// * calculate the sums
			for(int i = 0; i < this.from.getWidth(); i++)
				for(int j = 0; j < this.from.getHeight(); j++)
				{
					Color pixelFrom = new Color(this.from.getRGB(i, j));
					Color pixelTo = new Color(this.to.getRGB(i, j));
					
					double channelFrom = channel(pixelFrom)/255.0; 
					double channelTo = channel(pixelTo)/255.0;
					
					f += channelFrom;
					t += channelTo;
					tt += channelTo * channelTo;
					ft += channelTo * channelFrom;
					ff += channelFrom * channelFrom;
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
			double num = n*n * ft - f * t;
			double den = n*n * ff - f * f;
			
			if(den == 0.0)
				return 0.0f;
			
			return (float)(num/den);
		}
		
		public float brightness()
		{
			return (float)( (t - contrast() * f) / (n*n) );		
		}
		
		public double error()
		{
			double s = contrast();
			double o = brightness();
			
			double a = s * (s*ff - 2.0*ft + 2.0*o*f);
			double b = o * (o*n*n - 2.0*t);
			
			return (tt + a + b) / (n*n);
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
		
		int[] from;
		int[] to;
		int sideFrom;
		int sideTo;
		public Function(Transform red, Transform green, Transform blue,
				int[] from, int[] to, int sideFrom, int sideTo)
		{
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.from = from;
			this.to = to;
			this.sideFrom = sideFrom;
			this.sideTo = sideTo;
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
		public int[] from()
		{
			return from;
		}
		public int[] to()
		{
			return to;
		}
		public int sideFrom()
		{
			return sideFrom;
		}
		public int sideTo()
		{
			return sideTo;
		}
	}
	
	/**
	 * Stitches together from, to and the transformed from.
	 *  
	 * @return
	 */
	public BufferedImage comparison(Transform red, Transform green, Transform blue)
	{
		BufferedImage from = red.from(),
		              to = red.to();
		
		
		BufferedImage image = new BufferedImage(
				from.getWidth(), 
				from.getHeight() * 3, 
				BufferedImage.TYPE_INT_RGB);
		
		Graphics2D graphics = image.createGraphics();
		
		graphics.drawImage(from, 0, 0, null);
		graphics.drawImage(to, 0, from.getHeight(), null);
		graphics.drawImage(transform(from, red, green, blue), 0, from.getHeight() * 2, null);			
		
		graphics.dispose();	
		
		return image;			
	}	
	
	private static BufferedImage transform(BufferedImage from, Transform red, Transform green, Transform blue)
	{
		int w = from.getWidth();
		int h = from.getHeight();
		
		BufferedImage result = 
				new BufferedImage(w, h, from.getType());
		
		for(int i = 0; i < w; i++)
			for(int j = 0; j < h; j++)
			{
				Color color = new Color(from.getRGB(i, j));
				
				float  r = color.getRed()   / 255.0f, 
				       g = color.getGreen() / 255.0f, 
				       b = color.getBlue()  / 255.0f;
				
				r = r * red.contrast() + red.brightness();
				g = g * green.contrast() + green.brightness();
				b = b * blue.contrast() + blue.brightness();
				
				color = new Color(sq(r), sq(g), sq(b));
				
				result.setRGB(i, j, color.getRGB());
			}
		
		return result;
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
}
