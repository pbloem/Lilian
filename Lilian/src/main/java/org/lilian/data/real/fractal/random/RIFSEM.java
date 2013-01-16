package org.lilian.data.real.fractal.random;

import java.util.List;

import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;

public class RIFSEM
{

	private List<List<Point>> data;
	
	// * The three main components of an EM iteration
	private DiscreteRIFS<Similitude> model;
	private List<List<List<Integer>>> codes;
	private List<List<Integer>> sequences;
	
	private int dim, componentsPerIFS, numIFSs, depth;
	
	public RIFSEM(DiscreteRIFS<Similitude> initial, List<List<Point>> data, int depth)
	{
		this.model = initial;
		this.data = data;
		this.depth = depth;
		
	}
	
	/**
	 * Finds a new Model given the current codes and sequences.
	 */
	public void findModel()
	{
		
	}
	
	/**
	 * Finds a new coding given the current model and sequences.
	 */
	public void findCodes()
	{
		
	}
	
	/**
	 * Finds new sequences given the current model and coding
	 */
	public void findSequences()
	{
		
	}	
}
