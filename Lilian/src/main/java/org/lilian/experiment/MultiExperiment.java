package org.lilian.experiment;

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
public class MultiExperiment extends AbstractExperiment
{

	private List<Experiment> experiments;

	public MultiExperiment(List<Experiment> experiments)
	{
		super();
		this.experiments = experiments;
	}

	@Override
	protected void body()
	{
		for(Experiment exp : experiments)
			exp.run();
	}

	@Override
	protected void setup()
	{
		
	}
}
