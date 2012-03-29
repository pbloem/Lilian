package org.lilian.experiment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * General purpose annotation for assigning items a name. Mostly to force java 
 * to retain this information at runtime. 
 * 
 * @author Peter
 *
 */
@Retention(RetentionPolicy.RUNTIME)

public @interface Name
{
	String value();
}
