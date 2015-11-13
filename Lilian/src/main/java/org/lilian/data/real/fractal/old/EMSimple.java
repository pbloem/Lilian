//package org.lilian.data.real.fractal.old;
//package org.lilian.data.real.fractal;
//
//import static org.lilian.util.Functions.choose;
//import static org.lilian.util.MatrixTools.getDeterminant;
//import static org.lilian.util.Series.series;
//
//import java.awt.AlphaComposite;
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.util.AbstractList;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.imageio.ImageIO;
//
//import org.apache.commons.math.linear.Array2DRowRealMatrix;
//import org.apache.commons.math.linear.ArrayRealVector;
//import org.apache.commons.math.linear.RealMatrix;
//import org.apache.commons.math.linear.RealVector;
//import org.lilian.Global;
//import org.lilian.data.real.AffineMap;
//import org.lilian.data.real.Datasets;
//import org.lilian.data.real.Draw;
//import org.lilian.data.real.MVN;
//import org.lilian.data.real.Point;
//import org.lilian.data.real.Rotation;
//import org.lilian.data.real.Similitude;
//import org.lilian.data.real.fractal.EMOne.PreModel;
//import org.lilian.data.real.weighted.Weighted;
//import org.lilian.data.real.weighted.WeightedLists;
//import org.lilian.search.Parameters;
//import org.lilian.util.BitString;
//import org.lilian.util.Functions;
//import org.lilian.util.MatrixTools;
//
//public class EMSimple
//{	
//	public static final boolean DEBUG = true;
//	public File DEBUG_DIR = new File(".");
//	
//	public static final double DEV_START = 0.00001;
//	public static final double DEV_MIN = 10.0 * Functions.EPS;
//	private static final double PERTURB_VAR = 0.03;
//	
//	private List<Point> data;
//
//	private IFS<Similitude> model;
//	
//	private List<Double> priors;
//	private BitString useBackup;
//
//	private List<Point> sample;
//
//	private List<List<RealMatrix>> p;
//
//	private MVN basis;
//
//	private List<List<Double>> devs;
//	private int iterations = 0;
//	private int numDepths;
//	
//	private int numComponents;
//	private int dim;
//	
//	public EMSimple(List<Point> data, IFS<Similitude> model, int maxDepth)
//	{
//		this.data = data;
//		this.model = model;
//		this.numComponents = model.size();
//		this.dim = model.dimension();
//		this.numDepths = maxDepth;
//		
//		basis = MVN.find(data);
//		
//		
//		devs = new ArrayList<List<Double>>(numDepths);
//		for(int d : series(numDepths))
//		{
//			devs.add(new ArrayList<Double>());
//			for(int k : series(model.size()))
//				devs.get(d).add(DEV_START);
//		}
//	}
//	
//	public void iterate(int sampleSize, int depthSampleSize)
//	{
//		sample(sampleSize);
//		
//		expectation();
//		maximization(depthSampleSize);
//	}
//	
//	
//	public void sample(int sampleSize)
//	{
//		sample = sampleSize == -1 ? sample = data : Datasets.sample(data, sampleSize);
//	}
//	
//	public void expectation()
//	{
//		checkBackups();
//
//		System.out.println("backups " + useBackup);
//		
//		p = new ArrayList<List<RealMatrix>>(); 
//		for(int d : series(numDepths))
//		{
//			p.add(new ArrayList<RealMatrix>());
//			for(int k : series(numComponents))
//				p.get(d).add(new Array2DRowRealMatrix(sample.size(), sample.size()));
//		}
//
//		for(int d : series(numDepths))
//			for(int iFrom : series(sample.size()))
//			{
//				double fromDensity = useBackup.get(d) ? 
//						IFS.search(model, sample.get(iFrom), d, basis()).approximation() : 
//						Math.log(IFS.density(model, sample.get(iFrom), d, basis()));
//										
//				for(int iTo : series(sample.size()))
//					for(int k : series(numComponents))
//					{
//						double toDensity = MVN.logDensity(
//								sample.get(iTo), 
//								model.get(k).map(sample.get(iFrom)),
//								devs.get(d).get(k));
//						
//						p.get(d).get(k).setEntry(iTo, iFrom, fromDensity + toDensity);
//					}	
//			}
//		
//		// row-normalize p
//		for(int d : series(numDepths))
//			for(int iTo : series(sample.size()))
//			{
//				double [][] rows = new double[numComponents][];
//				for(int k : series(numComponents))
//					rows[k] = p.get(d).get(k).getRow(iTo);
//				
//				rows = normalizeLog(rows, Math.E);
//				for(int k : series(numComponents))
//					p.get(d).get(k).setRow(iTo, rows[k]);
//			}
//	
//		if(DEBUG)
//		{
////			for(int d : series(numDepths))
////				for(int k : series(numComponents))
////				{
////					System.out.println(d + " -- " + k);
////					
////					System.out.println(MatrixTools.toString(p.get(d).get(k), 3));
////				}
//		
//
//			debug();
//		}
//	}
//	
//	private void checkBackups()
//	{
//		useBackup = BitString.zeros(numDepths);
//		for(int d : series(numDepths))
//			useBackup.set(d, useBackup(d + 1));
//	}
//	
//	private boolean useBackup(int depth)
//	{
//		for(Point x : sample)
//			if(IFS.density(model, x, depth, basis()) > 0.0)
//				return false;
//	
//		return true;
//	}
//	
//	public void maximization(int depthSampleSize)
//	{
//		// * Compute the component maps 
//		List<PreModel> models = new ArrayList<PreModel>(numDepths);
//		for(int d : series(numDepths))
//		{
//			PreModel pre = new PreModel();
//			models.add(pre);
//			
//			for(int component : series(numComponents))
//			{
//				HashMap<String, Double> extras = new HashMap<String, Double>();				
//				Similitude map = Similitude.find(sample, sample, p.get(d).get(component), extras);
//				
//				if(map != null)
//				{
//					devs.get(d).set(component, Math.max(extras.get("std dev"), DEV_MIN));
//				} else
//					devs.get(d).set(component, DEV_START);
//			
//				pre.addMap(map, 1.0);
//			}
//		}
//		
//		if(DEBUG)
//		{
//			File dir = new File(DEBUG_DIR, String.format("%04d/", iterations));
//			dir.mkdirs();
//			
//			for(int i : series(models.size()))
//				if(!hasNull(models.get(i)))
//				{
//					BufferedImage image = Draw.draw(
//							models.get(i).ifs(), 100000, 
//							new double[]{-1.0, 1.0},new double[]{-1.0, 1.0}, 
//							1000, 1000, 
//							true, (double)i+1, basis());
//					
//					try
//					{
//						ImageIO.write(image, "PNG", new File(dir, String.format("model-depth-%d.png", i)));
//					} catch (IOException e)
//					{
//						throw new RuntimeException(e);
//					}
//				} else {
//					Global.log().info("null at depth " + i + " " + models.get(i));
//				}
//		}
//		
//		Global.log().info("standard deviations: " + devs);
//		
//		// * Compute the priors
//		List<Double> priors = new ArrayList<Double>(numComponents);
//		if(depthSampleSize < 0 || allContainNull(models))
//		{
//			for(int i : series(models.size()))
//				priors.add(1.0);
//		} else
//		{
//			List<Point> sample = Datasets.sample(data, depthSampleSize);
//			
//			for(int d : series(numDepths))
//			{
//				PreModel m = models.get(d);
//				if(! hasNull(m))
//				{
//					double logLikelihood = IFS.logLikelihood(sample, m.ifs(), d+1, basis());
//					priors.add(logLikelihood);
//				} else 
//				{
//					priors.add(Double.NEGATIVE_INFINITY);
//				}
//			}
//			
//			Global.log().info("logpriors: " + priors);
//			priors = Functions.normalizeLog(priors, Math.E);
//		}
//		
//		Global.log().info("priors: " + priors);
//		
//		PreModel preModel = new PreModel();
//		for(int component : series(numComponents))
//		{
//			List<Similitude> comps = new ArrayList<Similitude>(models.size());
//			List<Double> weights = new ArrayList<Double>(models.size());
//			for(int d : series(models.size()))
//			{
//				comps.add(models.get(d).get(component));
//				weights.add(models.get(d).weight(component));
//			}
//			
//			// * combine the maps
//			Similitude combinedMap = combine(comps, priors);
//			
//			// * combine the weights
//			double priorSum = 0.0;
//			for(int d : series(models.size()))
//				if(models.get(d) != null)
//					priorSum += priors.get(d);
//			
//			double combinedWeight = 0.0;
//			for(int d : series(models.size()))
//				if(models.get(d) != null)
//					combinedWeight += weights.get(d) * priors.get(d)/priorSum;
//
//			preModel.addMap(combinedMap, combinedWeight);
//		}		
//			
//		model = checkNullComponents(preModel);
//				
//		iterations++;
//	}
//	
//	private IFS<Similitude> checkNullComponents(PreModel pre)
//	{
//		if(allNull(pre))
//		{
//			
//			for(int d : series(numDepths))
//				for(int k : series(numComponents))
//					System.out.println(MatrixTools.toString(p.get(d).get(k), 1));
//			
//			throw new IllegalStateException("All components in model are null.");
//		}
//	
//		if(!hasNull(pre))
//			return pre.ifs();
//		
//		String model;
//			
//		// * collect the good components
//		List<Integer> good = new ArrayList<Integer>(numComponents),
//		              bad  = new ArrayList<Integer>(numComponents);
//		
//		for(int k : series(numComponents))
//			if(pre.get(k) != null)
//				good.add(k);
//			else
//				bad.add(k);
//		
//		Global.log().info("Bad components: " + bad + ", good:" + good);
//		
//		// * Assign each bad component to a good one
//		Map<Integer, List<Integer>> map = new LinkedHashMap<Integer, List<Integer>>();
//		for(int k : series(numComponents))
//			if(pre.get(k) == null)
//			{
//				int rGood = choose(good);
//				if(! map.containsKey(rGood))
//				{
//					map.put(rGood, new ArrayList<Integer>(numComponents));
//					map.get(rGood).add(rGood);
//				}
//				
//				map.get(rGood).add(k);
//			}
//		
//		for(List<Integer> comps : map.values())
//		{
//			Similitude initialComponent = pre.get(comps.get(0));
//			double initialWeight = pre.weight(comps.get(0));
//			
//			for(int i : comps)
//			{
//				// * Create a new, perturbed component 
//				Similitude component = Parameters.perturb(initialComponent, Similitude.similitudeBuilder(dim), PERTURB_VAR);
//				double weight = initialWeight / numComponents;
//				
//				pre.set(i, component, weight);
//			}
//		}
//		
//		return pre.ifs();
//	}
//	
//
//	
//	public Similitude combine(List<Similitude> maps, List<Double> weights)
//	{
//		double wSum = 0;
//		for(int i : series(maps.size()))
//			if(maps.get(i) != null)
//				wSum += weights.get(i);
//		 
//		RealMatrix mat = new Array2DRowRealMatrix(dim, dim);
//		double scalar = 0.0;
//		RealVector vect = new ArrayRealVector(dim);
//		
//		boolean allNull = true;
//		for(int i : series(maps.size()))
//		{
//			if(maps.get(i) != null)
//			{
//				
//				RealMatrix rotation = Rotation.toRotationMatrix(maps.get(i).angles());
//
//				mat = mat.add(rotation.scalarMultiply(weights.get(i)/wSum));
//				vect = vect.add(maps.get(i).getTranslation().mapMultiply(weights.get(i)/wSum));
//				scalar += maps.get(i).scalar() * (weights.get(i)/wSum);
//								
//				allNull = false;
//			}
//		}
//		
//		if(allNull)
//			return null;
//				
//		List<Double> angles = Rotation.findAngles(mat);
//
//		return new Similitude(scalar, new Point(vect), new Point(angles));		
//	}
//	
//	public IFS<Similitude> model()
//	{
//		return model;
//	}
//
//	public MVN basis()
//	{
//		return basis;
//	}
//	
//	public int dimension()
//	{
//		return data.get(0).dimensionality();
//	}
//	
//	private static double[][] normalize(double[][] logs)
//   	{ 
//    	double[][] ll = new double[logs.length][];
//    	for(int i : series(logs.length))
//    		ll[i] = new double[logs[0].length];
//   	
//    	for(int m : series(ll.length))
//    		for(int i : series(ll[0].length))
//    			ll[m][i] = logs[m][i];
//    	
//    	double sum = 0.0;
//    	for(int m : series(ll.length))
//    		for(int i : series(ll[0].length))
//    			sum += ll[m][i];
//    	
//    	for(int m : series(ll.length))
//    		for(int i : series(ll[0].length))
//    			ll[m][i] = ll[m][i]/sum;;
//    		
//    	return ll;
//   	}
//	
//	private static double[][] normalizeLog(double[][] logs, double base)
//   	{    	    	
//    	// We're implementing this algorithm:
//    	// http://stats.stackexchange.com/questions/66616/converting-normalizing-very-small-likelihood-values-to-probability
//    	
//    	double[][] ll = new double[logs.length][];
//    	for(int i : series(logs.length))
//    		ll[i] = new double[logs[0].length];
//    	
//    	for(int m : series(ll.length))
//    		for(int i : series(ll[0].length))
//    			ll[m][i] = logs[m][i];
//    	
//    	double maxLog = Double.NEGATIVE_INFINITY;
//    	for(int m : series(ll.length))
//    		for(int i : series(ll[0].length))
//    			maxLog = Math.max(ll[m][i], maxLog);
//    	
//    	if(maxLog == Double.NEGATIVE_INFINITY)
//    		return uniform(logs);
//    	
//    	for(int m : series(ll.length))
//    		for(int i : series(ll[0].length))
//    			ll[m][i] -= maxLog;
//    	    	
//    	for(int m : series(ll.length))
//    		for(int i : series(ll[0].length))
//    			ll[m][i] = Math.pow(base, ll[m][i]);
//    	
//    	// * No need to check for underflow. Java sets to 0 automatically
//    	
//    	double sum = 0.0;
//    	for(int m : series(ll.length))
//    		for(int i : series(ll[0].length))
//    			sum += ll[m][i];
//    	
//    	for(int m : series(ll.length))
//    		for(int i : series(ll[0].length))
//    			ll[m][i] = ll[m][i]/sum;;
//    		
//    	return ll;
//    }
//	
//	public static double[][] uniform(double[][] logs)
//	{
//    	double[][] ll = new double[logs.length][];
//    	int n = 0;
//    	for(int i : series(logs.length))
//    	{
//    		ll[i] = new double[logs[0].length];
//    		n += logs[0].length;
//    	}
//    	
//    	for(int i : series(ll.length))
//    		for(int j : series(ll[i].length))
//    			ll[i][j] = 1.0/n;
//    
//		return ll;
//	}
//	
//	/**
//	 * Lets us store the ingredients of a model, but allows null values
//	 * @author Peter
//	 *
//	 */
//	protected class PreModel extends AbstractList<Similitude>
//	{
//		List<Similitude> maps = new ArrayList<Similitude>(numComponents);
//		List<Double> weights = new  ArrayList<Double>(numComponents);
//		
//		public PreModel()
//		{
//		}
//		
//		public void addMap(Similitude map, double weight)
//		{
//			maps.add(map);
//			weights.add(weight);
//		}
//		
//		public Similitude get(int i)
//		{
//			return maps.get(i);
//		}
//		
//		public void set(int i, Similitude map, double weight)
//		{			
//			maps.set(i, map);
//			weights.set(i, weight);
//		}
//		
//		public double weight(int i)
//		{
//			return weights.get(i);
//		}
//		
//		public IFS<Similitude> ifs()
//		{
//			IFS<Similitude> model =  null;
//			for(int i: series(maps.size()))
//				if(model == null)
//					model = new IFS<Similitude>(maps.get(i), weights.get(i));
//				else
//					model.addMap(maps.get(i), weights.get(i));
//			
//			return model;
//		}
//
//		@Override
//		public int size()
//		{
//			return maps.size();
//		}
//	}
//	
//	private boolean containsNaN(double[][] in)
//	{
//		for(double[] row : in)
//			for(double d : row)
//				if(Double.isNaN(d))
//					return true;
//		
//		return false;
//	}
//	
//	private <T> boolean allNull(List<T> list)
//	{
//		for(T o : list)
//			if(o != null)
//				return false;
//		return true;
//	}
//	
//	private <T> boolean hasNull(List<T> list)
//	{
//		for(T o : list)
//			if(o == null)
//				return true;
//		
//		return false;
//	}
//	
//	private boolean allContainNull(List<PreModel> models)
//	{
//		for(PreModel model : models)
//			if(!hasNull(model))
//				return false;
//		return true;
//	}
//	
//	private void debug()
//	{	
//		for(int k : series(numComponents))
//			for(int d : series(numDepths))
//				debug(p.get(d).get(k), null, String.format("p.%d.%d",d, k));		
//	}
//	
//	public void debug(RealMatrix cor, org.lilian.data.real.Map map, String name)
//	{
//		File dir = new File(DEBUG_DIR, String.format("debug-%04d/", iterations));
//		dir.mkdirs();
//		
//		RealVector fromWeights = cor.preMultiply(new ArrayRealVector(cor.getRowDimension(), 1.0));
//		RealVector toWeights = cor.operate(new ArrayRealVector(cor.getColumnDimension(), 1.0));
//
//
//		Weighted<Point> pointsFrom = WeightedLists.combine(sample, l(fromWeights));
//		
//		if(map != null)
//			pointsFrom = WeightedLists.map(map, pointsFrom);
//		Weighted<Point> pointsTo = WeightedLists.combine(sample, l(toWeights));
//		
//		try
//		{
//			debug(new File(dir, name+".png"), pointsFrom, pointsTo);
//		} catch (IOException e)
//		{
//			throw new RuntimeException(e);
//		}
//	}
//	
//	private List<Double> l(RealVector v)
//	{
//		List<Double> res = new ArrayList<Double>(v.getDimension());
//		for(int i : series(v.getDimension()))
//			res.add(v.getEntry(i));
//		
//		return res;
//	}
//	
//	public static void debug(File file, Weighted<Point> from, Weighted<Point> to)
//			throws IOException
//		{
//			int res = 250;
//			
//			BufferedImage fromImage = Draw.draw(from, res, false),
//			              toImage   = Draw.draw(to, res, false);
//			
//			BufferedImage result = new BufferedImage(res*2, res, BufferedImage.TYPE_INT_ARGB);
//			Graphics2D graphics = result.createGraphics();
//			
//			graphics.setBackground(Color.white);
//			graphics.clearRect(0, 0, result.getWidth(), result.getHeight());		
//			
//			graphics.setComposite(AlphaComposite.SrcAtop);
//			
//			// * Colorize
//			fromImage = Draw.colorize(Color.red).filter(fromImage, null);
//			graphics.drawImage(fromImage, 0, 0, null);
//			
//			toImage = Draw.colorize(Color.green).filter(toImage, null);
//			graphics.drawImage(toImage, res, 0, null);
//		
//			graphics.dispose();
//			
//			ImageIO.write(result, "PNG", file);
//		}	
//
//}
