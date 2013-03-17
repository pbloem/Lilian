package org.lilian.data.real.fractal;

import static java.lang.Math.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.EuclideanDistance;

/**
 * NOTE: This is old code. It shoudl be checked for correctness before being used 
 * again.
 * 
 * @author Peter
 *
 */
public class Tools 
{
	/**
	 * Does a beam search to find the code fitting a given point 
	 * 
	 * @param model
	 * @param depth
	 * @return
	 */
	public static <M extends AffineMap> List<Integer> search(Point p, IFS<M> model, int depth, int beamWidth)
	{
		Search<M> search = new Search<M>(p, model, depth, beamWidth);
		search.search();
		
		return search.best();
	}
	
	private static class Search<M extends AffineMap> 
	{
		Distance<Point> distance = new EuclideanDistance();
		
		IFS<M> model;
		int depth;
		int beamWidth;
		Point point;
		
		List<Node> buffer;
		Node best;
		
		public Search(Point point, IFS<M> model, int depth, int beamWidth) 
		{
			this.point = point;
			this.model = model;
			this.depth = depth;
			this.beamWidth = beamWidth;
			
			buffer = new ArrayList<Node>(beamWidth);
		}
		
		public void search()
		{
			// * Initialize the buffer
			buffer.add (new Node( Collections.<Integer>emptyList(), log(1.0), Double.MAX_VALUE, null, log(1.0) ) );
			
			while(! buffer.isEmpty())
			{
				Node top = buffer.remove(0);
				
				if(top.code.size() == depth)
					if(best == null || top.logProb > best.logProb)
						best = top;
				
				if(top.code.size() < depth)
					for(int i = 0; i < model.size(); i++)
					{
						ArrayList<Integer> newCode = new ArrayList<Integer>(top.code.size() + 1);
						newCode.addAll(top.code);
						newCode.add(i);
						
						double newPrior = top.prior + log(model.probability(i));
						
						AffineMap map = (AffineMap) (top.map == null ? model.get(i) : top.map.compose(model.get(i)));
						
						MVN mvn = new MVN(map);
						double prob = newPrior + log(mvn.density(point));
						double dist = distance.distance(mvn.mean(), point);
						
						buffer.add(new Node(newCode, prob, dist, map, newPrior));
					}
				
				// * Sort by probability
				Collections.sort(buffer);
				
				// * Prune
				while(buffer.size() > beamWidth)
				{
					buffer.remove(buffer.size() - 1);
					System.out.println("PRUNE");
				}
			}
			
		}
		
		public List<Integer> best()
		{
			if(best == null)
				return null;
			ArrayList<Integer> c = new ArrayList<Integer>(best.code);
			Collections.reverse(c);
			return c;
		}

		private class Node implements Comparable<Node>
		{
			AffineMap map;
			double prior;
			
			List<Integer> code;
			double logProb;
			double dist;
			
			public Node(List<Integer> code, double logProb, double dist, AffineMap map, double prior)
			{
				this.code = code;
				this.logProb = logProb;
				this.dist = dist;
				
				this.map = map;
				this.prior = prior;
			}
			
			@Override
			public int compareTo(Node other) {
				if(cool(this.logProb) && cool(other.logProb))
					return - Double.compare(this.logProb, other.logProb);
				if(cool(other.logProb))
					return -1;
				if(cool(this.logProb))
					return 1;
				
				return Double.compare(this.dist, other.dist);
			}
			
			private boolean cool(double d)
			{
				if(Double.isNaN(d))
					return false;
				if(Double.isInfinite(d))
					return false;
				return true;
			}
		}
	}

}
