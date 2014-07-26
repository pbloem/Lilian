package org.lilian.data.real.fractal;

import static org.lilian.util.Functions.choose;
import static org.lilian.util.Series.series;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jfree.util.Log;
import org.lilian.Global;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Datasets;
import org.lilian.data.real.Draw;
import org.lilian.data.real.Generators;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.weighted.Weighted;
import org.lilian.data.real.weighted.WeightedLists;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.search.Builder;
import org.lilian.search.Parameters;
import org.lilian.search.Parametrizable;
import org.lilian.search.evo.Target;
import org.lilian.util.Functions;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

/**
 * An EM-style algorithm for learning iterated function systems.
 * <p/>
 * The algorithm is designed from an analogy with the basic EM algorithm for
 * Gaussian mixture models. In that case each point of data is assumed to
 * originate from one of n Gaussian distributions. If we knew which data-point
 * came from which distribution, we could calculate the parameters of each, and
 * if we knew the distribution parameters of the components, we could find the
 * most likely component for each data point. The solution is to start with an
 * arbitrary MOG model, and iterate both steps.
 * <p/>
 * Applying an affine transformation to a Gaussian distribution yields another
 * Gaussian distribution. Thus, if we start with an initial Gaussian
 * distribution, and apply the transformations of a k-component IFS model once,
 * we have a basic k-component Gaussian mixture model. If we apply the IFS
 * transformation again we have a k^2-component Gaussian mixture model, where
 * each component originates from first one application of a chosen
 * transformation, and then another.
 * <p/>
 * This gives us the following situation. At depth d, the IFS model can be seen
 * as a k^d-component Gaussian mixture model, where each component is described
 * by a d-length k-ary code. By analogy with the EM algorithm for MOG models: If
 * we know the transformations of the IFS, we can find the codes for the data
 * points. If we know which component each data point came from (and its code),
 * we can find the transformations. The solution, again, is to start with an
 * arbitrary model, and iterate the steps.
 * </p>
 * How do we find the codes? We simply iterate over all distributions to a given
 * depth and use the one for which the point has the highest likelihood. Since
 * the (log) likelihood goes to zero quite quickly with the depth, and for a bad
 * model points may lay far off the distribution's support, we revert to
 * distance to the mean as an approximation of likelihood of the data point has
 * likelihood 0 for all models. 
 * </p> How do we find the transformations given
 * the codes? We look for point pairs with codes like a=1012220 and b=012220?.
 * Since b is a left-shifted version of a, we know that applying transformation
 * 1 to b will yield a (with an error that decreases with the depth). We collect
 * all such points for each transformation (and at all depths) and find the
 * optimal mapping between them. </p> <h2>Details:</h2>
 * <ul>
 * <li>If there are many points to the same code, we risk losing rotational
 * information if we just use the mean. Instead we find an MVN model for the
 * points and map specific points drawn from the standard MVN to this MVN. We do
 * the same for the matching code.</li>
 * <li>
 * For a proper IFS model each transformation also has a weight. We find this by
 * comparing the sizes (in number of points) of matching code pairs. (see the
 * code for the details).</li>
 * <li>
 * If insufficient point received the correct codes for one of the components,
 * we 'split' one of the existing components by generating two slightly
 * perturbed versions and assigning one the original and one the left-over code.
 * </li>
 * <li>
 * The training data should be centered on the origin for the algorithm to work.
 * The best strategy is to use {@link Maps.centered()} and {@MappedList
 * } to created a centered version of the data. the inverse of this
 * map can be used to map points back to data space if necessary.</li>
 * </ul>
 * 
 * @author Peter
 * 
 */
public abstract class EM<M extends org.lilian.data.real.Map & Parametrizable> implements Serializable
{
	private static final boolean DEBUG = true; 
	public File DEBUG_DIR = new File(".");
	
	public static final int DEPTH_SAMPLE = 1000;
	
	public static boolean UNIFORM_DEPTH = false;
	
	// * Serial ID
	private static final long serialVersionUID = 774467486797440172L;

	// * Any component whose contraction rate is lower than this value is
	//   perturbed
	private static final double CONTRACTION_THRESHOLD = 1E-10;

	// * If a code contains more than this number of points, we consider its
	//   covariance (by fitting an MVN as described above).
	private static final int COVARIANCE_THRESHOLD = -1;
	
