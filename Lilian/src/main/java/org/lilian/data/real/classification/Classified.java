package org.lilian.data.real.classification;

import java.util.Collection;
import java.util.List;

/**<p>
 * Represents a list of items with associated class </p><p>
 * 
 * The normal functions for adding items become unsupported and are replaced with 
 * versions that include a class parameter.</p><p>
 * 
 * See {Classifiers.combine()} for the standard way of creating this type of data.</p>
 * 
 * @author Peter
 *
 * @param <P>
 */
public interface Classified<P> extends List<P>
{

	public int cls(int i);
	
	public int numClasses();	
	
	/**
	 * Optional operation 
	 * 
	 * @param item
	 * @param cls
	 */
	public boolean add(P item, int cls);
	
	
	public boolean add(int index, P item, int cls);

	
	public boolean addAll(Collection<? extends P> c, int cls);
	
	public boolean addAll(int index, Collection<? extends P> c, int cls);
	
	/**
	 * 
	 * @param data
	 * @param classes
	 * @return
	 */
	public P set(int i, P item, int cls);
	
	public Classified<P> subClassified(int from, int to);
	
	
	public List<P> points(int cls);
}
