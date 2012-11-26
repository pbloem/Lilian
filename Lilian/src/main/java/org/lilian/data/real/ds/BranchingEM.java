package org.lilian.data.real.ds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lilian.data.real.Datasets;
import org.lilian.data.real.Point;
import org.lilian.neural.Activations;
import org.lilian.neural.NeuralNetworks;
import org.lilian.neural.ThreeLayer;
import org.lilian.search.Builder;
import org.lilian.search.Parameters;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.HausdorffDistance;

public class BranchingEM extends EM
{
	private int iteration = 0;
	
	private Distance<List<Point>> distance = new HausdorffDistance<Point>();
	
	private double branchingVariance;
	private int beamWidth;
	private int sampleSize;
	
	private List<Candidate> buffer = new ArrayList<Candidate>();
	private Builder<ThreeLayer> builder;  
	
	public BranchingEM(List<Point> data, double sigma, int numSources,
			ThreeLayer map,
			double branchingVariance, int beamWidth, int sampleSize)
	{
		super(data, sigma, numSources, map);
		
		this.branchingVariance = branchingVariance;
		this.beamWidth = beamWidth;
		this.sampleSize = sampleSize;
		
		builder = ThreeLayer.builder(map.dimension(), map.hiddenSize(), Activations.sigmoid());
	}
	
	@Override
	public void maximization(int epochs, double learningRate, boolean reset)
	{
		super.maximization(epochs, learningRate, reset);
		
		buffer.add(new Candidate(map));
		for(int i : Series.series(beamWidth - 1))
		{
			ThreeLayer perturbed = 
					Parameters.perturb(map, builder, branchingVariance);
			buffer.add(new Candidate(perturbed));
		}
		
		for(Candidate c : buffer) // recalculate distance every iteration
			c.update();
		
		Collections.sort(buffer);
		while(buffer.size() > beamWidth)
			buffer.remove(buffer.size() - 1);
		
		map = buffer.get(0).map();
		
		iteration ++;
	}

	private class Candidate implements Comparable<Candidate>, Serializable
	{
		private static final long serialVersionUID = 4314278761845843374L;

		private static final int INITIAL = 100;
		
		private ThreeLayer model;
		private double distance;
		private int lastUpdated = -1;
		
		public Candidate(ThreeLayer model)
		{
			this.model = model;
			update();
		}
		
		public void update()
		{
			if(lastUpdated == iteration)
				return;
			
			List<Point> dataSample = Datasets.sample(data, sampleSize);
			List<Point> modelSample = 
					NeuralNetworks.orbit(model, new Point(dim), sampleSize);
			
			distance = BranchingEM.this.distance.distance(dataSample, modelSample);
			
			lastUpdated = iteration;
		}
		
		@Override
		public int compareTo(Candidate other)
		{
			return Double.compare(distance, other.distance);
		}
		
		public ThreeLayer map()
		{
			return model;
		}
	}
}
