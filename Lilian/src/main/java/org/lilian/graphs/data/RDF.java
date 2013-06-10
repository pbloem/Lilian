package org.lilian.graphs.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.lilian.graphs.DTNode;
import org.lilian.graphs.MapDTGraph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class RDF
{
	/**
	 * Reads the given file into a graph.
	 * 
	 * @param file
	 * @return
	 */
	public static MapDTGraph<String, String> read(File file)
	{
		
		RDFDataSet testSet = new RDFFileDataSet(file, RDFFormat.RDFXML);

		List<Statement> triples = testSet.getFullGraph();	
		
		return createDirectedGraph(triples, null, null);
	}
	
	public static MapDTGraph<String, String> readTurtle(File file)
	{
		
		RDFDataSet testSet = new RDFFileDataSet(file, RDFFormat.TURTLE);

		List<Statement> triples = testSet.getFullGraph();	
		
		return createDirectedGraph(triples, null, null);
	}
	
	public static MapDTGraph<String, String> createDirectedGraph(
			List<Statement> sesameGraph, 
			List<String> vWhiteList,
			List<String> eWhiteList)
	{
		List<Pattern> vertexWhiteList = null;
		
		if(vWhiteList != null) 
		{
			vertexWhiteList = new ArrayList<Pattern>(vWhiteList.size());
			for(String patternString : vWhiteList)
				vertexWhiteList.add(Pattern.compile(patternString));
		}
		
		
		List<Pattern> edgeWhiteList = null;
		if(eWhiteList != null)
		{
			edgeWhiteList = new ArrayList<Pattern>(eWhiteList.size());
			for(String patternString : eWhiteList)
				edgeWhiteList.add(Pattern.compile(patternString));
		}
		
		MapDTGraph<String, String> graph = new MapDTGraph<String, String>();
		DTNode<String, String> node1, node2;
		
		for (Statement statement : sesameGraph) 
		{
			if(vWhiteList != null)
			{
				if(! matches(statement.getObject().toString(), vertexWhiteList))
					continue;
				if(! matches(statement.getSubject().toString(), vertexWhiteList))
					continue;
			}
			
			if(eWhiteList != null)
				if(! matches(statement.getPredicate().toString(), edgeWhiteList))
					continue;
			
			String subject = statement.getSubject().toString(), 
			       object = statement.getObject().toString(), 
			       predicate = statement.getPredicate().toString();
			
			node1 = graph.node(subject);
			node2 = graph.node(object);
		
			if (node1 == null) 
				node1 = graph.add(subject);
	
			
			if (node2 == null) 
				node2 = graph.add(object);
							
			node1.connect(node2, predicate);
		}	
		
		return graph;
	}
	
	/** TODO move this to a proper Utility class/package
	 * Returns true if the String matches one or more of the patterns in the list.
	 * @param string
	 * @param patterns
	 * @return
	 */
	public static boolean matches(String string, List<Pattern> patterns)
	{
		for(Pattern pattern : patterns)
			if(pattern.matcher(string).matches())
				return true;
		return false;
	}
}
