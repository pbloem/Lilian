package org.lilian.adios;

import java.io.*;
import java.util.*;

import org.lilian.Log;
import org.lilian.adios.MexGraph.*;
import org.lilian.corpora.*;


/**
 * A modified version of Adios that can handle corpora with very long sentences
 * efficiently. This is particularly useful for corpora with no sentence markers 
 * (ie. the whole corpus is one long sentence).
 * 
 * The corpus defines a sentence window of a fixed length l, which it slides along 
 * the corpus with a fixed amount of tokens per step. After a fixed amount of sentence 
 * windows, the best pattern in the batch is used and wired into the graph.
 * 
 * @param <T>
 */
public class AdiosLS<T> extends Adios<T>
{
	protected int minPathLength = 1;
	
	protected int defaultSentenceLength = 16;
	protected int defaultStep = 8;
	protected int defaultBatch = 3;
	
	public AdiosLS(SequenceCorpus<T> corpus)
	throws IOException
	{
		super(corpus);
	}
	
	/**
	 * Creates an AdiosLS model with specific default parameters.
	 * If methods like generalization are called without these 
	 * parameters specified, these default values are used. 
	 * 
	 * @param corpus
	 * @param defaultSentenceLength
	 * @param defaultStep
	 * @param defaultBatch
	 * @throws IOException
	 */
	public AdiosLS(	SequenceCorpus<T> corpus, 
			int defaultSentenceLength,
			int defaultStep,
			int defaultBatch)
	throws IOException
	{
		super(corpus);
		this.defaultSentenceLength = defaultSentenceLength;
		this.defaultStep = defaultStep;
		this.defaultBatch = defaultBatch;
	}	
	
	/**
	 * Iterates once over all paths in the graph
	 * 
	 * @param sentenceLength The length of the sentence window
	 * @param step By how many tokens the sentence window should be shifted 
	 * @param batch How many sentence windows should be considered to
	 *		generate one pattern
	 * @return Whether any new motifs were found
	 */
	public boolean patternDistillation(
			double dropThreshold, 
			double significanceThreshold, 
			boolean contextSensitive,
			int sentenceLength,
			int step,
			int batch)
	{
		Iterator<Path> it = paths.iterator();
		Path path, subPath;
		boolean motifFound = false;
		Motif currentMotif, leadingMotif = null;
		int sentences = 0;
		while(it.hasNext())
		{
			path = it.next();
			for(int start= 0; start < path.size() - sentenceLength; start += step)
			{
				currentMotif = pathRunDistillation(path.subList(start, start + sentenceLength), dropThreshold, significanceThreshold, contextSensitive);
				
				sentences ++;
				
				if(currentMotif != null)
					if(leadingMotif == null || leadingMotif.compareTo(currentMotif) < 0)
					{
						leadingMotif = currentMotif;					
					}
								
				// if we've analyzed 'batch' sentences, add the best motif to the graph.
				if(sentences == batch)
				{
					sentences = 0;
					if(leadingMotif != null)
					{
						this.addSerialToken(
								leadingMotif, 
								contextSensitive, 
								dropThreshold, 
								significanceThreshold);
						motifFound = true;
						System.out.print("!" + leadingMotif);
					}else
						System.out.print("?");
					
					leadingMotif = null;
				}
				System.out.print("=");
			}
		}
			
		return motifFound;
	}
	
