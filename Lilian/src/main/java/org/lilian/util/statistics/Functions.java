package org.lilian.util.statistics;

import static java.lang.Math.*;

public class Functions
{
	/**
	 * A relatively arbitrary choice for epsilon
	 */
	private static final double EPSILON = 1E-12;
	
	
	/**
	 * Implementation of the Hurwitz zeta function (also known as the 
	 * generalized zeta function).
	 * 
	 * This implementation was ported from the cephes math library as included 
	 * in the scipy library (scipy/special/cephes/zeta.c)
	 * 
	 * TODO: memoize for low integer values
	 * 
	 * @param s
	 * @param k
	 * @return
	 */
	public static double zeta(double x, double q)
	{
		// * Check arguments
		if(x == 1.0)
			return Double.POSITIVE_INFINITY;
		if(q < 1.0)
			return Double.NaN;
		if(q <= 0.0)
			if(q == floor(q))
				return Double.POSITIVE_INFINITY;
			else
				return Double.NaN;
			
		double s = pow(q, -x);
		double a = q;
		double b = 0.0;
		
		int i = 0;
		
		boolean done = false;
		while( (i < 9 || a <= 9.0) && ! done)
		{
			i++;
			
			a++;
			b = pow(a, -x);
			s += b;
			
			if(abs(b/s) < EPSILON)
				done = true;
		}
		
		double k = 0.0;
		double w = a;
		s += b * w / (x - 1.0);
		s -= 0.5 * b;
		a = 1.0;

		double t;
		for(i = 0; i < 12 && ! done; i ++)
		{
			a *= x + k;
			b /= w;
			t = a * b / m[i];
			s += t;
			
			t = abs(t/s);
			if(t < EPSILON)
				done = true;
			
			k += 1.0;
			a *= x + k;
			b /= w;
			k += 1.0;
		}
		
		return s;
	}
	
	private static double m[] = {
		 12.0,
		-720.0,
		 30240.0,
		-1209600.0,
		 47900160.0,
		-1.8924375803183791606e9,  /*1.307674368e12/691*/
		 7.47242496e10,
		-2.950130727918164224e12,  /*1.067062284288e16/3617*/
		 1.1646782814350067249e14,  /*5.109094217170944e18/43867*/
		-4.5979787224074726105e15, /*8.028576626982912e20/174611*/
		 1.8152105401943546773e17,  /*1.5511210043330985984e23/854513*/
		-7.1661652561756670113e18  /*1.6938241367317436694528e27/236364091*/
		};

}
