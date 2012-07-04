package org.lilian.experiment;

import static org.lilian.util.Series.series;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
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
import java.util.Random;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.lilian.Global;
import org.lilian.data.real.Datasets;
import org.lilian.data.real.Histogram2D;
import org.lilian.data.real.Point;
import org.lilian.util.Series;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Executable. Runs either the experiment defined in the current directory, or
 * in the directory specified in the parameter.
 * 
 * @author Peter
 *
 */
public class Run
{

	
	public static final String INIT_FILE = "init.yaml";
	private static int numExperiments = 0;
	
	public static int repeats = 1;
	
	@Reportable
	public static long seed;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String dir = ".";
		if(args.length == 1)
			dir = args[0];
		
		try
		{
			run(new File(dir));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * The experiments that will be run
	 */
	public static List<Experiment> experiments = new ArrayList<Experiment>();
	
	/**
	 * Run the experiment defined in the file "init.yaml" in the given directory
	 * @param dir
	 * @throws IOException
	 */
	public static void run(File dir) throws IOException
	{
		// * Read the init file
		File initFile = new File(dir, INIT_FILE);
		if(! initFile.exists())
			throw new IOException("Init file ("+INIT_FILE+") not found in current directory ("+dir.getCanonicalPath()+").");
			
		Yaml yaml = new Yaml(new SafeConstructor());
		Object initObject = null;
		try {
			initObject = yaml.load(new FileReader(initFile));
		} catch (Exception e)
		{
			throw new RuntimeException("Problem reading init.yaml.", e);
		}
		
		System.out.println("Read init file" + initObject.getClass());
		
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
			Environment.current = new Environment(dir, Global.RANDOM_SEED);
			experiments.get(0).run();
			
			Global.log().info("Finished");
		}
	
	}
	
