package org.lilian;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class contains methods and objects that are used globally by all other 
 * classes. 
 * 
 * It contains, for example, a single random number generator that all
 * classes are supposed to use for random numbers. This rng can then be set to 
 * an instance with a specific seed, to make all experiments deterministic and 
 * reproducible.
 *
 * For commonly useful methods available for all classes that do not have this 
 * mandatory character, see org.lilian.util.Functions  
 * 
 */

public class Global
{
	/**
	 *
	 */
	public static Random random = new Random(42);
	
	// FIXME: Use a better solution for resources
	private static File resourcesDir = new File("/home/peter/Documents/Eclipse Workspace 3/lilian/resources/");
	
	/** 
	 * The directory containing the resources
	 * @return
	 */
	public static File resources() {
		return resourcesDir;
	}
	
	/**
	 * Shorthand for the global logger
	 * @return
	 */
	public static Logger log() { return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); }
	
}
