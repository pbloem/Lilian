package org.lilian.data.real;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Densities
{

	public static BufferedImage draw(Density density, int res, boolean log)
	{
		return draw(density, new double[]{-1.0, 1.0}, new double[]{-1.0, 1.0}, res, res, log);
	}
	
	public static BufferedImage draw(
			Density density, double[] xrange, double[] yrange, 
			int xRes, int yRes, boolean log)
	{
		// * size of the image in coordinates
		double 	xDelta = xrange[1] - xrange[0],
				yDelta = yrange[1] - yrange[0];
		
		// * coordinate distance per pixel
		double xStep = xDelta/(double) xRes;
		double yStep = yDelta/(double) yRes;		
		
		// int xRes = (int) (xDelta / xStep);
		// int yRes = (int) (yDelta / yStep);

		float max = Float.NEGATIVE_INFINITY;
		float min = Float.POSITIVE_INFINITY;		
		float[][] matrix = new float[xRes][];
		for(int x = 0; x < xRes; x++)
		{
			matrix[x] = new float[yRes];
			
			double xCoord = Draw.toCoord(x, xRes, xrange[0], xrange[1]);
					
			for(int y = 0; y < yRes; y++)
			{
				double yCoord = Draw.toCoord(y, yRes, yrange[0], yrange[1]);
				Point p = new Point(xCoord, yCoord);
				
				float d = (float) density.density(p);
				
				if(log) 
					d = (float) Math.log(d + 1.0f);
				
				matrix[x][y] = d;
				
				min = Math.min(d, min);
				max = Math.max(d, max);
			}
		}		
		
		BufferedImage image = 
			new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_ARGB);
		
		Color color;
		
		for(int x = 0; x < xRes; x++)
			for(int y = 0; y < yRes; y++)
			{	
				float gray = (matrix[x][y] - min)/(max - min);
								
				if(gray < 0.0)
					color = Color.BLUE;
				else if(gray > 1.0)
					color = Color.RED;
				else				
					color = new Color(0.0f, 0.0f, 0.0f, gray);
				
				image.setRGB(x, y, color.getRGB());
			}
		
		return image;		
	}
}
