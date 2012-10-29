package org.lilian.data.real.classification;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.lilian.data.real.Draw;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.fractal.IFS;
import org.lilian.data.real.fractal.IFSs;
import org.lilian.util.Functions;

public class Classifiers
{
	
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
		componentColors.add(Color.DARK_GRAY);

		
	}

	/**
	 * Draws a classifier of dimension two.
	 * 
	 * @param res The resolution of the smallest side of the image.
	 */
	public static BufferedImage draw(Classifier classifier, int res)
	{
		return draw(
				classifier, 
				new double[]{-1.0, 1.0},
				new double[]{-1.0, 1.0},
				res);
	}
	
//	/**
//	 * Draws a classifier of dimension two.
//	 * 
//	 * @param res The resolution of the smallest side of the image.
//	 */
//	public static BufferedImage drawClassifier(IFSClassifier classifier, int res, int samples, boolean log)
//		throws IOException
//	{
//		return drawClassifier(classifier, null, res, samples, log); 
//	}
//	
//	public static BufferedImage drawClassifier(
//			IFSClassifier classifier, Dataset<Integer> dataset, 
//			int res, int samples, boolean log)
//	throws IOException
//	{	
//		int depth = (int)classifier.getDepth();
//		int width = (dataset == null) ? 2 : 3;
//		
//		BufferedImage result = new BufferedImage(
//				width * res, res, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = result.createGraphics();
//
//		graphics.setBackground(Color.black);
//		graphics.clearRect(0, 0, result.getWidth(), result.getHeight());
//
//		BufferedImage current = drawClassifier(
//				classifier, 
//				new double[]{-1.0, 1.0},
//				new double[]{-1.0, 1.0},
//				res);
//		graphics.drawImage(current, 0, 0, null);
//		
//		graphics.setComposite(MiscComposite.getInstance(MiscComposite.ADD, 1.0f));		
//
//		List<Point> points = new ArrayList<Point>();		
//		List<IFSDensityModel> models = classifier.models();
//		
//		for(int i = 0; i < models.size(); i++)
//		{
//			BufferedImageOp op = new LookupFilter(new LinearColormap(
//					Color.BLACK.getRGB(),
//					componentColors.get(i).getRGB()));			
//			
//			points.clear();
//
//			models.get(i).endPoints(depth, points, null);
//			
//			current = Datasets.drawDataset(points, res, log);
//
//			graphics.drawImage(current, op, res, 0);
//		}
//		
//		if(dataset != null)
//		{
//			BufferedImage dataImage = Datasets.drawDataset(dataset, res, log);
//			graphics.drawImage(dataImage, res*2, 0, null);
//		}
//		
//		graphics.dispose();
//
//		return result;
//	}
	
