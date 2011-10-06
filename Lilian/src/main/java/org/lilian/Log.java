package org.lilian;


import java.io.*;

/**
 * A very simple logging system.
 * 
 *  
 *  To use, set the log to a stream with setLog(), and log strings 
 *  to that stream with log() or logln(). Of no stream is set, the
 *  logging commands are ignored.
 *  
 * @author Peter Bloem
 *
 */
public class Log
{
	private static PrintStream stream = null;
	
	public static void setStream(PrintStream p)
	{
		stream = p;
	}
	
	public static void log(Object in)
	{
		if(stream != null)
			stream.print(in);
	}
	
	public static void logln(Object in)
	{
		if(stream != null) 
			stream.println(in);
	}
	
	public static void logln()
	{
		if(stream != null) 
			stream.println();
	}		
}
