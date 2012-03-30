package org.lilian.experiment;

import static org.lilian.util.Series.series;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lilian.util.Series;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Exectuable. Runs either the experiment defined in the current directory, or
 * in the directory specified in the parameter.
 * 
 * 
 * @author Peter
 *
 */
public class Run
{
	
	public static final String INIT_FILE = "init.yaml";
	private static int numExperiments = 0;


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			run(new File("."));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * The experiments that will be run
	 */
	public static List<Experiment> experiments = new ArrayList<Experiment>();
	
	public static void run(File dir) throws IOException
	{
		// * Read the init file
		File initFile = new File(dir, INIT_FILE);
		if(! initFile.exists())
			throw new IOException("Init file ("+INIT_FILE+") not found in current directory ("+dir+").");
			
		Yaml yaml = new Yaml(new SafeConstructor());
		Object initObject = null;
		try {
			initObject = yaml.load(new FileReader(initFile));
		} catch (Exception e)
		{
			throw new RuntimeException("Problem readin init.yaml.", e);
		}
		
		System.out.println("Read init file" + initObject.getClass());

		// * Perform basic sanity tests
		
		// * Parse the file into experiments
		HashMap<?, ?> map = (LinkedHashMap<?, ?>)initObject;
		
		for(Object key : map.keySet())
		{
			if(!key.equals("experiment"))
				throw new IllegalArgumentException("Top level key must be 'experiment' (was '"+key+"')");
			Object value = map.get(key);
			if(! (value instanceof Map<?, ?>))
				throw new IllegalArgumentException("Could not parse experiment description");
			
			experiments.add(parseExperiment( (Map<String, ?>) value ));
		}
		
		if(numExperiments == 0)
			System.out.print("No experiments found");
		
		else 
		{
			System.out.println(numExperiments + " experiment(s) found. Running.");
			Environment.current = new Environment(new File("."), System.out);
			experiments.get(0).run();
		}
	
	}
	
	@SuppressWarnings("unchecked")
	public static Experiment parseExperiment(Map<String, ?> in)
	{
		Object classNameObj = in.get("class");
		if(classNameObj == null)
			throw new IllegalArgumentException("Experiment does not contain class key");
		if(!(classNameObj instanceof String))
			throw new IllegalArgumentException("Value of key 'class' was not parsed as a string, but as a " + classNameObj.getClass() + " (try enclosing with quotes).");
		String className = (String)classNameObj;

		Class<Experiment> experimentClass;
		try
		{
			experimentClass = (Class<Experiment>)Class.forName(className);
		} catch (ClassNotFoundException e)
		{
			throw new IllegalArgumentException("Class '"+className+"' was not found.", e);
		}
		
		System.out.println("Class found: " + experimentClass);
				
		List<String> parameters = new ArrayList<String>(in.keySet());
		
		parameters.remove("class");
		parameters.remove("name");
		parameters.remove("description");
		
		System.out.println("parameters: " + parameters);
		
		Constructor<?> thisCtor;
		Class<Parameter> paramClass;
		try
		{
			paramClass = (Class<Parameter>)Class.forName("org.lilian.experiment.Parameter");
		} catch (ClassNotFoundException e)
		{
			throw new IllegalStateException("class for Parameter annotation not found.", e);
		}
		
		Constructor<?> match = null;
		List<String> parametersOrdered = new ArrayList<String>(parameters.size());
		List<Parameter> pAnnotations = new ArrayList<Parameter>(parameters.size());
		
		for(Constructor<?> constructor : experimentClass.getDeclaredConstructors())
		{
			List<String> parametersCopy = new ArrayList<String>(parameters);
			parametersOrdered.clear();
			pAnnotations.clear();
			
			for(Annotation[] annotations : constructor.getParameterAnnotations())
			{
				Parameter pAnnotation = null;
				for(Annotation annotation : annotations)
				{
					if(annotation.annotationType().equals(paramClass))
						pAnnotation = (Parameter)annotation;
				}
				
				if(pAnnotation == null) // unannotated constructor argument
					break;
				
				if(! parametersCopy.remove(pAnnotation.name()) )
					break; // parameter in init file, but not in constructor
				
				parametersOrdered.add(pAnnotation.name());
				pAnnotations.add(pAnnotation);
			}
			
			if(parametersCopy.isEmpty())
				match = constructor;
		}
		
		System.out.println(parametersOrdered);
		System.out.println(match);
		
		Object[] inputs = new Object[parametersOrdered.size()];
		Class<?>[] types = match.getParameterTypes();
		
		for(int i : series(parametersOrdered.size()))
			inputs[i] =	interpretValue(
						in.get(parametersOrdered.get(i)), pAnnotations.get(i), types[i]);
		
		// * Whether we want to run a single experiment or multiple ones
		boolean runMulti = false;
		for(int i : series(inputs.length))
			if(inputs[i] instanceof Multi<?>)
				runMulti = true;
		
		Experiment exp = null;
		if(runMulti)
		{
			MultiExperiment mexp = new MultiExperiment((Constructor<Experiment>)match, inputs);
			numExperiments += mexp.size();
			exp = mexp;
		} else 
		{
			try
			{
				for(Object input : inputs)
					System.out.println("* " + input.getClass());
				exp =  (Experiment)match.newInstance(inputs);
			} catch (Exception e) {
				throw new RuntimeException("Error instantiating experiment", e);
			}
			
			numExperiments ++;
		}
			
		return exp;
	}
	
