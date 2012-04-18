package org.lilian.data.real;

import static java.lang.Math.abs;
import static java.lang.Math.floor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.ShortLookupTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lilian.data.real.fractal.IFS;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.models.FrequencyModel;

/**
 * A set of static methods to generate simple density plots of data.
 * 
 * This should be replaced by a good plotting solution.
 * 
 * @author Peter
 *
 */
public class Draw
{
	
	public static BufferedImage draw(List<Point> data, int res, boolean log)
	{
		double[] range = new double[]{-1.0, 1.0};
		return draw(data, range, range, res, log);
	}

	public static BufferedImage draw(List<Point> data, 
			double[] xrange, 
			double[] yrange, 
			int res)
	{
		return draw(data, xrange, yrange, res, false);
	}
	
	/**
	 * Draws a histogram of a 2D dataset as a grayscale image. 
	 * 
	 * @param res The resolution of the smallest side of the image.
	 * @param log If true, the log values are plotted, so that low values are 
	 * 				more visible
	 */
	public static BufferedImage draw(List<Point> data, 
											double[] xrange, 
											double[] yrange, 
											int res,
											boolean log)
	{	
		double 	xDelta = xrange[1] - xrange[0],
				yDelta = yrange[1] - yrange[0];
		
		double maxDelta = Math.max(xDelta, yDelta); 		
		double minDelta = Math.min(xDelta, yDelta);
		
		double step = minDelta/(double) res;
		
		int xRes = (int) (xDelta / step);
		int yRes = (int) (yDelta / step);

		float max = Float.NEGATIVE_INFINITY;
		float min = 0.0f;		
		float[][] matrix = new float[yRes][];
		for(int x = 0; x < xRes; x++)
		{
			matrix[x] = new float[yRes];
			for(int y = 0; y < yRes; y++)
			{
				matrix[x][y] = 0.0f;				
			}
		}
		
		int xp, yp;
		for(int i = 0; i < data.size(); i++)
		{
			Point point = data.get(i);
			
			xp = toPixel(point.get(0), xRes, xrange[0], xrange[1]); 
			yp = toPixel(point.get(1), yRes, yrange[0], yrange[1]);
			if(xp >= 0 && xp < xRes && yp >= 0 && yp < yRes)
			{
				matrix[xp][yp] ++;
				max = Math.max(matrix[xp][yp], max);
			}
		}
		
		BufferedImage image = 
			new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_ARGB);
		
		Color color;
		
		if(log)
		{
			min = (float)Math.log(min + 1.0f);
			max = (float)Math.log(max + 1.0f);
		}
		
		for(int x = 0; x < xRes; x++)
			for(int y = 0; y < yRes; y++)
			{
				//float value = matrix[x][yRes - y - 1];
				float value = matrix[x][y];				
				if(log)
					value = (float)Math.log(value + 1.0f);
				
				float gray = (value - min)/(max - min);
				
				if(gray < 0.0)
					color = Color.BLUE;
				else if(gray > 1.0)
					color = Color.RED;
				else				
					color  = new Color(gray, gray, gray, gray);
				
				image.setRGB(x, y, color.getRGB());
			}
		
