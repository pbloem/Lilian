package org.lilian.data.real;

import org.apache.commons.math.linear.RealVector;

/**
 * Implementation of the Runge Kutta method for approximating the trajectory
 * of a differential equation.
 * 
 * @author Peter
 *
 */
public class RK4 extends AbstractGenerator<Point>
{
	private Derivative d;
	private double h;
	private RealVector x;
	private double t = 0.0;
	
	public RK4(Derivative derivative, double h, RealVector initial)
	{
		this.d = derivative;
		this.h = h;
		this.x = initial;
	}
	
	public static interface Derivative
	{
		public RealVector derivative(RealVector x, double t);
	}

	@Override
	public Point generate()
	{		
		RealVector k1 = d.derivative(x, t).mapMultiply(h),
		           k2 = d.derivative(x.add(k1.mapMultiply(0.5)), t + 0.5 * h).mapMultiply(h),
		           k3 = d.derivative(x.add(k2.mapMultiply(0.5)), t + 0.5 * h).mapMultiply(h),
		           k4 = d.derivative(x.add(k3), t + h).mapMultiply(h);
		
		//System.out.println(k1 + " " + k2 + " " + k3 + " " + k4);
		
		RealVector k = k1
				.add(k2.mapMultiply(2.0))
				.add(k3.mapMultiply(2.0))
				.add(k4);
		
		x = x.add(k.mapMultiply(1.0/6.0));
		t = t + h;
		
		return new Point(x); 
	}
}
