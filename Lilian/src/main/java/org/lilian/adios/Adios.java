package org.lilian.adios;

import java.io.*;
import java.util.*;

import org.lilian.*;
import org.lilian.adios.MexGraph.Motif;
import org.lilian.adios.MexGraph.Path;
import org.lilian.adios.MexGraph.PositionList;
import org.lilian.adios.MexGraph.Token;
import org.lilian.corpora.*;
import org.lilian.util.*;


/**
 * This class specifies several methods to modify a MexGraph 
 * that together form the ADIOS algorithm.
 * 
 * TODO:
 *  - Give constructGenPath an extra parameter n that tells it to 
 *    disregard paths that differ in n or more positions from the context
 *    window. 
 *
 * @param <T>
 */

public class Adios<T> extends MexGraph<T>
{
	protected int minPathLength = 1;
	
	public Adios(SequenceCorpus<T> corpus)
		throws IOException
	{
		super(corpus);
	}
	
	/**
	 * Iterates once over all paths in the graph
	 * 
	 *  @return Whether any new motifs were found
	 */
	public boolean patternDistillation(
			double dropThreshold, double significanceThreshold, 
			boolean contextSensitive)
	{
		boolean motifFound = false;
		
		for(Path path : paths)
		{
			Motif motif = pathRunDistillation(
					path, dropThreshold, 
					significanceThreshold, contextSensitive);
			System.out.print(":");
			if(motif != null)
			{
				motifFound = true;
				super.addSerialToken(
						motif, contextSensitive, 
						significanceThreshold, dropThreshold);
			}
			System.out.print("_");
		}
			
		return motifFound;
	}
	
	/**
	 * Slides a context window over all paths, determining equivalence classes 
	 */
	public boolean generalization(	double dropThreshold, 
										double significanceThreshold, 
										boolean contextSensitive,
										int windowLength)
	{
		Iterator<Path> pathsIt = paths.iterator();
		Path path;
		boolean motifFound = false;
		Motif motif;
		// forall paths...
		while(pathsIt.hasNext())
		{
			path = pathsIt.next();
			motif = pathRun(path, dropThreshold, significanceThreshold, contextSensitive, windowLength);
			if(motif != null) //if the path has a leading motif
			{
				addSerialToken(		motif, 
									contextSensitive, 
									dropThreshold, 
									significanceThreshold);
				motifFound = true;
			}
		}
		
		return motifFound;
	}
	
	/**
	 * 
	 * 
	 * @param dropThreshold
	 * @param significanceThreshold
	 * @param overlapThreshold
	 * @param contextSensitive
	 * @param windowLength Length of the context window. Make this > 3
	 * @return True if new patterns were found, false otherwise 
	 */
	public boolean generalizationBootstrap(	double dropThreshold, 
											double significanceThreshold,
											double overlapThreshold,
											boolean contextSensitive,
											int windowLength)
	{
		runComplete = true;
		
		boolean motifFound = false;
		Motif motif;
		Iterator<Path> pathsIt = paths.iterator();
		Path path;
		while(pathsIt.hasNext())
		{
			path = pathsIt.next();
			
			motif = pathRunBootstrap(path, dropThreshold, significanceThreshold, 
							overlapThreshold, contextSensitive, windowLength);
			
			if(motif != null) //if the path has a leading motif
			{
				Log.logln("  ! leading motif for this path:"  + motif);
				addSerialToken(		motif, 
									contextSensitive, 
									dropThreshold, 
									significanceThreshold);
				motifFound = true;
			}
		}

		return motifFound;
	}
	
	/**
	 * Performs bootstrapped generalization on all paths iteratively until
	 * no new motifs are found for a certain number of paths.
	 * 
	 * @param dropThreshold
	 * @param significanceThreshold
	 * @param overlapThreshold
	 * @param contextSensitive
	 * @param windowLength Length of the context window. Make this > 3
	 * @param stop The method stops if no new patterns have been found
	 *             for this many paths
	 * @return True if new patterns were found, false otherwise 
	 */
	public void generalizationBootstrap(double dropThreshold, 
			double significanceThreshold,
			double overlapThreshold,
			boolean contextSensitive,
			int windowLength,
			int stop)
	{
		Iterator<Path> pathsIt;
		Path path;
		int noMotifs = 0;
		Motif motif;
		
		while(noMotifs < stop)
		{
			pathsIt = paths.iterator();
			while(pathsIt.hasNext())
			{
				System.out.print(".");
				
				path = pathsIt.next();
				motif = pathRunBootstrap(path, dropThreshold, significanceThreshold, 
						overlapThreshold, contextSensitive, windowLength);
		
				if(motif != null) //if the path has a leading motif
				{
					Log.logln("  ! leading motif for this path:"  + motif);
					addSerialToken(		motif, 
							contextSensitive, 
							dropThreshold, 
							significanceThreshold);
					noMotifs = 0;
				} else
				{
					noMotifs++;
				}				
			}
			
			System.out.println();
		}
	}


