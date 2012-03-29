package org.lilian.experiment;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

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
	protected List<Environment> children = new ArrayList<Environment>();
	protected File dir;
	protected PrintStream out;
	
	public Environment(File dir, PrintStream out)
	{
		super();
		this.dir = dir;
		this.out = out;
	}

	/**
	 * Generates a new environment in which to run an experiment. The environment
	 * resides in a subfolder of this environment, and is given its own 
	 * thread pool.
	 * 
	 * @return
	 */
	public Environment child()
	{
		return null;
	}
	
	/**
	 * The directory to which this experiment should write its results.
	 * @return
	 */
	public File directory()
	{
		return dir;
	}
	
	/**
	 * A printstream to write information to. 
	 * @return
	 */
	public PrintStream out()
	{
		return out;
	}
	
	/**
	 * Returns the environment of the currently running experiment.
	 * @return
	 */
	public static Environment current()
	{
		return current;
	}
	
	public static Environment current;
}
