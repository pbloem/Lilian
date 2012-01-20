package org.lilian.experiment;

/**
 * Any constructor of any @Experiment class can immediately be used to start an 
 * experiment, but annotating the parameters with @Parameter allows one to name 
 * the parameters. This makes them easier to set, and allows the names to be 
 * used in the output (for instance as labels of graphs).
 * 
 * @author Peter
 */
public @interface Parameter
{
	String name();
}
