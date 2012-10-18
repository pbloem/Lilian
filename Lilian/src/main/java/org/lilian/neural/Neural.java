package org.lilian.neural;

import java.util.List;

public interface Neural<T> extends List<T>
{
	/**
	 * The weight on the connection from node i to node j
	 * @param i
	 * @param j
	 * @return
	 */
	// public double weight(int i, int j);
	
	/**
	 * Iterate the network a single step 
	 */
	public void step();
}
