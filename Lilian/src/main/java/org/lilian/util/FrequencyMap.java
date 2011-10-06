package org.lilian.util;

import java.util.*;

import org.lilian.Global;

/**
 * Stores frequencies of frequencies. Used in smoothing
 *
 * This class counts how often frequencies are encountered. We'll
 * call the frequency of a frequency a metafrequency, If a requested
 * metafrequency is not stored, it is interpolated linearly from the
 * two nearest known frequencies.
 *
 * To be able to retrieve these neighbouring frequencies efficiently
 * a binary tree is constructed that splits the range of numbers from -infinity
 * to infinity into subsections. When a metafrequency is requested that the
 * map doesn't know, the tree can be used to find the two closest known values.
 *
 * @author Peter Bloem (0168491)
 */

public class FrequencyMap
{
	// maps frequencies to metafrequencies
	private Map<MDouble, MDouble> map;
	//stores ranges
	private TreeNode topNode;
	private int frequencies = 0;

	public FrequencyMap()
	{
		map = new HashMap<MDouble, MDouble>();
		topNode = new TreeNode(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Add a frequency. The metafrequency is incremented.
	 *
	 * @param frequency The frequency for which to increment the metaFrequency
	 */
	public void add(double frequency)
	{
		MDouble freq = new MDouble(frequency);

		if(map.containsKey(freq))
		{
			map.get(freq).increment(1.0);

		}else
		{	
			// add the new frequency to the map
			map.put(freq, new MDouble(1.0));

			// add the new frequency to the tree, ...
			TreeNode currentNode = topNode;

			// ... by following the tree down to our leaf node
			while(currentNode.hasChildren())
			{
				if(frequency > currentNode.getSplitpoint())
					currentNode = currentNode.getUpperChild();
				else
					currentNode = currentNode.getLowerChild();
			}
			// ... and splitting our leaf at the frequency ...
			currentNode.split(frequency);
			frequencies++;
		}
	}

	/**
	 * Add a frequency with a specific metafrequency. Overrides
	 * any old value. If the frequency is encountered again, the metafrequency will
	 * be incremented from this value.
	 */
	public void add(double frequency, double metaFrequency)
	{
		MDouble freq = new MDouble(frequency);

		if(map.containsKey(freq))
		{
			map.get(freq).setValue(metaFrequency);
		}else
		{
			// add the new frequency to the map
			map.put(freq, new MDouble(metaFrequency));

			// add the new frequency to the tree, ...
			TreeNode currentNode = topNode;

			// ... by following the tree down to our leaf node
			while(currentNode.hasChildren())
			{
				if(frequency > currentNode.getSplitpoint())
					currentNode = currentNode.getUpperChild();
				else
					currentNode = currentNode.getLowerChild();
			}
			// ... and splitting our leaf at the frequency ...
			currentNode.split(frequency);
			frequencies++;
		}
	}

	/**
	 * Get the metafrequency for a frequency. Interpolate if necessary
	 *
	 * @param frequency The frequency for which to find the metaFrequency
	 * @return The number of times this frequency has been encountered,
	 *			if no exact number is known, a linear interpolation is made,
	 *			to guess. If less than two different frequencies have been encountered,
	 *			interpolation will allways return 0.0
	 */
	public double get(double frequency)
	{

		MDouble metaFrequency;
		if(map.containsKey(new MDouble(frequency)))
		{
			metaFrequency = map.get(new MDouble(frequency));
		}else
		{
			if(frequencies < 2)
			{
				// not enough data to inter- or extrapolate
				metaFrequency = new MDouble(0.0);
			}else
			{
				// inter- or extrapolate if we haven't seen the frequency yet

				// x1 and x2 are the frequencies we'll base our estimate on and
				// y1 and y2 are their metafrequencies

				TreeNode currentNode = topNode;

				// find the leaf node for frequency ...
				while(currentNode.hasChildren()){
					if(frequency > currentNode.getSplitpoint())
						currentNode = currentNode.getUpperChild();
					else
						currentNode = currentNode.getLowerChild();
				}

				// ... if the leaf defines the left part of the range (infinity to something)
				if(currentNode.getStart() == Double.NEGATIVE_INFINITY)
				{
					currentNode = currentNode.getParent();
					currentNode = currentNode.getUpperChild();
					while(currentNode.hasChildren())
						currentNode = currentNode.getLowerChild();
				// ... or the right part of the range
				}else if(currentNode.getEnd() == Double.POSITIVE_INFINITY)
				{
					currentNode = currentNode.getParent();
					currentNode = currentNode.getLowerChild();
					while(currentNode.hasChildren())
						currentNode = currentNode.getUpperChild();
				}

				// currentNode is now the section part of our range
				// (from currentNode.getStart() to currentNode.getEnd() that has
				// real (non-infinite) values at either end, both of which are in our hashmap.

				MDouble x1 = new MDouble(currentNode.getStart()),
						x2 = new MDouble(currentNode.getEnd()),
						y1 = map.get(x1),
						y2 = map.get(x2);

				double y = interpolate( x1.getValue(),
										y1.getValue(),
										x2.getValue(),
										y2.getValue(),
										frequency);
				
				metaFrequency = new MDouble(y);
			}
		}

		return metaFrequency.getValue();
	}

	public String toString()
	{
		return map.toString();

	}
	/**
	 * Interpolates or extrapolates a value. Draws a line through (x1, y1) and (x2, y)
	 * and calculates the y coordinate the line passes through at x
	 *
	 * @throws IllegalArgumentException if x1 == x2
	 */
	private double interpolate(double x1, double y1, double x2, double y2, double x)
	{

		if(x1 == x2)
			throw new IllegalArgumentException("No interpolation possible, x1 == x2");

		// calculate a and b in the function y = ax + b, that describes the line
		double a, b, y;
		a = (y1 - y2) / (x1 - x2);
		b = y2 - a * x2;

		y = a * x + b;

		return y;
	}

	/**
	 * Used to create a binary tree that splits up the range of numbers from
	 * -inf to inf. Each time a new frequency is encountered, the tree is
	 * split at that frequency (by creating a new node).
	 */
	private class TreeNode
	{
		private TreeNode lowerChild, upperChild, parent;
		private double splitpoint, start, end;
		private boolean hasChildren = false;
		private boolean hasParent = false;

		public TreeNode(double start, double end)
		{
			this.start = start;
			this.end = end;
		}

		public TreeNode(double start, double end, TreeNode parent)
		{
			this.start = start;
			this.end = end;
			this.parent = parent;
			hasParent = true;
		}

		public double getStart()
		{
			return start;
		}

		public double getEnd()
		{
			return end;
		}

		public boolean hasChildren()
		{
			return hasChildren;
		}

		public boolean hasParent()
		{
			return hasParent;
		}

		public TreeNode getLowerChild()
		{
			return lowerChild;
		}

		public TreeNode getUpperChild()
		{
			return upperChild;
		}

		public TreeNode getParent()
		{
			return parent;
		}

		/**
		 * Returns the point around which the children of this node
		 * split the node's range
		 */
		public double getSplitpoint()
		{
			return splitpoint;
		}

		/**
		 * Splits this treenode's range in two, generating two child nodes
		 */
		public void split(double splitpoint)
		{
			if(splitpoint < start || end < splitpoint)
				throw new IllegalArgumentException("splitpoint (" + splitpoint + ") needs to be between start (" + start + ") and end (" + end + ")");

			this.splitpoint = splitpoint;

			lowerChild = new TreeNode(start, splitpoint, this);
			upperChild = new TreeNode(splitpoint, end, this);

			hasChildren = true;
		}

		public String toString()
		{
			return map.toString();
		}
	}

	/**
	 * For testing
	 *
	 */
	public static void main(String[] args)
	{
		int n = 100;
		FrequencyMap fm = new FrequencyMap();

		fm.add(8.0);
		fm.add(8.0);
		fm.add(8.0);
		fm.add(8.0);

		fm.add(10.0);

		fm.add(11.0);
		fm.add(11.0);
		fm.add(11.0);

		long t0;
		t0 = System.currentTimeMillis();
		
System.out.println(fm);		

		System.out.println(fm.get(8.0));
		System.out.println(fm.get(10.0));
		System.out.println(fm.get(9.0));
		System.out.println(fm.get(7.0));
		System.out.println(fm.get(11.0));
		System.out.println(fm.get(12.0));
		System.out.println(fm.get(10.5));
		System.out.println(fm.get(Math.PI));

		System.out.println("time:" + (System.currentTimeMillis() - t0) );

		System.out.println("Adding " + n + " values. ");

		for(int i = 0; i < n; i++)
		{
			fm.add((double)((int)(Global.random.nextDouble() * 15) * 15));
			if(i % 100 == 0)
				System.out.print("\r" + i);
		}

		System.out.println("\rDone.         ");

		t0 = System.currentTimeMillis();

		System.out.println(fm.get(8.0));
		System.out.println(fm.get(10.0));
		System.out.println(fm.get(9.0));
		System.out.println(fm.get(7.0));
		System.out.println(fm.get(11.0));
		System.out.println(fm.get(12.0));
		System.out.println(fm.get(1000));
		System.out.println(fm.get(Math.PI));

		System.out.println("time: " + (System.currentTimeMillis() - t0) );

		System.out.println(fm);

	}
}