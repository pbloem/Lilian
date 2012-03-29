package org.lilian.experiment;

/**
 * Classes implementing this interface represent experiments to be run.
 * 
 * 
 * Results of the experiments are methods tagged with @Result.
 * 
 * 
 * @author Peter
 *
 */
public interface Experiment
{
	/**
	 * Runs the experiment. Resumes if the current directory contains a state 
	 * description.
	 */
	public void run();
	
	/**
	 * Resumes the experiment form the directory indicated by the current 
	 * environment
	 */
	public void resume();
	
	/**
	 * Saves the experiment's state to the directory indicated by the current 
	 * environment.
	 * 
	 * The experiment itself will usually call this method.
	 */
	public void save();

}