	/**
	 * Parse an individual experiment from a map to an experiment 
	 * constructor/factory
	 * 
	 * @param in
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Experiment parseExperiment(Map<String, ?> in)
	{
		Object classNameObj = in.get("class");
		if(classNameObj == null)
			throw new IllegalArgumentException("Experiment does not contain class key");
		if(!(classNameObj instanceof String))
			throw new IllegalArgumentException("Value of key 'class' was not parsed as a string, but as a " + classNameObj.getClass() + " (try enclosing with quotes).");
		String className = (String)classNameObj;

		// * Retrieve the class for the experiment
		Class<Experiment> experimentClass;
		try
		{
			experimentClass = (Class<Experiment>)Class.forName(className);
		} catch (ClassNotFoundException e)
		{
			throw new IllegalArgumentException("Class '"+className+"' was not found.", e);
		}
		
		System.out.println("Class found: " + experimentClass);
		
		// * If the repeat parameter is set, parse it
		if(in.containsKey("repeat"))
		{
			Object r = in.get("repeat");
			if (r instanceof Integer)
				repeats = (Integer) r;
			else 
			{
				throw new IllegalArgumentException("Integer value expected for key 'repeat'. Value (" + r + ") could not be parsed to integer.");
			}
			System.out.println("Found 'repeat' key. Each single experiment will be repeated " + repeats +" times.");
		}
		
		// * Read and set the description of the experiment
		String description = "";
		if(in.containsKey("description"))
			description = in.get("description") + "";
		
		if(in.containsKey("seed"))
		{
			Object seedObj = in.get("seed");
			
			if(seedObj.toString().trim().toLowerCase().equals("random"))
				seed = new Random().nextLong();
			else
				seed = Long.parseLong(seedObj.toString());
			
			Global.log().info("Set seed to " + seed);
			Global.random = new Random(seed);
		}
		
		List<String> parameters = new ArrayList<String>(in.keySet());
		
		// * These are standard keys. The rest are passed to the experiment
		parameters.remove("class");
		parameters.remove("name");
		parameters.remove("description");
		parameters.remove("repeat");
		parameters.remove("seed");
		
		System.out.println("parameters: " + parameters);

		// * Search for Constructors or Factory methods
		AccessibleObject thisCtor;
		
		Class<Parameter> paramClass;
		try
		{
			paramClass = (Class<Parameter>)Class.forName("org.lilian.experiment.Parameter");
		} catch (ClassNotFoundException e)
		{
			throw new IllegalStateException("class for Parameter annotation not found.", e);
		}
				
		Builder match = null;
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
			{
				match = new Builder(constructor);
				break;
			}
		}
		
		System.out.println(parametersOrdered);
		System.out.println(match);
		
		if(match == null)
		{
			System.out.println("Parameters given in init file do not match any of the constructors of the experiment " + classNameObj);
			System.out.println("Checking for for factory methods");
		
			for(Method factory : experimentClass.getDeclaredMethods())
			{
				// * check for @Factory annotation
				boolean isFactory = false;
				for(Annotation annotation : factory.getAnnotations())
					if(annotation instanceof Factory)
						isFactory = true;
				
				if(isFactory)
				{	

					List<String> parametersCopy = new ArrayList<String>(parameters);
					parametersOrdered.clear();
					pAnnotations.clear();
					
					for(Annotation[] annotations : factory.getParameterAnnotations())
					{
						Parameter pAnnotation = null;
						for(Annotation annotation : annotations)
						{
							if(annotation instanceof Parameter)
								pAnnotation = (Parameter)annotation;
						}
						
						
						if(pAnnotation == null) // unannotated constructor argument
							break;
						
						if(! parametersCopy.remove(pAnnotation.name()) )
							break; // parameter in init file, but not in constructor
						
						System.out.println(pAnnotation);
						
						parametersOrdered.add(pAnnotation.name());
						pAnnotations.add(pAnnotation);
					}
					
					if(parametersCopy.isEmpty())
					{
						match = new Builder(factory);
						break;
					}
				}
			}			
		}
		
		// * If match is still null, we've found no @Factory methods of 
		//   constructors that match the parameters in the init file.
		if(match == null)
			throw new IllegalArgumentException("No appropriate constructors or factory methods found.");
		
		System.out.println(parametersOrdered);
		
		Object[] inputs = new Object[parametersOrdered.size()];
		
		Class<?>[] types = match.getParameterTypes();
		
		System.out.println(parametersOrdered.size());
		
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
			MultiExperiment mexp = new MultiValueExperiment(match, true, repeats, inputs);
			numExperiments += mexp.size();
			exp = mexp;
		} else 
		{
			try
			{
//				for(Object input : inputs)
//					System.out.println("* " + input.getClass() + " " + input);
				exp =  (Experiment)match.newInstance(inputs);
				numExperiments ++;
				if(repeats > 1)
				{
					MultiExperiment mexp = new RepeatExperiment(match, repeats, inputs);
					numExperiments += mexp.size();
					exp = mexp;					
				}
			} catch (Exception e) {
				throw new RuntimeException("Error instantiating experiment", e);
			}
		}
		
		exp.setDescription(description); 
			
		return exp;
	}
	
	public static Object interpretValue(Object value, Parameter parameter, Class<?> type)
	{
		if(value instanceof Map<?, ?>)
		{
			if(((Map<String, ?>) value).containsKey("resource"))
				return interpretResource(value, parameter, type);
			if(((Map<String, ?>) value).containsKey("file"))
				return interpretFile(((Map<String, ?>) value), parameter, type);
			if(((Map<String, ?>) value).containsKey("image"))
				return interpretImage(((Map<String, ?>) value), parameter, type);	
		}

		if(equals(value.getClass(), type))
			return value;
		
		if (type.isAssignableFrom(value.getClass()))
			return value;
		
		if(value instanceof Collection<?> && !((Collection<?>)value).isEmpty() )
		{
			Object first = ((Collection<?>)value).iterator().next();
			Class<?> typeClass = first.getClass();
			
			if(equals(typeClass, type))
				return interpretMulti(value, parameter, type);
			
			if(first instanceof Map<?, ?>)
				return interpretMultiResource(value, parameter, type);
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
		    c.equals(Float.class) && type.equals(float.class) ||
		    c.equals(Boolean.class) && type.equals(boolean.class))
			return true;
		return false;
	}
	
	public static Object interpretFile(Map<String, ?> map, Parameter parameter, Class<?> type)
	{
		String fileName = (String)map.get("file");
		File file = new File(fileName);
		
		if(type.equals(List.class)) // 
		{
			List<Point> data = null;
			try
			{
				data = Datasets.readCSV(file);
			} catch (Exception e)
			{
				throw new RuntimeException("Failed to read CSV file " + file, e);
			}
			
			return data;
		}
		
		return file;
	}
	
	public static Object interpretImage(Map<String, ?> map, Parameter parameter, Class<?> type)
	{
		String fileName = (String)map.get("image");
		File file = new File(fileName);
		BufferedImage image;
		try
		{
			image = ImageIO.read(file);
		} catch (IOException e)
		{
			throw new RuntimeException("Failed to read image file " + file, e);
		}
		
		if(type.equals(List.class)) // 
		{
			Map<String, ?> par = (Map<String, ?>) map.get("parameters");
			if(par == null)
				throw new IllegalArgumentException("No parameters were passed for image key");
		
			int n = (Integer)par.get("size"); // TODO handle exceptions
			
			List<Point> data = Histogram2D.fromImage(image).generate(n);

			return data;
		}
		
		return image;
	}	
	
	/**
	 * Match a @Resource method to the Map of values and call the 
	 * method with the declared values
	 * 
	 * @param value
	 * @param parameter
	 * @param type
	 * @return
	 */
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
			for(Annotation annotation : method.getAnnotations())
				if(annotation instanceof Resource)
					if( ((Resource)annotation).name().equals(resourceName))
						match = method;

		
		if(match == null)
			throw new IllegalArgumentException("Resource '"+resourceName+"' not found in class resources.");
		
