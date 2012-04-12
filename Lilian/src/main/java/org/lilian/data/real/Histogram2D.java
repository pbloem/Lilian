package org.lilian.data.real;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import java.io.*;

import javax.imageio.ImageIO;

import org.lilian.Global;
import org.lilian.util.Functions;
/**
 * Simple 2D histogram. Mainly used to translate images into sets of points
 * 
 * TODO: Create an ordered version of the probability list and translate indices
 * after the random draw.   
 * 
 * @author peter
 *
 */

public class Histogram2D 
		extends AbstractGenerator<Point>
	{
	
	private List<Double> probabilities;
	private int width;
	private int height;	
	private double probSum;
	
	private Histogram2D()
	{
	}	

	public int dimension() {
		return 2;
	}
		
	public Point generate() 
	{
		int i = Functions.draw(probabilities, probSum);

		// Find the pixel for i
		int x = toX(i, width),
		    y = toY(i, width);

		// Find the square in the domain represented by the pixel (x, y)
		double xrange = (2.0 / width),
		       yrange = (2.0 / height),
		       xstart = xrange * x - 1.0,
		       ystart = yrange * y - 1.0;
		
		// draw randomly in that square
		double xrand = Global.random.nextDouble() * xrange + xstart,
		       yrand = Global.random.nextDouble() * yrange + ystart;
		
		return new Point(xrand, yrand);
	}

	public static Histogram2D fromImage(File file)
	throws IOException
	{
		return fromImage(ImageIO.read(file));
	}

	/**
	 * Generates a random dataset, treating a given grayscale image as a 
	 * histogram.
	 */
	public static Histogram2D fromImage(BufferedImage image)
	{
	    int width  = image.getWidth(),
	        height = image.getHeight();
		
	    Double[] probabilities = new Double[width * height];
	    Double sum = 0.0, value;
	    
	    for(int i = 0; i < probabilities.length; i++)
	    {
	    	value = grayscale(image.getRGB(toX(i, width), toY(i, width)));
	    	probabilities[i] = value;
	    	sum += value;
	    }
	    
	    Histogram2D model = new Histogram2D();
	    model.probabilities = Arrays.asList(probabilities);
	    model.width = width;
	    model.height = height;	    
	    model.probSum = sum;
	    
	    return model;
	}
	
	private static double grayscale(int rgb)
	{
		// Reasonable weight values for the different channels
	    double redWeight   = 0.3, 
	           greenWeight = 0.59,
	           blueWeight  = 0.11;
	    
	    Color c = new Color(rgb);
	    return redWeight   * (c.getRed()   / 255.0) + 
	           greenWeight * (c.getGreen() / 255.0) +
	           blueWeight  * (c.getBlue()  / 255.0);	           
	}
	
	static int toSingle(int x, int y, int width)
	{
		return y * width + x;
	}
	
	static int toX(int i, int width)
	{
		return i % width;
	}
	
	static int toY(int i, int width) 
	{
		return i / width;
	}
			
}
