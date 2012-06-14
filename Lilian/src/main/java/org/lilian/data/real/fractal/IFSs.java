package org.lilian.data.real.fractal;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lilian.Global;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Map;
import org.lilian.data.real.Similitude;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;
import org.lilian.util.Series;


/**
 * Static helper functions for dealing with IFS models.
 * @author Peter
 *
 */
public class IFSs
{
	/**
	 * Generates a sierpinski gasket with non-uniform component weights
	 * 
	 * @return
	 */
	public static IFS<AffineMap> sierpinskiOff()
	{	
		Builder<IFS<AffineMap>> builder = IFS.builder(3, AffineMap.affineMapBuilder(2));
		return builder.build(Arrays.asList(
				0.5,0.0, 0.0,0.5,  0.0,-0.5, 10.0, 
				0.5,0.0, 0.0,0.5,  0.5, 0.5, 5.0, 
				0.5,0.0, 0.0,0.5, -0.5, 0.5, 20.0
				));
	}
	
	/**
	 * Generates a sierpinski gasket with non-uniform component weights
	 * 
	 * @return
	 */
	public static IFS<AffineMap> sierpinskiOff(double p1, double p2, double p3)
	{	
		Builder<IFS<AffineMap>> builder = IFS.builder(3, AffineMap.affineMapBuilder(2));
		return builder.build(Arrays.asList(
				0.5,0.0, 0.0,0.5,  0.0,-0.5, p1, 
				0.5,0.0, 0.0,0.5,  0.5, 0.5, p2, 
				0.5,0.0, 0.0,0.5, -0.5, 0.5, p3
				));
	}
	
	/**
	 * Generates a sierpinski gasket with non-uniform component weights
	 * 
	 * @return
	 */
	public static IFS<AffineMap> sierpinski()
	{	
	
		Builder<IFS<AffineMap>> builder = IFS.builder(3, AffineMap.affineMapBuilder(2));
		return builder.build(Arrays.asList(
				0.5,0.0, 0.0,0.5,  0.0, 0.5, 1.0, 
				0.5,0.0, 0.0,0.5,  0.5,-0.5, 1.0, 
				0.5,0.0, 0.0,0.5, -0.5,-0.5, 1.0
				));
	}
	
	/**
	 * Generates a sierpinski gasket with non-uniform component weights
	 * 
	 * @return
	 */
	public static IFS<Similitude> sierpinskiSim()
	{	
	
		Builder<IFS<Similitude>> builder = 
				IFS.builder(3, Similitude.similitudeBuilder(2));
		return builder.build(Arrays.asList(
				0.5,  0.0, 0.5, 0.0, 1.0, 
				0.5,  0.5,-0.5, 0.0, 1.0, 
				0.5, -0.5,-0.5, 0.0, 1.0
				));
	}	
	
	/**
	 * Generates a sierpinski gasket with non-uniform component weights
	 * 
	 * @return
	 */
	public static IFS<AffineMap> sierpinskiDown()
	{	
	
		Builder<IFS<AffineMap>> builder = IFS.builder(3, AffineMap.affineMapBuilder(2));
		return builder.build(Arrays.asList(
				0.5,0.0, 0.0,0.5,  0.0,-0.5, 1.0, 
				0.5,0.0, 0.0,0.5,  0.5, 0.5, 1.0, 
				0.5,0.0, 0.0,0.5, -0.5, 0.5, 1.0
				));
	}



	public static IFS<AffineMap> cantor()
	{
		Builder<IFS<AffineMap>> builder = IFS.builder(2, AffineMap.affineMapBuilder(2));
		return builder.build(Arrays.asList(
				1/3.0,0.0, 0.0,1/3.0,  2/3.0,0.0, 1.0,
				1/3.0,0.0, 0.0,1/3.0, -2/3.0,0.0, 1.0
				));		
	}
	
	public static IFS<AffineMap> cantorA()
	{
		Builder<IFS<AffineMap>> builder = IFS.builder(2, AffineMap.affineMapBuilder(2));
		return builder.build(Arrays.asList(
				.45,0.0, 0.0,.45,  .5, .5, 1.0,
				.45,0.0, 0.0,.45, -.5,-.5, 1.0
				));		
	}
	