	public Motif pathRunDistillation(
			List<Token> path, 
			double dropThreshold, 
			double significanceThreshold, 
			boolean contextSensitive)
	{
		Motif motif = null;
		
		// the copy of the path with Positions instead of Tokens
		List<Position> genPath = new Vector<Position>(path.size());
		for(Token t : path)
			genPath.add(new RegularPosition(t));
		
		if(path.size() > minPathLength)
		{
			System.out.print("(");
			motif = getLeadingMotif(genPath, dropThreshold, significanceThreshold);
			System.out.print(")");
		}

		return motif;
	}

		
	/**
	 * @param path
	 * @param dropThreshold
	 * @param significanceThreshold
	 * @param contextSensitive
	 * @param windowLength
	 * @return True if a leading path was found, false otherwise.
	 *
	 * NB: This code needs some thorough testing
	 */
	public Motif pathRun(	List<Token> path, 
							double dropThreshold, 
							double significanceThreshold, 
							boolean contextSensitive,
							int windowLength)
	{
		// the generalized search path
		List<Position> genPath;
		Motif currentMotif, leadingMotif = null;
		int start, end, slot;
		boolean motifFound = false;

		//fill the genPath with the start of the path
		genPath = new Vector<Position>(path.size());
		Position pos, prev, next;
		
		Iterator<Token> it = path.iterator();
		genPath.add(null);
		while(it.hasNext());
		{
			genPath.add(new RegularPosition(it.next()));
		}

		// forall context windows in this path			
		for(start = 0;start < path.size() - windowLength; start++)
		{

			// forall possible slots in the window
			for(slot = start; slot < windowLength; slot++)
			{
				if(slot > 0) 	prev = genPath.get(slot-1);
				else			prev = null;
				if(slot < windowLength-1) 	next = genPath.get(slot+1);
				else						next = null;
				
				pos = genPath.set(slot, new SlotPosition(prev, next));
				// find the tokens that occur at the slot
				setSlotTokens(genPath.subList(start, start + windowLength));
				
				currentMotif = getLeadingMotif(	genPath, 
												dropThreshold, 
												significanceThreshold);
				if(currentMotif != null)
				{
					if(leadingMotif == null || 
						currentMotif.compareTo(leadingMotif) < 0)
					{
						leadingMotif = currentMotif;
					}
				}
				
				// return the generalized path to it's original state
				
				genPath.set(slot, pos);
			}								
		}
	
		return leadingMotif;
	}
	
