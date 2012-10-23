package org.lilian.data.real.fractal;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lilian.data.real.Point;
import org.lilian.data.real.classification.AbstractClassifier;
import org.lilian.data.real.classification.Classifier;
import org.lilian.models.BasicFrequencyModel;

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
	private IFS<?> model;
	private Node root = new Node();

	public IFSClassifierSingle(IFS<?> model, int dimensionality, int numClasses)
	{
		super(dimensionality, numClasses);
		
		this.model = model;
	}

	@Override
	public List<Double> probabilities(Point point)
	{
		return super.probabilities(point);
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
			if(!children.containsKey(nextSymbol))
				children.put(nextSymbol, new Node(nextSymbol, this));
			
			children.get(nextSymbol).observe(code.subList(1, code.size()), cls);
		}
		
		public BasicFrequencyModel<Integer> probabilities(List<Integer> code)
		{
			if(code.isEmpty())
				return probabilities;
			
			return probabilities(code.subList(1, code.size()));
		}
		
		
	}

}
