package org.lilian.experiment;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
	
	public static List<Experiment>
	
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
		HashMap<?, ?> map = (LinkedHashMap<?, ?>)initObject;

		for(Object key : map.keySet())
		{
			Object value = map.get(key);
			System.out.println(key.getClass() + " : "+ value.getClass());
		}
	}

}
