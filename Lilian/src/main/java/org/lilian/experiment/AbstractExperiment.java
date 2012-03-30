package org.lilian.experiment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AbstractExperiment implements Experiment
{
	private static final String STATE_FILE = "state.lilian";
	
	private long t0;
	private long t;
	
	protected File dir;
	protected PrintStream out;

	public final void run()
	{
		dir = Environment.current().directory();
		out = Environment.current().out();
		
		t0 = System.currentTimeMillis();
		Environment.current().out().println("Starting run for experiment of type" + this.getClass());
		
		if(new File(dir, STATE_FILE).exists())
		{
			out.println("Found statefile. Attempting to restart experiment.");
			load();
		} else {
			out.println("No statefile found. Starting experiment from scratch.");
			setup();
		}		
		body();
		
		t = System.currentTimeMillis() - t0;
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
		@SuppressWarnings("unchecked")
		Class<Experiment> c = (Class<Experiment>)this.getClass();
		out.println(c);
		
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
				out.println(annotation);
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
			@SuppressWarnings("unchecked")
			Class<Experiment> c = (Class<Experiment>)this.getClass();
			
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
			@SuppressWarnings("unchecked")
			Class<Experiment> c = (Class<Experiment>)this.getClass();
			
			for(Field field : stateFields())
			{
				out.println("Reading object for field " + field);
				Object value = in.readObject();
				out.println(" * " + value.getClass());
				out.println(" - " + value);
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

	@Result(name = "Total running time of a run of the experiment in seconds.")
	public double runtime()
	{
		return 0;
	}
	
	@Reportable(description = "The date and time at which the run of the experiment was started.")
	public Date startTime()
	{
		return null;
	}
	
	@Reportable(description = "The date and time at which the run of the experiment was finished.")
	public Date finishTime()
	{
		return null;
	}
}
