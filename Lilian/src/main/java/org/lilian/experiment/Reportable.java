package org.lilian.experiment;

/**
 * A simple tag for any property whose valuer should be reported in the output 
 * of an experiment. A simple example is a hardcoded default that cannot be set
 * through the constructor, but may change as the code evolves. 
 * 
 * @author Peter
 */

public @interface Reportable
{
	String description();
}
