package org.lilian.data.real.fractal;

import static org.lilian.util.Series.series;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;
import org.lilian.Global;
import org.lilian.data.real.Datasets;
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
 * likelihood 0 for all models. </p> How do we find the transformations given
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
 * map can be used to map data back to data space if necessary.</li>
 * </ul>
 * 
 * @author Peter
 * 
 */
public abstract class EM<M extends org.lilian.data.real.Map & Parametrizable> implements Serializable
{
	// * Serial ID
	private static final long serialVersionUID = 774467486797440172L;

	// * Any component whose contraction rate is lower than this value is
	//   perturbed
	private static final double CONTRACTION_THRESHOLD = 1E-10;

	// * If a code contains more than this number of points, we consider its
	//   covariance (by fitting an MVN as described above).
	private static final int COVARIANCE_THRESHOLD = 3;
	
	private static final boolean PERTURB_UNASSIGNED = true;
	
	private static final boolean UNIFORM_WEIGHTS = false;

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
	
	protected int lastDepth = -1;
	
	// * The variance of the mvn from which the spanning point are sampled when 
	//   there aremany points to a single code.
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

	public void iterate(int sampleSize, int depth)
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
	protected void modelToCodes(int sampleSize, int depth)
	{

		List<Point> sample = sampleSize == -1 ? data : Datasets.sample(data,
				sampleSize);

		root = new Node(-1, null); // Challenging for the GC, but should be
									// fine...

		for (Point point : sample)
		{
			Weighted<List<Integer>> codes = codes(point, model, depth, maxSources); 
			
			for(int i : series(codes.size()))
				root.observe(codes.get(i), point, codes.probability(i));
		}
		
		lastDepth = depth;
	}
	
	protected abstract Weighted<List<Integer>> codes(
			Point point, IFS<M> model, int depth, int sources);
	
	protected abstract M findMap(List<Point> from, List<Point> to, M old);

