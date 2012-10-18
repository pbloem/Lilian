package org.lilian.data.real;

import static org.lilian.util.Series.series;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;

import org.lilian.Global;
import org.lilian.util.Pair;
import org.lilian.util.Series;
import org.lilian.util.distance.SquaredEuclideanDistance;

import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Static helper functions on lists of points.
 * 
 * @author Peter
 */
public class Datasets
{
	public static double distance(Point point, List<Point> data)
	{
		double d = Double.POSITIVE_INFINITY;
		for(Point p : data)
			d = Math.min(d, SquaredEuclideanDistance.dist(point, p));
		
		return Math.sqrt(d);
	}
	
	public static double distance(Point point, List<Point> data, int sample)
	{
		double d = Double.POSITIVE_INFINITY;
		for(int i = 0; i < sample; i++)
		{
			Point p = data.get(Global.random.nextInt(data.size()));
			d = Math.min(d, SquaredEuclideanDistance.dist(point, p));
		}
		
		return Math.sqrt(d);
	}
	
	/**
	 * Sample n elements randomly with replacement
	 * 
	 * @return
	 */
	public static <P> List<P> sample(List<P> data, int n)
	{
		List<P> res = new ArrayList<P>(n);
		
		for(int i : series(n))
			res.add(data.get(Global.random.nextInt(data.size())));
		
		return res;
	}
	
	/**
	 * Sample n elements randomly without replacement
	 * 
	 * @return
	 */
	public static <P> List<P> sampleWithoutReplacement(List<P> data, int n)
	{
		
		// *  Shuffle the list and copy the first n items
		List<P> res = new ArrayList<P>(data);
		Collections.shuffle(res);
		
		// * Copy the sublist over, so we the gc can clean up the shuffled list.
		List<P> res2 = new ArrayList<P>(res.subList(0, n));
		
		return res2;
	}
	
	/**
	 * Creates a generator for random points on the bi-unit cube of the given 
	 * dimension
	 */
	public static Generator<Point> cube(int dimension)
	{
		return new Cube(dimension);
	}
	
	private static class Cube extends AbstractGenerator<Point>
	{
		protected int dim;
		
		public Cube(int dim)
		{
			this.dim = dim;
		}

		public Point generate()
		{
			double[] vector = new double[dim];
			for(int i = 0; i < dim; i++)
				vector[i] = Global.random.nextDouble()*2.0 - 1.0;
			
			return new Point(vector);
		}
	}
	
	/**
	 * The swiss roll: Four MVNs in 2D mapped to 3d with a smooth spiralling 
	 * function
	 * 
	 * From this description: http://people.cs.uchicago.edu/~dinoj/manifold/swissroll.html
	 */
	public static Generator<Point> swiss()
	{
		return new Swiss(0.0);
	}

	public static Generator<Point> swiss(double noise)
	{
		return new Swiss(noise);
	}
	
	private static class Swiss extends AbstractGenerator<Point>
	{		
		List<MVN> mvns = new ArrayList<MVN>();
		MVN noise;
		
		public Swiss(double noise)
		{
			this.noise = noise == 0.0 ? null : new MVN(3, noise);
			
			mvns.add(new MVN(new Point( 7.5,   7.5), 1.0));
			mvns.add(new MVN(new Point( 7.5,  12.5), 1.0));
			mvns.add(new MVN(new Point(12.5,   7.5), 1.0));
			mvns.add(new MVN(new Point(12.5,  12.5), 1.0));
			
		}

		public Point generate()
		{
			int i = Global.random.nextInt(mvns.size());
	
			// * 2D point
			Point p = mvns.get(i).generate();

			if(noise == null)
				return new Point(
					p.get(0) * Math.cos(p.get(0)),
					p.get(1),
					p.get(0) * Math.cos(p.get(0)));
			
			Point n = noise.generate();
			return new Point(
					p.get(0) * Math.cos(p.get(0)) + n.get(0),
					p.get(1) + n.get(1),
					p.get(0) * Math.cos(p.get(0)) + n.get(2));
		}
	}	

