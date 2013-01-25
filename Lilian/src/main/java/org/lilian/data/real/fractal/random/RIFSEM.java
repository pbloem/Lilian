package org.lilian.data.real.fractal.random;

import static org.lilian.util.Functions.log2;
import static org.lilian.util.Series.series;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lilian.Global;
import org.lilian.data.real.Datasets;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.fractal.IFS;
import org.lilian.data.real.weighted.Weighted;
import org.lilian.data.real.weighted.WeightedLists;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.search.Builder;
import org.lilian.search.Parameters;
import org.lilian.util.Functions;
import org.lilian.util.Series;

/**
 * 
 * TODO:
 * - Use data as basis.
 * @author Peter
 *
 */
public class RIFSEM
{
	private static boolean useSphericalMVN = true;
	private static final int COVARIANCE_THRESHOLD = 5;
	private static final boolean PERTURB_UNASSIGNED = true;
	// * If a code has fewer than this number of points, it is not
	//   used in reconstructing the choice tree
	private static final int TREE_POINTS_THRESHOLD = 10; 
	
	private List<List<Point>> data;
	private List<List<Point>> dataSample;
	
	// * The three main components of an EM iteration
	private DiscreteRIFS<Similitude> model;
	private List<ChoiceTree> trees;
		
	// * A code tree for each dataset. This list holds the root nodes.
	private List<Node> codeTrees;
	
	private int compPerIFS, depth, sampleSize;
	
	private double spanningPointsVariance, perturbVar;
	
	private Builder<Similitude> builder;
	
	/**
	 * 
	 * @param initial Model consisting of IFSs with equal size.
	 * @param data
	 * @param depth
	 * @param sampleSize
	 */
	public RIFSEM(DiscreteRIFS<Similitude> initial, List<List<Point>> data, int depth, int sampleSize, double spanningPointsVariance, double perturbVar)
	{
		
		this.model = initial;
		this.data = data;
		this.depth = depth;
		this.sampleSize = sampleSize;
		this.spanningPointsVariance = spanningPointsVariance;
		this.compPerIFS = model.models().get(0).size();

		// * Check that all component IFSs of model have the same size
		for(IFS<Similitude> ifs : model.models())
			if(ifs.size() != compPerIFS)
				throw new IllegalArgumentException("All component IFSs of the initial model should have the sam enumber of component transformations.");
				
		dataSample = new ArrayList<List<Point>>(data.size());
		codeTrees = new ArrayList<Node>(data.size());
		
		resample();
		
		// * Add random trees
		trees = new ArrayList<ChoiceTree>(data.size());
		for(int i : Series.series(dataSample.size()))
			trees.add(model.randomInstance(depth));
		
		findCodes();		
		
		builder = Similitude.similitudeBuilder(model.dimension());
	}
	
	public void iteration()
	{
		resample();
		
		findTrees();
		findCodes();
		findModel();
	}
	
	public DiscreteRIFS<Similitude> model()
	{
		return model;
	}
	
