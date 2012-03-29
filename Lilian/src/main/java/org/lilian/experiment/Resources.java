package org.lilian.experiment;

import java.util.Collections;
import java.util.List;

import org.lilian.data.real.Point;
import org.lilian.data.real.fractal.IFSs;

/**
 * These functions manage the resources embedded in the Lilian library. 
 * 
 * Lilian has some datasets embedded in its jar, and it also has some methods 
 * for generating data automatically. These can be called from the init file.
 * @author Peter
 *
 */
public class Resources
{
	
	@Resource(name="sierpinski")
	public static List<Point> sierpinski(@Name("size") int size)
	{
		List<Point> data = IFSs.sierpinski().generator().generate(size);
		Collections.shuffle(data);
		
		return data;
	}

}
