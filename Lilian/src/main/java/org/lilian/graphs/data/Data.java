package org.lilian.graphs.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import org.lilian.graphs.MapUTGraph;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.UTNode;

public class Data {

	
	public static UTGraph<String, String> readString(File file)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		UTGraph<String, String> graph = new MapUTGraph<String, String>();
				
		String line;
		int i = 0;
		int edges = 0;
		
		do
		{
			line = reader.readLine();
			i++;
			
			if(line == null)
				continue;
			if(line.trim().isEmpty())
				continue;
			if(line.trim().startsWith("#"))
				continue;
			
			String[] split = line.split("\\s");
			if(split.length < 2)
				throw new IllegalArgumentException("Line "+i+" does not split into two elements.");
			
			String a, b, c = null;
			try {
				a = split[0];
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The first element on line "+i+" ("+split[0]+") cannot be parsed into an integer.", e);
			}
			
			try {
				b = split[1];
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The second element on line "+i+" ("+split[1]+") cannot be parsed into an integer.", e);
			}

			if(split.length > 2)
				try {
					c = split[2];
				} catch(NumberFormatException e)
				{
					throw new RuntimeException("The third element on line "+i+" ("+split[1]+") cannot be parsed into an integer.", e);
				}				
						
			UTNode<String, String> nodeA = graph.node(a);
			if(nodeA == null)
				nodeA = graph.add(a);
			
			UTNode<String, String> nodeB = graph.node(b);
			if(nodeB == null)
				nodeB = graph.add(b);
			
			nodeA.connect(nodeB);
			
		} while(line != null);
		
		System.out.println("\nFinished. Read " + graph.numLinks() + "edges");
		return graph;
	}	
	
}
