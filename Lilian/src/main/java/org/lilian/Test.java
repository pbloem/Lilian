package org.lilian;

public class Test
{
	
	public interface Top 
	{
	}	
	
	public interface A extends Top 
	{
		public A self();
	}
	
	public interface B extends Top 
	{
		public B self();
	}
	
	public interface Bottom extends A, B
	{
		public Bottom self();
	}
}
