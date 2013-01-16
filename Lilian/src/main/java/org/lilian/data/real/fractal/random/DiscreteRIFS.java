package org.lilian.data.real.fractal.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.lilian.Global;
import org.lilian.data.real.AbstractGenerator;
import org.lilian.data.real.Generator;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.fractal.IFS;
import org.lilian.search.Parametrizable;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class DiscreteRIFS<M extends Map & Parametrizable> extends AbstractRIFS {

	private List<IFS<M>> models = new ArrayList<IFS<M>>();
	private List<Double> weights = new ArrayList<Double>();
	private double weightSum = 0.0;
	
	public DiscreteRIFS(IFS<M> model, double weight)
	{
		super(model.dimension());
		addModel(model, weight);
	}
	
//	public DiscreteRIFS(List<Double> parameters, int dimension, int numModels, int numComponents)
//	{
//		super(dimension);
//		
//		int n = parameters.size() / numModels;
//		for(int i = 0; i < numModels; i++)
//		{
//			double weight = Math.abs(parameters.get(i * n));
//			List<Double> subParams = parameters.subList(i*n + 1, (i+1) * n);
//					
//			addModel(new IFSDensityModel(subParams, dimension, numComponents), weight);
//		}
//	}
	
	public void addModel(IFS<M> model, double weight)
	{
		models.add(model);
		weights.add(weight);
		weightSum += weight;
	}
	
	public IFS<M> random(Random rand)
	{
		return models.get(draw(rand));
	}
	
	public int draw()
	{
		return Functions.draw(weights, weightSum);
	}
	
	public int draw(Random rand)
	{
		return Functions.draw(weights, weightSum, rand);
	}
	
	public int size()
	{
		return models.size();
	}
	
	public List<IFS<M>> models()
	{
		return Collections.unmodifiableList(models);
	}
	
	public Generator<Point> generator(ChoiceTree instance)
	{
		return new CTGenerator(instance);
	}
	
	private class CTGenerator extends AbstractGenerator<Point>
	{
		private ChoiceTree ct;

		public CTGenerator(ChoiceTree ct)
		{
			this.ct = ct;
		}

		@Override
		public Point generate()
		{
			return generate(ct.root());
		}
		
		private Point generate(ChoiceTree.Node node)
		{
			if(node.leaf())
				return basis.generate();
			
			IFS<M> ifs = models.get(node.codon());
			int choice = ifs.draw();
			
			
			ChoiceTree.Node child = node.children().get(choice);
			Point point = generate(child);
			
			return ifs.get(choice).map(point); 
		}
	}
	
	public IFS<M> meanModel()
	{
		IFS<M> result = null;
		for(int i = 0; i < models.size(); i++)
		{
			IFS<M> model = models.get(i);			
			double modelProb = weights.get(i) / weightSum;
			for(int j = 0; j < model.size(); j++)
			{
				M map = model.get(j);
				double mapProb = model.probability(j);
				
				if(result == null)
					result = new IFS<M>(map, mapProb * modelProb);
				else
					result.addMap(map, mapProb * modelProb);
			}
		}
		
		return result;
	}
	
	public String toString()
	{
		return "-"  + models.size() + "p:" + weights + "m:" + models;
	}
	
	public ChoiceTree randomInstance(int depth)
	{
		return ChoiceTree.random(this, depth);
	}
}