	public static IFS<Similitude> cantorASim()
	{
		Builder<IFS<Similitude>> builder = IFS.builder(2, Similitude.similitudeBuilder(2));
		return builder.build(Arrays.asList(
				.45,  .5, .5, 0.0, 1.0,
				.45, -.5,-.5, 0.0, 1.0
				));		
	}	

	public static IFS<AffineMap> cantorB()
	{
		Builder<IFS<AffineMap>> builder = IFS.builder(2, AffineMap.affineMapBuilder(2));
		return builder.build(Arrays.asList(
				.45,0.0, 0.0,.45, -.5, .5, 1.0,
				.45,0.0, 0.0,.45,  .5,-.5, 1.0
				));		
	}
	
	
	public static IFS<Similitude> koch2Sim()
	{
		double 	a = Math.sqrt(13.0/9.0),
				angle = Math.atan(2.0/3.0),
				y = - 0.5 * a * Math.sin(angle),
				x1 = - 1.0 + 0.5 * a * Math.cos(angle),
				x2 =   1.0 - 0.5 * a * Math.cos(angle);
		
		Builder<IFS<Similitude>> builder = IFS.builder(2, Similitude.similitudeBuilder(2));
		return builder.build(Arrays.asList(
				a/2.0, x1,y, -angle - Math.PI, 1.0,
				a/2.0, x2,y,  angle + Math.PI, 1.0
				));
	}
	
	public static IFS<Similitude> koch2DownSim()
	{
		double 	a = Math.sqrt(13.0/9.0),
				angle = Math.atan(2.0/3.0),
				y = - 0.5 * a * Math.sin(angle),
				x1 = - 1.0 + 0.5 * a * Math.cos(angle),
				x2 =   1.0 - 0.5 * a * Math.cos(angle);
		
		Builder<IFS<Similitude>> builder = IFS.builder(2, Similitude.similitudeBuilder(2));
		return builder.build(Arrays.asList(
				a/2.0,  x1, -y, -(angle + Math.PI), 1.0,
				a/2.0, -x2, -y,  (angle + Math.PI), 1.0
				));
	}	
	
//	public static IFSDensityModel koch2Down()
//	{
//		double 	a = Math.sqrt(13.0/9.0),
//				angle = Math.atan(2.0/3.0),
//				y = - 0.5 * a * Math.sin(angle),
//				x1 = - 1.0 + 0.5 * a * Math.cos(angle),
//				x2 =   1.0 - 0.5 * a * Math.cos(angle);
//		
//		List<Double> params = Arrays.asList(
//				1.0, x2, -y, a/2.0, a/2.0, -(angle + Math.PI),
//				1.0, x1, -y, a/2.0, a/2.0,  (angle + Math.PI));		
//		
//		IFSDensityModel koch = new IFSDensityModel(params, 2, 2);
//		
//		return koch;
//	}	
	
	
	public static IFS<AffineMap> random()
	{
		return random(2, 3, 0.25);
	}
		
	public static IFS<AffineMap> random(int d, int k, double stdDev)
	{
		// * Create a random IFS model
		Builder<IFS<AffineMap>> builder = IFS.builder(k, AffineMap.affineMapBuilder(d));
		int num = builder.numParameters(); //IFS.numParameters(k, AffineMap.numParameters(d));
		
		List<Double> params = new ArrayList<Double>(num);
		for(int j = 0; j < num; j++)
				params.add(Global.random.nextGaussian() * stdDev);		
		
		return builder.build(params);		
	}
	