	/**
	 * Finds a new Model given the current codes and sequences.
	 */
	public void findModel()
	{	
		// * Look for matching codes in the code tree
		List<Maps> mapsList = findMaps();

		// * Frequency model for all component IFSs
		BasicFrequencyModel<Integer> freqs = new BasicFrequencyModel<Integer>(); 
		for(ChoiceTree tree : trees)
			tree.count(freqs);
		
		DiscreteRIFS<Similitude> newModel = null;

		for(int h : series(model.size()))
		{
			IFS<Similitude> component = model.models().get(h);
			int numComponents = component.size();
			Maps maps = mapsList.get(h);
			
			List<Similitude> trans = new ArrayList<Similitude>(numComponents);
			for (int i : Series.series(numComponents))
				trans.add(null);
	
			List<Double> weights = new ArrayList<Double>(numComponents);
			for (int i : Series.series(numComponents))
				weights.add(1.0 / numComponents);
	
			// * Keep check of components which were unassigned
			List<Integer> assigned = new ArrayList<Integer>(numComponents);
			List<Integer> unassigned = new ArrayList<Integer>(numComponents);
	
			for (int i : series(numComponents))
			{
				int n = maps.size(i);
	
				if (n != 0) // codes found containing this comp
				{
					// * Find the map for the point pairs
					Similitude map = findMap(maps.from(i), maps.to(i), component.get(i));
					
					
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
			{
				for(Node root : codeTrees)
					for(int i : series(3))
						System.out.println(root.random().code());
				throw new IllegalStateException(
						"No points were assigned to any components");
			}
	
			// * For each unassigned component, take a random assigned component and
			// perturb it slightly.
			for (int i : unassigned)
			{
				if(PERTURB_UNASSIGNED) 
				{
					int j = assigned.get(Global.random.nextInt(assigned.size()));
					Similitude source = trans.get(j);
					double sourceWeight = weights.get(j);
		
					Similitude perturbed0 = Parameters.perturb(source, builder, perturbVar);
					Similitude perturbed1 = Parameters.perturb(source, builder, perturbVar);

					trans.set(i, perturbed0);
					trans.set(j, perturbed1);
		
					weights.set(i, sourceWeight / 2.0);
					weights.set(j, sourceWeight / 2.0);
				} else {
					trans.set(i, component.get(i));
					weights.set(i, component.probability(i));
				}
			}
										
			component = new IFS<Similitude>(trans.get(0), weights.get(0));
			for (int i : series(1, numComponents))
				component.addMap(trans.get(i), weights.get(i));
			
			double ifsPrior = freqs.probability(h);
			if(newModel == null)
				newModel = new DiscreteRIFS<Similitude>(component, ifsPrior);
			else
				newModel.addModel(component, ifsPrior);
		}
		
		model = newModel;
	}
	
	/**
	 * Resample the sampled datasets.
	 * 
	 * @param sampleSize The number of points to sample per dataset.
	 */
	public void resample()
	{
		dataSample.clear();
		
		for(List<Point> points : data)
			dataSample.add(Datasets.sample(points, sampleSize));
		
	}
	
	/**
	 * Finds a new coding given the current model and sequences.
	 */
	public void findCodes()
	{
		codeTrees.clear();
		
		for(int i : Series.series(dataSample.size()))
		{
			List<Point> points = dataSample.get(i);
			ChoiceTree tree = trees.get(i);
			Node root = new Node(-1, null, i);
			
			for(Point point : points)
				root.observe(DiscreteRIFS.code(model, tree, point), point);
			
			codeTrees.add(root);
		}
	}
	
	/**
	 * Finds new choicetrees given the current model and coding
	 */
	public void findTrees()
	{
		trees.clear();
		
		for(int i : series(data.size()))
		{
			// * We start with a random tree, and add the choices we can figure
			//   out from the data.
			ChoiceTree tree = ChoiceTree.random(model(), depth);
			
			codeTrees.get(i).build(tree);
			trees.add(tree);
		}
	}	
	
	/**
	 * 
	 * @return A list of maps so that Maps i in the list represents the 
	 * maps for component IFS i.
	 */
	private List<Maps> findMaps()
	{
		List<Maps> mapsList = new ArrayList<Maps>(model.size());
		
		for(int i : series(model.size()))
			mapsList.add(new Maps());
		
		for(Node root : codeTrees)
			root.findPairs(mapsList);
		
		return mapsList;
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
		private static final long serialVersionUID = -6512700670917962320L;

		private int dataset; 
		
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
		public Node(int symbol, Node parent, int dataset)
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
			
			this.dataset = dataset;
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
			points.add(point);
			mvn = null; // signal that the mvn needs to be recomputed

			if (codeSuffix.size() == 0)
			{
				isLeaf = true;
				return;
			}

			int symbol = codeSuffix.get(0);
			if (!children.containsKey(symbol))
				children.put(symbol, new Node(symbol, this, dataset));

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
					mvn = useSphericalMVN ? MVN.findSpherical(points) : 
						MVN.find(points);
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
		 * 
		 * 
		 * @param maps
		 */
		public void findPairs(List<Maps> maps)
		{
			if (children.size() > 0) // * Recurse
			{
				for (int i : children.keySet())
					children.get(i).findPairs(maps);
			}

			if (code().size() > 0) // * Execute for this node
			{
				List<Integer> codeFrom = new ArrayList<Integer>(code);
				
				// * t is the symbol of the transformation
				int t = codeFrom.remove(0);
				// * c is the symbol of the component IFS to which t belongs
				int c = trees.get(dataset).get(codeFrom).codon();

				// * Find the node that matches
				Node nodeFrom = codeTrees.get(dataset).find(codeFrom);
				
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
								maps.get(c).add(t, from.mean(), to.mean());
						} else
						{
							// * Consider the covariance by taking not just the
							//   means,
							//   but points close to zero mapped to both
							//   distributions

							//   We generate as many points as are in the to node.
							//   (for depth one a handful would suffice, but for
							//   higher values the amount of points generated gives
							//   a sort of weight to this match in the codes among 
							//   the other points)
							List<Point> points = new MVN(model.dimension(), spanningPointsVariance)
									.generate(points().size());

							List<Point> pf = from.map().map(points);
							List<Point> pt = to.map().map(points);

							for (int i = 0; i < points.size(); i++)
								maps.get(c).add(t, pf.get(i), pt.get(i));
						}
					} else
					{
						// Global.log().info("Points for code " + code +
						// " formed deficient MVN. No points added to pairs.");
					}

					// * Register the drop in frequency as the symbol t gets added
					//   to the code
					maps.get(c).weight(t, nodeFrom.frequency(), this.frequency());
				}
			}
		}
		
