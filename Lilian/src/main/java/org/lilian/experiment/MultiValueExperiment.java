package org.lilian.experiment;

import static org.lilian.util.Series.series;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs several independent experiments sequentially, each in its own 
 * environment and directory inside the directory of the MultiExperiment.
 * 
 * The MultiExperiment will detect which experiments inherit from the same class
 * and collate their results if they do so.
 * 
 * For numerical results, it will compute the average, median, mode, variance, etc
 * 
 * For each parameter varying between experiments, the results will be plotted
 * as appropriate.
 * 
 * 
 * @author Peter
 *
 */
public class MultiValueExperiment extends MultiExperiment
{
	
	private Constructor<Experiment> structor;

	// * The parameters for which multiple values where defined
	private List<Integer> multiParameterIndices = new ArrayList<Integer>();
	private List<Parameter> multiParameter = new ArrayList<Parameter>();
	
	public @State int repeats = 1;
	/**
	 * Create different experiments for different inputs
	 * 
	 * @param ctr
	 * @param sameSeed Whether to start each experiment with the same seed, or to give ach a new seed
	 * @param inputs
	 */
	public MultiValueExperiment(Constructor<Experiment> ctr, boolean sameSeed, int repeats, Object... inputs)
	{
		
		super(ctr.getDeclaringClass(), sameSeed);
		structor = ctr;
		
		for(int i : series(inputs.length))
		{
			if(inputs[i] instanceof Run.Multi<?>)
			{
				multiParameterIndices.add(i);
				for(Annotation annotation : ctr.getParameterAnnotations()[i])
					if(annotation instanceof Parameter)
						multiParameter.add((Parameter) annotation);
			}
		}
		
		createExperiments(inputs, new Object[inputs.length], 0, repeats);
		
		this.repeats = repeats;
		this.sameSeed = sameSeed;
	}
	
	private void createExperiments(Object[] master, Object[] current, int i, int repeats)
	{
		if(master.length == i)
		{
			try
			{
				if(repeats == 1)
					experiments.add(structor.newInstance(current));
				else 
					experiments.add(new RepeatExperiment(structor.newInstance(current), repeats));
			} catch (Exception e)
			{
				throw new RuntimeException("Failed to create experiment", e);
			}
		} else if(! multiParameterIndices.contains(i))
		{
			current[i] = master[i];
			createExperiments(master, current, i + 1, repeats);
		} else 
		{
			for(Object value : ((Run.Multi<?>) master[i]))
			{
				Object[] newCurrent = Arrays.copyOf(current, current.length);
				newCurrent[i] = value;
				createExperiments(master, newCurrent, i+1, repeats);
			}
		}
	}

}
