package org.lilian.experiment;

import java.lang.annotation.*;


/**
 * Any constructor of any @Experiment class can immediately be used to start an 
 * experiment, but annotating the parameters with @Parameter allows one to name 
 * the them. This makes them easier to set, and allows the names to be 
 * used in the output (for instance as labels of graphs).
 * 
 * @author Peter
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter
{
	String name();
	String description() default "";
}