	/**
	 * Slides a context window over all paths, determining equivalence classes 
	 */
	public boolean generalization(	double dropThreshold, 
									double significanceThreshold, 
									boolean contextSensitive,
									int windowLength,
									int sentenceLength,
									int step,
									int batch)
	{
		Iterator<Path> it = paths.iterator();
		Path path, subPath;
		boolean motifFound = false;
		Motif currentMotif, leadingMotif = null;
		int sentences = 0;
		while(it.hasNext())
		{
			path = it.next();
			for(int start= 0; start < path.size() - sentenceLength; start += step)
			{
				currentMotif = this.pathRun(path.subList(start, start + sentenceLength), dropThreshold, significanceThreshold, contextSensitive, windowLength);
				sentences ++;
				
				if(currentMotif != null)
					if(leadingMotif == null || leadingMotif.compareTo(currentMotif) < 0)
					{
						leadingMotif = currentMotif;					
					}
				
				// if we've analyzed 'batch' sentences, add the best motif to the graph.
				if(sentences == batch)
				{
					sentences = 0;
					if(leadingMotif != null)
					{
						this.addSerialToken(
								leadingMotif, 
								contextSensitive, 
								dropThreshold, 
								significanceThreshold);
						motifFound = true;
					}
					leadingMotif = null;
				}
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
											int windowLength,
											int sentenceLength,
											int step,
											int batch)
	{
		Iterator<Path> it = paths.iterator();
		Path path, subPath;
		boolean motifFound = false;
		Motif currentMotif, leadingMotif = null;
		int sentences = 0;
		while(it.hasNext())
		{
			path = it.next();
			for(int start= 0; start < path.size() - sentenceLength; start += step)
			{
				currentMotif = this.pathRunBootstrap(path.subList(start, start + sentenceLength), dropThreshold, significanceThreshold, overlapThreshold, contextSensitive, windowLength);
				sentences ++;
				
				if(currentMotif != null)
					if(leadingMotif == null || leadingMotif.compareTo(currentMotif) < 0)
					{
						leadingMotif = currentMotif;					
					}
				
				// if we've analyzed 'batch' sentences, add the best motif to the graph.
				if(sentences == batch)
				{
					sentences = 0;
					if(leadingMotif != null)
					{
						this.addSerialToken(
								leadingMotif, 
								contextSensitive, 
								dropThreshold, 
								significanceThreshold);
						motifFound = true;
					}
					leadingMotif = null;
				}
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
	public void generalizationBootstrap(
			double dropThreshold, 
			double significanceThreshold,
			double overlapThreshold,
			boolean contextSensitive,
			int windowLength,
			int stop,
			int sentenceLength,
			int step,
			int batch)
	{
		System.out.println("n = " + dropThreshold + ", a = " + significanceThreshold  + ", w = " + overlapThreshold + ", cs = " + contextSensitive  + ", L = " + windowLength  + ", stop = " + stop  + ", sL = " +  sentenceLength  + ", step = " +  step  + ", batch = " + batch);
			
		Iterator<Path> it;
		Path path, subPath;
		boolean motifFound = false;
		Motif currentMotif, leadingMotif = null;
		int sentences = 0, noMotifs = 0;
		while(noMotifs < stop)
		{
			it = paths.iterator();
			while(it.hasNext())
			{
				path = it.next();
				for(int start= 0; start < path.size() - sentenceLength; start += step)
				{
System.out.print(",");					
					currentMotif = this.pathRunBootstrap(path.subList(start, start + sentenceLength), dropThreshold, significanceThreshold, overlapThreshold, contextSensitive, windowLength);
					

					sentences ++;

					if(currentMotif == null)
						noMotifs++;
					else
					{
						noMotifs = 0;

						if(leadingMotif == null || leadingMotif.compareTo(currentMotif) < 0)
						{
							leadingMotif = currentMotif;					
						}
					}

					// if we've analyzed 'batch' sentences, add the best motif to the graph.
					if(sentences == batch)
					{
						sentences = 0;
						if(leadingMotif != null)
						{
							this.addSerialToken(
									leadingMotif, 
									contextSensitive, 
									dropThreshold, 
									significanceThreshold);						
						}
						leadingMotif = null;
					}
				}
			}
System.out.println();			
		}
	}
	
	/**
	 * Calls patternDistillation with default values for the long sentence 
	 * parameters.
	 */
	public boolean patternDistillation(
			double dropThreshold, 
			double significanceThreshold, 
			boolean contextSensitive)
	{
		return this.patternDistillation(
				dropThreshold, 
				significanceThreshold, 
				contextSensitive, 
				defaultSentenceLength, defaultStep, defaultBatch);
	}
	
	/**
	 * Slides a context window over all paths, determining equivalence classes 
	 */
	public boolean generalization(	double dropThreshold, 
										double significanceThreshold, 
										boolean contextSensitive,
										int windowLength)
	{
		return this.generalization(dropThreshold, significanceThreshold, contextSensitive, windowLength, 
				defaultSentenceLength, defaultStep, defaultBatch);
	}
	
	/**
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
		return this.generalizationBootstrap(dropThreshold, significanceThreshold, overlapThreshold, contextSensitive, windowLength, 
				defaultSentenceLength, defaultStep, defaultBatch);
		
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
		this.generalizationBootstrap(
				dropThreshold, 
				significanceThreshold, 
				overlapThreshold, contextSensitive,
				windowLength, stop,
				defaultSentenceLength, defaultStep, defaultBatch);
		
	}
}
