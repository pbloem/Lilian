package org.lilian.data.real.fractal.old;
//package org.lilian.data.real.fractal;
//
//import static java.lang.Math.exp;
//import static java.lang.Math.pow;
//import static org.lilian.util.Functions.choose;
//import static org.lilian.util.MatrixTools.getDeterminant;
//import static org.lilian.util.MatrixTools.multiplyByElement;
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
//import java.util.Arrays;
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
//import org.lilian.data.real.weighted.Weighted;
//import org.lilian.data.real.weighted.WeightedLists;
//import org.lilian.search.Parameters;
//import org.lilian.util.BitString;
//import org.lilian.util.Functions;
//import org.lilian.util.MatrixTools;
//import org.lilian.util.Series;
//
//public class EMOne
//{
//	public static final boolean DEBUG = true;
//	public File DEBUG_DIR = new File(".");
//
//	
//	public static final double DEV_START = 0.001;
//	public static final double DEV_MIN = 10.0 * Functions.EPS;
//	private static final double PERTURB_VAR = 0.03;
//	private List<Point> data;
//	
//	private IFS<Similitude> model;
//	private List<Double> priors;
//	
//	private List<Point> sample;
//	
//	// * Summed likelihood for each point in the sample. One for each depth.
//	private List<RealVector> sums;
//	// * Sums for the approximation 
//	private List<RealVector> sumsBackup;
//	// * Whether to use the backup likelihood for a given depth
//	private BitString useBackup;
//	
//	private List<RealMatrix> pCoding;
//	private List<RealMatrix> pRegistration;
//	private List<RealMatrix> p;
//	
//	private MVN basis;
//	
//	private List<Double> devs;
//	private int iterations = 0;
//	private int numDepths;
//	
//	private int numComponents;
//	private int dim;
//	
//	public EMOne(List<Point> data, IFS<Similitude> model, int maxDepth)
//	{
//		this.data = data;
//		this.model = model;
//		this.numComponents = model.size();
//		this.dim = model.dimension();
//		this.numDepths = maxDepth;
//		
//		basis = MVN.find(data);
//		
//		devs = new ArrayList<Double>();
//		for(int k : series(model.size()))
//			devs.add(DEV_START);
//	}
//
//	public void iterate(int sampleSize, int depthSampleSize)
//	{
//		modelToCodes(sampleSize);
//		codesToModel(depthSampleSize);
//	}
//	
//	public void modelToCodes(int sampleSize)
//	{
//		sample = Datasets.sample(data, sampleSize);
//				
//		pRegistration = new ArrayList<RealMatrix>(numDepths);
//		for(int k : series(model.size()))
//			pRegistration.add(new Array2DRowRealMatrix(sampleSize, sampleSize).scalarAdd(Double.NEGATIVE_INFINITY));
//			
//		
//		pCoding = new ArrayList<List<RealMatrix>>(); 
//		for(int d : series(numDepths))
//		{
//			pCoding.add(new ArrayList<RealMatrix>());
//			for(int k : series(model.size()))
//				pCoding.get(d).add(new Array2DRowRealMatrix(sampleSize, sampleSize));
//		}
//		
//		p = new ArrayList<RealMatrix>();
//
//		sums = new ArrayList<RealVector>(numDepths);
//		sumsBackup = new ArrayList<RealVector>(numDepths);
//		
//		for(int d : series(numDepths))
//		{
//			sums.add(new ArrayRealVector(sampleSize, Double.NEGATIVE_INFINITY));
//			sumsBackup.add(new ArrayRealVector(sampleSize, Double.NEGATIVE_INFINITY));
//		}
//		
//		useBackup = BitString.zeros(numDepths);
//		
//		// * Find the summed likelihood for each point over all endpoint distributions
//		findSums(AffineMap.identity(dim), 0);
//		// - normalize sums
//		for(int d : series(numDepths))
//			if(contains(sums.get(d), Double.NEGATIVE_INFINITY))
//				useBackup.set(d, true);
//		
//		System.out.println("backups " + useBackup);
//		
//		// * Compute the coding matrix for each depth
//		findCoding(AffineMap.identity(dim), null, 0);
//		
//		
//		// * Compute the registration matrix for each depth
//		findRegistration();
//		// - normalize the registration matrix over columns
//		for(int k : series(numComponents))
//			for(int iTo : series(sampleSize))
//			{
//				double[] row = pRegistration.get(k).getRow(iTo);
//				row = Functions.normalizeLog(row, Math.E);
//				pRegistration.get(k).setRow(iTo, row);
//			}
//		
//		// * Create the final ps by multiplying the coding and registration matrices
//		for(int d : series(numDepths))
//			for(int k : series(numComponents))
//				p.get(d)
//					.add(
//						multiplyByElement(
//							pCoding.get(d).get(k), 
//							pRegistration.get(k)));
//		
//		// - normalize p column-wise
//		for(int d : series(numDepths))
//			for(int iFrom : series(sampleSize))
//			{
//				double [][] rows = new double[numComponents][];
//				for(int k : series(numComponents))
//					rows[k] = p.get(d).get(k).getColumn(iFrom);
//				
//				row = normalize(rows);
//				for(int k : series(numComponents))
//					p.get(d).get(k).setColumn(iFrom, rows[k]);
//			}
//				
//		if(DEBUG)
//		{
//			debug();
//			
//			System.out.println(useBackup);
//			System.out.println(sums);
//			System.out.println(sumsBackup);
//			
//			for(int k : series(numComponents))
//			{
//				// System.out.println(MatrixTools.toString(pRegistration.get(k), 1));
//				// System.out.println();
//			}
//			
//			for(int d : series(numDepths))
//			{
//				System.out.println("--- DEPTH " + d);
//				
//				for(int k : series(numComponents))
//				{
//					// System.out.println(MatrixTools.toString(pCoding.get(d).get(k), 1));
//					// System.out.println();
//					
//					// System.out.println(MatrixTools.toString(p.get(d).get(k), 1));
//				}
//			}
//		}
//	}
//
//	private boolean contains(RealVector v, double d)
//	{
//		for(int i : series(v.getDimension()))
//			if(v.getEntry(i) == d)
//				return true;
//		
//		return false;
//	}
//
//	private void findSums(AffineMap map, int d)
//	{
//		
//		for(int m : series(numComponents))
//		{
//			AffineMap newMap = (AffineMap) map.compose(model.get(m));
//			
//			MVN mvn = null;
//			try {
//				mvn = basis().transform(newMap);
//			} catch(Exception e) {
//				useBackup.set(d, true);
//			}
//			
//			Point mean = newMap.map(basis().mean());
//			
//			double backupSigma = Math.pow(getDeterminant(newMap.getTransformation()), 1.0/dim);
//			
//			for(int i : series(sample.size()))
//			{
//				if(map != null)
//				{
//					double density = mvn.logDensity(sample.get(i));
//					double val = sums.get(d).getEntry(i);
//					val = Functions.logSum(val, density);
//					sums.get(d).setEntry(i, val);
////					if(i == 0)
////						System.out.println("::: " + i + " " + density + " " + val);
//				}
//				
//				// backup
//				double density = logDensity(sample.get(i), mean, backupSigma);
//				double val = sumsBackup.get(d).getEntry(i);
//				val = Functions.logSum(val, density);
//				sumsBackup.get(d).setEntry(i, val);
//			}
//			
//			if(d < numDepths - 1) // * recurse
//				findSums(newMap, d + 1);
//		}
//	}
//	
//	private void findCoding(AffineMap mapTo, AffineMap mapFrom, int dTo)
//	{
//		for(int m : series(numComponents))
//		{
//			boolean useBackupTo   = useBackup.get(dTo);
//			boolean useBackupFrom = dTo == 0 ? true : useBackup.get(dTo-1);
//			
//			AffineMap newMapTo = (AffineMap) mapTo.compose(model.get(m));
//			AffineMap newMapFrom = (AffineMap)(mapFrom == null ? AffineMap.identity(dim) : mapFrom.compose(model.get(m)));
//			
//			MVN mvnTo = useBackupTo ? null : basis().transform(newMapTo);
//			MVN mvnFrom = useBackupFrom ? null : basis().transform(newMapFrom);
//			
//			Point meanTo   = newMapTo.map(basis().mean());
//			Point meanFrom = dTo == 0 ? basis().mean() : newMapFrom.map(basis().mean());
//			
//			double backupSigmaTo   = pow(getDeterminant(newMapTo.getTransformation())  , 1.0/dim);
//			double backupSigmaFrom = pow(getDeterminant(newMapFrom.getTransformation()), 1.0/dim);
//			
//			
//			RealVector rTo = new ArrayRealVector(sample.size());
//			for(int iTo : series(sample.size()))
//			{
//				Point to = sample.get(iTo);
//				
//				double densityTo = 
//						useBackupTo ? 
//						logDensity(to, meanTo, backupSigmaTo) : 
//						mvnTo.logDensity(to);
//				double sum = 
//						useBackupTo ? 
//						sumsBackup.get(dTo).getEntry(iTo) : 
//						sums.get(dTo).getEntry(iTo); 
//				
//				rTo.setEntry(iTo, exp(densityTo - sum));
//			}
//			
//			RealVector rFrom = new ArrayRealVector(sample.size());
//			for(int iFrom : series(sample.size()))
//			{					
//				Point from = sample.get(iFrom);
//				
//				if(dTo == 0)
//				{
//					rFrom.setEntry(iFrom, 1.0);
//				} else
//				{
//					double densityFrom = 
//							useBackupFrom ?  
//							logDensity(from, meanFrom, backupSigmaFrom) :
//							mvnFrom.logDensity(from);
//					double sum = 
//						useBackupFrom ? 
//						sumsBackup.get(dTo-1).getEntry(iFrom) : 
//						sums.get(dTo-1).getEntry(iFrom);
//						
//					rFrom.setEntry(iFrom, exp(densityFrom - sum));
//				}
//			}
//			
//			System.out.println("component " + m);
//			System.out.println(rFrom);
//			System.out.println(rTo);
//			System.out.println();
//			
//			// * Add the outproduct of the two vectors to the relevant coding matrix
//			RealMatrix p  = pCoding.get(dTo).get(m);
//			p = p.add(rTo.outerProduct(rFrom));
//			pCoding.get(dTo).set(m, p);
//						
//			if(dTo < numDepths - 1) // * recurse
//				findCoding(newMapTo, newMapFrom, dTo + 1);
//		}
//	}
//
//	private void findRegistration()
//	{
//		for(int k : series(numComponents))
//		{
//			AffineMap map = model.get(k);
//
//			for(int iFrom : series(sample.size()))
//				for(int iTo : series(sample.size()))
//				{
//					Point from = sample.get(iFrom);
//					Point to = sample.get(iTo);
//					
//					double density = logDensity(to, map.map(from), devs.get(k)); 
//					
//					pRegistration.get(k).setEntry(iTo, iFrom, density);
//				}
//		}
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
//	public void codesToModel(int depthSampleSize)
//	{
//		
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
//				
//				// System.out.println(MatrixTools.toString(p.get(d).get(component), 1));
//				
//				Similitude map = Similitude.find(sample, sample, p.get(d).get(component), extras);
//				System.out.println("map " + map);
//				
//				if(map != null)
//				{
//					devs.set(component, Math.max(extras.get("std dev"), DEV_MIN));
//				} else
//					devs.set(component, DEV_START);
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
//	private void debug()
//	{	
//		for(int k : series(numComponents))
//		{
//			debug(pRegistration.get(k), model.get(k), String.format("reg.%d", k));
//
//			for(int d : series(numDepths))
//			{
//				debug(pCoding.get(d).get(k), null, String.format("coding.%d.%d",d, k));
//				debug(p.get(d).get(k), null, String.format("final.%d.%d",d, k));
//			}
//		}
//		
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
//	/**
//	 * Density estimate
//	 * @param x
//	 * @param mean
//	 * @param dev
//	 */
//	private static double logDensity(Point x, Point mean, double dev)
//	{
//		int dim = x.dimensionality();
//		
//		double constant = - (dim/2.0) * (LOG2PI + Math.log(dev));
//		double norm = x.getVector().subtract(mean.getBackingData()).getNorm();
//		
//		return constant - (1.0 / (2.0 * dev)) * (norm * norm);
//	}
//	
//	private static final double LOG2PI = Math.log(2.0 * Math.PI);
//	
//	private static double density(Point x, Point mean, double dev)
//	{
//		double norm = x.getVector().subtract(mean.getBackingData()).getNorm();
//		double scalar = 1.0/ Math.sqrt(Math.pow(2.0 * Math.PI*dev, x.dimensionality()));
//		
//		return scalar * Math.exp(- (1.0 / (2.0 * dev)) * (norm * norm));
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
////			System.out.println("from :" + fromWeights);
////			System.out.println("  to :" +   toWeights);
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
//}