		return image;
	}
	
	public static BufferedImage draw(
			Generator generator, int samples, int res, boolean log)
	{
		double[] range = new double[]{-1.0, 1.0};
		return draw(generator, samples, range, range, res, res, log);
	}
	
	/**
	 * Draws a histogram of a 2D dataset as a grayscale image. 
	 * 
	 * @param res The resolution of the smallest side of the image.
	 * @param log If true, the log values are plotted, so that low values are 
	 * 				more visible
	 */
	public static BufferedImage draw(Generator<Point> generator,
											int samples,
											double[] xrange, 
											double[] yrange, 
											int xRes,
											int yRes,											
											boolean log)
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
		float min = 0.0f;		
		float[][] matrix = new float[xRes][];
		for(int x = 0; x < xRes; x++)
		{
			matrix[x] = new float[yRes];
			for(int y = 0; y < yRes; y++)
				matrix[x][y] = 0.0f;				
		}
		
		int xp, yp;
		for(int i = 0; i < samples; i++)
		{
			Point point = generator.generate();
			
			xp = toPixel(point.get(0), xRes, xrange[0], xrange[1]); 
			yp = toPixel(point.get(1), yRes, yrange[0], yrange[1]);
			if(xp >= 0 && xp < xRes && yp >= 0 && yp < yRes)
			{
				matrix[xp][yp] ++;
				max = Math.max(matrix[xp][yp], max);
			}
		}
		
		BufferedImage image = 
			new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_ARGB);
		
		Color color;
		
		if(log)
		{
			min = (float)Math.log(min + 1.0f);
			max = (float)Math.log(max + 1.0f);
		}
		
		for(int x = 0; x < xRes; x++)
			for(int y = 0; y < yRes; y++)
			{
				//float value = matrix[x][yRes - y - 1];
				float value = matrix[x][y];				
				if(log)
					value = (float)Math.log(value + 1.0f);
				
				float gray = (value - min)/(max - min);
				
				if(gray < 0.0)
					color = Color.BLUE;
				else if(gray > 1.0)
					color = Color.RED;
				else				
					color  = new Color(gray, gray, gray, 1.0f);
				
				image.setRGB(x, y, color.getRGB());
			}
		
		return image;
	}
	
	
	public static ArrayList<Color> colors = new ArrayList<Color>();
	public static ArrayList<Color> componentColors = new ArrayList<Color>();	
	public static Color errorColor = Color.RED;		
	
	static 
	{
		colors.add(Color.BLACK);
		colors.add(Color.WHITE);		
		colors.add(Color.BLUE);
		colors.add(Color.GREEN);
		colors.add(Color.YELLOW);
		colors.add(Color.PINK);
		
		componentColors.add(Color.GREEN);
		componentColors.add(Color.RED);		
		componentColors.add(Color.YELLOW);	
		componentColors.add(Color.ORANGE);
		componentColors.add(Color.WHITE);
		componentColors.add(Color.CYAN);
		componentColors.add(Color.MAGENTA);
		componentColors.add(Color.PINK);
		componentColors.add(Color.LIGHT_GRAY);

		
	}

	/**
	 * Draws the component distribution in the codes of a three component 
	 * classifier.
	 * 
	 * @param res The resolution of the smallest side of the image.
	 */
	public static <M extends AffineMap> 
		BufferedImage drawCodes(IFS<M> ifs, 
											double[] xrange, 
											double[] yrange, 
											int res, int depth)
		throws IOException
	{
		if(ifs.size() > 3)
			throw new IllegalArgumentException("IFS must have three components or less, had "+ifs.size()+".");
		
		double 	xDelta = xrange[1] - xrange[0],
				yDelta = yrange[1] - yrange[0];
		
		double maxDelta = Math.max(xDelta, yDelta); 		
		double minDelta = Math.min(xDelta, yDelta);
		
		double step = minDelta/(double) res;
		
		int xRes = (int) (xDelta / step);
		int yRes = (int) (yDelta / step);
		
		BufferedImage image = 
			new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_RGB);		
		
		double x, y;
		int classInt;
		Point p;
		
		for(int i = 0; i < xRes; i++)
		{
			x =  xrange[0] + step*0.5 + step * i;
			for(int j = 0; j < yRes; j++)				
			{
				y = yrange[0] + step*0.5 + step * j;
				
				
				p = new Point(x, y);
				List<Integer> code = IFS.code(ifs, p, depth);
				
				Color color = null;
				
				if(code == null)
				{
					color = Color.BLACK;
				} else
				{
					FrequencyModel<Integer> mod = 
							new BasicFrequencyModel<Integer>(code);
					
					float red   = (float)mod.probability(0);
					float green = (float)mod.probability(1);
					float blue  = (float)mod.probability(2);
					color = new Color(red, green, blue);
				}
				
				image.setRGB(i, j, color.getRGB());			
			}
		}

		return image;
	}
		

