package org.lilian.data.real;

import java.util.ArrayList;
import java.util.List;

import org.lilian.Global;
import org.lilian.search.evo.Target;
import org.lilian.util.distance.HausdorffDistance;

/**
 * A target function for generators, based on a target dataset and the 
 * hausdorff metric.
 * 
 * @author peter
 *
 * @param <G>
 */
public class GeneratorTarget<G extends Generator> implements Target<G>
{
	private int sampleSize;
	private List<Point> target;
	
	public GeneratorTarget(int sampleSize, List<Point> target)
	{
		super();
		this.sampleSize = sampleSize;
		this.target = target;
	}

	@Override
	public double score(G object)
	{
		List<Point> generatedPoints;
		List<Point> targetPoints;
		
		if(sampleSize == 0)
		{
			targetPoints = target;
			generatedPoints = object.generate(target.size());
		}
		else
		{
			targetPoints = new ArrayList<Point>(sampleSize);
			for(int i = 0; i < sampleSize; i++)
				targetPoints.add(target.get(Global.random.nextInt(target.size())));
			generatedPoints = object.generate(sampleSize);			
		}
		
		return - HausdorffDistance.hausdorff(generatedPoints, targetPoints);		
	}

}
