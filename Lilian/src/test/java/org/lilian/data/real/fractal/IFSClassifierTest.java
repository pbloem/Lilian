package org.lilian.data.real.fractal;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.search.Builder;

public class IFSClassifierTest
{

	@Test
	public void test()
	{
		Builder<Similitude> sBuilder = Similitude.similitudeBuilder(2);
		Builder<IFS<Similitude>> ifsBuilder = IFS.builder(2, sBuilder);
		Builder<IFSClassifier> cBuilder = IFSClassifier.builder(2, 6, ifsBuilder);
		
		IFSClassifier cls = cBuilder.build(Point.random(cBuilder.numParameters(), 0.6)); 
		
		System.out.println(Arrays.toString(cls.density(new Point(0, 0), 1)));
		
	}

}
