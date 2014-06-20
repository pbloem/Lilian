package org.lilian.data.real.fractal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lilian.data.real.Datasets;
import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.weighted.Weighted;
import org.lilian.neural.NeuralNetworks;
import org.lilian.neural.ThreeLayer;
import org.lilian.search.Builder;
import org.lilian.search.Parameters;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.HausdorffDistance;

public class BranchingEM extends SimEM
{
	private static final long serialVersionUID = -1580982630968024708L;

	private int iteration = 0;
	
	private Distance<List<Point>> distance = new HausdorffDistance<Point>();
	
	private double branchingVariance;
	private int beamWidth;
	private int sampleSize;
	
	private List<Candidate> buffer = new ArrayList<Candidate>();
	
	public BranchingEM(IFS<Similitude> initial, List<Point> data,
			int numSources, Builder<Similitude> builder,
			double branchingVariance, int beamWidth, int sampleSize, double spanningPointsVariance)
	{
		super(initial, data, numSources, builder, spanningPointsVariance);
		
		this.branchingVariance = branchingVariance;
		this.beamWidth = beamWidth;
		this.sampleSize = sampleSize;	
	}

	@Override
	protected void codesToModel()
	{
		super.codesToModel();
		
		buffer.add(new Candidate(model));
		for(int i : Series.series(beamWidth - 1))
		{
			IFS<Similitude> perturbed = 
					perturb(model, branchingVariance);
			buffer.add(new Candidate(perturbed));
		}
		
		for(Candidate c : buffer) // recalculate distance every iteration
			c.update(lastDepth);
		
		Collections.sort(buffer);
		while(buffer.size() > beamWidth)
			buffer.remove(buffer.size() - 1);
		
		model = buffer.get(0).map();
		
		iteration ++;	
	}
	
	protected IFS<Similitude> perturb(IFS<Similitude> in, double var)
	{
		IFS<Similitude> out = null;
		
		for(int i : Series.series(in.size()))
		{
			Similitude sim = in.get(i);
			sim = new Similitude(sim.scalar(), sim.translation(), 
					Parameters.perturb(sim.angles(), var));
			
			if(out == null)
				out = new IFS<Similitude>(sim, in.probability(i));
			else
				out.addMap(sim, in.probability(i));
		}
		
		return out;
	}

	private class Candidate implements Comparable<Candidate>, Serializable
	{
		private static final long serialVersionUID = 3303154652916321964L;
		
		private IFS<Similitude> model;
		private double distance;
		private int lastUpdated = -1;
		
		public Candidate(IFS<Similitude> model)
		{
			this.model = model;
		}
		
		public void update(double depth)
		{
			if(lastUpdated == iteration)
				return;
			
			List<Point> dataSample = Datasets.sample(data(), sampleSize);
			List<Point> modelSample = model.generator(depth).generate(sampleSize);
			
			distance = BranchingEM.this.distance.distance(dataSample, modelSample);
			
			lastUpdated = iteration;
		}
		
		@Override
		public int compareTo(Candidate other)
		{
			return Double.compare(distance, other.distance);
		}
		
		public IFS<Similitude> map()
		{
			return model;
		}
	}
}
