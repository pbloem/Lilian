package org.lilian.data.real.fractal;

import java.util.ArrayList;
import java.util.List;

import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.search.evo.Target;

public class EMBranching extends EM
{

	private int branches;
	private Target<IFS<Similitude>> target;
	
	public List<Candidate> candidates = new ArrayList<Candidate>();
	
	public EMBranching(IFS<Similitude> initial, List<Point> data, int beamwidth, double variance)
	{
		super(initial, data);
	}
	
	@Override
	public void iterate(int sampleSize, int depth)
	{
		super.iterate(sampleSize, depth);
		
		
	}

	private class Candidate implements Comparable<Candidate>
	{
		IFS<Similitude> model;
		double score;
		
		public Candidate(IFS<Similitude> model)
		{
			this.model = model;
			rescore();
		}

		@Override
		public int compareTo(Candidate other)
		{
			return - Double.compare(score, other.score());
		}

		public IFS<Similitude> model()
		{
			return model;
		}
		
		public void rescore()
		{
			this.score = target.score(model);
		}

		public double score()
		{
			return score;
		}
	}
	
}