	/**
	 * @param path
	 * @param dropThreshold
	 * @param significanceThreshold
	 * @param contextSensitive
	 * @param windowLength
	 * @return True if a leading path was found, false otherwise.
	 */
	public Motif pathRunBootstrap(	List<Token> path, 
										double dropThreshold, 
										double significanceThreshold,
										double overlapThreshold,
										boolean contextSensitive,
										int windowLength)
	{
		
		Log.logln("  * Starting path :" + path); 		
		
		// the copy of the path, with Positions instead of Tokens
		List<Position> genPath = new Vector<Position>(path.size());
		Iterator<Token> it = path.iterator();
		while(it.hasNext())
			genPath.add(new RegularPosition(it.next()));
		
		Set<Token> slotTokens;
		Motif currentMotif, leadingMotif = null;
		Position prev, next, pos;
		int start, end, slot;
		boolean motifFound = false;

		// forall context windows in this path			
		for(start = 0;start < path.size() - windowLength; start++)
		{
			Log.logln("  _ Starting window " + start + ", length " + windowLength);
			// find equivalence sets for all positions in the window.			
			constructGenPath(genPath, start, windowLength, overlapThreshold);

			// forall possible slots in the window
			for(slot = 0; slot < windowLength; slot++)
			{
				Log.logln("    slot " + slot);
				
				if(slot + start > 0) 	prev = genPath.get((slot + start) - 1);
				else			prev = null;
				if(slot + start < genPath.size()) 	next = genPath.get((slot + start) + 1);
				else						next = null;
				
				// create a slot in the search path	
				pos = genPath.set(start + slot, new SlotPosition(prev, next));
				// find the tokens that occur at the slot
				setSlotTokens(genPath.subList(start, start + windowLength));
				
				// perform MEX and find the best Motif
				currentMotif = getLeadingMotif(	genPath, 
												dropThreshold, 
												significanceThreshold);

				Log.logln(genPath);
				Log.logln();
				Log.logln(Functions.matrixToString(n));
				Log.logln();				
				Log.logln(Functions.matrixToString(m));
				Log.logln();				

				if(currentMotif != null)
				{
					if(leadingMotif == null || 
						currentMotif.compareTo(leadingMotif) < 0)
					{
						leadingMotif = currentMotif;
						Log.logln("  ? current leading motif for this path:"  + leadingMotif);						
					}
				}
				
				// return the generalized path to its original state
				genPath.set(start + slot, pos);
			}
			
			// return the genPath to its original state (copying the original path)
			for(int i = start; i < start + windowLength; i++)
			{
				genPath.set(i, new RegularPosition(path.get(i)));
			}
		}
		
		return leadingMotif;
	}	

	/**
	 * Create a position object for this set of tokens. This method first finds the
	 * ParallelToken that has the greatest overlap with this one, if it's bigger
	 * than overlapThreshold, an EquivPosition is created for it. If not,
	 * it creates a RegularPosition that represents token.
	 * 
	 * @return The Position that best matches this set. If none is found for 
	 *         which the overlap >= the overlap threshold, null is returned. 
	 */
	protected Position findMatch(Set<Token> set, Token token, double overlapThreshold)
	{
		if(set.size() < 2)
			return new RegularPosition(token);
		
		Iterator<ParallelToken> it = ptokens.iterator();
		double 	leadingOverlap = -1.0, 
				currentOverlap;
		
		ParallelToken current, leading = null;
		
		Position result;
		
		while(it.hasNext()){
			current = it.next();
			currentOverlap = current.overlap(set);
			if(currentOverlap > leadingOverlap && current.getMembers().contains(token))
			{
				leadingOverlap = currentOverlap;
				leading = current;
			}
		}
		
		if(leadingOverlap >= overlapThreshold)
			result = new EquivPosition(leading, leadingOverlap, set);
		else
			result = new RegularPosition(token);
		
		Log.logln("parallel tokens:" + ptokens);
		Log.logln("matching:" + set + " -> " + result);
			
		return result;		
	}
	
	/**
	 * Adds to the SlotPositions in context, all Tokens that occur there
	 * if the rest of the context is kept the same
	 */
	protected void setSlotTokens(List<Position> context)
	{	
		// starting nodes for all paths to check
		Iterator<Node> pathsIt = context.get(0).nodes(true).iterator();
		Iterator<Position> controlIt, controlIt2;
		
		Node startNode, currentNode, currentNode2;
		Position controlPosition;
		
		while(pathsIt.hasNext())
		{
			startNode = pathsIt.next();
			currentNode = startNode;
			
			controlIt = context.iterator();
			controlPosition = controlIt.next();
			while(true)
			{
				//the path deviates from the control
				if(! controlPosition.matches(currentNode.getToken()))
					break;
				
				// if we've reached the end of the context,
				// the path fits
				if(! controlIt.hasNext())
				{
					//traverse the path again, adding Tokens to slots
					currentNode2 = startNode;
					controlIt2 = context.iterator();
					while(controlIt2.hasNext())
					{
						controlIt2.next().addToken(currentNode2.getToken());
						currentNode2 = currentNode2.getNextNode();
					}
					break;
				}					
				
				//the path ends prematurely
				if(! currentNode.hasNextNode())
					break;
				
				controlPosition = controlIt.next();
				currentNode = currentNode.getNextNode();
			}
		}
	}
	
