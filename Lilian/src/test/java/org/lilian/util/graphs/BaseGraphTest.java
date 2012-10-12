package org.lilian.util.graphs;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;
import org.lilian.util.graphs.old.BaseGraph;

public class BaseGraphTest
{

	@Test
	public void sizeTest()
	{
		BaseGraph<String> a = new BaseGraph<String>();
		
		BaseGraph<String>.Node a0 = a.addNode("a0");
		BaseGraph<String>.Node a1 = a.addNode("a1");
		BaseGraph<String>.Node a2 = a.addNode("a2");
		
		assertEquals(a.size(), 3);
	}
	
	@Test
	public void iteratorTest()
	{
		BaseGraph<String> a = new BaseGraph<String>();
		
		BaseGraph<String>.Node a0 = a.addNode("a0");
		BaseGraph<String>.Node a1 = a.addNode("a1");
		BaseGraph<String>.Node a2 = a.addNode("a2");
		
		Iterator<BaseGraph<String>.Node> it = a.iterator();
		while(it.hasNext())
			System.out.println(it.next());
		
		BaseGraph<String> b = new BaseGraph<String>();
		
		BaseGraph<String>.Node b0 = b.addNode("a0");
		BaseGraph<String>.Node b1 = b.addNode("a1");
		BaseGraph<String>.Node b2 = b.addNode("a2");
		BaseGraph<String>.Node b3 = b.addNode("a0");
		BaseGraph<String>.Node b4 = b.addNode("a1");
		BaseGraph<String>.Node b5 = b.addNode("a2");
		BaseGraph<String>.Node b6 = b.addNode("a0");
		BaseGraph<String>.Node b7 = b.addNode("a1");
		BaseGraph<String>.Node b8 = b.addNode("a2");
		
		
		it = b.iterator();
		while(it.hasNext())
			System.out.println(it.next());	
	}

}
