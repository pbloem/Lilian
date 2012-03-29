package org.lilian.experiment;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Resource
{
	String name();
}