	public static Generator<Point> addNoise(Generator<Point> base, double var)
	{
		return new Noisy(base, var);
	}
	
	private static class Noisy extends AbstractGenerator<Point> 
	{
		Generator<Point> master;
		MVN noise;

		public Noisy(Generator<Point> master, double var)
		{
			this.master = master;
			noise = new MVN(master.generate().dimensionality(), var);
		}

		@Override
		public Point generate()
		{
			Point base = master.generate();
			Point n = noise.generate();
			double [] b = base.getBackingData();
			for(int i = 0; i < b.length; i++)
				b[i] += n.get(i);
			
			return Point.fromRaw(b);
		}
	}
	
	/**
	 * 
	 * tMin = -45, tMax =45 
	 * 
	 * @param tMin
	 * @param tMax
	 * @return
	 */
	public static Generator<Point> spiral(double tMin, double tMax)
	{
		return new LogSpiral(tMin, tMax);
	}	
	
	public static class LogSpiral extends AbstractGenerator<Point>
	{
		double tMin, tMax;

		public LogSpiral(double tMin, double tMax)
		{
			super();
			this.tMin = tMin;
			this.tMax = tMax;
		}

		@Override
		public Point generate()
		{
			double t = Global.random.nextDouble();
			t = t * (tMax - tMin) + tMin;
			
			
			double x = Math.exp(0.1 * t) * Math.cos(t),
			       y = Math.exp(0.1 * t) * Math.sin(t);
			
			return new Point(x, y);
		}
	}
	
	/**
	 * Three small MVNs in R^2
	 */
	public static Generator<Point> three()
	{
		return new Three();
	}
	
	private static class Three extends AbstractGenerator<Point>
	{		
		List<MVN> mvns = new ArrayList<MVN>();
		
		public Three()
		{
			
			mvns.add(new MVN(new Point(-0.5, -0.5), 0.1));
			mvns.add(new MVN(new Point(-0.5,  0.5), 0.1));
			mvns.add(new MVN(new Point( 0.0,  0.5), 0.1));
			
		}

		public Point generate()
		{
			int i = Global.random.nextInt(3);
	
			return mvns.get(i).generate();
		}
	}
	
	/**
	 * Creates a generator for random points on a sphere of the given dimension 
	 * with radius 1.0
	 */
	public static Generator<Point> sphere(int dimension)
	{
		return new Sphere(dimension, 1.0);
	}
	

	/**
	 * Creates a generator for random points on a sphere of the given dimension 
	 * and radius
	 */
	public static Generator<Point> sphere(int dimension, double radius)
	{
		return new Sphere(dimension, radius);
	}	
	
	private static class Sphere extends AbstractGenerator<Point>
	{
		protected int dim;
		protected double radius;
		
		public Sphere(int dim, double radius)
		{
			this.dim = dim;
			this.radius = radius;
		}

		public Point generate()
		{
			// ** Draw a vector with standard normal random entries 
			double[] vector = new double[dim];
			for(int i = 0; i < dim; i++)
				vector[i] = Global.random.nextGaussian();
			
			
			double length = 0;
			for(int i = 0; i < dim; i++)
				length += vector[i]*vector[i];
			length = Math.sqrt(length);
			
			// ** Normalize it (the distribution is now uniform over the
			//    unit sphere)
			if(length != 0.0)
				for(int i = 0; i < dim; i++)
					vector[i] /= length;
			
			// ** Extend to the required radius
			if(radius != 1.0)
				for(int i = 0; i < dim; i++)
					vector[i] *= radius;
			
			return new Point(vector);
		}
	}
	
	public static Generator<Point> ball(int dim, double radius)
	{
		return new Ball(dim, radius);
	}	
	
	
	public static Generator<Point> ball(int dim)
	{
		return new Ball(dim, 1.0);
	}	
	
	private static class Ball extends Sphere
	{
		double ballRadius;
		public Ball(int dim, double radius)
		{
			super(dim, 1.0);
			this.ballRadius = radius;
		}

