package org.lilian.models;

import java.io.*;
import java.util.*;

import org.lilian.corpora.Corpus;
import org.lilian.corpora.SequenceIterator;
import org.lilian.util.*;
import org.lilian.util.ranges.Range;
import org.lilian.util.ranges.RangeComparator;
import org.lilian.util.ranges.RangeSet;


/**
 * 
 */
public class Histogram extends BasicFrequencyModel<Range>
	implements RangeSet, Serializable
{

	private static final long serialVersionUID = -687895012053337798L;
	protected RangeSet ranges;

	public Histogram(RangeSet ranges)
	{
		this.ranges = ranges;
	}

	@Override
	public boolean hasRange(double value)
	{
		return ranges.hasRange(value);
	}

	@Override
	public Range first(double value)
	{
		return ranges.first(value);
	}

	@Override
	public List<Range> all(double value)
	{
		return ranges.all(value);
	}
	
	public void add(Number token)
	{
		if(ranges.hasRange(token.doubleValue()))
			add(ranges.first(token.doubleValue()));
	}
	
	/**
	 * Prints an extensive multiline summary of the model to an outputstream
	 */
	public void print(PrintStream out)
	{
		out.printf("total:           %.0f \n", total());
		out.printf("distinct:        %.0f \n", distinct());
		out.printf("mean value:      %.3f \n", mean());
		out.printf("variance:        %.3f \n", var());		
		out.printf("sample variance: %.3f \n", var());		
		out.printf("min, max:        %.3f, %.3f \n", min(), max());
		out.printf("entropy:         %.3f \n", entropy()); 
		out.println();
		
		List<Range> keys = new ArrayList<Range>(tokens());
		Collections.sort(keys, new RangeComparator());
		
		for(Range key : keys)
			out.printf("%s:  %.1f\n", key, frequency(key)); 
	}

	private double max()
	{
		// TODO Auto-generated method stub
		return 0.0;
	}

	private double min()
	{
		// TODO Auto-generated method stub
		return 0.0;
	}

	private double var()
	{
		// TODO Auto-generated method stub
		return 0.0;
	}

	private double mean()
	{
		// TODO Auto-generated method stub
		return 0.0;
	}	

}