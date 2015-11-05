package org.lilian.data.real.fractal.old;

import static java.lang.Math.pow;
import static org.lilian.util.Series.series;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lilian.data.real.AffineMap;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
import org.lilian.data.real.fractal.old.IFS.SearchResult;
import org.lilian.data.real.weighted.Weighted;
import org.lilian.data.real.weighted.WeightedLists;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.neural.ThreeLayer;
import org.lilian.search.Parametrizable;
import org.lilian.util.Series;

public class NeuralIFS
{
	
	public static <M extends Map & Parametrizable> SearchResult search(Point point, 
			IFS<M> ifs, int depth, double contractionEstimate, int bufferLimit)
	{
		SearchResultImpl sr = new SearchResultImpl(bufferLimit);
		
		search(
			ifs, sr, new ArrayList<Integer>(), 
			point, new Point(ifs.dimension()), 1.0, 
			depth, pow(contractionEstimate, depth));
		
		return sr;
		
	}
	
	private static <M extends Map & Parametrizable> void search(
			IFS<M> ifs, SearchResultImpl sr, List<Integer> prefix, 
			Point point, Point end, double prior, double depth, double contraction)
	{
		// * NOTE: At this point prefix is in the reverse order of the 
		//   code that is actually returned (since we're applying the maps
		//   rather than composing them.
		if(depth == 0)
		{
			sr.show(reverseCopy(prefix), point, prior * new MVN(end, contraction).density(point));
			
		} else
		{
			for(int i : series(ifs.size()))
			{
				prefix.add(i);
				search(ifs, sr, prefix, 
						point, ifs.get(i).map(end), 
						prior * ifs.probability(i),
						depth - 1, contraction);
				prefix.remove(prefix.size() - 1);
			}
		}
	}
	
	
	/** 
	 * The result of a search through all endpoint distributions
	 * @author Peter
	 *
	 */
	public static interface SearchResult
	{
		
		public List<Integer> code();
				
		public Point mean();
		
		/**
		 * The sum of all the probability densities. This is an estimate for the
		 * probability density of the point.
		 * @return
		 */
		public double density();
		
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
		public Weighted<List<Integer>> codes();

	}
	
	private static class SearchResultImpl implements SearchResult
	{
		private int bufferLimit;
		
		private List<WCode> buffer = new ArrayList<WCode>();
		
		private double density = 0.0;
		
		public SearchResultImpl(int bufferLimit)
		{
			this.bufferLimit = bufferLimit;
		}
		
		public void show(List<Integer> code, Point mean, double prob)
		{
				density += prob;
				
//				System.out.println(code + " " + mean + " " + prob);
				
				buffer.add(new WCode(code, prob, mean));
				Collections.sort(buffer);
				while(buffer.size() > bufferLimit)
					buffer.remove(buffer.size() - 1);
		}
		
		public List<Integer> code()
		{
			return buffer.get(0).code();
		}
		
		public Point mean()
		{
			return buffer.get(0).mean();
		}
		
		/**
		 * The sum of all the probability densities. This is an estimate for the
		 * probability density of the point.
		 * @return
		 */
		public double density()
		{
			return density;
		}

		
		public Weighted<List<Integer>> codes()
		{
			Weighted<List<Integer>> codes = WeightedLists.empty();
			for(WCode wc : buffer)
				codes.add(wc.code(), wc.weight());
			
			return codes;
		}
		
		private class WCode implements Comparable<WCode>
		{
			private List<Integer> code;
			private double weight;
			private Point mean;

			public WCode(List<Integer> code, double weight, Point mean)
			{
				this.code = code;
				this.weight = weight;
			}

			@Override
			public int compareTo(WCode other)
			{
				return - Double.compare(weight, other.weight);
			}
			
			public List<Integer> code()
			{
				return code;
			}
			
			public double weight()
			{
				return weight;
			}
			
			public Point mean()
			{
				return mean;
			}
		}
		
	}	
	
	/**
	 * Creates a duplicate of the given IFS model by training neural networks 
	 * for each individual map.
	 *  
	 * @param ifs
	 * @return
	 */
	public static <M extends Map & Parametrizable> IFS<ThreeLayer> copy(
			IFS<M> ifs, int hidden, int examples, 
			double learningRate, double initVar)
	{
		IFS<ThreeLayer> copy = null;
		
		for(int i : Series.series(ifs.size()))
		{
			ThreeLayer tl = ThreeLayer.copy(ifs.get(i), hidden, examples, learningRate, initVar);
			if(copy == null)
				copy = new IFS<ThreeLayer>(tl, ifs.probability(i));
			else
				copy.addMap(tl, ifs.probability(i));
		}
			
		return copy;
	}
	
	/**
	 * Produces a reversed copy of the argument
	 * 
	 * @param in
	 * @return
	 */
	private static ArrayList<Integer> reverseCopy(List<Integer> in)
	{
		int n = in.size();
		ArrayList<Integer> out = new ArrayList<Integer>(n);
		for(int i : Series.series(n))
			out.add(in.get(n - 1 - i));
		return out;
	}
	
	/**
	 * Draws the component distribution in the codes of a three component 
	 * classifier.
	 * 
	 * @param res The resolution of the smallest side of the image.
	 */
	public static <M extends Map & Parametrizable> 
		BufferedImage drawMultiCodes(IFS<M> ifs, 
											double[] xrange, 
											double[] yrange, 
											int res, int depth,
											int bufferLimit)
	{
		if(ifs.size() > 3)
			throw new IllegalArgumentException("IFS must have three components or less (had "+ifs.size()+").");
		
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
				
				BasicFrequencyModel<Integer> mod = 
						new BasicFrequencyModel<Integer>();
					
				p = new Point(x, y);
				NeuralIFS.SearchResult result = NeuralIFS.search(p, ifs, depth, 0.9, bufferLimit);
				
				if(result.codes().size() > 0)
				{
					Weighted<List<Integer>> codes = result.codes();
					for(int k : Series.series(codes.size()))
						for(int symbol : codes.get(k))
							mod.add(symbol, codes.weight(k));
				} else {
					mod.add(result.code());
				}
				
				Color color = null;
				
				if(mod.total() == 0.)
				{
					color = Color.BLACK;
				} else
				{
			
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
}
