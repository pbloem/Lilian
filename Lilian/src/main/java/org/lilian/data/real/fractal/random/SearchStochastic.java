package org.lilian.data.real.fractal.random;

import static java.lang.Math.exp;
import static java.lang.Math.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.fractal.old.IFS;
import org.lilian.data.real.fractal.random.DiscreteRIFS.Codon;
import org.lilian.data.real.fractal.random.DiscreteRIFS.SearchResult;
import org.lilian.data.real.weighted.Weighted;
import org.lilian.data.real.weighted.WeightedLists;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;


/**
 * Searches a discrete RIFS model based on a stochastic choice tree.
 * 
 * @author Peter
 *
 */
public class SearchStochastic
{


	public static <M extends AffineMap> double density(DiscreteRIFS<M> rifs, SChoiceTree tree, Point point)
	{
		return density(rifs, tree, point,  new MVN(rifs.dimension()));
	}
	
	public static <M extends AffineMap> double density(DiscreteRIFS<M> rifs, SChoiceTree tree, Point point, MVN basis)
	{
		SearchResult result = search(rifs, tree, point, basis);
		
		return result.probSum();
	}
	
	/** 
	 * The endpoints of an IFS are the means of the distributions mapped to a 
	 * given depth. This method returns the endpoint of the distribution whose 
	 * code is assigned to this point by code(...).
	 * 
	 * @param ifs
	 * @param point
	 * @param depth
	 * @return
	 */
	public static Point endpoint(DiscreteRIFS<Similitude> rifs, SChoiceTree tree, Point point)
	{
		return endpoint(rifs, tree, point, new MVN(rifs.dimension()));
	}
	
	public static Point endpoint(DiscreteRIFS<Similitude> rifs, SChoiceTree tree, Point point,
			MVN basis)
	{
		SearchResult result = search(rifs, tree, point, basis);
		
		return result.mean();
	}
	
//	public static Point endpoint(IFS<Similitude> ifs, ChoiceTree tree, List<Integer> code)
//	{
//		return endpoint(ifs, tree, code, new Point(ifs.dimension()));
//	}
//	
//	public static Point endpoint(IFS<Similitude> ifs, ChoiceTree tree, List<Integer> code, Point current)
//	{
//		if(code.isEmpty())
//			return current;
//		
//		Point next = ifs.get(code.get(code.size() - 1)).map(current);
//		
//		return endpoint(ifs, ChoiceTree tree, code.subList(0, code.size() - 1), next);
//	}
	
	/**
	 * Finds the transformation of the initial distribution that is most likely 
	 * to generate the given point. Transformations considered are all d length
	 * compositions of the base transformations of this IFS model.
	 * 
	 * @return null If all codes represent probability distributions which assigns
	 * a density to this point that is too low to be represented as a double
	 * 
	 */
	public static <M extends AffineMap> List<Codon> code(
			DiscreteRIFS<M> rifs, SChoiceTree tree, Point point)
	{
		return code(rifs,tree,  point, new MVN(rifs.dimension()));
	}
		
	public static <M extends AffineMap> List<Codon> code(
			DiscreteRIFS<M> rifs, SChoiceTree tree, Point point,  MVN basis)
	{		
		SearchResult res = search(
			rifs, tree, point, basis);
		return res.code();
	}
	
	public static <M extends AffineMap> SearchResult search(
			DiscreteRIFS<M> rifs, SChoiceTree tree, Point point)
	{
		return search(rifs, tree, point, new MVN(rifs.dimension()));
	}
	
	public static <M extends AffineMap> SearchResult search(
			DiscreteRIFS<M> rifs, SChoiceTree tree, Point point, MVN basis)
	{
		return search(rifs, tree, point, basis, 1);
	}
	
	/**
	 * Performs a walk over all endpoint distributions of the given IFS. It 
	 * collects information like codes and probability density for the given 
	 * point.
	 * 
	 * @param ifs
	 * @param point
	 * @param depth
	 * @param basis
	 * @param bufferLimit The SearchResult object returns a weighted list of 
	 * 	top codes. This parameter determines the maximum number of codes to 
	 *  return
	 * @return
	 */
	public static <M extends AffineMap> SearchResult search(
			DiscreteRIFS<M> rifs, SChoiceTree tree, Point point, MVN basis, int bufferLimit)
	{

		SearchResult res = search(
				rifs, tree.root(), point,new SearchResultImpl(tree.depth(), bufferLimit),
				new ArrayList<Codon>(tree.depth()), 0.0,
				MatrixTools.identity(rifs.dimension()), new ArrayRealVector(rifs.dimension()),
				basis);
		return res;
	}
	
	private static <M extends AffineMap> SearchResult search(
			DiscreteRIFS<M> rifs, SChoiceTree.Node node, Point point,
			SearchResultImpl result, List<Codon> current, 
			double logPrior, 
			RealMatrix transform, RealVector translate,
			MVN basis)	
	{
		if(node == null)
		{
			double logProb;
			
			AffineMap map = new AffineMap(transform, translate);
			AffineMap mvnMap = basis.map();
			
			map = (AffineMap) map.compose(mvnMap);
			
			if(map.invertible())
			{
				MVN mvn = new MVN(map);
				logProb = logPrior + Math.log(mvn.density(point));
			} else { 
				logProb = Double.NEGATIVE_INFINITY;
			}
						
			result.show(logProb, new ArrayList<Codon>(current), point, new Point(translate), logPrior);
			return result;
		}
		
		for(int choice : Series.series(node.tree().max()))
		{
			double choicePrior = node.codon().probability(choice);
			if(choicePrior == 0.0)
				continue;
			
			IFS<M> ifs = rifs.models().get(choice);
			
			for(int i = 0; i < ifs.size(); i ++)
			{
				current.add(new Codon(choice, i));
				SChoiceTree.Node newNode = node.leaf() ? null : node.children().get(i);
				
				RealMatrix cr = transform.multiply(ifs.get(i).getTransformation());
				RealVector ct = transform.operate(ifs.get(i).getTranslation());
				ct = ct.add(translate);
				
				search(rifs, newNode, point, result, current, 
						logPrior + log(choicePrior) + log(ifs.probability(i)), cr, ct, basis);
				
				current.remove(current.size() - 1);
			}
		}
		
		return result;
		
	}
	
