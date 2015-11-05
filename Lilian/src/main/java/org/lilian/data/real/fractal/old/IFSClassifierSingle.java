package org.lilian.data.real.fractal.old;

import static org.lilian.util.Series.series;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.classification.AbstractClassifier;
import org.lilian.data.real.classification.Classified;
import org.lilian.data.real.classification.Classifier;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Series;

/**
 * This is a classifier that uses only a single IFS model. The classification is 
 * based on the codes assigned to points. 
 * 
 * 
 * TODO
 * @author Peter
 *
 */
public class IFSClassifierSingle extends AbstractClassifier implements Serializable 
{
	private IFS<Similitude> model;
	private int depth;
	private Node root = new Node();
	private boolean smooth;
	
	private AffineMap preMap = null;

	public IFSClassifierSingle(IFS<Similitude> model, int depth, boolean smooth, int numClasses)
	{
		super(model.dimension(), numClasses);
		this.depth = depth;
		this.model = model;
		this.smooth = smooth;
	}
	
	public IFSClassifierSingle(IFS<Similitude> model, int depth, boolean smooth, AffineMap preMap, int numClasses)
	{
		super(model.dimension(), numClasses);
		this.depth = depth;
		this.model = model;
		this.smooth = smooth;
		this.preMap = preMap;
	}	
	
	public void train(Point point, int cls)
	{
		if(preMap != null)
			point = preMap.map(point);
		
		List<Integer> code = IFS.code(model, point, depth);
//		System.out.println(code);
		root.observe(code, cls);
	}
	
	public void train(Classified<Point> data)
	{
		for(int i : series(data.size()))
			train(data.get(i), data.cls(i));
	}

	@Override
	public List<Double> probabilities(Point point)
	{	
		if(preMap != null)
			point = preMap.map(point);
		
		List<Integer> code = IFS.code(model, point, depth);
		BasicFrequencyModel<Integer> frq;
		
		if(smooth)
		{
			frq = new BasicFrequencyModel<Integer>();
			root.probabilitiesSmoothed(code, frq);
		} else
			frq = root.probabilities(code);
		
		// System.out.println(code + " " + frq);
		
		List<Double> probabilities = new ArrayList<Double>(numClasses);
		for(int i : series(numClasses))
			probabilities.add(frq.probability(i));
		
		return probabilities;
	}
	
	/**
	 * Nodes of the code tree
	 * 
	 * @author Peter
	 */
	private class Node
	{
		private Node parent = null;
		private int symbol = -1;
		private BasicFrequencyModel<Integer> probabilities = 
				new BasicFrequencyModel<Integer>();
		private Map<Integer, Node> children = new LinkedHashMap<Integer, Node>();
		
		private Node()
		{
			
		}
		
		public Node(int symbol, Node parent)
		{
			this.parent = parent;
		}
		
		public void observe(List<Integer> code, int cls)
		{
			probabilities.add(cls);
			
			if(code.isEmpty())
				return;
			
			int nextSymbol = code.get(0);
			// System.out.println("..." + nextSymbol + " " + probabilities);
			
			if(!children.containsKey(nextSymbol))
				children.put(nextSymbol, new Node(nextSymbol, this));
			
			children.get(nextSymbol).observe(code.subList(1, code.size()), cls);
		}
		
		public BasicFrequencyModel<Integer> probabilities(List<Integer> code)
		{
			if(code.isEmpty())
				return probabilities;
			
			int nextSymbol = code.get(0);
			if(!children.containsKey(nextSymbol))
				return probabilities;

			return children.get(nextSymbol).probabilities(code.subList(1, code.size()));
		}
		
		public void probabilitiesSmoothed(
				List<Integer> code, BasicFrequencyModel<Integer> sum)
		{
			add(sum, probabilities);
			
			if(code.isEmpty())
				return;
			
			int nextSymbol = code.get(0);
			if(!children.containsKey(nextSymbol))
				return;

			children.get(nextSymbol).probabilitiesSmoothed(code.subList(1, code.size()), sum);
		}		
	}
	
	private static <T> void add(BasicFrequencyModel<T> base, BasicFrequencyModel<T> toAdd)
	{
		for(T token : toAdd.tokens())
			base.add(token, toAdd.probability(token));
	}
}
