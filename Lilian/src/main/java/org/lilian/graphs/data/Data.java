package org.lilian.graphs.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import org.lilian.Global;
import org.lilian.graphs.DGraph;
import org.lilian.graphs.DNode;
import org.lilian.graphs.DTGraph;
import org.lilian.graphs.DTNode;
import org.lilian.graphs.Graph;
import org.lilian.graphs.LightDGraph;
import org.lilian.graphs.MapDTGraph;
import org.lilian.graphs.MapUTGraph;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.UTNode;

public class Data {

	
	/**
	 * Reads a file in edge-list representation into a string-labeled undirected 
	 * graph
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static UTGraph<String, String> edgeList(File file)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		UTGraph<String, String> graph = new MapUTGraph<String, String>();
				
		String line;
		int i = 0;
		
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
	
	/**
	 * Reads a file in edge-list representation into a string-labeled directed 
	 * graph
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static DTGraph<String, String> edgeListDirected(File file)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		DTGraph<String, String> graph = new MapDTGraph<String, String>();
				
		String line;
		int i = 0;
		
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
						
			DTNode<String, String> nodeA = graph.node(a);
			if(nodeA == null)
				nodeA = graph.add(a);
			
			DTNode<String, String> nodeB = graph.node(b);
			if(nodeB == null)
				nodeB = graph.add(b);
			
			nodeA.connect(nodeB);
			
		} while(line != null);
		
		System.out.println("\nFinished. Read " + graph.numLinks() + "edges");
		return graph;
	}		
	
	/**
	 * Reads a file in edge-list representation into a string-labeled directed 
	 * graph
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static DGraph<String> edgeListDirectedUnlabeled(File file, boolean clean)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		LightDGraph<String> graph = new LightDGraph<String>();
				
		String line;
		int i = 0;
		
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
			
			Integer a, b, c = null;
			try {
				a = Integer.parseInt(split[0]);
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The first element on line "+i+" ("+split[0]+") cannot be parsed into an integer.", e);
			}
			
			try {
				b = Integer.parseInt(split[1]);
			} catch(NumberFormatException e)
			{
				throw new RuntimeException("The second element on line "+i+" ("+split[1]+") cannot be parsed into an integer.", e);
			}
						
			ensure(graph, Math.max(a, b));
			
			DNode<String> nodeA = graph.nodes().get(a);
			if(nodeA == null)
				nodeA = graph.nodes().get(a);
			
			DNode<String> nodeB = graph.nodes().get(b);
			if(nodeB == null)
				nodeB = graph.nodes().get(b);
			
			nodeA.connect(nodeB);
			
			int links = graph.numLinks();
			if(links%100000 == 0)
				Global.log().info("Loaded " + links + " links (n="+graph.size()+", l="+graph.numLinks()+")");
			
			
		} while(line != null);
		
		Global.log().info("Sorting");
		graph.sort();
		
		if(clean)
		{
			Global.log().info("Compacting");
			graph.compact(0);
			Global.log().info("Done");

		}
		
		return graph;
	}

	private static void ensure(Graph<String> graph, int max)
	{
		while(graph.size() < max + 1)
			graph.add(null);
	}		
}
