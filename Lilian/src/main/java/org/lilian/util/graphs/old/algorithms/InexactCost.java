package org.lilian.util.graphs.old.algorithms;

/**
 * Specification of the cost function for inexact match 
 * 
 * TODO: the original paper (Bunke & Allerman 1983) specifies a seperate cost for 
 * link removal/addition when one of the nodeshas been deleted/added.
 * 
 * All functions should return values greater than one, so that 1 can be used as
 * a lower bound by the search algorithm.
 * 
 * @author Peter
 *
 */
public interface InexactCost<L>
{
	/**
	 * Cost of relabeling a node
	 * @return
	 */
	public double relabel(L in, L out);


	/**
	 * Cost of removing a node
	 * 
	 * @param label
	 * @return
	 */
	public double removeNode(L label);
	
	/**
	 * 
	 * @param label
	 * @return
	 */
	public double addNode(L label);
	
	public double removeLink();
	
	public double addLink();
}
