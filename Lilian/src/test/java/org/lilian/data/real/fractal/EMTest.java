package org.lilian.data.real.fractal;

import static java.lang.Math.log;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.junit.Test;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

import java_cup.sym;

public class EMTest
{
	@Test
	public void quick()
	{
		RealVector a = EM.ones(2);
		RealVector b = EM.ones(13);
		
		// System.out.println(a.outerProduct(b));
	}
	
	@Test
	public void testExp()
	{
		int md = 2;
		IFS<Similitude> ifs = IFSs.sierpinskiSim();
		System.out.println(ifs);
		
		Similitude post = new Similitude(Arrays.asList(1.0, 1.0, 2.0, 0.111));
		System.out.println(post);
		
		List<Point> data = new ArrayList<Point>(13);
		
		data.add(new Point(2));
		for(Similitude sim : ifs)
			data.add(sim.map(data.get(0)));
		
		for(Similitude sim : ifs)
			data.addAll(sim.map(data.subList(1, 4)));

		data = post.map(data);
		
		RealMatrix p        = EM.p(13,  3, md);
		List<RealMatrix> pk = EM.pk(13, 3, md);
		RealVector z        = EM.z(13,  3, md);
		List<RealVector> zk = EM.zk(13, 3, md);
		RealMatrix t        = EM.t(2,   3, md);
			
		List<Double> depths = 
				asList(log(1.0/13.0), log(3.0/13.0), log(9.0/13.0));
		
		EM.expectation(depths, ifs, post, data, z, zk, p, pk, t);
		
		System.out.println(MatrixTools.toString(t));
		
		depths = EM.maximizeDepths(3, md, p);
		
		for(double depth : depths)
			System.out.print(Math.exp(depth) + " ");
		System.out.println("\n");
		
		for(int i : series(3))
			System.out.println(zk.get(i));
		
		RealMatrix tfd = EM.tFromData(3, md, data, post, p);
		RealVector zfd = EM.zFromData(3, md, data, post, p);
		
		RealMatrix tfm = t.getSubMatrix(0, t.getRowDimension()-1, 0, t.getColumnDimension()-1-(int)Math.pow(3, md));
		RealVector zfm = z.getSubVector(0, z.getDimension()-(int)Math.pow(3, md));
		
		ifs = EM.maximizeIFS(3, md, data, post, pk, tfm, zfm);
		
		System.out.println(ifs);
		
		post = EM.maximizePost(data, p, z, t);
		System.out.println(post);
	}	

	@Test
	public void testIndexOf()
	{
		List<Integer> empty = Collections.emptyList();

		int k;
		
		k= 2;
		assertEquals(0,  EM.indexOf(empty, k));
		assertEquals(1,  EM.indexOf(Arrays.asList(0), k));
		assertEquals(2,  EM.indexOf(Arrays.asList(1), k));
		assertEquals(3,  EM.indexOf(Arrays.asList(0, 0), k));
		assertEquals(10, EM.indexOf(Arrays.asList(1, 1, 0), k));
		assertEquals(24, EM.indexOf(Arrays.asList(1, 0, 0, 1), k));		
		
		k = 3;
		assertEquals(0,  EM.indexOf(empty, k));
		assertEquals(1,  EM.indexOf(Arrays.asList(0), k));
		assertEquals(2,  EM.indexOf(Arrays.asList(1), k));
		assertEquals(3,  EM.indexOf(Arrays.asList(2), k));
		assertEquals(10, EM.indexOf(Arrays.asList(0, 2), k));
		assertEquals(24, EM.indexOf(Arrays.asList(2, 0, 1), k));
		
		k = 16;
		assertEquals(0,  EM.indexOf(empty, k));
		assertEquals(1,  EM.indexOf(Arrays.asList(0), k));
		assertEquals(2,  EM.indexOf(Arrays.asList(1), k));
		assertEquals(16, EM.indexOf(Arrays.asList(15), k));
		assertEquals(18, EM.indexOf(Arrays.asList(1, 0), k));
	}
	
	@Test
	public void testCode()
	{		
		List<Integer> empty = Collections.emptyList();

		int k;
		
		k= 2;
		assertEquals(empty,            EM.code(0, k));
		assertEquals(asList(0),          EM.code(1, k));
		assertEquals(asList(1),          EM.code(2, k));
		assertEquals(asList(0, 0),       EM.code(3, k));
		assertEquals(asList(1, 0, 0, 1), EM.code(24, k));		

		k = 3;
		assertEquals(empty,  EM.code(0, k));
		assertEquals(asList(0),  EM.code(1, k));
		assertEquals(asList(1),  EM.code(2, k));
		assertEquals(asList(2),  EM.code(3, k));
		assertEquals(asList(0, 2), EM.code(10, k));
		assertEquals(asList(2, 0, 1), EM.code(24, k));
		
		k = 16;
		assertEquals(empty,  EM.code(0, k));
		assertEquals(asList(0),  EM.code(1, k));
		assertEquals(asList(1),  EM.code(2, k));
		assertEquals(asList(15), EM.code(16, k));
		assertEquals(asList(1, 0), EM.code(18, k));
	}

}
