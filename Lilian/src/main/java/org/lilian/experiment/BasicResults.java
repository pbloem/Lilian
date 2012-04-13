package org.lilian.experiment;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of multiple experiment results
 * @author Peter
 *
 */
public class BasicResults implements Results
{
	private List<Object> values = new ArrayList<Object>();
	private List<Result> annotations = new ArrayList<Result>();
	
	@Override
	public int size()
	{
		return values.size();
	}

	@Override
	public Object value(int i)
	{
		return values.get(i);
	}

	@Override
	public Result annotation(int i)
	{
		return annotations.get(i);
	}
	
	public void add(Object value, Result annotation)
	{
		values.add(value);
		annotations.add(annotation);
	}

}