//	/**
//	 * Draw a classifier with its representation.
//	 * 
//	 * @param classifier
//	 * @param res
//	 * @param samples The suggested number of points fo rthe representation
//	 * @param log
//	 * @return
//	 * @throws IOException
//	 */
//	public static BufferedImage drawClassifier(
//			RepresentingClassifier classifier, int res, int samples, boolean log)
//	throws IOException
//	{	
//		int width = 2;
//		
//		BufferedImage result = new BufferedImage(
//				width * res, res, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = result.createGraphics();
//
//		graphics.setBackground(Color.black);
//		graphics.clearRect(0, 0, result.getWidth(), result.getHeight());
//
//		BufferedImage current = drawClassifier(
//				classifier, 
//				new double[]{-1.0, 1.0},
//				new double[]{-1.0, 1.0},
//				res);
//		graphics.drawImage(current, 0, 0, null);
//		
//		graphics.setComposite(MiscComposite.getInstance(MiscComposite.ADD, 1.0f));
//		
//		Dataset<Integer> rep = classifier.representation(samples);
//		Pointset points;
//		
//		for(int target : rep.targetSet())
//		{
//			BufferedImageOp op = new LookupFilter(new LinearColormap(
//					Color.BLACK.getRGB(),
//					componentColors.get(target).getRGB()));
//			
//			points = rep.pointsByTargetUniform(target);			
//			current = Datasets.drawDataset(points, res, log);
//
//			graphics.drawImage(current, op, res, 0);
//		}
//		
//		graphics.dispose();
//
//		return result;
//	}		
	
	/**
	 * Draws a classifier whose dimensionality is two.
	 * 
	 * @param res The resolution of the smallest side of the image.
	 */
	public static BufferedImage draw(Classifier classifier, 
											double[] xrange, 
											double[] yrange, 
											int res)
	{
		if(classifier.dimension() != 2)
			throw new IllegalArgumentException("Classifier must have dimensionality two (has "+classifier.dimension()+")");
		
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
		Color color;

		
		for(int i = 0; i < xRes; i++)
		{
			x =  xrange[0] + step*0.5 + step * i;
			for(int j = 0; j < yRes; j++)				
			{
				y = yrange[0] + step*0.5 + step * j;
				p = new Point(x, y);
				
				classInt = classifier.classify(p);
				if(classInt >= 0 && classInt < colors.size())						
					color = colors.get(classInt);
				else
					color = errorColor;
				
				image.setRGB(i, j, color.getRGB());			
			}
		}

		return image;
	}
	
	
	public static BufferedImage draw(Classified<Point> data,
			int res,
			boolean log)	
	{
		double[] range = new double[]{-1.0, 1.0};		
		return draw(data, range, range, res, log);
	}
	
	public static BufferedImage draw(
			Classified<Point> data,
			double[] xrange, 
			double[] yrange, 
			int res,
			boolean log)	
	{
		BufferedImage image = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		
		graphics.setBackground(Color.black);
		graphics.clearRect(0, 0, image.getWidth(), image.getHeight());		
		
		BufferedImage current = null;
		
		graphics.setComposite(AlphaComposite.SrcAtop);
		
		for(int i = 0; i < data.numClasses(); i++)
		{
			System.out.println(i + ":" + data.points(i).size());
			current = Draw.draw(data.points(i), xrange, yrange, res, res, log, true);
			
			// * Colorize
			current = Draw.colorize(componentColors.get(i)).filter(current, null);
			
			graphics.drawImage(current, 0, 0, null);
		}
		
		graphics.dispose();
		
		return image;
	}	
	
	
	/**
	 * Returns a classifier that classifies 2d points according to the 
	 * mandelbrot set. The classifier does not learn, and is only used to define 
	 * a learning task for other classifiers.
	 * 
	 * @return
	 */
	public static Classifier mandelbrot()
	{
		return new MandelbrotClassifier();
	}
	
	private static class MandelbrotClassifier extends AbstractClassifier
	{
		// private AffineMap map = new AffineMap(Arrays.asList(3.0, -0.5, 0.0, 0.0), AffineMap.Mode.SIMILITUDE);
		
		public MandelbrotClassifier()
		{
			super(2, 2);
		}

		public int classify(Point point) 
		{
			double range = 10.0, topRange = 100000.0;
			int steps = 1000;
			
			double x0 = point.get(0) - 0.5, y0 = point.get(1);
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
				return 1;
			return 0;
		}

	}
	
	/**
	 * The Newton fractal as a classifier.
	 * 
	 * The Newton fractal is a coloring of points in the complex plane by which 
	 * of the possible roots of a complex polynomial Netwon's method converges to.
	 * @return
	 */
	public static Classifier newton()
	{
		return new NewtonClassifier();
	}	
	
	private static class NewtonClassifier extends AbstractClassifier
	{
		private static DistanceClassifier base;
		
		static {
			base = new DistanceClassifier(new Point(-0.5, -0.9));
			base.addPoint(new Point(-0.5, 0.9));
			base.addPoint(new Point(1.0, 0.0));
		}
		
		public NewtonClassifier()
		{
			super(2, 3);
		}

		public int classify(Point point) {
			
			int steps = 2000;
			
			double x = point.get(0), y = point.get(1);
			double xp, yp;
			
			for(int i = 0; i < steps; i ++)
			{
				double poly = x*x*x*x + 2*x*x*y*y + y*y*y*y;
				
				xp = (2.0/3.0) * x + (1.0/3.0) * (x*x - y*y)/poly;
				yp = (2.0/3.0) * y + (1.0/3.0) *    (-2*x*y)/poly;

				x = xp; y = yp;
			}
			
			// System.out.printf("%.2f\t %.2f\n", x, y);
			return base.classify(new Point(x,y)); 
		}
	}
	

	/**
	 * A classifier for the magnet fractal.
	 * 
	 * The magnet fractal is a simulation of a simple physical system consisting
	 * of a metal pendulum suspended over three magnets. The initial states 
	 * (points in the plane) are colored by which magnet the pendulum halts 
	 * over, when released from that state.
	 * 
	 * @return
	 */
	public static Classifier magnet() {
		return new MagnetClassifier();
	}
	
	
	private static class MagnetClassifier extends AbstractClassifier
	{
		
		private static ArrayList<RealVector> sources = new ArrayList<RealVector>();
		
		private int maxSteps = 20000;
		private int minSteps = 500;		
		private double stopDist     = 0.01;
		private double stopVelocity = 0.01;
		private double frictConst	= 0.00125;
		private double magnConst	= 0.000002;
		private double pendheight	= 0.02;		
		private double dt = 1.0;
		
		static 
		{
			// sets the three sources at the vertices of an equilateral triangle
			double 	a = 0.25,
					y = a * Math.sin((30.0/360.0) * (2 * Math.PI)),
					x = a * Math.cos((30.0/360.0) * (2 * Math.PI));
			
			for(int i = 0; i < 3; i++)
				sources.add(new ArrayRealVector(2));
			
			sources.get(0).setEntry(0, 0);
			sources.get(0).setEntry(1, a);
			
			sources.get(1).setEntry(0, x);
			sources.get(1).setEntry(1,-y);			

			sources.get(2).setEntry(0,-x);
			sources.get(2).setEntry(1,-y);
		}
		
		public MagnetClassifier()
		{
			super(2, 3);
		}

		public int classify(Point point) 
		{
			RealVector velocity         = new ArrayRealVector(2);
			RealVector acceleration     = new ArrayRealVector(2);
			RealVector accelerationNew  = new ArrayRealVector(2);
			RealVector accelerationPrev = new ArrayRealVector(2);
			
			RealVector dist = new ArrayRealVector(2);
			
			RealVector position = new ArrayRealVector(2);
			position.setEntry(0, point.get(0));
			position.setEntry(1, point.get(1));
			
			for(int i = 0; i < maxSteps; i++)
			{
// System.out.println(position + " " + velocity);				
				
				position = position.add(velocity.mapMultiply(dt));
				position = position.add(acceleration.mapMultiply(sq(dt) * (2.0/3.0)));
				position = position.add(accelerationPrev.mapMultiply(-sq(dt) * (1.0/6.0)));
				
				for(int s = 0; s < sources.size(); s++)
				{
					RealVector source = sources.get(s);
					
					// dist = position - source
					dist.setSubVector(0, position);
					dist = dist.add(source.mapMultiply(-1.0));

					double sourceDist = dist.getNorm();
					double norm = 0.0;
					for(int k = 0; k < dist.getDimension(); k++)
						norm += sq(dist.getEntry(k)); 
					norm = Math.sqrt(norm + sq(pendheight));
					
					dist.mapMultiplyToSelf(1.0/(norm*norm*norm));
					
					accelerationNew = accelerationNew.add(dist.mapMultiply(-magnConst));
					
// System.out.println(velocity.getNorm() + "\t" + sourceDist + "\t" + source);
// System.out.println(i);
					if(i > minSteps && sourceDist < stopDist && velocity.getNorm() < stopVelocity)
					{
						return s;
					}
				}
				
				accelerationNew = accelerationNew.add(velocity.mapMultiply(-frictConst));
				
				velocity = velocity.add(accelerationNew  .mapMultiply( dt*(1.0/3.0)));
				velocity = velocity.add(acceleration     .mapMultiply( dt*(5.0/6.0)));
				velocity = velocity.add(accelerationPrev .mapMultiply(-dt*(1.0/6.0)));
				
				RealVector tmp = accelerationPrev;
				accelerationPrev = acceleration;
				acceleration  = accelerationNew;
				accelerationNew = tmp;
				accelerationNew.set(0.0);
			}
			System.out.println("?");
						
			return -1;
		}
		
		private static double sq(double in)
		{
			return in*in;
		}

		@Override
		public int dimension()
		{
			return 2;
		}

		@Override
		public int size()
		{
			return 3;
		}
	}	
	
	/**
	 * A classifier for the game of nim
	 * 
	 * @param numHeaps
	 * @param max
	 * @return
	 */
	public static Classifier nim(int numHeaps, int max) {
		return new NimClassifier(numHeaps, max);
	}
	
	
	private static class NimClassifier extends AbstractClassifier
	{
		private double max = 200;
		
		public NimClassifier(int numHeaps, int max)
		{
			super(numHeaps, 2);
			this.max = max;
		}

		public int classify(Point point) 
		{
			List<Integer> ints = new ArrayList<Integer>();
			for(double d : point)
				ints.add( (int)Math.floor(((d+1.0)/2.0) * max) );
			
			int nimSum = 0;
			for(int heapSize : ints)
				nimSum = nimSum ^ heapSize;
			
			if(nimSum == 0) 
				return 0;
			return 1;
		}
	}
	
	public static Classifier wythoff(int max) {
		return new WythoffClassifier(max);
	}
	
	private static class WythoffClassifier extends AbstractClassifier
	{
		private double max = 200;
		private double phi = 1.6180339887498948482; 
		
		public WythoffClassifier(int max)
		{
			super(2, 2);
			this.max = max;
		}

		public int classify(Point point) 
		{
			int n = (int)Math.floor(((point.get(0) + 1.0)/2.0) * max);
			int m = (int)Math.floor(((point.get(1) + 1.0)/2.0) * max);			
			
			int k = m - n; 
			
			if((int)Math.floor(phi * phi * k) == m && (int)Math.floor(phi * k) == n)
				return 0;
			return 1;
		}
	}	
	
	/**
	 * Returns a classifier that classifies points according to the bi-unit 
	 * square or high dimensional analog
	 * 
	 * @return
	 */
	public static Classifier square(int n, double r)
	{
		return new SquareClassifier(n , r);
	}
	
	private static class SquareClassifier extends AbstractClassifier
	{
		private double r = 0.5;
		
		public SquareClassifier(int n)
		{
			super(n, 2);
		}
		public SquareClassifier(int n, double r)
		{
			super(n, 2);
			this.r = r;			
		}

		public int classify(Point point) 
		{
			for(double x : point)
				if(x > r || x < -r)
					return 0;
			
			return 1;
		}

		public void learn(List<? extends List<Double>> data, List<Integer> classes) {
			throw new UnsupportedOperationException("The Square classifier doesn't learn");			
		}
	}	
	

	public static Classifier line(int dim)
	{
		return new LineClassifier(dim);
	}

	private static class LineClassifier extends AbstractClassifier
	{		
		public LineClassifier(int n)
		{
			super(n, 2);
		}

		public int classify(Point point) 
		{
			if(point.get(0) > 0 )
				return 0;
			
			return 1;
		}

		public void learn(List<? extends List<Double>> data, List<Integer> classes) {
			throw new UnsupportedOperationException("The Square classifier doesn't learn");			
		}
	}	
	
	public static Classifier sine()
	{
		return new SineClassifier();
	}
	
	private static class SineClassifier extends AbstractClassifier
	{		
		public SineClassifier()
		{
			super(2, 2);
		}

		public int classify(Point point) 
		{
			double x = point.get(0);
			
			if(point.get(1) > Math.sin(x*6.0)/2.0)
				return 0;
			
			return 1;
		}

		public void learn(List<? extends List<Double>> data, List<Integer> classes) {
			throw new UnsupportedOperationException("The Square classifier doesn't learn");			
		}
	}
	
	public static Classifier ifs(int depth)
	{
		return new IFSExampleClassifier(depth);
	}
	
	private static class IFSExampleClassifier extends AbstractClassifier
	{		
		int depth;
		IFS<Similitude> model = IFSs.square();
		
		public IFSExampleClassifier(int depth)
		{
			super(2, 2);
			this.depth = depth;
		}

		public int classify(Point point) 
		{
			List<Integer> code = IFS.code(model, point, depth);
			
			int sum = 0;
			for(int i : code)
				sum += i;
		
			return code.contains(0) ? 1 : 0;
		}
	}			
}