	private static final boolean PERTURB_UNASSIGNED = true;
	
	private static final double ALPHA = 0.01;

	// * Left-over components (with no data assigned) are given to
	//   perturbed copies of other components. They are perturbed by adding
	//   Gaussian noise with the given variance to their parameters.
	private double perturbVar;

	// * This makes the algorithm a true MoG EM at depth=1 but may make it less
	//   good at finding rotations.
	private boolean useSphericalMVN;

	private List<Point> data;
	private MVN basis;

	protected IFS<M> model;

	protected int numComponents;
	protected int dimension;
	
	// * The maximum number of endpoints to distribute responsibility over
	private int maxSources; 

	// * Root node of the code tree
	private Node root;

	// * Used for perturbing IFS models
	private Builder<M> mapBuilder;
	protected Builder<IFS<M>> ifsBuilder;
	
	private int iterations = 0;
	
	protected double lastDepth = -1;
	
	// * The variance of the mvn from which the spanning point are sampled when 
	//   there are many points to a single code.
	//   For high values the algorithm tends to behave more deterministically,
	//   but some maps with strong rotation won't be found. 
	protected double spanningPointsVariance;

	/**
	 * Sets up the EM algorithm with a given initial model.
	 * 
	 */
	public EM(IFS<M> initial, List<Point> data, int numSources, Builder<M> builder, double spanningPointsVariance)
	{
		this(initial, data, numSources, 0.3, 
				true, 
			builder, spanningPointsVariance);
	}

	/**
	 * Sets up the EM algorithm with a given initial model.
	 * 
	 */
	public EM(IFS<M> initial, List<Point> data, int maxSources, double perturbVar,
			boolean useSphericalMVN, Builder<M> builder, double spanningPointsVariance)
	{
		this.maxSources = maxSources;
		
		this.numComponents = initial.size();
		this.dimension = initial.dimension();
		this.data = data;
		this.useSphericalMVN = useSphericalMVN;
		this.perturbVar = perturbVar;

		if (dimension != data.get(0).dimensionality())
			throw new IllegalArgumentException("Data dimension ("
					+ data.get(0).dimensionality()
					+ ") must match initial model argument (" + dimension + ")");

		model = initial;

		root = new Node(-1, null);

		this.mapBuilder = builder;
		this.ifsBuilder = IFS.builder(numComponents, mapBuilder);

		basis = useSphericalMVN ? MVN.findSpherical(data) : MVN.find(data);
		this.spanningPointsVariance = spanningPointsVariance;
	}

	public void iterate(int sampleSize, double depth)
	{
		modelToCodes(sampleSize, depth);
		codesToModel();
	}

	/**
	 * Samples a subset of the data, converts it to codes and
	 * 
	 * @param sampleSize
	 * @param depth
	 */
	protected void modelToCodes(int sampleSize, double depth)
	{

		List<Point> sample = sampleSize == -1 ? data : Datasets.sample(data,
				sampleSize);

		root = new Node(-1, null); // Challenging for the GC, but should be
								   // fine...

		for (Point point : sample)
		{
			Weighted<List<Integer>> codes = codes(point, model, depth, maxSources);
			// System.out.println(codes);
						
			for(int i : series(codes.size()))
				root.observe(codes.get(i), point, codes.probability(i));
		}
		
		lastDepth = depth;
	}
	
	
	protected abstract Weighted<List<Integer>> codes(
			Point point, IFS<M> model, double depth, int sources);
	
	
	protected abstract M findMap(List<Point> from, List<Point> to);
	
	protected abstract double logLikelihood(List<Point> sample, IFS<M> model, int depth);

