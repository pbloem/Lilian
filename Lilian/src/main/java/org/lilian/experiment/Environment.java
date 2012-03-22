package org.lilian.experiment;

/**
 * Represents the basics of a running experiment, for instance the details of which
 * folder the experiment places its output, and the thread(s) of the experiment.
 * 
 * 
 * 
 * @author Peter
 *
 */
public class Environment
{

	/**
	 * Generates a new environment in which to run an experiment. The environment
	 * resides in a subfolder of this environment, and is given its own 
	 * threadpool.
	 * 
	 * @return
	 */
	public Environment child()
	{
		return null;
	}
	
	/**
	 * Returns the environment of the currently running experiment.
	 * @return
	 */
	public static Environment current()
	{
		return null;
	}
}