	public static IFS<Similitude> randomSimilitude(int d, int k, double stdDev)
	{
		// * Create a random IFS model
		Builder<IFS<Similitude>> builder = IFS.builder(k, Similitude.similitudeBuilder(d));
		int num = builder.numParameters(); //IFS.numParameters(k, AffineMap.numParameters(d));
		
		List<Double> params = new ArrayList<Double>(num);
		for(int j = 0; j < num; j++)
				params.add(Global.random.nextGaussian() * stdDev);		
		
		return builder.build(params);		
	}
		
	
	/**
	 * Returns a slightly perturbed version of an IFS model.
	 * 
	 * @param in
	 * @param variance The variance of the random values added to each parameter
	 * @return
	 */
	public static <M extends Map & Parametrizable> IFS<M> perturb(
			IFS<M> in, Builder<IFS<M>> builder, double variance)
	{
		List<Double> params = new ArrayList<Double>(in.parameters());
		
		for(int i = 0; i < params.size(); i++)
			params.set(i,  params.get(i) + Global.random.nextGaussian() * variance);
		
		return builder.build(params);
	}
	
	/**
	 * Returns a slightly perturbed version of an IFS model.
	 * 
	 * @param in
	 * @param variance The variance of the random values added to each parameter
	 * @return
	 */
	public static <M extends Map & Parametrizable> IFS<M> perturb(
			IFS<M> in, Builder<IFS<M>> builder, List<Double> change)
	{
		List<Double> params = new ArrayList<Double>(in.parameters());
		
		for(int i = 0; i < params.size(); i++)
			params.set(i,  params.get(i) + change.get(i));
		
		return builder.build(params);
	}

//	public static IFSDensityModel randomTSR(int comp, double stdDev)
//	{
//		List<Double> parameters = new ArrayList<Double>();
//		
//		int size = comp * (1 + AffineMap.numParameters(2, AffineMap.Mode.TSR));
//		for(int i = 0; i < size; i++)
//			parameters.add(Global.random.nextGaussian() * stdDev);
//		
//		return new IFSDensityModel(parameters, 2, comp);
//	}
//	
//	public static IFSDensityModel barnsley()
//	{
//		
//		EllipsoidModel basis = Conf.current.basisModel(2);		
//		
//		Matrix r1, r2, r3, r4;
//			
//		Vector t1, t2, t3, t4;
//				
//		
//		double p1, p2, p3, p4;
//		
//		// * Create the IFS representation of the Barnsley Fern		Matrix r1, r2, r3, r4;
//		
//		r1 = new DenseMatrix(2, 2);
//		r1.set(0, 0,  0.85); r1.set(0, 1, 0.04);
//		r1.set(1, 0, -0.04); r1.set(1, 1, 0.85);
//		
//		r2 = new DenseMatrix(2, 2);
//		r2.set(0, 0, -0.15); r2.set(0, 1, 0.28);
//		r2.set(1, 0,  0.26); r2.set(1, 1, 0.24);
//
//		r3 = new DenseMatrix(2, 2);
//		r3.set(0, 0, 0.20); r3.set(0, 1, -0.26);
//		r3.set(1, 0, 0.23); r3.set(1, 1,  0.22);
//
//		r4 = new DenseMatrix(2, 2);
//		r4.set(0, 0, 0.01); r4.set(0, 1, 0.01);
//		r4.set(1, 0, 0.01); r4.set(1, 1, 0.16);
//		
//		t1 = new DenseVector(2);
//		t2 = new DenseVector(2);
//		t3 = new DenseVector(2);
//		t4 = new DenseVector(2);		
//		
//		t1.set(0, 0.0);	t1.set(1,  0.6);		
//		t2.set(0, 0.0);	t2.set(1, -0.66);
//		t3.set(0, 0.0); t3.set(1,  0.6);
//		t4.set(0, 0.0); t4.set(1,  0.0);		
//		
//		p1 = 0.25;
//		p2 = 0.25;
//		p3 = 0.25;
//		p4 = 0.01;
//		
//
//		IFSDensityModel barnsley = new IFSDensityModel(new AffineMap(r1, t1), p1, basis);
//		barnsley.addOperation(new AffineMap(r2, t2), p2);
//		barnsley.addOperation(new AffineMap(r3, t3), p3);
//		barnsley.addOperation(new AffineMap(r4, t4), p4);		
//		
//		return barnsley;
//	}	
//	
//	public static IFSDensityModel square()
//	{	
//		EllipsoidModel basis = Conf.current.basisModel(2);		
//		
//		Matrix r1, r2, r3, r4;
//			
//		Vector t1, t2, t3, t4;
//				
//		
//		double p1, p2, p3, p4;
//		
//		// * Create the IFS representation of the Barnsley Fern		Matrix r1, r2, r3, r4;
//		
//		r1 = new DenseMatrix(2, 2);
//		r1.set(0, 0,  0.5); r1.set(0, 1, 0.0);
//		r1.set(1, 0, -0.0); r1.set(1, 1, 0.5);
//		
//		r2 = r1.copy();
//		
//		r3 = r1.copy();
//	
//		r4 = r1.copy();
//		
//		t1 = new DenseVector(2);
//		t2 = new DenseVector(2);
//		t3 = new DenseVector(2);
//		t4 = new DenseVector(2);		
//		
//		t1.set(0, 0.4);	t1.set(1, 0.4);		
//		t2.set(0, 0.4);	t2.set(1, -0.4);
//		t3.set(0, -0.4); t3.set(1, 0.4);
//		t4.set(0, -0.4); t4.set(1, -0.4);		
//		
//		p1 = 0.25;
//		p2 = 0.25;
//		p3 = 0.25;
//		p4 = 0.25;
//		
//	
//		IFSDensityModel square = new IFSDensityModel(new AffineMap(r1, t1), p1, basis);
//		square.addOperation(new AffineMap(r2, t2), p2);
//		square.addOperation(new AffineMap(r3, t3), p3);
//		square.addOperation(new AffineMap(r4, t4), p4);		
//		
//		return square;
//	}
//	
//	public static IFSDensityModel squareUneven()
//	{	
//		EllipsoidModel basis = Conf.current.basisModel(2);		
//		
//		Matrix r1, r2, r3, r4;
//			
//		Vector t1, t2, t3, t4;
//				
//		
//		double p1, p2, p3, p4;
//		
//		// * Create the IFS representation of the Barnsley Fern		Matrix r1, r2, r3, r4;
//		
//		r1 = new DenseMatrix(2, 2);
//		r1.set(0, 0,  0.5); r1.set(0, 1, 0.0);
//		r1.set(1, 0, -0.0); r1.set(1, 1, 0.5);
//		
//		r2 = r1.copy();
//		
//		r3 = r1.copy();
//	
//		r4 = r1.copy();
//		
//		t1 = new DenseVector(2);
//		t2 = new DenseVector(2);
//		t3 = new DenseVector(2);
//		t4 = new DenseVector(2);		
//		
//		t1.set(0, 0.5);	t1.set(1, 0.5);		
//		t2.set(0, 0.5);	t2.set(1, -0.5);
//		t3.set(0, -0.5); t3.set(1, 0.5);
//		t4.set(0, -0.5); t4.set(1, -0.5);		
//		
//		p1 = 20;
//		p2 = 10;
//		p3 = 10;
//		p4 = 5;
//	
//		IFSDensityModel square = new IFSDensityModel(new AffineMap(r1, t1), p1, basis);
//		square.addOperation(new AffineMap(r2, t2), p2);
//		square.addOperation(new AffineMap(r3, t3), p3);
//		square.addOperation(new AffineMap(r4, t4), p4);		
//		
//		return square;
//	}	
//	
//	public static IFSDensityModel koch()
//	{	
//		double angle = (2.0 * Math.PI*60)/360.0;
//		double a = (2.0/6.0) * Math.sin(angle),
//		       b = (4.0/6.0) * Math.sin(angle/2.0),
//		       c = Math.sqrt(b*b - a*a);		
//		
//		List<Double> params = Arrays.asList(
//				1.0, -2.0/3.0, 0.0, 1/3.0, 1/3.0, 0.0,
//				1.0,       -c,  -a, 1/3.0, 1/3.0, -angle,
//				1.0,        c,  -a, 1/3.0, 1/3.0, angle,
//				1.0,  2.0/3.0, 0.0, 1/3.0, 1/3.0, 0.0);		
//		
//		IFSDensityModel koch = new IFSDensityModel(params, 2, 4);
//		
//		return koch;
//	}
//	
//	public static IFSDensityModel kochDown()
//	{	
//		double angle = (2.0 * Math.PI*60)/360.0;
//		double a = (2.0/6.0) * Math.sin(angle),
//		       b = (4.0/6.0) * Math.sin(angle/2.0),
//		       c = Math.sqrt(b*b - a*a);		
//		
//		List<Double> params = Arrays.asList(
//				1.0, -2.0/3.0, 0.0, 1/3.0, 1/3.0, 0.0,
//				1.0,       -c,  a, 1/3.0, 1/3.0, angle,
//				1.0,        c,  a, 1/3.0, 1/3.0, -angle,
//				1.0,  2.0/3.0, 0.0, 1/3.0, 1/3.0, 0.0);		
//		
//		IFSDensityModel koch = new IFSDensityModel(params, 2, 4);
//		
//		return koch;
//	}
//	
//	public static IFSDensityModel koch2()
//	{
//		double 	a = Math.sqrt(13.0/9.0),
//				angle = Math.atan(2.0/3.0),
//				y = - 0.5 * a * Math.sin(angle),
//				x1 = - 1.0 + 0.5 * a * Math.cos(angle),
//				x2 =   1.0 - 0.5 * a * Math.cos(angle);
//		
//		List<Double> params = Arrays.asList(
//				1.0, x1, y, a/2.0, a/2.0, -(angle + Math.PI),
//				1.0, x2, y, a/2.0, a/2.0,  (angle + Math.PI));		
//		
//		IFSDensityModel koch = new IFSDensityModel(params, 2, 2);
//		
//		return koch;
//	}
//	
//	public static IFSDensityModel koch2Down()
//	{
//		double 	a = Math.sqrt(13.0/9.0),
//				angle = Math.atan(2.0/3.0),
//				y = - 0.5 * a * Math.sin(angle),
//				x1 = - 1.0 + 0.5 * a * Math.cos(angle),
//				x2 =   1.0 - 0.5 * a * Math.cos(angle);
//		
//		List<Double> params = Arrays.asList(
//				1.0, x2, -y, a/2.0, a/2.0, -(angle + Math.PI),
//				1.0, x1, -y, a/2.0, a/2.0,  (angle + Math.PI));		
//		
//		IFSDensityModel koch = new IFSDensityModel(params, 2, 2);
//		
//		return koch;
//	}	
//	
//	public static IFSDensityModel twoPartLine(Point p)
//	{
//		Point a = new Point(-1.0, 0);
//		Point b = new Point( 1.0, 0);
//		
//		List<Point> x, y0, y1;
//		x = new ArrayList<Point>(2);
//		y0 = new ArrayList<Point>(2);				
//		y1 = new ArrayList<Point>(2);			
//		
//		x.add(a);  x.add(b);
//		y0.add(a); y0.add(p);
//		y1.add(p); y1.add(b);			
//		
//		AffineMap m0 = AffineMap.findMap(x, y0);
//		AffineMap m1 = AffineMap.findMap(x, y1);
//		
//		IFSDensityModel model = new IFSDensityModel(m0, 1.0);
//		model.addOperation(m1, 1.0);
//		
//		return model;		
//	}
//	
//	public static IFSDensityModel nPartLine(Point... p)
//	{
//		return nPartLine(Arrays.asList(p));
//	}
//	
//	public static IFSDensityModel nPartLine(List<Point> p)
//	{	
//		Point a = new Point(-1.0, 0);
//		Point b = new Point( 1.0, 0);
//		
//		List<Point> x; 
//		x = new ArrayList<Point>(2);
//		x.add(a);  x.add(b);
//		
//		IFSDensityModel model = null;		
//		
//		for(int i = 0; i < p.size()+1;i++)
//		{
//			List<Point> y = new ArrayList<Point>();
//			Point q, r;
//			q = (i == 0)        ? a : p.get(i - 1);  
//			r = (i == p.size()) ? b : p.get(i);			
//			
//			y.add(q); 
//			y.add(r);
//			
//			AffineMap m = AffineMap.findMap(x, y);
//			
//			if(i == 0)
//				model = new IFSDensityModel(m, 1.0);
//			else
//				model.addOperation(m, 1.0);
//		}
//		
//		return model;	
//	}		

}
