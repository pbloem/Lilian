package org.lilian.experiment;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.jfree.io.FileUtilities;
import org.jfree.ui.FilesystemFilter;
import org.lilian.Global;
import org.lilian.data.real.classification.Classified;
import org.lilian.models.BasicFrequencyModel;
import org.lilian.util.Functions;
import org.lilian.util.ResultTurtleWriter;
import org.lilian.util.Series;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class AbstractExperiment implements Experiment
{
	private static final String STATE_FILE = "state.lilian";

	// *  FreeMarker config
	protected static Configuration fmConfig = new Configuration();
	
	private long t0;
	private long t1;
	private long t;
	
	private String description = "";	
	
	protected File dir;
	protected Logger logger;
	
	public AbstractExperiment()
	{
		fmConfig.setClassForTemplateLoading(getClass(), "/templates");
		fmConfig.setObjectWrapper(new DefaultObjectWrapper());  
	}

	public final void run()
	{
		dir = Environment.current().directory();
		logger = Environment.current().logger();
		
		t0 = System.currentTimeMillis();
		logger.info("Starting run for experiment of type" + this.getClass() + "(directory is "+dir+")");
		
		if(new File(dir, STATE_FILE).exists())
		{
			logger.info("Found statefile. Attempting to restart experiment.");
			load();
		} else {
			logger.info("No statefile found. Starting experiment from scratch.");
			setup();
		}	
		
		body();
		
		if(new File(dir, STATE_FILE).exists())
		{
			new File(dir, STATE_FILE).delete();	
		}
		
		t1 = System.currentTimeMillis();
		t = t1 - t0;
		
		try {
			runScripts();
		} catch(Exception e)
		{
			throw new RuntimeException("Error running scripts", e);
		}		
		
		try {
			writeReport();
		} catch(IOException e)
		{
			throw new RuntimeException("Error writing report", e);
		}
		
		tearDown();
		
		logger.info("Run finished");
	}
	
	/**
	 * Set up the experiment if it is started for the first time. If the 
	 * experiment is restarted, this method is skipped.
	 */
	protected void setup() {}
	
	protected abstract void body();
	
	/**
	 * This method can be used to set certain fields to null so that they can be
	 * garbage collected (since the runner retains all experiment objects)
	 */
	protected void tearDown() {}	

	private List<Field> stateFields()
	{
		// * Save all the fields tagged with @State
		// @SuppressWarnings("unchecked")
		Class<? extends Experiment> c = (Class<? extends Experiment>)this.getClass();
		// logger.info(c + "");
		
		List<Field> stateFields = new ArrayList<Field>();
		for(Field field : c.getFields())
		{
			System.out.println(field);
			for(Annotation annotation : field.getAnnotations())
			{
				if(annotation instanceof State)
				{
					stateFields.add(field);	
				}
				// logger.info(annotation + "");
			}
		}
		
		return stateFields;
	}
	
	public void save()
	{		
		File stateFileOld = new File(dir, STATE_FILE + ".old");
		File stateFile = new File(dir, STATE_FILE);
		File stateFileNext = new File(dir, STATE_FILE + ".next");
		
		try
		{
			ObjectOutputStream out = 
					new ObjectOutputStream(
							new BufferedOutputStream(
									new FileOutputStream(stateFileNext)));

			// * Save all the fields tagged with @State
			// @SuppressWarnings("unchecked")
			Class<? extends Experiment> c = this.getClass();
						
			for(Field field : stateFields())
			{
				Object value = field.get(this);
				out.writeObject(value);
			}
		
			out.flush();
			out.close();
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		} catch (IllegalAccessException e)
		{	
			throw new RuntimeException("Attempted to access experiment object with permission", e);
		}

		stateFile.renameTo(stateFileOld);
		stateFileNext.renameTo(stateFile);
	}
	
	public void load()
	{
		
		File stateFile = new File(dir, STATE_FILE);
		
		try
		{
			ObjectInputStream in = 
					new ObjectInputStream(
							new BufferedInputStream(
									new FileInputStream(stateFile)));

			// * Save all the fields tagged with @State
			// @SuppressWarnings("unchecked")
			Class<? extends Experiment> c = this.getClass();
			
			for(Field field : stateFields())
			{
				logger.info("Reading object for field " + field);
				Object value = in.readObject();
				logger.info(" * " + value.getClass());
				logger.info(" - " + value);
				field.set(this, value);
			}
			
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		} catch (IllegalAccessException e)
		{	
			throw new RuntimeException("Attempted to access experiment object with permission", e);
		} catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Error reading experiment state from stream", e);
		}
	}

	@Result(name = "run time", description = "Total running time of this run of the experiment in seconds.")
	public double runtime()
	{
		return t/1000.0;
	}
	
	@Reportable(description = "The date and time at which the run of the experiment was started.")
	public Date startTime()
	{
		return new Date(t0);
	}
	
	@Reportable(description = "The date and time at which the run of the experiment was finished.")
	public Date finishTime()
	{
		return new Date(t1);
	}
	
	
	public void writeReport() throws IOException
	{
		// * Create data model
		Map<String, Object> results = new HashMap<String, Object>();
		
		// * Fill data model
		results.put("short_name", this.getClass().getName());
		results.put("name", this.getClass().toString());
		results.put("description", description());
		results.put("start_date_time", new Date(this.t0).toString());
		results.put("start_millis", t0);
		results.put("end_date_time", new Date(this.t).toString());
		results.put("end_millis", t);
		
		// * Run through all methods tagged 'result'
		List<Map<String, Object>> rs = new ArrayList<Map<String, Object>>();
		for(Method method : Tools.allMethods(this.getClass()))
			for(Annotation anno : method.getAnnotations())
				if(anno instanceof Result)
					processResult(rs, invoke(method), (Result) anno);
		
		logger.info("Found " + rs.size() + " results");
		results.put("results", rs);
		
		
		Template tpl = null;
		
		try
		{
			tpl = fmConfig.getTemplate("index.ftl");
		} catch (IOException e)
		{
			// * Non fatal error (results may be recoverable from the log file). Log and continue
			Global.log().warning("Failed to load report template. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
			return;
		}
		
		File reportDir = new File(dir, "report/");
		reportDir.mkdirs();

		copy("static", reportDir);
		
		
		Writer turtleOut = new BufferedWriter( new FileWriter(new File(reportDir, "output.ttl")));;
		ResultTurtleWriter writer = new ResultTurtleWriter(this, turtleOut);
		writer.writeExperiment();
		
		Writer out = null; 
		
		try
		{
			out = new BufferedWriter( new FileWriter(new File(reportDir, "index.html")));
			
			tpl.process(results, out);
			
			out.flush();			
			
		} catch (IOException e)
		{
			Global.log().warning("Failed to write to report directory. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
			return;
		} catch (TemplateException e)
		{
			Global.log().warning("Failed to process report template. Continuing without writing report. IOException: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace()));
			return;			
		}
		
		logger.info("Report written");
	}
	
	private void processResult(List<Map<String, Object>> rs, Object value, Result anno) throws IOException
	{
		// *  The method returns multiple results in a Results object
		if(value instanceof Results)
		{
			Results results = (Results) value;
			
			for(int i : Series.series(results.size()))
				processResult(rs, results.value(i), results.annotation(i));
			
			return;
		}
		
		Map<String, Object> resMap = new HashMap<String, Object>();


		if(value instanceof Reporting)
		{
			Reporting reporting = (Reporting) value;
			
			resMap.put("name", reporting.name());
			resMap.put("description", reporting.description());
			
			Template tpl = reporting.template();
			Object dataModel = reporting.data();
			
			resMap.put("value", "Failed to invoke result method. See the log file for details.");

			try
			{
				StringWriter out = new StringWriter();
				tpl.process(dataModel, out);
				out.flush();
				
				resMap.put("value", out.toString());
			} 
			catch (TemplateException e) { Global.log().warning("Failed to process result-specific template " + tpl + " " + this + ". Exception: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace())); }	
			catch (IOException e) { throw new IllegalStateException("IOException on StringWriter", e); } 
		} else if(value instanceof List<?>) // * If the result is a list of something
		{
			if(Tools.tabular((List<?>)value)) // for a list of lists
			{
				int height = ((List<?>) value).size();
				int width = Tools.tableWidth((List<?>) value);
				
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("table", value);
				map.put("id", Tools.cssSafe(anno.name()));
				map.put("width", width);
				map.put("height", height);
				
				map.put("plottype", anno.plot().toString().toLowerCase());
				
				StringWriter out = new StringWriter();
				Template tpl;
				try
				{
					tpl = fmConfig.getTemplate("table.ftl");
					tpl.process(map, out);
				} catch (Exception e)
				{
					throw new RuntimeException(e);
				}
	
				out.flush();
				
				resMap.put("value", out.toString());
				
				resMap.put("name", anno.name());
				resMap.put("description", anno.description());
				
				// * Also write to CSV
				
				File csvDir = new File(dir, "csv/");
				csvDir.mkdirs();	

				File outFile = new File(csvDir, "data." + Tools.cssSafe(anno.name()) + ".csv");
				
				writeCSV(outFile, value, width, height);
				
			}  else // for a list of values (possibly numeric)
			{
			
				StringWriter out = new StringWriter();
				List<String> valueStr = Tools.stringList((List<Object>) value);
				
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("list", valueStr);
				map.put("id", Tools.cssSafe(anno.name()));
				
				boolean isNumeric = Tools.isNumeric((List<Object>)value);
				map.put("is_numeric", isNumeric);
				
				if(isNumeric)
				{
					map.put("mean",   Tools.mean((List<? extends Number>)value));
					map.put("dev",    Tools.standardDeviation((List<? extends Number>)value));
					map.put("median", Tools.median((List<? extends Number>)value));
					map.put("mode",   Tools.mode((List<?>)value));
				}
				
				BasicFrequencyModel<Object> model = new BasicFrequencyModel<Object>((List<Object>)value);
				List<Object> tokens;
				
				if(isNumeric)
				{
					List<Number> numTokens = new ArrayList<Number>((int)model.distinct());
					for(Object t : model.tokens())
						numTokens.add(((Number) t));
					
					Collections.sort(numTokens, Functions.numberComparator());
					tokens = new ArrayList<Object>(numTokens);
				} else
					tokens = model.sorted();
				
				List<Double> frequencies = new ArrayList<Double>(tokens.size());
				for(Object token : tokens)
					frequencies.add(model.frequency(token));
				
				List<List<Object>> pairs = new ArrayList<List<Object>>(tokens.size());
				for(int i : Series.series(tokens.size()))
					pairs.add(Arrays.asList(tokens.get(i), frequencies.get(i)));
				map.put("histogram", pairs);
				
				map.put("plottype", anno.plot().toString().toLowerCase());
				
				Template tpl;
				try
				{
					tpl = fmConfig.getTemplate("list.ftl");
					tpl.process(map, out);
				} catch (Exception e)
				{
					throw new RuntimeException(e);
				}
	
				out.flush();
				
				resMap.put("value", out.toString());
				
				resMap.put("name", anno.name());
				resMap.put("description", anno.description());
			}
		} else if(value instanceof BufferedImage) 
		{
			BufferedImage image = (BufferedImage)value;
			int height = image.getHeight();
			int width = image.getWidth();
			
			File outFile = new File(new File(dir, "images/"), Tools.cssSafe(anno.name()) + ".png");
			outFile.mkdirs();
			
			ImageIO.write(image, "PNG", outFile);
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("path", outFile.getAbsolutePath());
			map.put("width", width);
			map.put("height", height);
			
			StringWriter out = new StringWriter();
			Template tpl;
			try
			{
				tpl = fmConfig.getTemplate("image.ftl");
				tpl.process(map, out);
			} catch (Exception e)
			{
				throw new RuntimeException(e);
			}

			out.flush();
			
			resMap.put("value", out.toString());
			
			resMap.put("name", anno.name());
			resMap.put("description", anno.description());				
		
		} else
		{
			resMap.put("value", value == null ? "null" : value.toString());

			resMap.put("name", anno.name());
			resMap.put("description", anno.description());
		}

		rs.add(resMap);
	}
	
	private void writeCSV(File outFile, Object value, int width, int height) 
			throws IOException
	{
		BufferedWriter outWriter = new BufferedWriter(new FileWriter(outFile));
		for(int i : Series.series(height))
		{
			if(i != 0)
				outWriter.write("\n");
			for(int j : Series.series(width))
			{
				if(j != 0)
					outWriter.write(", ");
				String str = ((List<?>)((List<?>)value).get(i)).get(j).toString();
				
				str = str.replaceAll("[\"\",]", ""); // remove quotes
				if(str.matches(".*\\s.*"))
					str = "\"" + str + "\"";
				
				outWriter.write(str);
			}
			
			if(value instanceof Classified<?>)
				outWriter.write(", " + ((Classified<?>)value).cls(i));
		}
		
		outWriter.close();
	}

	private Object invoke(Method method)
	{
		try{
			return method.invoke(this);
		}
		catch (InvocationTargetException e) { Global.log().warning("Failed to invoke result method " + method + " on experiment " + this + ". Exception: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace())); }
		catch (IllegalArgumentException e) { Global.log().warning("Failed to invoke result method " + method + " on experiment " + this + ". Exception: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace())); }
		catch (IllegalAccessException e) { Global.log().warning("Failed to invoke result method " + method + " on experiment " + this + ". Exception: " + e.getMessage() + " -  " + Arrays.toString(e.getStackTrace())); } 
		
		return null;
	}
	
	public String description() 
	{
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description; 
	}
	
	/**
	 * A collection of python scripts to execute after the experiment is complete
	 * 
	 * Each string represents a url to the script in the classpath.
	 * 
	 * @return
	 */
	public List<String> scripts() 
	{
		return Collections.emptyList();
	}
	
	
	public void runScripts() throws IOException, InterruptedException
	{
		File target = new File(dir, "python/");
		target.mkdirs();
		
		// * For each script:
		for(String script : scripts())
		{
			// * ... copy the script into the directory ./python/
			copy("scripts/" + script, target); 
			
			// * Change directory
			Runtime runtime = Runtime.getRuntime();
			
			// * ... run the script in python/
			// TODO: Make this platform independent (sort of)
			Process p = runtime.exec(
					"arch -i386 /usr/bin/python " + script, 
					new String[]{
							"PATH=/Library/Frameworks/Python.framework/Versions/2.7/bin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/usr/X11/bin:/usr/local/git/bin:/usr/texbin",
							"PYTHONPATH=/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/site-packages"},
					target);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			BufferedReader ebr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(target, script+".log")));			
			BufferedWriter ebw = new BufferedWriter(new FileWriter(new File(target, script+".err.log")));
			
			System.out.println(p.waitFor());
			
			String line;
			while ( (line = br.readLine()) != null) 
				bw.write(line + "\n");
			while ( (line = ebr.readLine()) != null) 
				ebw.write(line + "\n");
			
			
			bw.close();
			ebw.close();
		}
	}	

	/**
	 * Copies all files and directories in the given classpath directory to 
	 * the given target directory in the filesystem.
	 * 
	 * @param cpDir
	 * @param target
	 */
	public void copy(String cpDir, File target)
	{
		URL sourcePath = this.getClass().getClassLoader().getResource(cpDir);
		logger.info("Copying static files from path " + sourcePath);
		
		//* Copy static files (css, js, etc)
		try
		{
			copyResources(sourcePath, target);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		logger.info("Finished copying");				
	}
	
	public void copyResources(URL originUrl, File destination) 
			throws IOException 
	{
		System.out.println(originUrl);
	    URLConnection urlConnection = originUrl.openConnection();
	    
	    File file = new File(originUrl.getPath());
	    if (file.exists()) 
	    {	
	    	if(file.isDirectory())
	    		FileUtils.copyDirectory(new File(originUrl.getPath()), destination);
	    	else
	    		FileUtils.copyFile(file, new File(destination, file.getName()));
	    } else if (urlConnection instanceof JarURLConnection) 
	    {
	        copyJarResourcesRecursively(destination, (JarURLConnection) urlConnection);
	    } else {
	        throw new RuntimeException("URLConnection[" + urlConnection.getClass().getSimpleName() +
	                "] is not a recognized/implemented connection type.");
	    }
	}

	public void copyJarResourcesRecursively(File destination, JarURLConnection jarConnection ) 
			throws IOException 
	{
	    JarFile jarFile = jarConnection.getJarFile();
	    
	    Enumeration<JarEntry> entries = jarFile.entries();
	    
	    while(entries.hasMoreElements()) {
	    	JarEntry entry = entries.nextElement();
	    	
	        if (entry.getName().startsWith(jarConnection.getEntryName())) 
	        {
	            String fileName = removeStart(entry.getName(), jarConnection.getEntryName());
	            if (! entry.isDirectory())
	            {
	                InputStream entryInputStream = null;
	                entryInputStream = jarFile.getInputStream(entry);
					copyStream(entryInputStream, new File(destination, fileName));
	               
	            } else
	            {
	                new File(destination, fileName).mkdirs();
	            }
	        }
	    }
	}

	private void copyStream(InputStream in, File file) 
			throws IOException
	{
		OutputStream out = new FileOutputStream(file);
		int bt = in.read();
		while(bt != -1)
		{
			out.write(bt);
			bt = in.read();
		}
		out.flush();
		out.close();
	}

	private String removeStart(String string, String prefix)
	{
		if(string.indexOf(prefix) != 0)
			return null;
		
		return string.substring(prefix.length());
	}


}
