package org.lilian.data.real;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;

public class RotationTest
{

	@Test
	public void testFindAngles()
	{
		List<Double> angles = Arrays.asList(1.1, 1.0, 0.9);
		
		RealMatrix matrix = Rotation.toRotationMatrix(angles);
		List<Double> out = Rotation.findAngles(matrix, 1000);
		
		for(double angle : out)
			System.out.println(
					(angle + Math.PI*10) % Math.PI
					);
	}

}
