package org.lilian.data.real;

import static org.lilian.util.Series.series;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	 * Creates a generator for random points on a sphere of the given dimension,
	 * with 
	 */
	public static Generator<Point> sphere(int dimension)
	{
		return new Sphere(dimension);
	}
	
	private static class Sphere extends AbstractGenerator<Point>
	{
		protected int dim;
		
		public Sphere(int dim)
		{
			this.dim = dim;
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
			
			return new Point(vector);
		}
	}
	
	public static Generator<Point> ball(int dim)
	{
		return new Ball(dim);
	}	
	
	private static class Ball extends Sphere
	{
		public Ball(int dim)
		{
			super(dim);
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
}
