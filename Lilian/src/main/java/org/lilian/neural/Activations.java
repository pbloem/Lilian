package org.lilian.neural;

public class Activations
{

	public static Activation sigmoid()
	{
		return new Sigmoid();
	}
	
	private static class Sigmoid implements Activation
	{
		public Sigmoid()
		{
			
		}

		@Override
		public double function(double in)
		{
			return 1.0/(1.0 + Math.exp(-in)) ;
		}

		@Override
		public double derivative(double in)
		{
			return in * (1.0 - in);
		}
		
	}
}