	/**
	 * Constructs a generalized search path from this window according
	 * to the method for bottstrapped generalization.
	 * 
	 */
	protected void constructGenPath(
			List<Position> path,
			int windowStart,
			int windowLength,
			double overlapThreshold)
	{
		
		// construct a vector with a set of tokens for each position in 
		// the window
		Vector<Set<Token>> temp = new Vector<Set<Token>>(windowLength);
		for(int i = 0; i < windowLength; i++)
			temp.add(new LinkedHashSet<Token>());
		
		// we consider all (sub)paths with the same length as window that
		// start in a token from startTokens and end in a token from
		// endTokens
		
		// all nodes occuring in EC's where window[0] also occurs
		Set<Token> startTokens = new LinkedHashSet<Token>();
		Token startToken = null;
		try{
			startToken = ((RegularPosition)path.get(windowStart)).getToken();
		}catch(ClassCastException e){
			throw new RuntimeException("Non-regular position (slot or EC) found in the generalized search path.");
		}
		startTokens.add(startToken);
		
		Set<ParallelToken> set;
		if(pTokenMap.containsKey(startToken))
				set = pTokenMap.get(startToken);
		else
				set = Collections.emptySet();
		
		Iterator<ParallelToken> it = set.iterator();
		ParallelToken ptoken;
		while(it.hasNext())
		{
			ptoken = it.next();
			startTokens.addAll(ptoken.getMembers());			
		}
		
		// all nodes occuring in EC's where window[n] also occurs
		Set<Token> endTokens = new LinkedHashSet<Token>();
		Token endToken = null;
		try{
			endToken = ((RegularPosition)path.get(windowStart + windowLength - 1)).getToken();
		}catch(ClassCastException e){
			throw new RuntimeException("Non-regular position (slot or EC) found in the generalized search path.");
		}
		endTokens.add(endToken);
		
		if(pTokenMap.containsKey(endToken))
				set = pTokenMap.get(endToken);
		else
				set = Collections.emptySet();
		
		it = set.iterator();
		while(it.hasNext())
		{
			ptoken = it.next();
			endTokens.addAll(ptoken.getMembers());			
		}

		// the starting nodes of all paths we'll consider
		Set<Node> startNodes = new LinkedHashSet<Node>();
		Iterator<Token> tokenIt = startTokens.iterator();
		while(tokenIt.hasNext())
			startNodes.addAll(tokenIt.next().getNodes());
		
		// the first nodes of all the paths we'll consider
		Iterator<Node> pathIt = startNodes.iterator();
		Iterator<Position> controlIt;
		Node firstNode, currentNode, currentNode2;
		Token controlToken;
		
		int i, i2;
		// the number of nodes the path deviates from the master
		int nodesOff;
		
		while(pathIt.hasNext())
		{
			
			firstNode = pathIt.next();
			currentNode = firstNode;
			
			controlIt = path.listIterator(windowStart);
			controlToken = controlIt.next().getToken();		
			
			i = 0;
			
			nodesOff = 0;
			
			// follow the path until i == window.length, then check
			// if the path ends in the right token
			while(true)
			{
				if(! currentNode.getToken().equals(controlToken))
					nodesOff++;
				
				// if the path deviates in more than 1 position, 
				// we're not interested
				if(nodesOff > 1)
					break;
					
				// break if the path ends prematurely 
				if(currentNode.getNextNode() == null)
					break;
				
				currentNode = currentNode.getNextNode();
				i++;
				
				if(i == windowLength - 1)
				{
					// success, follow the path again, and add all tokens to the
					// appropriate sets in temp.
					if(endTokens.contains(currentNode.getToken()))
					{
						currentNode2 = firstNode;
						i2 = 0;
						while(i2 < windowLength)
						{
							temp.get(i2).add(currentNode2.getToken());
							currentNode2 = currentNode2.getNextNode();
							i2++;							
						}
					} else
					{						
						break;
					}
				}				
			}		
		}
		
		Iterator<Set<Token>> setIt = temp.iterator();
		Position newPos;
		Set<Token> tokenSet;
		for(int j = windowStart; setIt.hasNext(); j++)
		{
			tokenSet = setIt.next();
			newPos = null;
			
			// find the best matching parallel token for this set
			try{
				newPos = findMatch(tokenSet, ((RegularPosition)path.get(j)).getToken(), overlapThreshold);				
			}catch(ClassCastException e){
				throw new RuntimeException("Non-regular position (slot or EC) found in the generalized search path.");
			}
			
			// add it to the path at the appropriate place 
			path.set(j, newPos);			
		}
	}
	
	public void writeResults(File directory, String base)
	throws IOException
	{
		super.writeResults(directory, base);
	}
}