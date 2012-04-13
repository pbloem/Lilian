package org.lilian.experiment;

/**
 * An object that contains multiple results.
 * 
 * @author Peter
 */
public interface Results
{

	public int size();
	
	public Object value(int i);
	public Result annotation(int i);
	
}
