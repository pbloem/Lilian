package org.lilian.data.real;

import java.util.List;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

/**
 * A selection of maps from 3D to 2D.
 * @author Peter
 *
 */
public class Cameras
{
	public static Map basic()
	{
		double d = 1.1, s2 = Math.sqrt(2.0);
		
		return new Basic(
			new Point(d, -d, d/5).getVector(), // x, y, z 
			new Point(0, 0, 1.0).getVector(),
			new Point(0.40 * Math.PI, 0.75 * Math.PI, Math.PI)); // y z x
	}
	
	private static class Basic extends AbstractMap
	{
		private static final long serialVersionUID = 1L;
		RealVector cameraPosition, displayPosition;
		RealMatrix rotation;
				
		public Basic(RealVector cameraPosition, RealVector displayPosition, List<Double> cameraOrientation)
		{
			this.cameraPosition = cameraPosition;
			this.displayPosition = displayPosition;
			this.rotation = Rotation.toRotationMatrix(cameraOrientation);
		}

		@Override
		public Point map(Point in)
		{
			RealVector v = in.getVector();
			v = v.subtract(cameraPosition);
			
			// * d is our point in the camera's coord system
			RealVector d = rotation.operate(v);
			
			double x = (d.getEntry(0) - displayPosition.getEntry(0)) * (displayPosition.getEntry(2)/d.getEntry(2));
			double y = (d.getEntry(1) - displayPosition.getEntry(1)) * (displayPosition.getEntry(2)/d.getEntry(2));
			
			return new Point(x, y, 0);
		}

		@Override
		public boolean invertible()
		{			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Map inverse()
		{
			return null;
		}

		@Override
		public int dimension()
		{
			return 3;
		}

	}

}
