package org.lilian.data.real;

import java.util.ArrayList;
import java.util.List;

import org.lilian.util.Series;

public class CompositeMap extends AbstractMap
{
	private static final long serialVersionUID = 3238841774125616289L;
	private List<Map> components;
		
	public CompositeMap(Map...maps)
	{
		if(maps.length == 0)
			throw new IllegalArgumentException("Cannot create compositemap with no components");
		components = new ArrayList<Map>(maps.length);
		for(Map map : maps)
			components.add(map);
	}

	@Override
	public Point map(Point in)
	{
		Point p = in;
		for(Map map : components)
			p = map.map(p);
		
		return p;
	}

	@Override
	public boolean invertible()
	{
		for(Map map : components)
			if(! map.invertible())
				return false;
		return true;
	}

	@Override
	public Map inverse()
	{
		List<Map> inverses = new ArrayList<Map>(components.size());
		for(int i : Series.series(components.size()-1, -1))
		{
			inverses.add(components.get(i).inverse());
		}
		
		CompositeMap c = new CompositeMap();
		c.components = inverses;
		
		return c;
	}

	@Override
	public int dimension()
	{
		return components.get(0).dimension();
	}
}