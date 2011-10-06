package org.lilian.data.real;

import java.util.Random;

public interface Generator
{
	public Point generate();	
	public Point generate(Random random);	
	
	public Point generate(int n);
	public Point generate(int n, Random random);	
	

}
