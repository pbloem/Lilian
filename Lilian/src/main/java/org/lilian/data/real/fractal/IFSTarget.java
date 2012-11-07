package org.lilian.data.real.fractal;

import java.util.ArrayList;
import java.util.List;

import org.lilian.Global;
import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
import org.lilian.search.Parametrizable;
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
public class IFSTarget<M extends Map & Parametrizable> implements Target<IFS<M>>
{
	private int sampleSize;
	private List<Point> target;
	
	public IFSTarget(int sampleSize, List<Point> target)
	{
		this.sampleSize = sampleSize;
		this.target = target;
	}

	@Override
	public double score(IFS<M> object)
	{
		List<Point> generatedPoints;
		List<Point> targetPoints;
		
		if(sampleSize == 0)
		{
			targetPoints = target;
			generatedPoints = object.generator().generate(target.size());
		}
		else
		{
			targetPoints = new ArrayList<Point>(sampleSize);
			for(int i = 0; i < sampleSize; i++)
				targetPoints.add(target.get(Global.random.nextInt(target.size())));
			generatedPoints = object.generator().generate(sampleSize);			
		}
		
		return - HausdorffDistance.hausdorff(generatedPoints, targetPoints);		
	}

}
