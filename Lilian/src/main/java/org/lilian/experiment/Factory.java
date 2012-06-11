package org.lilian.experiment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to tag static factory methods to be used in place of a constructor for 
 * an experiment
 * 
 * @author Peter
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Factory
{

}