	public static Object interpretValue(Object value, Parameter parameter, Class<?> type)
	{
		if(value instanceof Map<?, ?>)
			if(((Map<String, ?>) value).containsKey("resource"))
				return interpretResource(value, parameter, type);

		if(equals(value.getClass(), type))
			return value;
		
		if(value instanceof Collection<?>)
		{
			Class<?> typeClass = ((Collection<?>)value).iterator().next().getClass();
			if( !((Collection<?>)value).isEmpty() &&
					equals(typeClass, type))
				return interpretMulti(value, parameter, type);
		}
		
		throw new IllegalArgumentException("Error on value '"+value+"' of type '"+value.getClass()+"' for parameter '"+parameter+"'. Object of type '"+type+"' was expected or a collection of such objects, or a description that can be parsed into such an object.");
	}
	
	private static boolean equals(Class<?> c, Class<?> type)
	{
		if(c.equals(type)) 
			return true;
		if( c.equals(Integer.class) && type.equals(int.class) || 
		    c.equals(Long.class) && type.equals(long.class) ||
		    c.equals(Double.class) && type.equals(double.class) ||
		    c.equals(Float.class) && type.equals(float.class))
			return true;
		return false;
	}
	
	public static Object interpretResource(Object value, Parameter parameter, Class<?> type)
	{
		System.out.println(value);
		
		Map<String, ?> map = (Map<String, ?>) value;
		String resourceName = (String)map.get("resource");
		
		Class<Resources> resources = null;
		try
		{
			resources = (Class<Resources>)Class.forName("org.lilian.experiment.Resources");
		} catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Resources class not found in classpath", e);
		}
		
		Method match = null;
		for(Method method : resources.getDeclaredMethods())
		{
			for(Annotation annotation : method.getAnnotations())
				if(annotation instanceof Resource)
					if( ((Resource)annotation).name().equals(resourceName))
					{
						match = method;
					}
		}
		
		if(match == null)
			throw new IllegalArgumentException("Resource '"+resourceName+"' not found in class resources.");
		
		System.out.println(match);
		int n  = match.getParameterTypes().length;
		if(n == 0)
			try
			{
				return match.invoke(null);
			} catch (Exception e)
			{
				throw new RuntimeException("Method invocation failed", e);
			}
		
		Map<String, ?> par = (Map<String, ?>) map.get("parameters");
		if(par == null)
			throw new IllegalArgumentException("No parameters were passed for resource ("+resourceName+") but resource requires parameters ("+Arrays.toString(match.getParameterTypes())+").");
		
		Object[] inputs = new Object[n];
		Annotation[][] annoss = match.getParameterAnnotations();
		for(int i : series(annoss.length))
		{
			Annotation[] annos = annoss[i];
			
			Name name = null;
			for(Annotation anno : annos)
				if(anno instanceof Name)
					name = (Name) anno;
			if(name == null)
				throw new IllegalStateException("One or more of the parameters of resource '"+resourceName+"' is not annotated.");
			
			Object input = par.get(name.value());
			if(input == null)
				throw new IllegalArgumentException("Resource description does not define parameter " + name);
			
			inputs[i] = input;
		}
		
		Object resource;
		try {
			resource = match.invoke(null, inputs);
		} catch (Exception e)
		{
			throw new RuntimeException("Method invocation failed", e);
		}
		return resource;
	}
	
	public static Multi<Object> interpretMulti(Object value, Parameter parameter, Class<?> type)
	{
		Multi<Object> m = new Multi<Object>((Collection<?>) value);
		return m;
	}

	public static class Multi<T> extends ArrayList<T>
	{
		private static final long serialVersionUID = 2956329843595902841L;

		public Multi()
		{
			super();
		}

		public Multi(Collection<? extends T> c)
		{
			super(c);
		}

		public Multi(int initialCapacity)
		{
			super(initialCapacity);
		}
	}
}