	/** 
	 * The result of a search through all endpoint distributions
	 * @author Peter
	 *
	 */
	public static interface SearchResult
	{
		public double logProb();
		
		public List<Codon> code();
				
		public Point mean();
		
		/**
		 * The sum of all the probability densities. This is an estimate for the
		 * probability density of the point.
		 * @return
		 */
		public double probSum();
		
		/**
		 * This value works as an approximation to the probability density. This 
		 * can be used if probSum() returns zero for all points under 
		 * investigation.
		 * 
		 *   
		 * Should only be used to compare different values (ie. the point with 
		 * the highest approximate value probably has highest density).
		 * @return
		 */
		
		public double approximation();
		
		/**
		 * A weighted list of the codes with maximum responsibility for the 
		 * given point. The weights are the responsibilities (likelihood of the 
		 * point under the endpoint distribution).  
		 * 
		 * Only codes with definite likelihoods are counted. There is no 
		 * fallback in distances (as with {@link code()}) 
		 * 
		 * @return
		 */
		public Weighted<List<Codon>> codes();

	}
	
	private static class SearchResultImpl implements SearchResult
	{
		private static final double KRIGING_SIGMA = 0.01;
		private int bufferLimit = 1;
		private double logProb = Double.NEGATIVE_INFINITY;
		private double codeApprox = Double.NEGATIVE_INFINITY;
		
		private List<Codon> code = null;
		private List<Codon> codeFallback = null;
		
		private List<WCode> buffer = new ArrayList<WCode>();
		private List<WCode> bufferFallback = new ArrayList<WCode>();

		
		private Point mean = null;
		private Point meanFallback = null;
		
		private double probSum = 0.0;
		private double densityApprox = 0.0;
		private int daTotal = 0;
		private double mvnSigma;
		
		public SearchResultImpl(int depth)
		{
			mvnSigma = Math.pow(KRIGING_SIGMA, depth);

		}
		
		public SearchResultImpl(int depth, int bufferLimit)
		{
			this(depth);
			this.bufferLimit = bufferLimit;
		}
		
		public void show(double logProb, List<Codon> code, Point point, Point mean, double logPrior)
		{
			if(!Double.isNaN(logProb) && !Double.isInfinite(logProb))
			{
				probSum += exp(logProb);
				
				if(Math.exp(logProb) > 0.0 && bufferLimit >= 1)
				{
					buffer.add(new WCode(code, Math.exp(logProb)));
					Collections.sort(buffer);
					while(buffer.size() > bufferLimit)
						buffer.remove(buffer.size() - 1);
				}
			}
			

			// densityApprox += Math.exp(-0.5 * distance * distance) * Math.exp(logPrior);
			double approx = exp(logPrior) * new MVN(mean, mvnSigma).density(point);
			densityApprox += approx;
			
			if(approx > 0.0 && bufferLimit >= 1)
			{
				bufferFallback.add(new WCode(code, approx));
				Collections.sort(bufferFallback);
				while(bufferFallback.size() > bufferLimit)
					bufferFallback.remove(bufferFallback.size() - 1);
			}	
			
			if(logProb > this.logProb && !Double.isNaN(logProb) && !Double.isInfinite(logProb))
			{	
				this.logProb = logProb;
				this.code = code;
				this.mean = mean;
			}
			
			if(approx > this.codeApprox && !Double.isNaN(approx) && !Double.isInfinite(approx))
			{
				this.codeApprox = approx;
				this.codeFallback = code;
				this.meanFallback = mean;
			}
		}
		
		public double logProb()
		{
			return logProb;
		}
		
		public List<Codon> code()
		{
			return code != null ? code : codeFallback;
		}
		
		public Point mean()
		{
			return mean != null ? mean : meanFallback;
		}
		
		/**
		 * The sum of all the probability densities. This is an estimate for the
		 * probability density of the point.
		 * @return
		 */
		public double probSum()
		{
			return probSum;
		}

		@Override
		public double approximation()
		{
			// return Math.exp(0.5 * codeApprox);
			return densityApprox;
		}
		
		public Weighted<List<Codon>> codes()
		{
			List<WCode> buff = buffer.size() > 0 ? buffer : bufferFallback;
			
			Weighted<List<Codon>> codes = WeightedLists.empty();
			for(WCode wc : buff)
				codes.add(wc.code(), wc.weight());
			
			return codes;
		}
		
		private class WCode implements Comparable<WCode>
		{
			private List<Codon> code;
			private double weight;

			public WCode(List<Codon> code, double weight)
			{
				this.code = code;
				this.weight = weight;
			}

			@Override
			public int compareTo(WCode other)
			{
				return - Double.compare(weight, other.weight);
			}
			
			public List<Codon> code()
			{
				return code;
			}
			
			public double weight()
			{
				return weight;
			}
		}
	}	
	
}
