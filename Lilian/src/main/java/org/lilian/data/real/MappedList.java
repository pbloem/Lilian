package org.lilian.data.real;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

public class MappedList extends AbstractList<Point>
	implements Serializable
{
	private static final long serialVersionUID = 4128056767336874825L;
	private List<Point> base;
	private Map map;
	
	public MappedList(List<Point> base, Map map)
	{
		this.base = base;
		this.map = map;
	}

	@Override
	public Point get(int index)
	{
		return map.map(base.get(index));
	}
	
	@Override
	public int size()
	{
		return base.size();
	}
		
}
