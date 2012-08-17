package org.lilian.data.real;

import static org.lilian.util.Series.series;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.lilian.Global;
import org.lilian.util.Series;
import org.lilian.util.distance.SquaredEuclideanDistance;

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
	public static List<Point> sample(List<Point> data, int n)
	{
		List<Point> res = new ArrayList<Point>(n);
		
		for(int i : series(n))
			res.add(data.get(Global.random.nextInt(data.size())));
		
		return res;
	}
	
	/**
	 * Sample n elements randomly with replacement
	 * 
	 * @return
	 */
	public static List<Point> sampleWithReplacement(List<Point> data, int n)
	{
		
		// *  Shuffle the list and copy the first n items
		List<Point> res = new ArrayList<Point>(data);
		Collections.shuffle(res);
		
		// * Copy the sublist over, so we the gc can clean up the shuffled list.
		List<Point> res2 = new ArrayList<Point>(res.subList(0, n));
		
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
		return new Swiss();
	}
	
	private static class Swiss extends AbstractGenerator<Point>
	{		
		List<MVN> mvns = new ArrayList<MVN>();
		
		public Swiss()
		{
			
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
			return new Point(
				p.get(0) * Math.cos(p.get(0)),
				p.get(1),
				p.get(0) * Math.cos(p.get(0)));
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
	public Point readImage(File imageFile, boolean gray) throws IOException
	{
		BufferedImage image = ImageIO.read(imageFile);
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
							values[h] = color.getBlue() / 255.0;
						if(c == 2)
							values[h] = color.getGreen() / 255.0;
						
						h ++;
					}
			}
		}
		
		return Point.fromRaw(values);
	}
}