		public Point generate()
		{
			// * draw a random point on the unit sphere
			Point point = super.generate();
			double[] p = point.getBackingData();
			
			// * multiply it by a unirandom point from the unit interval
			//   raised to the power of 1/d
			double r = Global.random.nextDouble();
			for(int i = 0; i < dim; i++)
				p[i] *= Math.pow(r, 1.0/dim);
			
			// ** Extend to the required radius
			if(ballRadius != 1.0)
				for(int i = 0; i < dim; i++)
					p[i] *= ballRadius;
			
			return new Point(p);
		}
	}
	
	public static Generator<Point> mandelbrot()
	{
		return new Mandelbrot();
	}
	
	private static class Mandelbrot extends AbstractGenerator<Point>
	{
		
		
	    /**
	     * @param point
	    * @return
	    */
		public static boolean inM(double x0, double y0)
		{
			double range = 10.0, topRange = 100000.0;
			int steps = 1000;
			
			double x = x0, y = y0;
			double xp, yp;
			
			for(int i = 0; i < steps; i ++)
			{
				xp = x*x - y*y + x0;
				yp = 2*x*y + y0;
	
				x = xp; y = yp;
				
				if(x*x + y*y > topRange*topRange)
					break;
			}
			
			if(x*x + y*y > range * range)
				return false;
			return true;
		}

		@Override
		public Point generate()
		{
			double x = 0.0, y = 0.0;
			
			boolean rejected = true;
			while(rejected)
			{
				x = Global.random.nextDouble() * 3.0 - 2.0;
				y = Global.random.nextDouble() * 2.0 - 1.0;
				
				rejected = ! inM(x, y);
			}			
			
			return new Point(x+0.5, y);
		}
	}
	
	/**
	 * Reads a CSV file containing numerical values into a list of points.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static List<Point> readCSV(File file) throws IOException
	{
		List<Point> data = new ArrayList<Point>();
		
	    CSVReader reader = new CSVReader(new FileReader(file));
	    String [] nextLine;
	    while ((nextLine = reader.readNext()) != null) 
	    {
	    	double[] values = new double[nextLine.length];
	    	for(int i = 0; i < nextLine.length; i++)
	    		values[i] = Double.parseDouble(nextLine[i]);
	    	data.add(new Point(values));
	    }
	    
	    return data;
	}
	
	/**
	 * Reads an image file and flattens it into a vector of double values
	 * @param imageFile
	 * @param gray Whether to flatten any color values to a single gray (true)
	 *  or to concatenate the three separate channel images into a single vector
	 * @return
	 * @throws IOException 
	 */
	public static Point readImage(File imageFile, boolean gray) throws IOException
	{
		BufferedImage image = read(imageFile);
	
		int width = image.getWidth();
		int height = image.getHeight();

		
		double[] values;
		
		if(gray) {
			values = new double[width * height];
			
			int h = 0;
			for(int i = 0; i < width; i++)
				for(int j = 0; j < height; j++)
				{

					Color color = new Color(image.getRGB(i, j));
					double value = color.getRed() + color.getGreen() + color.getBlue();
					value = value / (255.0 * 3.0);
					values[h] = value; 
					
					h++;
				}
		} else 
		{
			values = new double[width * height * 3];
			
			int h = 0;
			for(int c = 0; c < 3; c++)
			{
				for(int i = 0; i < width; i++)
					for(int j = 0; j < height; j++)
					{
						Color color = new Color(image.getRGB(i, j));
						if(c == 0)
							values[h] = color.getRed() / 255.0;
						if(c == 1)
							values[h] = color.getGreen() / 255.0;
						if(c == 2)
							values[h] = color.getBlue() / 255.0;
						
						h ++;
					}
			}
		}
		
		return Point.fromRaw(values);
	}
	