	protected void codesToModel()
	{
		// * Look for matching codes in the code tree
		Maps maps = findMaps();

		// * A model for every depth
		List<PreModel> models = new ArrayList<PreModel>(maps.depth());
		for(int d : series(1, maps.depth() + 1))
			models.add(codesToModel(d, maps));
		
		if(DEBUG)
		{
			File dir = new File(DEBUG_DIR, String.format("%04d/", iterations));
			dir.mkdirs();
			
			for(int i : series(models.size()))
				if(!hasNull(models.get(i)))
				{
					BufferedImage image = Draw.draw(models.get(i).ifs(), 100000, 1000, true);
					try
					{
						ImageIO.write(image, "PNG", new File(dir, String.format("%d.png", i)));
					} catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				} else {
					Global.log().info("null at depth " + i);
				}
		}
		
		List<Double> priors = new ArrayList<Double>(numComponents);
		if(UNIFORM_DEPTH)
		{
			for(int i : series(models.size()))
				priors.add(1.0);
		} else
		{
			List<Point> sample = Datasets.sample(data, DEPTH_SAMPLE);
			
			for(PreModel m : models)
			{
				if(! hasNull(m))
				{
					double likelihood = logLikelihood(sample, m.ifs(), models.size());
					priors.add(likelihood);
				} else 
				{
					priors.add(Double.NEGATIVE_INFINITY);
				}
			}
			
			priors = Functions.normalizeLog(priors, Math.E);
		}
		
		Global.log().info("priors: " + priors);
		
		PreModel preModel = null;
		for(int component : series(numComponents))
		{
			List<M> comps = new ArrayList<M>(models.size());
			List<Double> weights = new ArrayList<Double>(models.size());
			for(int d : series(models.size()))
			{
				comps.add(models.get(d).get(component));
				weights.add(models.get(d).weight(component));
			}
			
			// * combine the maps
			M combinedMap = combine(comps, weights);
			
			// * combine the weights
			double priorSum = 0.0;
			for(int d : series(models.size()))
				if(models.get(d) != null)
					priorSum += priors.get(d);
			
			double combinedWeight = 0.0;
			for(int d : series(models.size()))
				if(models.get(d) != null)
					combinedWeight += weights.get(d) * priors.get(d)/priorSum;
			
			if(preModel == null)
				preModel = new PreModel(combinedMap, combinedWeight);
			else
				preModel.addMap(combinedMap, combinedWeight);
		}
		
		model = checkNullComponents(preModel);
		
		iterations++;
	}
	
	
	private IFS<M> checkNullComponents(PreModel pre)
	{
		if(allNull(pre))
			throw new IllegalStateException("All components in model are null.");
	
		if(!hasNull(pre))
			return pre.ifs();
		
		String model;
			
		// * collect the good components
		List<Integer> good = new ArrayList<Integer>(numComponents),
		              bad  = new ArrayList<Integer>(numComponents);
		
		for(int k : series(numComponents))
			if(pre.get(k) != null)
				good.add(k);
			else
				bad.add(k);
		
		Global.log().info("Bad components: " + bad + ", good:" + good);
		
		// * Assign each bad component to a good one
		Map<Integer, List<Integer>> map = new LinkedHashMap<Integer, List<Integer>>();
		for(int k : series(numComponents))
			if(pre.get(k) == null)
			{
				int rGood = choose(good);
				if(! map.containsKey(rGood))
				{
					map.put(rGood, new ArrayList<Integer>(numComponents));
					map.get(rGood).add(rGood);
				}
				
				map.get(rGood).add(k);
			}
		
		for(List<Integer> comps : map.values())
		{
			M initialComponent = pre.get(comps.get(0));
			double initialWeight = pre.weight(comps.get(0));
			
			for(int i : comps)
			{
				// * Create a new, perturbed component 
				M component = Parameters.perturb(initialComponent, mapBuilder, perturbVar);
				double weight = initialWeight / numComponents;
				
				pre.set(i, component, weight);
			}
		}
		
		return pre.ifs();
	}
	
	private boolean hasNull(PreModel model)
	{
		for(M map : model)
			if(map == null)
				return true;
		return false;
	}
	
	private boolean allNull(PreModel model)
	{
		for(M map : model)
			if(map != null)
				return false;
		return true;
	}

	protected PreModel codesToModel(int depth, Maps maps)
	{
		// * Frequencies of components over all codes
		BasicFrequencyModel<Integer> priors = new BasicFrequencyModel<Integer>();
		root.count(priors);

		PreModel model = null;

		for (int component : Series.series(numComponents))
			if(model == null)
				model= new PreModel(maps.getMap(component, depth), maps.getWeight(component, depth));
			else
				model.addMap(maps.getMap(component, depth), maps.getWeight(component, depth));			
	
		return model;
	}

