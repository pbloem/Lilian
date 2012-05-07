package org.lilian.experiment;

import java.util.Collections;
import java.util.List;

import org.lilian.data.real.Datasets;
import org.lilian.data.real.Generator;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.classification.Classification;
import org.lilian.data.real.classification.Classified;
import org.lilian.data.real.classification.Classifier;
import org.lilian.data.real.classification.Classifiers;
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
	
	@Resource(name="koch")
	public static List<Point> koch(@Name("size") int size)
	{
		List<Point> data = IFSs.koch2Sim().generator().generate(size);
		Collections.shuffle(data);
		
		return data;
	}
		
	
	@Resource(name="sierpinski-off")
	public static List<Point> sierpinskiOff(@Name("size") int size, @Name("p1") double p1, @Name("p2") double p2, @Name("p3") double p3)
	{
		List<Point> data = IFSs.sierpinskiOff(p1, p2, p3).generator().generate(size);
		Collections.shuffle(data);
		
		return data;
	}	
	
	@Resource(name="mandelbrot")
	public static List<Point> mandelbrot(@Name("size") int size)
	{
		List<Point> data = Datasets.mandelbrot().generate(size);
		
		return data;
	}
	
	@Resource(name="ball")
	public static List<Point> ball(@Name("dim") int dim,  @Name("size") int size)
	{
		List<Point> data = Datasets.ball(dim).generate(size);
		
		return data;
	}
	
	@Resource(name="sphere")
	public static List<Point> sphere(@Name("dim") int dim,  @Name("size") int size)
	{
		List<Point> data = Datasets.sphere(dim).generate(size);
		
		return data;
	}	

	@Resource(name="mvn")
	public static List<Point> mvn(@Name("dim") int dim,  @Name("size") int size)
	{
		Generator<Point> gen = new MVN(dim);
		List<Point> data = gen.generate(size);
		
		return data;
	}	
	
	@Resource(name="mandelbrot-class")
	public static Classified<Point> mandelbrotClass(@Name("size") int size)
	{
		List<Point> points = new MVN(2).generate(size);
		List<Integer> classes = Classifiers.mandelbrot().classify(points);
		
		return Classification.combine(points, classes);
	}		
}