	protected void codesToModel()
	{
		// * Look for matching codes in the code tree
		Maps maps = findMaps();

		// * Frequencies of components over all codes
		BasicFrequencyModel<Integer> priors = new BasicFrequencyModel<Integer>();
		root.count(priors);

		List<M> trans = new ArrayList<M>(numComponents);
		for (int i : Series.series(numComponents))
			trans.add(null);

		List<Double> weights = new ArrayList<Double>(numComponents);
		for (int i : Series.series(numComponents))
			weights.add(1.0 / numComponents);

		// * Keep check of components which were unassigned
		List<Integer> assigned = new ArrayList<Integer>(numComponents);
		List<Integer> unassigned = new ArrayList<Integer>(numComponents);

		for (int i : Series.series(numComponents))
		{
			int n = maps.size(i);

			if (n != 0) // codes found containing this comp
			{
				// * Find the map for the point pairs
				M map = findMap(maps.from(i), maps.to(i), model.get(i));
				
				
				// * Find the weight for the frequency pairs
				double weight = findScalar(maps.fromWeights(i),
						maps.toWeights(i));

				// * If the map contracts too much, we perturb it slightly
//				double det = MatrixTools
//						.getDeterminant(map.getTransformation());
//				if (Math.abs(det) < CONTRACTION_THRESHOLD || Double.isNaN(det))
//					map = Parameters
//							.perturb(map,
//									Similitude.similitudeBuilder(dimension),
//									perturbVar);

				trans.set(i, map);
				weights.set(i, weight);

				assigned.add(i);
			} else
			{ // No codes found with this component
				unassigned.add(i);
			}
		}

		if(! unassigned.isEmpty())
			Global.log().info("unassigned: " + unassigned);
		
		if (assigned.isEmpty())
			throw new IllegalStateException(
					"No points were assigned to any components");

		// * For each unassigned component, take a random assigned component and
		// perturb it slightly.
		for (int i : unassigned)
		{
			if(PERTURB_UNASSIGNED) 
			{
				int j = assigned.get(Global.random.nextInt(assigned.size()));
				M source = trans.get(j);
				double sourceWeight = weights.get(j);
	
				M perturbed0 = Parameters.perturb(source, mapBuilder, perturbVar);
	
				M perturbed1 = Parameters.perturb(source, mapBuilder, perturbVar);
	
	//			// * Make sure that both are contractive
	//			perturbed0 = perturbed0.scalar() > 1.0 ? perturbed0.inverse()
	//					: perturbed0;
	//			perturbed1 = perturbed1.scalar() > 1.0 ? perturbed1.inverse()
	//					: perturbed1;
	
				trans.set(i, perturbed0);
				trans.set(j, perturbed1);
	
				weights.set(i, sourceWeight / 2.0);
				weights.set(j, sourceWeight / 2.0);
			} else {
				trans.set(i, model.get(i));
				weights.set(i, model.probability(i));
			}
		}
				
		if(UNIFORM_WEIGHTS)
			for(int i : series(0, numComponents))
				weights.set(i, 1.0/numComponents);
				
		model = new IFS<M>(trans.get(0), weights.get(0));
		for (int i : series(1, numComponents))
			model.addMap(trans.get(i), weights.get(i));
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
					mvn = useSphericalMVN ? MVN.findSpherical(points) : MVN
							.find(points);
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
								maps.add(t, from.mean(), to.mean());
						} else
						{
							// Consider the covariance by taking not just the
							// means,
							// but points close to zero mapped to both
							// distributions

							// We generate as many points as are in the to node.
							// (for depth one a handful would suffice, but for
							// higher values the amount of points generated gives
							// a sort of weight to this match in the codes among 
							// the other points)
							List<Point> points = new MVN(dimension, spanningPointsVariance)
									.generate(points().size());

							List<Point> pf = from.map().map(points);
							List<Point> pt = to.map().map(points);

							for (int i = 0; i < points.size(); i++)
								maps.add(t, pf.get(i), pt.get(i));
						}
					} else
					{
						// Global.log().info("Points for code " + code +
						// " formed deficient MVN. No points added to pairs.");
					}

					// Register the drop in frequency as the symbol t gets added
					// to the code
					maps.weight(t, nodeFrom.frequency(), this.frequency());
				}
			}
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
		// * The inner list stores all 'from' points. We store one such list for
		// each component
		private List<List<Point>> from = new ArrayList<List<Point>>();
		// * The inner list stores all 'to' points. We store one such list for
		// each component
		private List<List<Point>> to = new ArrayList<List<Point>>();

		// * The same but for the weights (inner lists store frequencies)
		private List<List<Double>> fromWeights = new ArrayList<List<Double>>();
		private List<List<Double>> toWeights = new ArrayList<List<Double>>();

		/**
		 * The number of point pairs stored for a given component
		 * 
		 * @param i
		 *            The component index
		 * @return The number of point pairs stored for component i
		 */
		public int size(int i)
		{
			ensure(i);
			return from.get(i).size();
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
		public void add(int component, Point from, Point to)
		{
			ensure(component);
			this.from.get(component).add(from);
			this.to.get(component).add(to);
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
		public void weight(int component, double from, double to)
		{
			ensure(component);
			this.fromWeights.get(component).add(from);
			this.toWeights.get(component).add(to);
		}

		/**
		 * Ensure that lists exist for the given component (and below)
		 */
		private void ensure(int component)
		{
			while (from.size() <= component)
				from.add(new ArrayList<Point>());
			while (to.size() <= component)
				to.add(new ArrayList<Point>());

			while (fromWeights.size() <= component)
				fromWeights.add(new ArrayList<Double>());
			while (toWeights.size() <= component)
				toWeights.add(new ArrayList<Double>());
		}

		/**
		 * Returns a list of 'from' points which the given component should map
		 * into the points returned by {@link to()}
		 * 
		 * @param component
		 * @return
		 */
		public List<Point> from(int component)
		{
			if (component < from.size())
				return from.get(component);

			return Collections.emptyList();
		}

		/**
		 * Returns a list of 'to' points which should be mapped by the given
		 * component into the points returned by {@link from()}
		 * 
		 * @param component
		 * @return
		 */
		public List<Point> to(int component)
		{
			if (component < to.size())
				return to.get(component);

			return Collections.emptyList();
		}

		/**
		 * Returns a list of 'from' frequencies which the given component weight
		 * should 'scale' into the points returned by {@link to()}
		 * 
		 * @param component
		 * @return
		 */
		public List<Double> fromWeights(int component)
		{
			if (component < fromWeights.size())
				return fromWeights.get(component);

			return Collections.emptyList();
		}

		/**
		 * Returns a list of 'to' points which should be 'scales' by the weight
		 * of the given component into the points returned by {@link from()}
		 * 
		 * @param component
		 * @return
		 */
		public List<Double> toWeights(int component)
		{
			if (component < toWeights.size())
				return toWeights.get(component);

			return Collections.emptyList();
		}

		@Override
		public String toString()
		{
			String out = "";

			for (int i : Series.series(from.size()))
			{
				out += i + ":" + from(i).size() + "_" + to(i).size() + " ";
			}

			return out;
		}
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

}