	private static BufferedImage read(File file) throws IOException
	{
		if(file.getName().toLowerCase().endsWith(".tiff") || file.getName().toLowerCase().endsWith(".tif"))
		{
			 RenderedImage image = null;
			 
			 FileInputStream in = new FileInputStream(file);
			 FileChannel channel = in.getChannel();
			 ByteBuffer buffer = ByteBuffer.allocate((int)channel.size());
			 channel.read(buffer);
			 
			 SeekableStream stream = new ByteArraySeekableStream(buffer.array());
			 String[] names = ImageCodec.getDecoderNames(stream);
			 ImageDecoder dec = 
					 ImageCodec.createImageDecoder(names[0], stream, null);
			 RenderedImage im = dec.decodeAsRenderedImage();
			 image = PlanarImage.wrapRenderedImage(im).getAsBufferedImage();
			 return convertRenderedImage(image);
		}
		
		BufferedImage image = ImageIO.read(file);
		if(image == null)
			throw new IOException("Failed to read image from file: " + file);
		return image;
	}
	
	/**
	 * From http://www.jguru.com/faq/view.jsp?EID=114602
	 * 
	 * @param img
	 * @return
	 */
	private static BufferedImage convertRenderedImage(RenderedImage img) 
	{
		if (img instanceof BufferedImage) {
			return (BufferedImage)img;	
		}	
		ColorModel cm = img.getColorModel();
		int width = img.getWidth();
		int height = img.getHeight();
		WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		Hashtable properties = new Hashtable();
		String[] keys = img.getPropertyNames();
		if (keys!=null) {
			for (int i = 0; i < keys.length; i++) {
				properties.put(keys[i], img.getProperty(keys[i]));
			}
		}
		BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
		img.copyData(raster);
		return result;
	}
	
	/**
	 * Reads a directory of images into a list of points.
	 * 
	 * The images are not resized, so if they have different dimensions then the 
	 * points in the returned list will have different dimensionality.
	 * @param dir
	 * @param gray
	 * @return
	 */
	public static List<Point> readImages(File dir, boolean gray)
		throws IOException
	{
		File[] files = dir.listFiles();
		if(files == null)
			throw new IllegalArgumentException("Argument ("+dir+") is not a directory.");
		
		List<Point> dataset = new ArrayList<Point>(files.length);
		
		for(File file : files)
			if(!file.isDirectory() && !file.isHidden())
				dataset.add(readImage(file, gray));
		
		return dataset;
	}
	
	/**
	 * returns the file size of the first image in the given directory
	 */
	public static Pair<Integer, Integer> size(File dir)
		throws IOException
	{
		File[] files = dir.listFiles();
		if(files == null)
			throw new IllegalArgumentException("Argument (+"+dir+"+) is not a directory.");
		
		int width = -1, height = -1;
		for(File file : files)
			if(!file.isDirectory() && !file.isHidden())
			{
				BufferedImage image = read(file);
				
				width = image.getWidth();
				height = image.getHeight();
				
				break;
			}
		
		return new Pair<Integer, Integer>(width, height);
	}
	
	/**
	 * Reconstructs an image from the given point.
	 * 
	 * @param point
	 * @param width
	 * @param height
	 * @param gray
	 * @return
	 */
	public static BufferedImage toImage(Point point, int width, int height, boolean gray)
	{

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		
		int index = 0;
		
		if(gray) {
			for(int i = 0; i < width; i++)
				for(int j = 0; j < height; j++)
				{

					float f = clip((float)(double) point.get(index ++));
					
					Color color = new Color(f, f, f);
					result.setRGB(i, j, color.getRGB());
				}
		} else 
		{
			int channelSize = width * height;
			for(int i = 0; i < width; i++)
				for(int j = 0; j < height; j++)
				{
					
					float r = clip((float)(double) point.get(index));
					float g = clip((float)(double) point.get(index + channelSize));
					float b = clip((float)(double) point.get(index + channelSize * 2));
					index ++;
					
					Color color = new Color(r, g, b);
					result.setRGB(i, j, color.getRGB());
			}
		}
				
		return result;
		
	}
	
	private static float clip(float f)
	{
		f = f < 0.0f ? 0.0f : f;
		f = f > 1.0f ? 1.0f : f;
		return f;
	}
}