	/**
	 * Find the optimal scalar c so that sum (y - c * x)^2 is minimized.
	 * 
	 * For finding IFS weights we have |from| * w = |to| so that y should be the
	 * list of sizes of 'from' nodes.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static double findScalar(List<Double> x, List<Double> y)
	{
		double sumXX = 0.0;
		double sumYX = 0.0;

		for (int i = 0; i < x.size(); i++)
		{
			sumXX += x.get(i) * x.get(i);
			sumYX += y.get(i) * x.get(i);
		}

		return sumYX / sumXX;
	}
	
	/**
	 * Combines the given maps into a linear 
	 * @param maps
	 * @param weights
	 * @return
	 */
	public abstract M combine(List<M> maps, List<Double> weights);

	/**
	 * Returns, for each component, a set of domain and range points that define
	 * the map. The points span mvn distributions of subsets of the data (as
	 * defined by codes of varying length).
	 * 
	 * @return
	 */
	public Maps findMaps()
	{
		Maps maps = new Maps();
		root.findPairs(maps);
		
		if(DEBUG)
			try
			{
				maps.debug(DEBUG_DIR);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		
		return maps;
	}
	
	/**
	 * @return The current iteration's model.
	 */
	public IFS<M> model()
	{
		return model;
	}

	/**
	 * Set the internal IFS model to the given parameter. The next call of 
	 * iterate or modelToCodes will use this model. 
	 * 
	 * @param model
	 */
	public void setModel(IFS<M> model)
	{
		this.model = model;
	}
	
	/**
	 * @return The multivariate Gaussian model (possibly spherical) that was
	 *         fitted to the data.
	 */
	public MVN basis()
	{
		return basis;
	}
	
	public List<Point> data()
	{
		return data;
	}
	
	/**
	 * A node in the code tree. Each code represents a path in this tree from
	 * root to leaf. At each node, we store each point whose path visits that
	 * node. (ie. the root node contains all points, and each node below the
	 * root contains all points whose code starts with a given symbol).
	 * 
	 * The Node object also contains the search algorithm for matching codes.
	 */
	protected class Node implements Serializable
	{
		// * An MVN fitted to the point stored in this node
		MVN mvn = null;

		// * The parent in the tree
		Node parent;
		// * The child nodes for each symbol (represented by an Integer)
		Map<Integer, Node> children;
		// * How deep this node is in the tree
		int depth = 0;

		// * This node's code
		List<Integer> code;

		// * Whether this node represents a leaf node
		boolean isLeaf = false;

		// * The points stored at this node
		Weighted<Point> points = WeightedLists.empty();

		/**
		 * Create a child node for the given symbol under this parent
		 * 
		 * @param symbol
		 * @param parent
		 */
		public Node(int symbol, Node parent)
		{
			this.parent = parent;
			code = new ArrayList<Integer>(
					parent != null ? parent.code().size() + 1 : 1);

			if (parent != null)
				code.addAll(parent.code());
			if (symbol >= 0)
				code.add(symbol);

			if (parent != null)
				depth = parent.depth + 1;

			children = new HashMap<Integer, Node>();
		}

		/**
		 * Add the symbols of this node and the subtree below it to the given
		 * frequency model.
		 * 
		 * @param model
		 */
		public void count(BasicFrequencyModel<Integer> model)
		{
			if (!isRoot())
				model.add(symbol());

			for (int i : children.keySet())
				children.get(i).count(model);
		}

		/**
		 * Returns whether this node is the root node of the tree.
		 * 
		 * @return
		 */
		public boolean isRoot()
		{
			return parent == null;
		}

		/**
		 * How far from the root this tree is.
		 * 
		 * @return
		 */
		public int depth()
		{
			return depth;
		}

		/**
		 * The code represented by this node
		 */
		public List<Integer> code()
		{
			return code;
		}

		/**
		 * The symbol for this node (ie. the last symbol in its code).
		 * 
		 * @return
		 */
		public int symbol()
		{
			return code.get(code.size() - 1);
		}

		/**
		 * Store the given point at this node, and pass it on to the correct
		 * child.
		 * 
		 * @param codeSuffix
		 *            The suffix of the code after this node
		 * @param point
		 *            The point to be observed.
		 */
		public void observe(List<Integer> codeSuffix, Point point)
		{
			observe(codeSuffix, point, 1.0);
		}
		
		public void observe(List<Integer> codeSuffix, Point point, double weight)
		{
			points.add(point, weight);
			mvn = null; // signal that the mvn needs to be recomputed

			if (codeSuffix.size() == 0)
			{
				isLeaf = true;
				return;
			}

			int symbol = codeSuffix.get(0);
			if (!children.containsKey(symbol))
				children.put(symbol, new Node(symbol, this));

			children.get(symbol).observe(
					codeSuffix.subList(1, codeSuffix.size()), point);
		}

		/**
		 * The points stored at this node
		 * 
		 * @return
		 */
		public List<Point> points()
		{
			return points;
		}

		/**
		 * The number of times this node was visited (ie. the number of points
		 * stored here)
		 */
		public double frequency()
		{
			return points.size();
		}

		/**
		 * @return Whether this node is a leaf node in the tree. A node is a
		 *         leaf if it is at the set maximum depth for this iteration of
		 *         the EM algorithm. It may be that is has no children yet but
		 *         they will be created by the observation of a future code.
		 */
		public boolean isLeaf()
		{
			return isLeaf;
		}

		/**
		 * A multivariate normal distribution fitted to the points stored at
		 * this node
		 * 
		 * Note: returns null if the points for this code form a deficient mvn
		 * model.
		 * 
		 * @return
		 */
		public MVN mvn()
		{
			if (mvn == null)
				try
				{
//					mvn = useSphericalMVN ? MVN.findSpherical(points) : MVN
//							.find(points);

					mvn = MVN.find(points);
				} catch (RuntimeException e)
				{
					// * Could not find proper MVN model
					return null;
				}

			return mvn;
		}
		


		/**
		 * Print a lengthy (multiline) representation of this node to the given
		 * outputstream
		 * 
		 * @param out
		 * @param indent
		 *            The number of tabs to indent with
		 */
		public void print(PrintStream out, int indent)
		{
			String ind = "";
			for (int i : series(indent))
				ind += "\t";

			String code = "";
			for (int i : code())
				code += i;

			out.println(ind + code + " f:" + frequency() + ", p: " + points());
			for (int symbol : children.keySet())
				children.get(symbol).print(out, indent + 1);
		}

		/**
		 * Returns the node for the given code (suffix) (starting from this
		 * node).
		 * 
		 * @param code
		 *            The code suffix for which to find the node starting from
		 *            the current node.
		 * @return the requested Node if it exists. null otherwise.
		 */
		public Node find(List<Integer> code)
		{
			if (code.size() == 0)
				return this;

			int symbol = code.get(0);
			if (!children.containsKey(symbol))
				return null;

			return children.get(symbol).find(code.subList(1, code.size()));
		}

		/**
		 * This method implements the search algorithm for finding code pairs
		 * where the second is a rightshifted version of the first.
		 * 
		 * This method is executed first for all descendants of this node, and 
		 * then for the node itself.
		 * 
		 * It takes this nodes' code and retrieves the node that has the same code, 
		 * but with the first element removed. It then knows that the points of 
		 * that node should map to the points of this node under the 
		 * transformation denoted by the first element of this node's code.
		 */ 
		public void findPairs(Maps maps)
		{
			if (children.size() > 0) // * Recurse
			{
				for (int i : children.keySet())
					children.get(i).findPairs(maps);
			}

			if (code().size() > 0) // * Execute for this node
			{
				List<Integer> codeFrom = new ArrayList<Integer>(code);
				int t = codeFrom.remove(0);

				// * Find the node that matches
				Node nodeFrom = root.find(codeFrom);

				if (nodeFrom != null)
				{				
					int m = Math
							.min(nodeFrom.points().size(), this.points().size());

					MVN from = nodeFrom.mvn(), to = mvn();

					if (from != null & to != null)
					{	
						if (m < COVARIANCE_THRESHOLD) // Not enough points to
												      // consider covariance
						{
							for (int i : series(points.size()))
								maps.add(t, from.mean(), to.mean(), code);
						} else
						{
							AffineMap map = (AffineMap) to.map().compose(from.map().inverse()); 
							
							List<Point> newFrom = new ArrayList<Point>();
							
							for(Point toPoint : points())
							{
								Point bestFrom = null;
								double bestDistance = Double.POSITIVE_INFINITY;
								
								for(Point fromPoint : nodeFrom.points())
								{
									double distance = map.map(fromPoint).distance(toPoint);
									if(distance <= bestDistance)
									{
										bestFrom = fromPoint;
										bestDistance = distance;
									}
								}
															
								newFrom.add(bestFrom);
								maps.add(t, bestFrom, toPoint, code);
							}
							
//							try
//							{
//								File dir = new File("maptest/"+code+"/");
//								dir.mkdirs();
//								
//								debug(new File(dir, "points.png"), nodeFrom.points(), points());
//								debug(new File(dir, "dists.png"), from.generate(100), to.generate(100));
//								debug(new File(dir, iterations+" "+code+".png"), nodeFrom.points(), map.map(nodeFrom.points()));
//								debug(new File(dir, "final.png"), newFrom, points());
//
//							} catch (IOException e)
//							{
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
							

						}
					} else
					{
						// Global.log().info("Points for code " + code +
						// " formed deficient MVN. No points added to pairs.");
					}

					// Register the drop in frequency as the symbol t gets added
					// to the code
					maps.weight(t, nodeFrom.frequency(), this.frequency(), code);
				}
			}
		}

		private List<Point> pair(List<Point> froms, List<Point> tos,
				AffineMap map)
		{			
			List<Point> result = new ArrayList<Point>(tos.size());
			
			for(Point to : tos)
			{
				Point bestFrom = null;
				double bestDistance = Double.POSITIVE_INFINITY;
				
				for(Point from : froms)
				{
					double distance = map.map(from).distance(to);
					if(distance <= bestDistance)
					{
						bestFrom = from;
						bestDistance = distance;
					}
				}
				
				result.add(bestFrom);
			}
			
			return result;
		}

		public String toString()
		{
			String code = depth() + ") ";
			for (int i : code())
				code += i;
			return code;
		}

	}

	/**
	 * Helper class for storing paired points and paired frequencies for each
	 * component map
	 * 
	 */
	protected class Maps
	{
		// * This class stores the mapping from component to depth to a list of 
		//   things
		private class Cor<T> extends LinkedHashMap<Integer, Map<Integer, List<T>>>
		{
			
			public void add(int component, int depth, T thing)
			{
				ensure(component, depth);
				get(component).get(depth).add(thing);
			}
			
			public List<T> get(int component, int depth)
			{
				if(!containsKey(component))
					return Collections.emptyList();
				
				Map<Integer, List<T>> map = get(component);
				
				if(! map.containsKey(depth))
					return Collections.emptyList();
				
				return map.get(depth);
			}
			
			public void ensure(int component, int depth)
			{
				Maps.this.depth = Math.max(depth, Maps.this.depth); 
				Maps.this.maxComponent = Math.max(component, Maps.this.maxComponent); 

				
				for(int c : series(component+1))
				{
					if(! this.containsKey(c))
						this.put(c, new LinkedHashMap<Integer, List<T>>());
					
					Map<Integer, List<T>> map = this.get(c);
					for(int d : series(depth+1))
					{
						if(!map.containsKey(d))
							map.put(d, new ArrayList<T>());
					}
						
				}
			}
		}

		private Cor<Point>    fromPoints = new Cor<Point>(); 
		private Cor<Point>      toPoints = new Cor<Point>(); 
		private Cor<Double>  fromWeights = new Cor<Double>(); 
		private Cor<Double>    toWeights = new Cor<Double>(); 
		private Cor<List<Integer>> codes = new Cor<List<Integer>>(); 

		private int depth = 0;
		private int maxComponent = 0;
		/**
		 * The number of point pairs stored for a given component
		 * 
		 * @param i
		 *            The component index
		 * @return The number of point pairs stored for component i
		 */
		public int size(int component)
		{
			Map<Integer, List<Point>> map = fromPoints.get(component);
			int sum = 0;
			for(List<Point> values : map.values())
				sum += values.size();
			
			return sum;
		}

		public int size(int component, int depth)
		{
			return fromPoints.get(component).get(depth).size();
		}
		
		public int depth()
		{
			return depth;
		}

		/**
		 * Add a point pair to a given component
		 * 
		 * @param component
		 *            The component that maps from the first to the second point
		 * @param from
		 *            The from point
		 * @param to
		 *            The to point
		 */
		public void add(int component, Point from, Point to, List<Integer> code)
		{
			fromPoints.add(component, code.size(), from);
			toPoints.add(component, code.size(), to);
		}

		/**
		 * Add a frequency pair to a given component
		 * 
		 * @param component
		 *            The component that maps from the first to the second
		 *            frequency
		 * @param from
		 *            The from frequency
		 * @param to
		 *            The to frequency
		 */
		public void weight(int component, double from, double to, List<Integer> code)
		{
			fromWeights.add(component, code.size(), from);
			  toWeights.add(component, code.size(), to);
		}

		/**
		 * Returns a list of 'from' points which the given component should map
		 * into the points returned by {@link to()}
		 * 
		 * @param component
		 * @return
		 */
		public List<Point> from(int component, int depth)
		{
			return fromPoints.get(component, depth);
		}

		/**
		 * Returns a list of 'to' points which should be mapped by the given
		 * component into the points returned by {@link from()}
		 * 
		 * @param component
		 * @return
		 */
		public List<Point> to(int component, int depth)
		{
			return toPoints.get(component, depth);
		}

		/**
		 * Returns a list of 'from' frequencies which the given component weight
		 * should 'scale' into the points returned by {@link to()}
		 * 
		 * @param component
		 * @return
		 */
		public List<Double> fromWeights(int component, int depth)
		{
			return fromWeights.get(component, depth);
		}

		/**
		 * Returns a list of 'to' points which should be 'scales' by the weight
		 * of the given component into the points returned by {@link from()}
		 * 
		 * @param component
		 * @return
		 */
		public List<Double> toWeights(int component, int depth)
		{
			return toWeights.get(component, depth);
		}
		
		public M getMap(int component, int depth)
		{
			List<Point> from = fromPoints.get(component, depth), 
			              to = toPoints.get(component, depth);
			
			return findMap(from, to);
		}
		
		/**
		 * 
		 */
		public double getMapError(int component, int depth, M map, double alpha)
		{	
			if(map == null)
				return 0.0;
			
			List<Point> from = fromPoints.get(component, depth), 
		              toGold = toPoints.get(component, depth),
			        toMapped = map.map(from);
					
			double mean = 0.0;
			
			double c = 1.0 / Math.sqrt(Math.pow(2.0 * Math.PI * alpha, (double) dimension));
			for(int i : series(from.size()))
			{
				Point gold = toGold.get(i), mapped = toMapped.get(i); 
				double sqDistance = gold.sqDistance(mapped);
				
				double prob = c * Math.exp(-sqDistance/(2.0 * alpha));
				
				mean += prob;
			}
		
			mean = mean / from.size();
			
			return mean;
		}
		
		public double getWeight(int component, int depth)
		{
			List<Double> x = fromWeights.get(component, depth);
			List<Double> y = toWeights.get(component, depth);
			
			if(x.size() == 0)
				return 0.0;
			
			double sumXX = 0.0;
			double sumYX = 0.0;

			for (int i = 0; i < x.size(); i++)
			{
				sumXX += x.get(i) * x.get(i);
				sumYX += y.get(i) * x.get(i);
			}

			return sumYX / sumXX;
		}
		
		public double getWeightError(int component, int depth, double weight, double alpha)
		{	
			if(weight == 0.0)
				return 0.0;
			
			List<Double> from = fromWeights.get(component, depth);
			List<Double> toGold = toWeights.get(component, depth),
			        	 toMapped = new ArrayList<Double>(toGold.size());
			
			for(double f : from)
				toMapped.add(weight * f);
				
					
			double mean = 0.0;
			
			double c = 1.0 / Math.sqrt(2.0 * Math.PI * alpha);
			for(int i : series(from.size()))
			{
				Double gold = toGold.get(i), mapped = toMapped.get(i); 
				double diff = gold - mapped;
				diff = diff * diff;
				
				double prob = c * Math.exp(-diff / (2.0 * alpha));
				
				mean += prob;
			}
		
			mean = mean / from.size();
			
			return mean;
		}

//		@Override
//		public String toString()
//		{
//			String out = "";
//
//			for (int i : Series.series(from.size()))
//			{
//				out += i + ":" + from(i).size() + "_" + to(i).size() + " ";
//			}
//
//			return out;
//		}
		
		/**
		 * Writes out some images
		 * @param baseDir
		 * @throws IOException
		 */
		public void debug(File baseDir) throws IOException
		{
			for(int c : series(maxComponent + 1))
				for(int d : series(1, depth + 1))
				{
					File dir = new File(baseDir, String.format("%04d/comp%d/depth-%d.png", iterations, c, d));
					dir.mkdirs();
				
					EM.debug(dir, fromPoints.get(c, d), toPoints.get(c, d));
				}
		}
		
	}
	
	public static void debug(File file, List<Point> from, List<Point> to)
			throws IOException
		{
			int res = 250;
			
			BufferedImage fromImage = Draw.draw(from, res, true),
			              toImage = Draw.draw(to, res, true);
			
			BufferedImage result = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = result.createGraphics();
			
			graphics.setBackground(Color.white);
			graphics.clearRect(0, 0, result.getWidth(), result.getHeight());		
			
			graphics.setComposite(AlphaComposite.SrcAtop);
			
			// * Colorize
			fromImage = Draw.colorize(Color.red).filter(fromImage, null);
			graphics.drawImage(fromImage, 0, 0, null);
			
			toImage = Draw.colorize(Color.green).filter(toImage, null);
			graphics.drawImage(toImage, 0, 0, null);
		
			graphics.dispose();
			
			ImageIO.write(result, "PNG", file);
		}	

	/**
	 * A convenience method to quickly learn an EM model for a given dataset.
	 * 
	 * @param data
	 * @param components
	 * @param depth
	 * @param iterations
	 * @param eval
	 * @return
	 */
	public static IFS<Similitude> learn(List<Point> data, int components,
			int depth, int iterations, int eval)
	{
		int dim = data.get(0).dimensionality();

		EM<Similitude> em = new SimEM(
				IFSs.initialSphere(dim, components, 1.0, 0.5), 
				data, 1, Similitude.similitudeBuilder(dim), 0.01);

		Target<IFS<Similitude>> target = new IFSTarget<Similitude>(eval, data);

		IFS<Similitude> model = null;
		double score = Double.NEGATIVE_INFINITY;

		for (int i : Series.series(iterations))
		{
			em.iterate(eval, depth);

			IFS<Similitude> currentModel = em.model();
			double currentScore = target.score(currentModel);

			if (currentScore > score)
			{
				score = currentScore;
				model = currentModel;
			}

			Global.log()
					.info("Generation " + i + ", score " + score + " "
							+ currentScore);
		}

		return model;
	}
	
	/**
	 * Finds the depth that gives the highest likelihood to a sample of the data 
	 * 
	 * @param from
	 * @param step
	 * @param to
	 * @param samples
	 * @return
	 */
	public static <M extends AffineMap> double depth(EM<M> em, double from, double step, double to, int samples, List<Point> data)
	{
		
		double bestDepth = Double.NaN;
		double bestLL = Double.NEGATIVE_INFINITY; 
		
		for(double depth : Series.series(from, step, to))
		{
			System.out.print(" " + depth);

			List<Point> sample = Datasets.sample(data, samples);
			
			double ll = 0.0;
			for(Point point : sample)
				ll += Math.log(IFS.density(em.model(), point, depth, em.basis()));
				
			if(ll >= bestLL)
			{
				bestLL = ll;
				bestDepth = depth;
			}
		}
		System.out.println();
		return bestDepth;
	}
	

	/**
	 * Let's us store the ingredients of a model, but allows null values
	 * @author Peter
	 *
	 */
	protected class PreModel extends AbstractList<M>
	{
		List<M> maps = new ArrayList<M>(numComponents);
		List<Double> weights = new  ArrayList<Double>(numComponents);
		
		public PreModel(M map, double weight)
		{
			addMap(map, weight);
		}
		
		public void addMap(M map, double weight)
		{
			maps.add(map);
			weights.add(weight);
		}
		
		public M get(int i)
		{
			return maps.get(i);
		}
		
		public void set(int i, M map, double weight)
		{			
			maps.set(i, map);
			weights.set(i, weight);
		}
		
		public double weight(int i)
		{
			return weights.get(i);
		}
		
		public IFS<M> ifs()
		{
			IFS<M> model =  null;
			for(int i: series(maps.size()))
				if(model == null)
					model = new IFS<M>(maps.get(i), weights.get(i));
				else
					model.addMap(maps.get(i), weights.get(i));
			
			return model;
		}

		@Override
		public int size()
		{
			return maps.size();
		}
	}
	
}