		public void build(ChoiceTree tree)
		{
			if(children == null || children.size() == 0)
				return;
			if(points().size() < TREE_POINTS_THRESHOLD)
				return;
				
			
			// * Find the best IFS for this node
			
			// * We calculate the score of an IFS by fitting an MVN to the points 
			//   of this node, mapping it by each component transformation, and 
			//   taking the log probability of the points of the matching node.
			int best = -1;
			double bestScore = Double.NEGATIVE_INFINITY;
			
			for(int i : series(model.size()))
			{
				IFS<Similitude> component = model.models().get(i);
				double logIFSPrior = log2(model.probability(i));
			
				double score = logIFSPrior;
				MVN mvn = mvn();
				
				// System.out.println(code() + " " + points() + " " + mvn);
				
				for(int j : series(component.size()))
				{
					MVN mapped = mvn.transform(component.get(j));
					
					if(! children.containsKey(i))
						continue;
					
					List<Point> to = children.get(i).points();
					
					for(Point point : to)
						score += log2(mapped.density(point));
				}
				
				if(score >= bestScore)
				{
					bestScore = score;
					best = i;
				}
			}
			
			// * Submit to the choice tree
			tree.set(code(), best);
			
			// * Recurse
			for(Node child : children.values())
				child.build(tree);
		}

		public String toString()
		{
			String code = depth() + ") ";
			for (int i : code())
				code += i;
			return code;
		}

		public Node random()
		{
			if(children.size() == 0)
				return this;
			
			List<Integer> keys = new ArrayList<Integer>(children.keySet());
			
			return children.get(keys.get(Global.random.nextInt(keys.size())));
		}
		
	}

	
	/**
	 * Helper class for storing paired points and paired frequencies for each
	 * component map of a given IFS
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
	
	protected Similitude findMap(List<Point> from, List<Point> to, Similitude old)
	{
		return org.lilian.data.real.Maps.findMap(from, to);
	}
	
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
}
