package org.lilian.experiment;

import java.util.List;

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
	
	public String description();
	
	public void setDescription(String description);
}
