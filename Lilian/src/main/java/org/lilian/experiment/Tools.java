package org.lilian.experiment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions, mostly for reflection
 * @author Peter
 *
 */
public class Tools
{
	
	/**
	 * Returns all declared methods, including inherited.
	 */
	public static List<Method> allMethods(Class<?> clss)
	{
		List<Method> list =  new ArrayList<Method>();
		allMethods(clss, list, null);
		return list;
	}
	
	/**
	 * Returns all declared methods, including inherited, with the given annotation.
	 * 
	 * @param clss
	 * @param annotationType
	 * @return
	 */
	public static List<Method> allMethods(Class<?> clss, Class<? extends Annotation> annotationType)
	{
		List<Method> list =  new ArrayList<Method>();
		allMethods(clss, list, annotationType);
		return list;
	}
	
	private static void allMethods(Class<?> clss, List<Method> list, Class<? extends Annotation> annotationType)
	{
		if(clss == null)
			return;
		
		for(Method method : clss.getDeclaredMethods())
			if(annotationType == null)
				list.add(method);
			else
				for(Annotation ann : method.getAnnotations())
					if(annotationType.isAssignableFrom(ann.getClass()))
					{
						list.add(method);
						break;
					}	
					
		allMethods(clss.getSuperclass(), list, annotationType);
	}

}
