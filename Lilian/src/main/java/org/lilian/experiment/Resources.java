package org.lilian.experiment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Datasets;
import org.lilian.data.real.Generator;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.classification.Classification;
import org.lilian.data.real.classification.Classified;
import org.lilian.data.real.classification.Classifier;
import org.lilian.data.real.classification.Classifiers;
import org.lilian.data.real.fractal.IFS;
import org.lilian.data.real.fractal.IFSs;
import org.lilian.search.Builder;

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
	
	@Resource(name="random-ifs")
	public static List<Point> randomIFS(@Name("dim") int dim, @Name("components") int components, @Name("size") int size, @Name("var") double var)
	{
		List<Point> data = IFSs.random(dim, components, var).generator().generate(size);
		Collections.shuffle(data);
		
		return data;
	}	
	
	@Resource(name="random-sim-ifs")
	public static List<Point> randomSimIFS(@Name("dim") int dim, @Name("components") int components, @Name("size") int size, @Name("var") double var)
	{
		List<Point> data = IFSs.randomSimilitude(dim, components, var).generator().generate(size);
		Collections.shuffle(data);
		
		return data;
	}
	
	@Resource(name="by-parameters")
	public static List<Point> byParameters(@Name("dim") int dim, @Name("components") int components, @Name("parameters") List<Double> parameters, @Name("size") int size)
	{
		Builder<IFS<Similitude>> builder = IFS.builder(components, Similitude.similitudeBuilder(dim));
		IFS<Similitude> ifs = builder.build(parameters);
		
		List<Point> data = ifs.generator().generate(size);
		Collections.shuffle(data);
		
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
