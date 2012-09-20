package org.lilian.util.graphs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.util.graphs.algorithms.UndirectedVF2;

public class VF2Test
{

	@Test
	public void test()
	{
		BaseGraph<String> a = new BaseGraph<String>(),
		                  b = new BaseGraph<String>();
		
		BaseGraph<String>.Node a0 = a.addNode("a0");
		BaseGraph<String>.Node a1 = a.addNode("a1");
		BaseGraph<String>.Node a2 = a.addNode("a2");
		
		BaseGraph<String>.Node b0 = b.addNode("b0");
		BaseGraph<String>.Node b1 = b.addNode("b1");
		BaseGraph<String>.Node b2 = b.addNode("b2");
		
		a0.connect(a1);
		a1.connect(a2);
		
		b0.connect(b1);
		b0.connect(b2);
		
		System.out.println(a);
		System.out.println(b);
		
		UndirectedVF2<String, BaseGraph<String>.Node> vfs;
		
		vfs = new UndirectedVF2<String, BaseGraph<String>.Node>(a, b, true);
		assertFalse(vfs.matches());
		
		vfs = new UndirectedVF2<String, BaseGraph<String>.Node>(a, b, false);
		assertTrue(vfs.matches());
	
	}
	
	@Test
	public void test2()
	{
		BaseGraph<String> a = new BaseGraph<String>(),
		                  b = new BaseGraph<String>();
		
		BaseGraph<String>.Node aa = a.addNode("0");
		BaseGraph<String>.Node ab = a.addNode("1");
		BaseGraph<String>.Node ac = a.addNode("2");
		BaseGraph<String>.Node ad = a.addNode("3");
		BaseGraph<String>.Node ag = a.addNode("4");
		BaseGraph<String>.Node ah = a.addNode("5");
		BaseGraph<String>.Node ai = a.addNode("6");
		BaseGraph<String>.Node aj = a.addNode("7");
		
		BaseGraph<String>.Node bb = b.addNode("1");
		BaseGraph<String>.Node ba = b.addNode("0");
		BaseGraph<String>.Node bc = b.addNode("2");
		BaseGraph<String>.Node bg = b.addNode("4");		
		BaseGraph<String>.Node bd = b.addNode("3");
		BaseGraph<String>.Node bj = b.addNode("7");
		BaseGraph<String>.Node bh = b.addNode("5");
		BaseGraph<String>.Node bi = b.addNode("6");		


		aa.connect(ag);
		aa.connect(ah);
		aa.connect(ai);
		ab.connect(ag);
		ab.connect(ah);
		ab.connect(aj);
		ac.connect(ag);
		ac.connect(ai);
		ac.connect(aj);
		ad.connect(ah);
		ad.connect(ai);
		ad.connect(aj);

		ba.connect(bg);
		ba.connect(bh);
		ba.connect(bi);
		bb.connect(bg);
		bb.connect(bh);
		bb.connect(bj);
		bc.connect(bg);
		bc.connect(bi);
		bc.connect(bj);
		bd.connect(bh);
		bd.connect(bi);
		bd.connect(bj);
		
		System.out.println(a.size());
		System.out.println(b.size());
		
		UndirectedVF2<String, BaseGraph<String>.Node> vfs;
		
//		vfs = new UndirectedVF2<String, BaseGraph<String>.Node>(a, b, true);
//		assertTrue(vfs.matches());
		
		vfs = new UndirectedVF2<String, BaseGraph<String>.Node>(a, b, false);
		assertTrue(vfs.matches());
	
	}
	
	@Test
	public void test3()
	{
		Global.random = new Random();
		
		BaseGraph<String> a = new BaseGraph<String>(),
		                  b = new BaseGraph<String>();
		
		BaseGraph<String>.Node aa = a.addNode("0");
		BaseGraph<String>.Node ab = a.addNode("1");
		BaseGraph<String>.Node ac = a.addNode("2");
		BaseGraph<String>.Node ad = a.addNode("3");
		BaseGraph<String>.Node ag = a.addNode("4");
		BaseGraph<String>.Node ah = a.addNode("5");
		BaseGraph<String>.Node ai = a.addNode("6");
		BaseGraph<String>.Node aj = a.addNode("7");
				
		aa.connect(ag);
		aa.connect(ah);
		aa.connect(ai);
		ab.connect(ag);
		ab.connect(ah);
		ab.connect(aj);
		ac.connect(ag);
		ac.connect(ai);
		ac.connect(aj);
		ad.connect(ah);
		ad.connect(ai);
		ad.connect(aj);
		
		List<String> labels = new ArrayList<String>(Graphs.labels(a));
		Collections.shuffle(labels, Global.random);
		
		for(String label : labels)
			b.addNode(label);
			
		for(String first : labels)
			for(String second : labels)
				if(a.node(first).connected(a.node(second)))
					b.node(first).connect(b.node(second));
				
		System.out.println(a.size());
		System.out.println(b.size());
		
		UndirectedVF2<String, BaseGraph<String>.Node> vfs;
		
//		vfs = new UndirectedVF2<String, BaseGraph<String>.Node>(a, b, true);
//		assertTrue(vfs.matches());
		
		vfs = new UndirectedVF2<String, BaseGraph<String>.Node>(a, b, false);
		assertTrue(vfs.matches());
	
	}
	

}
