package org.lilian.data.real.fractal.flame;

import java.util.List;

import org.lilian.data.real.AbstractMap;
import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;

public abstract class FlameMap extends AbstractMap implements Parametrizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 955015396260062829L;
	private Point color;
	
	public FlameMap(Point color)
	{
		this.color = color;
	}

	@Override
	public boolean invertible() 
	{
		return false;
	}

	@Override
	public Map inverse() 
	{
		return null;
	}
	
	public Point color()
	{
		return color;
	}
}
