package org.lilian.data.real.fractal.random;

import static org.lilian.util.Series.series;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.*;

import org.lilian.data.real.Draw;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.fractal.EM;
import org.lilian.data.real.fractal.IFS;
import org.lilian.data.real.fractal.IFSs;
import org.lilian.data.real.fractal.SimEM;
import org.lilian.search.Parametrizable;
import org.lilian.util.Series;

/**
 * Provides utility functions for dealing with random fractal models. 
 */
public class RIFSs {
	
	
	public static DiscreteRIFS<Similitude> sierpinski()
	{
		DiscreteRIFS<Similitude> d = new DiscreteRIFS<Similitude>(IFSs.sierpinskiSim(), 1.0);
		d.addModel(IFSs.sierpinskiDownSim(), 1.0);
		
		return d;
	}

	
	public static DiscreteRIFS<Similitude> cantor()
	{
		DiscreteRIFS<Similitude> d = new DiscreteRIFS<Similitude>(IFSs.cantorASim(), 1.0);
		d.addModel(IFSs.cantorBSim(), 1.0);
		
		return d;
	
	}

	public static DiscreteRIFS<Similitude> kochUpDown()
	{
		DiscreteRIFS<Similitude> d = new DiscreteRIFS<Similitude>(IFSs.koch4Sim(), 1.0);
		d.addModel(IFSs.koch4DownSim(), 1.0);
		
		return d;
	}
	
	public static DiscreteRIFS<Similitude> koch2UpDown()
	{
		DiscreteRIFS<Similitude> d = new DiscreteRIFS<Similitude>(IFSs.koch2Sim(), 1.0);
		d.addModel(IFSs.koch2DownSim(), 1.0);
		
		return d;
	}	
	
	public static DiscreteRIFS<Similitude> koch2UpDownOff(double w)
	{
		DiscreteRIFS<Similitude> d = new DiscreteRIFS<Similitude>(IFSs.koch2SimOff(1.0, 1.0), w);
		d.addModel(IFSs.koch2SimDownOff(1.0, 1.0), 1.0 - w);
		
		return d;
	}

	public static BufferedImage draw(DiscreteRIFS<Similitude> model, int res,
			int numRandom)
	{
		return draw(model, res, numRandom, 10000, 10, true);
	}	
	
