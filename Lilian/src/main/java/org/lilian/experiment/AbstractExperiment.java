package org.lilian.experiment;

import java.util.Date;

public abstract class AbstractExperiment implements Experiment
{
	public long t0;
	private long t;

	public final void run()
	{
		System.out.println("!!!starting!!!");
		t0 = System.currentTimeMillis();
		Environment.current().out().println("Starting run for experiment of type" + this.getClass());
		
		body();
		
		t = System.currentTimeMillis() - t0;
	}
	
	
	protected abstract void body();
	
	@Override
	public void resume()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void save()
	{
		// TODO Auto-generated method stub

	}

	@Result(name = "Total running time of a run of the sexperiment in seconds.")
	public double runtime()
	{
		return 0;
	}
	
	@Reportable(description = "The date and time at which the run of the experiment was started.")
	public Date startTime()
	{
		return null;
	}
	
	@Reportable(description = "The date and time at which the run of the experiment was finished.")
	public Date finishTime()
	{
		return null;
	}
}
