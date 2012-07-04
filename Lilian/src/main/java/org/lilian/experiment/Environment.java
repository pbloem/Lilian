package org.lilian.experiment;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.lilian.Global;

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
	protected Logger logger;
	
	@Reportable
	protected long rngSeed;
	
	public Environment(File dir, long rngSeed)
	{
		super();
		this.dir = dir;
		
		this.rngSeed = rngSeed;
		Global.random = new Random(rngSeed);
		
		this.logger = Logger.getLogger(this.getClass().toString());
		logger.setLevel(Level.INFO);
		
		FileHandler handler;
		try
		{
			handler = new FileHandler(dir.getAbsolutePath() + File.separator + "log.txt");
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		handler.setFormatter(new SimpleFormatter());
		logger.addHandler(handler);
		
		ConsoleHandler cHandler = new ConsoleHandler();
		cHandler.setFormatter(new SimpleFormatter());
		logger.addHandler(cHandler);
		
		try
		{
			logger.info("Environment created in directory " + dir.getCanonicalPath());
		} catch (IOException e)
		{
			throw new RuntimeException("Directory " + dir + " not acessible");
		}
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
		File childDir = new File(dir, children.size() + "/");
		childDir.mkdirs();
		long childSeed = Global.random.nextLong();
		
		children.add(new Environment(childDir, childSeed));
		
		return children.get(children.size()-1);
	}
	
	/**
	 * Runs an experiment in a child environment of the current environment.
	 * 
	 * This only works in a strictly serial (non-parallel) environment.
	 * 
	 * @param e
	 * @return
	 */
	public void child(Experiment e)
	{
		Environment childEnvironment = child();
		Environment current = Environment.current;
		
		Environment.current = childEnvironment;
		e.run();
		
		Environment.current = current;
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
	public Logger logger()
	{
		return logger;
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

	public long seed()
	{
		return rngSeed;
	}
}