	/**
	 * Returns a BufferedImage that is a concatenation, from left to right, of 
	 * the mean instance, and several random instances. 
	 * 
	 * @param model The random IFS model to draw from
	 * @param res	The width and height of each individual image.
	 * @param numRandom The number of random instances to include
	 * @param samples The number of samples to use per image
	 * @param depth The depth to use when generating the dataset
	 * @param log When true, a log plot is created, giving low hitting 
	 * 	probabilities more dynamic range. 
	 * @return A buffered image representing the given random IFS model.
	 */
	public static BufferedImage draw(
						RIFS<?> model, int res, 
						int numRandom, int samples, int depth, boolean log)
	{
		BufferedImage result = new BufferedImage(
				res * (numRandom + 1), res, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = result.createGraphics();
		
		BufferedImage current = Draw.draw(
				model.meanInstance(samples, depth), res, log);
		graphics.drawImage(current, 0, 0, null);		
		
		for(int i = 1; i < numRandom + 1; i++)
		{
			current = Draw.draw(model.randomInstance(samples, depth), res, log);			
			graphics.drawImage(current, i*res, 0, null);
		}
		
		return result;
	}
	
	public static <M extends org.lilian.data.real.Map & Parametrizable> BufferedImage draw(
			DiscreteRIFS<M> model, int res, 
			int numRandom, int samples, int depth, boolean log)
	{
		int width = 1 + model.size() + numRandom;
		
		BufferedImage result = new BufferedImage(
				width * res, res, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = result.createGraphics();

		BufferedImage current = Draw.draw(
				model.meanInstance(samples, depth), res, log);
		graphics.drawImage(current, 0, 0, null);
		
		List<IFS<M>> models = model.models();
		for(int i = 0; i < models.size(); i++)
		{
			current = Draw.draw(models.get(i), samples, res, log);			
			graphics.drawImage(current, (i+1) * res, 0, null);
		}		

		for(int i = 0; i < numRandom; i++)
		{
			current = Draw.draw(model.randomInstance(samples, depth), res, log);			
			graphics.drawImage(current, (i + 1 + models.size()) *res, 0, null);
		}

		return result;
	}	
	
//	public static DiscreteRIFSModel kochUpDownSquare()
//	{
//		DiscreteRIFSModel d = new DiscreteRIFSModel(DensityModels.koch(), 1.0);
//		d.addModel(DensityModels.kochDown(), 1.0);
//		d.addModel(DensityModels.squareUneven(), 1.0);
//		
//		return d;
//	}
//	
//	public static DiscreteRIFSModel cantorHV()
//	{
//		AffineMap m00, m01, m10, m11;
//		
//		m00 = new AffineMap(Arrays.asList(1/3.0, 0.0,  2/3.0, 0.0), AffineMap.Mode.SIMILITUDE);
//		m01 = new AffineMap(Arrays.asList(1/3.0, 0.0, -2/3.0, 0.0), AffineMap.Mode.SIMILITUDE);		
//
//		m10 = new AffineMap(Arrays.asList(1/3.0,  2/3.0, 0.0, 0.0), AffineMap.Mode.SIMILITUDE);
//		m11 = new AffineMap(Arrays.asList(1/3.0, -2/3.0, 0.0, 0.0), AffineMap.Mode.SIMILITUDE);		
//		
//		
//		IFSDensityModel model0 = new IFSDensityModel(m00, 1.0);
//		model0.addOperation(m01, 1.0);		
//		IFSDensityModel model1 = new IFSDensityModel(m10, 1.0);
//		model1.addOperation(m11, 1.0);
//		
//		DiscreteRIFSModel d = new DiscreteRIFSModel(model0, 1.0);
//		d.addModel(model1, 1.0);
//		
//		return d;
//	}
//
//	public static DiscreteRIFSModel sierpinskiUpDown()
//	{
//		DiscreteRIFSModel d = new DiscreteRIFSModel(DensityModels.sierpinski(), 1.0);
//		d.addModel(DensityModels.sierpinskiDown(), 1.0);
//		
//		return d;
//	}
//	
//	public static DiscreteRIFSModel kochSquare()
//	{
//		DiscreteRIFSModel d = new DiscreteRIFSModel(DensityModels.square(), 1.0);
//		d.addModel(DensityModels.koch(), 1.0);
//		
//		return d;
//	}	
//
//	public static DiscreteRIFSModel biCantor()
//	{
//		DiscreteRIFSModel d = new DiscreteRIFSModel(DensityModels.cantorA(), 1.0);
//		d.addModel(DensityModels.cantorB(), 1.0);
//		
//		return d;
//	}		
//	
//	
//	/**
//	 * Returns a @D model that represents a distribution, such that each IFS 
//	 * returned by this model maps the line (0,-1)-(0,1) to the lines
//	 * (0,-1)-p-(0,1), where p is a random point on the unit disc. 
//	 * 
//	 * @return
//	 */
//	public static RIFS<Similitude> line()
//	{
//		return line(0.2);
//	}
//	
//	public static RIFS<Similitude> line(double scale)
//	{
//		return new RandomLineModel(scale);
//	}
//	
//	public static RandomModel koch(double stdDev)
//	{
//		return new RandomKochModel(stdDev);
//	}	
//
//	private static class RandomLineModel extends AbstractRIFS {
//		
//		private double scale;
//		
//		public RandomLineModel(double scale)
//		{
//			super(2);
//			this.scale = scale;
//		}
//		
//		@Override
//		public IFS<Similitude> random(Random rand) 
//		{
//			Point va = new Point(rand.nextGaussian() * scale, rand.nextGaussian() * scale); 
//			Point vb = new Point(rand.nextGaussian() * scale, rand.nextGaussian() * scale);
//			
//			Point a = new Point(va.add(Functions.vector(-1.0/3.0, 0.0)));
//			Point b = new Point(vb.add(Functions.vector(1.0/3.0,  0.0)));			
//			return DensityModels.nPartLine(a, b);
//		}
//	}
//	
//	private static class RandomKochModel extends AbstractRIFSModel {
//
//		private double stdDev;
//		
//		public RandomKochModel(double stdDev)
//		{
//			super(2);
//			this.stdDev = stdDev;
//		}
//		
//		@Override
//		public IFSDensityModel draw(Random rand) 
//		{
//			Point a, b, c, ap, bp, cp;
//			
//			a = new Point(-1.0/3.0, 0);
//			b = new Point( 0, 1.0/Math.sqrt(3.0));
//			c = new Point( 1.0/3.0, 0);
//			
//			Matrix cov = Matrices.identity(2);
//			cov.scale(stdDev);
//			
//			ap = new Point(Functions.drawMVN(a.getVector(), cov)); 
//			bp = new Point(Functions.drawMVN(b.getVector(), cov));
//			cp = new Point(Functions.drawMVN(c.getVector(), cov));
//			
//			IFSDensityModel model = DensityModels.nPartLine(ap, bp, cp);
//			
//			return model;
//		}		
//	}
//	
//	public static AbstractRIFSModel rotatedCantor()
//	{
//		return new RotatedCantorModel(); 
//	}
//	
//	private static class RotatedCantorModel extends AbstractRIFSModel {
//		
//		public RotatedCantorModel()
//		{
//			super(2);
//		}
//		
//		@Override
//		public IFSDensityModel draw(Random rand) 
//		{
//			double a = Global.random.nextDouble() * 2.0 * Math.PI;
//			
//			double sa = Math.sin(a);
//			double ca = Math.cos(a);
//			
//			AffineMap m0, m1;
//			
//			m0 = new AffineMap(Arrays.asList(1/3.0, -(2/3.0) * sa, (2/3.0) * ca, 0.0), AffineMap.Mode.SIMILITUDE);
//			m1 = new AffineMap(Arrays.asList(1/3.0, (2/3.0) * sa, -(2/3.0) * ca, 0.0), AffineMap.Mode.SIMILITUDE);		
//			
//			IFSDensityModel m = new IFSDensityModel(m0, 1.0);
//			m.addOperation(m1, 1.0);	
//			
//			return m;
//		}
//	}
//	
//	public static RandomModel full(int components, double scale)
//	{
//		return new FullModel(components, scale); 
//	}
//	
//	private static class FullModel extends AbstractRIFSModel {
//		private int numParams;
//		private int components;
//		private double scale;
//		
//		public FullModel(int components, double scale)
//		{
//			super(2);
//		
//			this.components = components;
//			this.numParams = (AffineMap.numParameters(2, AffineMap.Mode.SIMILITUDE) + 1)* components;
//			this.scale = scale;
//		}
//		
//		@Override
//		public IFSDensityModel draw(Random rand) 
//		{
//			List<Double> params = new ArrayList<Double>(numParams);
//			for(int i = 0; i < numParams; i++)
//				params.add(rand.nextGaussian() * scale);
//			
//			return new IFSDensityModel(params, 2, components);
//		}
//	}
//	
//	public static RandomModel fullIFS(int modelComponents, int components, double scale)
//	{
//		return new FullIFSModel(modelComponents, components, scale); 
//	}
//	
//	private static class FullIFSModel extends AbstractRIFSModel {
//		private IFSDensityModel parameterModel;
//		int components;
//		
//		public FullIFSModel(int modelComponents, int components, double scale)
//		{
//			super(2);
//		
//			this.components = components;
//			
//			int dimension = 
//				(AffineMap.numParameters(2, AffineMap.Mode.SIMILITUDE) + 1) * components;
//			int numParams = 
//				(AffineMap.numParameters(dimension, AffineMap.Mode.SIMILITUDE) + 1) * modelComponents;
//			
//			List<Double> parameters = new ArrayList<Double>();
//			for(int i = 0; i < numParams; i++)
//				parameters.add(Global.random.nextGaussian() * scale);
//			
//			parameterModel = new IFSDensityModel(parameters, dimension, modelComponents);
//		}
//		
//		@Override
//		public IFSDensityModel draw(Random rand) 
//		{
//			List<Double> params = parameterModel.generate();
//			
//			return new IFSDensityModel(params, 2, components);
//		}
//	}	
//		
//	
//	public static RandomModel square()
//	{
//		return new RandomSquareModel();
//	}
//	
//	private static class RandomSquareModel extends AbstractRIFSModel {
//		
//		public RandomSquareModel()
//		{
//			super(2);
//		}
//		
//		@Override
//		public IFSDensityModel draw(Random rand) 
//		{
//			EllipsoidModel basis = Conf.current.basisModel(2);		
//			
//			Matrix r1, r2, r3, r4;
//				
//			Vector t1, t2, t3, t4;
//					
//			
//			double p1, p2, p3, p4;
//			
//			// * Create the IFS representation of the Barnsley Fern		Matrix r1, r2, r3, r4;
//			
//			r1 = new DenseMatrix(2, 2);
//			r1.set(0, 0,  0.5); r1.set(0, 1, 0.0);
//			r1.set(1, 0, -0.0); r1.set(1, 1, 0.5);
//			
//			r2 = r1.copy();
//			
//			r3 = r1.copy();
//		
//			r4 = r1.copy();
//			
//			t1 = new DenseVector(2);
//			t2 = new DenseVector(2);
//			t3 = new DenseVector(2);
//			t4 = new DenseVector(2);		
//			
//			t1.set(0, 0.5);	t1.set(1, 0.5);		
//			t2.set(0, 0.5);	t2.set(1, -0.5);
//			t3.set(0, -0.5); t3.set(1, 0.5);
//			t4.set(0, -0.5); t4.set(1, -0.5);		
//			
//			p1 = rand.nextDouble();
//			p2 = rand.nextDouble();
//			p3 = rand.nextDouble();
//			p4 = rand.nextDouble();
//		
//			IFSDensityModel square = new IFSDensityModel(new AffineMap(r1, t1), p1, basis);
//			square.addOperation(new AffineMap(r2, t2), p2);
//			square.addOperation(new AffineMap(r3, t3), p3);
//			square.addOperation(new AffineMap(r4, t4), p4);		
//			
//			return square;
//		}
//	}
//	
//	public static AbstractRIFSModel shearedModel(double varA, double varB)
//	{
//		return new ShearedModel(varA, varB);
//	}
//	
//	private static class ShearedModel extends AbstractRIFSModel {
//		
//		private double varA, varB;
//		
//		public ShearedModel(double varA, double varB)
//		{
//			super(2);
//			
//			this.varA = varA;
//			this.varB = varB;
//		}
//		
//		@Override
//		public IFSDensityModel draw(Random rand) 
//		{
//			double alpha, beta, h0, h1, ty0, ty1;
//			
//			alpha = Global.random.nextGaussian() * varA;
//			beta  = Global.random.nextGaussian() * varB;
//			
//			h0 = alpha;
//			h1 = (beta - alpha);
//			
//			ty0 = 0.5 * alpha;
//			ty1 = 0.5 * (alpha + beta);
//			
//			Matrix r0, r1;
//			Vector t0, t1;
//			
//			r0 = Matrices.identity(2);
//			r0.set(1, 0, h0);
//			r0.scale(0.5);
//			
//			r1 = Matrices.identity(2);
//			r1.set(1, 0, h1);
//			r1.scale(0.5);
//			
//			t0 = Functions.vector(-0.5, ty0);
//			t1 = Functions.vector(0.5,  ty1);			
//			
//			AffineMap m0 = new AffineMap(r0, t0);
//			AffineMap m1 = new AffineMap(r1, t1);		
//			
//			IFSDensityModel m = new IFSDensityModel(m0, 1.0);
//			m.addOperation(m1, 1.0);	
//			
//			return m;
//		}
//	}
//	
//	public static AbstractRIFSModel shearedBiModel(double d)
//	{
//		IFSDensityModel m0 = DensityModels.sheared(d);
//		IFSDensityModel m1 = DensityModels.sheared(-d);		
//		
//		DiscreteRIFSModel model = new DiscreteRIFSModel(m0, 0.5);
//		model.addModel(m1, 0.5);
//		
//		return model;
//	}
//	
//	
//	/**
//	 * 
//	 * @param horizontalResolution
//	 * @return
//	 */
//	public static RandomModel brownian()
//	{
//		return new BrownianModel();
//		
//	}
//	
//	private static class BrownianModel implements RandomModel
//	{
//		public BrownianModel()
//		{
//		}
//
//		@Override
//		public List<Point> meanInstance(int n, int depth) {
//			return new ArrayList<Point>(0);
//		}
//		
//		@Override
//		public List<Point> randomInstance(int n, int depth) {
//			return randomInstance(n, depth, Global.random.nextLong());
//		}
//
//		@Override
//		public List<Point> randomInstance(int n, int depth, long seed) {
//			Random random = new Random(seed); 
//			
//			List<Point> points = new ArrayList<Point>(n);
//			
//			double std = 1.0/(0.025 * n); // This should ensure that the peaks stay within (-1.0, 1.0)
//			
//			double step = 2.0/n;
//			
//			double y = 0;
//			points.add(new Point(-1.0, y));
//			for(double x = -1.0+step; x <= 1.0; x+= step)
//			{
//				y += random.nextGaussian() * std;				
//				points.add(new Point(x, y));
//			}
//			
//			double scalar = y/n, trend;
//			
//			Point point;
//			for(int i = 0; i < n; i ++)
//			{
//				point = points.get(i);
//				trend = scalar * i;
//				point.set(1, point.get(1) - trend);				
//			}
//
//			return points;
//		}
//
//		@Override
//		public void setBasis(EllipsoidModel basis) {
//		}
//		
//	}
//
//	/**
//	 * Returns a BufferedImage that is a concatenation, from left to right, of 
//	 * the mean instance, and several random instances. 
//	 * 
//	 * @param model The random IFS model to draw from
//	 * @param res	The width and height of each individual image.
//	 * @param numRandom The number of random instances to include
//	 * @param samples The number of samples to use per image
//	 * @param depth The depth to use when generating the dataset
//	 * @param log When true, a log plot is created, giving low hitting 
//	 * 	probabilities more dynamic range. 
//	 * @return A buffered image representing the given random IFS model.
//	 */
//	public static BufferedImage draw(
//						RandomModel model, int res, 
//						int numRandom, int samples, int depth, boolean log)
//	{
//		BufferedImage result = new BufferedImage(
//				res * (numRandom + 1), res, BufferedImage.TYPE_INT_RGB);
//		Graphics2D graphics = result.createGraphics();
//		
//		BufferedImage current = Datasets.drawDataset(
//				model.meanInstance(samples, depth), res, log);
//		graphics.drawImage(current, 0, 0, null);		
//		
//		for(int i = 1; i < numRandom + 1; i++)
//		{
//			current = Datasets.drawDataset(model.randomInstance(samples, depth), res, log);			
//			graphics.drawImage(current, i*res, 0, null);
//		}
//		
//		return result;
//	}
//	
//	public static BufferedImage draw(
//			DiscreteRIFSModel model, int res, 
//			int numRandom, int samples, int depth, boolean log)
//	{
//		int width = 1 + model.numModels() + numRandom;
//		
//		BufferedImage result = new BufferedImage(
//				width * res, res, BufferedImage.TYPE_INT_RGB);
//		Graphics2D graphics = result.createGraphics();
//
//		BufferedImage current = Datasets.drawDataset(
//				model.meanInstance(samples, depth), res, log);
//		graphics.drawImage(current, 0, 0, null);
//		
//		List<IFSDensityModel> models = model.models();
//		for(int i = 0; i < models.size(); i++)
//		{
//			current = DensityModels.drawData(models.get(i), samples, res, log);			
//			graphics.drawImage(current, (i+1) * res, 0, null);
//		}		
//
//		for(int i = 0; i < numRandom; i++)
//		{
//			current = Datasets.drawDataset(model.randomInstance(samples, depth), res, log);			
//			graphics.drawImage(current, (i + 1 + models.size()) *res, 0, null);
//		}
//
//		return result;
//	}	
	
	public static DiscreteRIFS<Similitude> initialSphere(int dim, int compIFS, int compMaps, double radius, double scale)
	{
		DiscreteRIFS<Similitude> rifs = null;
		for(int i : series(compIFS))
		{
			IFS<Similitude> ifs =  IFSs.initialSphere(dim, compMaps, radius, scale);
			if(rifs == null)
				rifs = new DiscreteRIFS<Similitude>(ifs, 1.0);
			else
				rifs.addModel(ifs, 1.0);
		}
		
		return rifs;
	}
	
	public static DiscreteRIFS<Similitude> initialLearn(
			List<List<Point>> data, int compIFS, int compMaps,
			int generations, int depth, int sample, double spread 
			)
	{
		List<Point> flat = new ArrayList<Point>();
		for(List<Point> set : data)
			flat.addAll(set);
		
		int compTot = compIFS * compMaps;
		
		IFS<Similitude> initial = IFSs.initialSphere(
				flat.get(0).dimensionality(), compTot, 0.7, 0.1);
		
		EM<Similitude> em = new SimEM(initial, flat, 1, spread);
		for(int i : Series.series(generations))
			em.iterate(sample, depth);
		
		IFS<Similitude> meanModel = em.model();
				
		int c = 0;
		DiscreteRIFS<Similitude> rifs = null;
		for(int i : series(compIFS))
		{
			IFS<Similitude> compModel = null;
			double totalPrior = 0.0;
			
			for(int j : series(compMaps))
			{
				Similitude sim = meanModel.get(c);
				double prior = meanModel.probability(c);
				
				if(compModel == null)
					compModel = new IFS<Similitude>(sim, prior);
				else
					compModel.addMap(sim, prior);
				
				totalPrior += prior;
				c ++;
			}
			
			if(rifs == null)
				rifs = new DiscreteRIFS<Similitude>(compModel, totalPrior);
			else
				rifs.addModel(compModel, totalPrior);
		}
		
		return rifs;
	}
}
