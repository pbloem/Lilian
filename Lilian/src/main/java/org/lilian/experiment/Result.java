package org.lilian.experiment;

/**
 * Any method annotated as a Result returns some value which represents the 
 * outcome of the experiment.
 * 
 * The running code decides what to do with a result based on its return type.
 * numeric results may be plotted against parameters, a list of numbers will be 
 * plotted against its index, and returned as a CSV file.
 * 
 * The annotation will be extended with features to communicate how the result 
 * should be interpreted.
 * 
 * @author Peter
 *
 */
public @interface Result
{
	String name();
}
