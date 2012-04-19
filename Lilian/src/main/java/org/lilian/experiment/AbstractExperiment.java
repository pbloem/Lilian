package org.lilian.experiment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.lilian.Global;
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
		logger.info("Starting run for experiment of type" + this.getClass());
		
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
		
		writeReport();
	}
	
	/**
	 * Set up the experiment if it is started for the first time. If the 
	 * experiment is restarted, this method is skipped.
	 */
	protected abstract void setup();
	
	protected abstract void body();

	private List<Field> stateFields()
	{
		// * Save all the fields tagged with @State
		// @SuppressWarnings("unchecked")
		Class<? extends Experiment> c = (Class<? extends Experiment>)this.getClass();
		logger.info(c + "");
		
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
				logger.info(annotation + "");
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
			
			System.out.println(stateFields());
			
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
	
	public void writeReport()
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
	}
	
	private void processResult(List<Map<String, Object>> rs, Object value, Result anno)
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
		} else
		{
			resMap.put("value", value.toString());

			resMap.put("name", anno.name());
			resMap.put("description", anno.description());
		}

		rs.add(resMap);
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
}
