package org.lilian.util.graphs.test;

import java.util.AbstractList;
import java.util.List;

public class Test
{


	interface Person {}
	
	interface Builder extends Person {}
	
	interface Thinker extends Person {}
	
	interface BrickLayer extends Builder {}
	
	interface Network<L extends Person> {}
	
	
}
