package org.lilian.experiment;

import java.lang.annotation.*;


/**
 * A simple tag for any property whose valuer should be reported in the output 
 * of an experiment. A simple example is a hardcoded default that cannot be set
 * through the constructor, but may change as the code evolves. 
 * 
 * Reportables are recorded, like Results, but their values aren't collated, 
 * analyzed, plotted, averaged, etc.
 * 
 * @author Peter
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Reportable
{
	String description() default "";
}