		System.out.println(match);
		int n = match.getParameterTypes().length;
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
		Class<?>[] types = match.getParameterTypes();
		System.out.println(Arrays.toString(types));
		
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
			// * Strings match when files are requested
			if(File.class.equals(types[i]) && input instanceof String)
				input = new File((String)input);
			
			if(input == null)
				throw new IllegalArgumentException("Resource description does not define parameter " + name);

			System.out.println(name + " value-type: " + input.getClass() + " " + input );		
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
	
	public static String name(Map<String, ?> resource)
	{
		Object name = resource.get("name");
		if(name == null)
			throw new RuntimeException("Resource description does not contain 'name' key.");
		
		return name.toString();
	}
	
	public static Multi<Object> interpretMulti(Object value, Parameter parameter, Class<?> type)
	{
		Multi<Object> m = new Multi<Object>((Collection<?>) value);
		return m;
	}

	private static Object interpretMultiResource(Object value,
			Parameter parameter, Class<?> type)
	{
		Multi<Object> m = new Multi<Object>();
		
		int i = 0;
		for(Object object : (Collection<?>) value)
		{
			m.add(interpretResource(object, parameter, type));
			m.setName(i, name((Map<String, ?>)object));
			i++;
		}
		
		return m;
	}

	/**
	 * This  is a light warpper around an arraylist, to indicate later on that 
	 * the list is meant as a parameter sweep rather than a single parameter value. 
	 */
	public static class Multi<T> extends ArrayList<T>
	{	
		private static final long serialVersionUID = 2956329843595902841L;
		
		private List<String> names = new ArrayList<String>();

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
		
		public void setName(int i, String name)
		{
			while(names.size() < i + 1)
				names.add(null);
			
			names.set(i, name);
		}
		
		public String name(int i)
		{
			if(names.size() <= i)
				return get(i).toString();
			if(names.get(i) == null)
				return get(i).toString();
			
			return names.get(i);
		}
	}
	

	/**
	 * This object represents something callable that returns an experiment. 
	 * Either a constructor or a factory method annotated with @Factory.
	 * 
	 * @author Peter
	 *
	 */
	public static class Builder
	{
		private Constructor<?> structor = null;
		private Method factory = null;
		
		public Builder(Constructor<?> structor)
		{
			this.structor = structor;
		}
		
		public Experiment newInstance(Object[] inputs)
		{
			try
			{
				if(structor != null)
					return (Experiment) structor.newInstance(inputs);

				return (Experiment)factory.invoke(null, inputs);
			} catch (Exception e)
			{
				throw new RuntimeException("Failed to instantiate experiment", e);
			}
		}

		public Builder(Method factory)
		{
			this.factory = factory;
		}
		
		public Class<?>[] getParameterTypes()
		{
			if(structor != null)
				return structor.getParameterTypes();
			return factory.getParameterTypes();
		}
		
		public Class<? extends Experiment> getDeclaringClass()
		{
			if(structor != null)
				return (Class<? extends Experiment>)structor.getDeclaringClass();
			return (Class<? extends Experiment>)factory.getDeclaringClass();
		}

		public Annotation[][] getParameterAnnotations()
		{
			if(structor != null)
				return structor.getParameterAnnotations();
			return factory.getParameterAnnotations();
		}
	}
}
