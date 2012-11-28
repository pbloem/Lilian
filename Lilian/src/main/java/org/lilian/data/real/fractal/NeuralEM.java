package org.lilian.data.real.fractal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.fractal.IFS.SearchResult;
import org.lilian.data.real.weighted.Weighted;
import org.lilian.data.real.weighted.WeightedLists;
import org.lilian.neural.Activations;
import org.lilian.neural.ThreeLayer;
import org.lilian.search.Builder;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class NeuralEM extends EM<ThreeLayer>
{
	public static final double ESTIMATED_CONTRACTION = 0.9; 
	public static final boolean RESET = true;
	
	protected int epochs;
	protected double learningRate;
	
	public NeuralEM(IFS<ThreeLayer> initial, List<Point> data, int numSources,
			Builder<ThreeLayer> builder, int epochs, double learningRate)
	{
		super(initial, data, numSources, 0.001, true, builder, 0.01);
		
		this.epochs = epochs;
		this.learningRate = learningRate;
	}

	@Override
	protected Weighted<List<Integer>> codes(
			Point point, IFS<ThreeLayer> model,
			int depth, int sources)
	{
		Weighted<List<Integer>> codes = 
				NeuralIFS.search(point, model, depth, ESTIMATED_CONTRACTION, sources).codes();
		// System.out.println(codes);
		
		return codes;
	}

	@Override
	protected ThreeLayer findMap(List<Point> from, List<Point> to, ThreeLayer old)
	{
//		ThreeLayer network = null;
//		if(RESET)
//			network = ThreeLayer.random(dimension, old.hiddenSize(), 0.5, Activations.sigmoid());
//		else
//			network = old;
		
		Similitude sim = org.lilian.data.real.Maps.findMap(from, to);
		ThreeLayer network = ThreeLayer.copy(sim, old.hiddenSize(), epochs*from.size(), learningRate, 0.1);
		
		// network.train(to, from, learningRate, epochs);
		return network;
	}

	public static IFS<ThreeLayer> initial(
			int dim, int comp, int hidden, double var)
	{
		Builder<ThreeLayer> tlBuilder = ThreeLayer.builder(dim, hidden, Activations.sigmoid());
		Builder<IFS<ThreeLayer>> ifsBuilder = IFS.builder(comp, tlBuilder);
		
		return ifsBuilder.build(Point.random(ifsBuilder.numParameters(), var));
	}
	
	public static IFS<ThreeLayer> initial(
			List<Point> data, int comp, int hidden, int examples, double learningRate, double var)
	{
		int dim = data.get(0).size();
		
		IFS<ThreeLayer> ifs = null;
		for(int i : Series.series(comp))
		{
			ThreeLayer map = ThreeLayer.random(dim, hidden, var, Activations.sigmoid());
			for(int j : Series.series(examples))
			{
				Point x = Functions.choose(data),
				      y = Functions.choose(data);
				
				map.train(x, y, learningRate);
			}
			
			if(ifs == null)
				ifs = new IFS<ThreeLayer>(map, 1.0);
			else
				ifs.addMap(map, 1.0);
		}
		
		return ifs;
	}
}
