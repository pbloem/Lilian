package org.lilian.data.real.fractal;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.util.List;

import org.junit.Test;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.search.Builder;
import org.lilian.search.evo.Target;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;
import org.lilian.util.distance.HausdorffDistance;

public class IFSTargetTest
{

	@Test
	public void testScore()
	{
		
		IFS<AffineMap> ifsModel = IFSs.sierpinski();
		MVN mvnModel = new MVN(2);
		
		List<Point> setA = ifsModel.generator().generate(1000);
		List<Point> setB = ifsModel.generator().generate(1000);
		List<Point> setC = mvnModel.generate(1000);
		
		Distance<List<Point>> d = new HausdorffDistance<Point>();
		
		System.out.println(d.distance(setA, setB));
		System.out.println(d.distance(setA, setC));		
	}

}