//	/**
//	 * Draws multidimensional classed data.
//	 * 
//	 * @param data
//	 * @param res
//	 * @param log
//	 * @return
//	 */
//	public static BufferedImage rugplot(Dataset<Integer> data,
//			int res,
//			boolean log)	
//	{
//		int dim = data.get(0).size();
//		BufferedImage image = new BufferedImage(res*(dim-1) + dim, res*(dim-1) + dim, 
//				BufferedImage.TYPE_INT_ARGB);
//		
//		Graphics2D graphics = image.createGraphics();
//		
//		BufferedImage current;
//		for(int h = 0; h < dim-1; h++)
//			for(int v = h+1; v < dim; v++)
//			{
//				current = drawDataset(data, h, v, res, log);
//				
//				graphics.drawImage(current, h * res + h, (v-1) * res + v, null);
//			}
//		
//		graphics.dispose();
//		
//		return image;
//		
//	}
//	
//	public static BufferedImage rugplot(WeightedList<Point> points,
//			int res,
//			boolean log)
//	{
//		int dim = points.get(0).size();
//		BufferedImage image = new BufferedImage(res*(dim-1) + dim, res*(dim-1) + dim, 
//				BufferedImage.TYPE_INT_ARGB);
//		
//		Graphics2D graphics = image.createGraphics();
//		
//		BufferedImage current;
//		for(int h = 0; h < dim-1; h++)
//			for(int v = h+1; v < dim; v++)
//			{
//				current = drawDataset(points, h, v, res, log);
//				
//				graphics.drawImage(current, h * res + h, (v-1) * res + v, null);
//			}
//		
//		graphics.dispose();
//		
//		return image;
//		
//	}	
	
