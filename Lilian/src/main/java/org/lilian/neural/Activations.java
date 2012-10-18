package org.lilian.neural;

public class Activations
{

	public static Activation sigmoid()
	{
		return new Sigmoid();
	}
	
	public static Activation sigmoid(double max)
	{
		return new Sigmoid(max);
	}	
	
	private static class Sigmoid implements Activation
	{
		private double max;

		public Sigmoid()
		{
			this(1.0);
		}
		
		public Sigmoid(double max)
		{
			this.max = max;
		}

		@Override
		public double function(double in)
		{
			return (2.0 * max)/(1.0 + Math.exp(-in)) - max;
		}
		
	}
}
