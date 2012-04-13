package org.lilian.experiment;

/**
 * Classes implementing this interface represent experiments to be run.
 * 
 * Results of the experiments are methods tagged with @Result.
 * 
 * @author Peter
 *
 */
public interface Experiment extends Cloneable
{
	/**
	 * Runs the experiment. Resumes if the current directory contains a state 
	 * description.
	 */
	public void run();

	public Experiment clone();
	
	public String description();
	
	public void setDescription(String description);

}