//	
//	public static BufferedImage drawDataset(Dataset<Integer> data,
//			int hori,
//			int verti,
//			int res,
//			boolean log)	
//	{
//		double[] range = new double[]{-1.0, 1.0};		
//		return drawDataset(data, hori, verti, range, range, res, log);
//	}
//	
//	public static BufferedImage drawDataset(Dataset<Integer> data,
//			int hori,
//			int verti,
//			double[] xrange, 
//			double[] yrange, 
//			int res,
//			boolean log)	
//	{
//		BufferedImage image = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = image.createGraphics();
//		
//		graphics.setBackground(Color.black);
//		graphics.clearRect(0, 0, image.getWidth(), image.getHeight());		
//		
//		List<Integer> classes = data.targetSet();
//		
//		Pointset ps;
//		BufferedImage current;
//		
//		graphics.setComposite(MiscComposite.getInstance(MiscComposite.ADD, 1.0f));		
//		
//		for(int i = 0; i < classes.size(); i++)
//		{
//			ps = data.pointsByTarget(classes.get(i));
//			current = drawDataset(ps, hori, verti, xrange, yrange, res, log);
//			
//			BufferedImageOp op = new LookupFilter(new LinearColormap(
//					Color.BLACK.getRGB(),
//					Classifiers.componentColors.get(i).getRGB()));
//			
//			graphics.drawImage(current, op, 0, 0);
//		}
//		
//		graphics.dispose();
//		
//		return image;
//	}
//	
//	public static BufferedImage drawDataset(WeightedList<Point> data,
//			int hori,
//			int verti,
//			int res,
//			boolean log)	
//	{
//		return drawDataset(data, hori, verti, new double[]{-1.0, 1.0}, new double[]{-1.0, 1.0}, res, log);
//	}
//	
//	public static BufferedImage drawDataset(WeightedList<Point> data,
//			int hori,
//			int verti,
//			double[] xrange, 
//			double[] yrange, 
//			int res,
//			boolean log)	
//	{
//		double 	xDelta = xrange[1] - xrange[0],
//		yDelta = yrange[1] - yrange[0];
//
//		double maxDelta = Math.max(xDelta, yDelta); 		
//		double minDelta = Math.min(xDelta, yDelta);
//
//		double step = minDelta/(double) res;
//
//		int xRes = (int) (xDelta / step);
//		int yRes = (int) (yDelta / step);
//
//		float max = Float.NEGATIVE_INFINITY;
//		float min = 0.0f;		
//		float[][] matrix = new float[yRes][];
//		for(int x = 0; x < xRes; x++)
//		{
//			matrix[x] = new float[yRes];
//			for(int y = 0; y < yRes; y++)
//			{
//				matrix[x][y] = 0.0f;				
//			}
//		}
//
//		int xp, yp;
//		for(int i = 0; i < data.size(); i++)
//		{
//			Point point = data.get(i);
//
//			xp = toPixel(point.get(hori), xRes, xrange[0], xrange[1]); 
//			yp = toPixel(point.get(verti), yRes, yrange[0], yrange[1]);
//			if(xp >= 0 && xp < xRes && yp >= 0 && yp < yRes)
//			{
//				matrix[xp][yp] += data.weight(i);
//				max = Math.max(matrix[xp][yp], max);
//			}
//		}
//
//		BufferedImage image = 
//			new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_ARGB);
//
//		Color color;
//
//		if(log)
//		{
//			min = (float)Math.log(min + 1.0f);
//			max = (float)Math.log(max + 1.0f);
//		}
//
//		for(int x = 0; x < xRes; x++)
//			for(int y = 0; y < yRes; y++)
//			{
//				//float value = matrix[x][yRes - y - 1];
//				float value = matrix[x][y];				
//				if(log)
//					value = (float)Math.log(value + 1.0f);
//
//				float gray = (value - min)/(max - min);
//
//				if(gray < 0.0)
//					color = Color.BLUE;
//				else if(gray > 1.0)
//					color = Color.RED;
//				else				
//					color  = new Color(gray, gray, gray, 1.0f);
//
//				image.setRGB(x, y, color.getRGB());
//			}
//
//		return image;
//	}
	
	public static LookupOp colorize(Color color)
	{
		short r = (short) color.getRed(), 
		      g = (short) color.getGreen(), 
		      b = (short) color.getBlue();
		
	    short[] alpha = new short[256];
	    short[] red   = new short[256];
	    short[] green = new short[256];
	    short[] blue  = new short[256];

	    for (short i = 0; i < 256; i++) {
	        alpha[i] = i;
	        red[i]   = (short) ((r + i * .3)/2);
	        green[i] = (short) ((g + i * .59)/2);
	        blue[i]  = (short) ((b + i * .11)/2);
	    }

	    short[][] data = new short[][] {
	            red, green, blue, alpha
	    };

	    LookupTable lookupTable = new ShortLookupTable(0, data);
	    return new LookupOp(lookupTable, null);
	}

	
	
	/**
	 * Converts a double value to its index in a given range when that range 
	 * is discretized to a given number of bins. Useful for finding pixel values 
	 * when creating images.
	 * 
	 * @param coord 
	 * @param res
	 * @param rangeStart
	 * @param rangeEnd
	 * @return The pixel index. Can be out of range (negative of too large).
	 */
	public static int toPixel(double coord, int res, double rangeStart, double rangeEnd)
	{
		double pixSize = abs(rangeStart - rangeEnd) / (double) res;
		return (int)floor((coord - rangeStart)/pixSize);
	}
	
	/**
	 * 
	 * @param pixel
	 * @param res
	 * @param rangeStart
	 * @param rangeEnd
	 * @return
	 */
	public static double toCoord(int pixel, int res, double rangeStart, double rangeEnd){
		double pixSize = abs(rangeStart - rangeEnd) / (double) res;
		return pixSize * ((double) pixel) + pixSize * 0.5 + rangeStart;		
	}			

}
