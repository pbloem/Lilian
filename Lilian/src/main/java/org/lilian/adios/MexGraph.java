package org.lilian.adios;

import java.io.*;
import java.util.*;
import java.math.*;

import org.lilian.*;
import org.lilian.corpora.*;
import org.lilian.grammars.*;
import org.lilian.util.*;


/**
 * This class represents a MEX graph, as used by the mex and adios algorithms.
 *  
 * Each sentence in the corpus is loaded onto a graph as a path. Each node in 
 * the graph represents a distinct token in the corpus. The amount of paths along 
 * a sequence of tokens can then be used to calculate probabilities and extract motifs  
 *   
 *  TODO:
 *  - move all the matrix classes to a separate object MarkovMatrix. Or maybe not.
 *  - write a nice serialization method that recursively stores all token
 *    objects in order of dependency.
 *  - cache productivity values. (But think it through first).
 *  - Consider removing markertokens. Check JAdios.
 */

public class MexGraph<T> implements Serializable
{
	protected final MarkerToken startToken = new MarkerToken("START");
	protected final MarkerToken endToken   = new MarkerToken("END");
	
	// stores atomic tokens by value 
	protected Map<T, Token> tokens = new LinkedHashMap<T, Token>();
	protected Vector<SerialToken> stokens = new Vector<SerialToken>();
	public Vector<ParallelToken> ptokens  = new Vector<ParallelToken>();
	// this maps all tokens to all official parallel tokens in 
	// which they occur
	protected Map<Token, Set<ParallelToken>> pTokenMap = new LinkedHashMap<Token, Set<ParallelToken>>(); 
	protected Map<Integer, Set<Token>> heightMap = new LinkedHashMap<Integer, Set<Token>>();
	protected int topHeight = 0;
	
	protected Vector<Path> paths = new Vector<Path>();
	
	private BinomialCoefficient bc = new BinomialCoefficient();
	protected boolean runComplete = false;
	
	protected int totalNodes = 0;
	protected int parallelTokens = 0;
	protected int serialTokens = 0;
	
	// used in extracting motifs from sentences
	protected int[][] n;
	protected double[][] m;
	protected Vector<Segment> df;
	protected Vector<Segment> db;
	
	protected int modCount = 0;
	
	public MexGraph(SequenceCorpus<T> corpus)
	{
		// * Create the paths
	
		Path path = new Path();
		path.add(startToken);
		
		SequenceIterator<T> si = corpus.iterator();
		Token token;
		T value;
		while(si.hasNext())
		{
			value = si.next();
			
			if(tokens.containsKey(value))
				token = tokens.get(value);
			else
			{
				token = new AtomicToken(value);
				addToken(token);
			}

			path.add(token);
			
			if(si.atSequenceEnd())
			{
				path.add(endToken);
				paths.add(path);
				
				if(si.hasNext())
				{
					path = new Path();
					path.add(startToken);
				}
			}
		}
		
		if(path.size() > 1)
		{
			path.add(endToken);
			paths.add(path);
		}	
	}
	
	/**
	 * Returns the number of sentences in this MexGraph.
	 * 
	 * @return the number of sentences in this MexGraph
	 */
	public int numberOfSentences()
	{
		return paths.size();
	}
	
	/**
	 * TODO, FIXME: This method usually doesn't work
	 */
	public void save(File directory, String base)
	throws IOException
	{
		FileOutputStream fos = new FileOutputStream(new File(directory, base + ".mexgraph"));
		ObjectOutputStream out = new ObjectOutputStream(fos);
		
		out.writeObject(this);
		out.close();
	}
	
	/**
	 * Creates a corpus based on this Adios graph that returns as its tokens 
	 * string representations of the top level tokens in the graph.
	 * 
	 * The corpus returned is backed by the graph, and will throw a 
	 * {@link ConcurrentModificationException} if the graph is modified in any way.
	 * 
	 * @return A string corpus based on this graph.
	 */
	public SequenceCorpus<String> stringCorpus()
	{
		return new GraphCorpus();
	}	
		
	protected Motif getLeadingMotif(	
			List<Position> sequence, 
			double dropThreshold, 
			double significanceThreshold)
	{
		// * Fill the matrices used to find the motifs
		//   if something goes wrong (ie. no paths match the sequence)
		//   we return null
		if(! fillMatrices(sequence, dropThreshold, significanceThreshold))
			return null;

		Segment forward = new Segment(0, 0, false, 0.0, 0.0);
		Segment backward = new Segment(0, 0, false, 0.0, 0.0);
		boolean motifFound = true;
		double score;
		Segment cForward, cBackward;
		double cScore;

		// * This could probably be implemented more efficiently
		Iterator<Segment> itf;
		Iterator<Segment> itb;

		int forwardIndex = -1;
		int backwardIndex = -1;
		int f;
		int b;
		
		int productivity, cProductivity, start, end, cStart, cEnd;

		itf = df.iterator();
		motifFound = false;
		score = 100000.0;

		f = 0;
		// * Forall forward segments
		while(itf.hasNext())
		{
			itb = db.iterator();
			cForward = itf.next();

			b = 0;
			// * Forall backward segments
			while(itb.hasNext())
			{
				cBackward = itb.next();

				// * Check if they overlap correctly
				if( 	cBackward.getStart()   >= cForward.getStart()
						&& cForward.getStart()  > cBackward.getEnd()
						&& cBackward.getStart() < cForward.getEnd()
						&& (cForward.getEnd() - cBackward.getEnd()) > 2) //this avoids patterns of length 1 (creating an infinitely long branch in the tree)				
				{
					cScore = Math.min(cBackward.getSignificance(), cForward.getSignificance());
					if(cScore < score){
						forward = cForward;
						backward = cBackward;
						score = cScore;
						motifFound = true;
						forwardIndex = f;
						backwardIndex = b;
					}else if(cScore == score)
					{ // in case of equal signif. we go by productivity
						Log.logln("equal score:" + score);
						productivity = 1;
						start = backward.getEnd() + 1;
						end = forward.getEnd() - 1;
						
						cProductivity = 1;
						cStart = cBackward.getEnd() + 1;
						cEnd   = cForward.getEnd() - 1;
						
						for(int i = start; i <= end; i++)
							productivity *= sequence.get(i).productivity();
						
						for(int i = cStart; i <= cEnd; i++)
							cProductivity *= sequence.get(i).productivity();
						
						if(cProductivity > productivity)
						{
							forward = cForward;
							backward = cBackward;
							score = cScore;
							motifFound = true;
							forwardIndex = f;
							backwardIndex = b;
						}
					}
				}
				b++;
			}
			f++;
		}

		Motif motif = null;

		if(motifFound){
			df.remove(forwardIndex);
			db.remove(backwardIndex);

			// * First token of the motif
			int mStart = backward.getEnd() + 1;
			// * Last token of the motif
			int mEnd = forward.getEnd() - 1;

			List<Position> subsequence = new Vector<Position>(sequence.subList(mStart, mEnd + 1));

			motif = new Motif(	subsequence,
								backward.getSignificance(),
								forward.getSignificance(),
								backward.getStrength(),
								forward.getStrength());
		}
			
		return motif;
	}
	
	public void writeResults(File directory, String base)
			throws IOException
	{
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(directory, base + ".motifs.csv")));
		
		Iterator<SerialToken> it = stokens.iterator();
		while(it.hasNext())
		{
			out.write(it.next().toString() + "\n\r");
		}
		out.write("\n\r");
		Iterator<ParallelToken> it2 = ptokens.iterator();
		while(it2.hasNext())
		{
			out.write(it2.next().toString() + "\n\r");
		}	
		
		out.flush();
		out.close();
		
		// * Write the retokenized corpus
		out = new BufferedWriter(new FileWriter(new File(directory, base + ".re_corpus.csv")));
		
		Iterator<Path> pathIt = paths.iterator();
		Path p;
		Iterator<Token> tokenIt;

		while(pathIt.hasNext())
		{
			p = pathIt.next();			
			tokenIt = p.iterator();
			while(tokenIt.hasNext())
				out.write(tokenIt.next().toString("") + " ");
			out.write("\n");
		}
		
		out.flush();
		out.close();		
	}
	
	/**
	 * Generates a grammar from this model.
	 * @return
	 */
	public Grammar<String> toGrammar()
	{
		Grammar<String> g = new PCFGrammar<String>();
		Iterator<Path> pathIt = paths.iterator();
		Path path;
		Iterator<Token> tokenIt;
		Token token;
		
		String from = "S";
		Collection<String> to;
		
		
		while(pathIt.hasNext())
		{
			path = pathIt.next();
			tokenIt = path.iterator();
			to = new Vector<String>(path.length());
			
			while(tokenIt.hasNext())
			{
				token = tokenIt.next();
				if(! (token instanceof MexGraph<?>.MarkerToken))
				{
					to.add(token.toStringSingle());
					addRule(token, g);
				}
			}
			
			if(to.size() > 0)
				g.addRule(from, to);
		}
		
		return g;
	}
	
	public int topHeight()
	{
		return topHeight;
	}
	
	/**
	 * Add this token to this grammar as a rule.
	 * 
	 * The method does the same recursively for all 
	 * tokens under this token.
	 */
	private void addRule(Token t, Grammar<String> g)
	{
		String from;
		Collection<String> to;
		
		if(t instanceof MexGraph<?>.AtomicToken)
			return;
		
		if(t instanceof MexGraph<?>.SerialToken)
		{
			SerialToken s = (SerialToken)t;
			Token subToken;
			from = s.toStringSingle();
			to = new Vector<String>();
			Iterator<Token> it = s.getTokens().iterator();
			while(it.hasNext())
			{
				subToken = it.next();
				to.add(subToken.toStringSingle());
				addRule(subToken, g);
			}
			g.addRule(from, to);
			
			return;
		}
		
		if(t instanceof MexGraph<?>.ParallelToken)
		{
			ParallelToken p = (ParallelToken)t;
			Token subToken;
			from = p.toStringSingle();
			Iterator<Token> it = p.getMembers().iterator();
			while(it.hasNext())
			{
				to = new Vector<String>();
				subToken = it.next();
				to.add(subToken.toStringSingle());
				g.addRule(from, to);
				
				addRule(subToken, g);
			}
			return;
		}
	}
	
	/**
	 * Returns all pattern-symbols (as they occur in the grammar returned by toGrammar)
	 * for a given height. A symbol's height is defined by the height of its 
	 * highest child + 1. Terminal symbols have height 1.
	 *  
	 * @param height
	 * @return a set of Strings
	 */
	public Set<String> symbolsForHeight(int height)
	{
		if(! heightMap.containsKey(new Integer(height)))
			return Collections.emptySet();
		
		Set<String> result = new LinkedHashSet<String>();
		Iterator<Token> it = heightMap.get(new Integer(height)).iterator();
		Token token;
		while(it.hasNext())
		{
			token = it.next();
			if(token instanceof MexGraph<?>.SerialToken) 
					result.add(token.toStringSingle());
		}
		
		return result;		
	}	

	/**
	 * Adds a new token that takes the place of a sequence of tokens
	 */
	protected void addSerialToken(		Motif motif, 
										boolean contextSensitive, 
										double dropThreshold,
										double significanceThreshold)
	{
		// create the new token
		List<Position> sequence = motif.getSequence();
		
		List<Token> pattern = new Vector<Token>();
		Iterator<Position> posIt = sequence.iterator();
		Position position;
		Token token;
		while(posIt.hasNext())
		{
			position = posIt.next();
			token = position.getToken();
			
			// TODO think this through:
			// If the slot has no tokens at its position
			// abandon the pattern
			if(token == null)
			{
				System.out.println();
				throw new RuntimeException("Found slot with no tokens");
			}
			if(position.newToken())
				addToken(token);
			pattern.add(token);
		}
		
		SerialToken stoken = new SerialToken(pattern);
		addToken(stoken);

		ListIterator<Position> patternIt;
		
		// * Rewire the graph ...
		Position firstPosition = sequence.get(0);
		Collection<Node> nodes = firstPosition.nodes(true);
		
		// * Follow all nodes to see if the path they belong to 
		//   follows our pattern
		Iterator<Node> nodeIt = nodes.iterator();
		Node node, firstNode;
		Path path;
		Position control = null;
		int first, i;
		int pathsRewired = 0;
		boolean matches;
		while(nodeIt.hasNext())
		{
			//retrieve the node
			node = nodeIt.next();
			firstNode = node;
			matches = false;
			
			patternIt = null;
	
			patternIt = sequence.listIterator();

			if(patternIt.hasNext())
				control = patternIt.next();
			
			while(true)
			{ 
				// * Follow the path
				
				// break if the path we're following deviates from pattern
				if(! control.fits(node.getToken()))
				{
					break;
				}else
				{
				}				

				if(! patternIt.hasNext()){
					matches = true;
					break;					
				}
				
				i = patternIt.nextIndex();
				control = patternIt.next();

				if(!node.hasNextNode())
				{
					break;
				}
				node = node.getNextNode();
			}			
			if(matches && contextSensitive)
			{ 
				// * In context sensitive mode, we only add the parallelnode if
				//   the pattern is significant on this path.
				path  = firstNode.getPath();
				first = path.indexOf(firstNode);
								
				if(! isSignificant(first, pattern.size(), path, dropThreshold, significanceThreshold))
						matches = false;				
			}
			if(matches)
			{
				path = firstNode.getPath();
				path.replaceRange(firstNode, node, pattern.size(), stoken);
				pathsRewired++;
			}
		}
		
		modCount++;
	}
	
	/**
	 * Adds this token to the lists of tokens that are officially part 
	 * of the graph 
	 */
	private void addToken(Token t)
	{
		if(t instanceof MexGraph<?>.AtomicToken)
		{
			tokens.put(((AtomicToken)t).getValue(), ((AtomicToken)t));
		}
		if(t instanceof MexGraph<?>.SerialToken)
		{
			SerialToken s = (SerialToken)t; 
			if(! stokens.contains(s))
				stokens.add(s);
		}
		if(t instanceof MexGraph<?>.ParallelToken)
		{
			ParallelToken p = (ParallelToken)t;
			if(! ptokens.contains(p))
				ptokens.add(p);
			
			// * Add the paralleltoken to pTokenMap, so we can look up 
			//   ptokens by members
			Iterator<Token> it = p.getMembers().iterator();
			Token token;
			Set<ParallelToken> map;
			while(it.hasNext())
			{
				token = it.next();
				if(pTokenMap.containsKey(token))
				{
					map = pTokenMap.get(token);
				}else
				{
					map = new LinkedHashSet<ParallelToken>();
					pTokenMap.put(token, map);
				}
				map.add(p);
			}
		}
		
		// * Add the token to the heightmap, so we can retrieve tokens by height
		Set<Token> tMap;
		int height = t.height();
		if(heightMap.containsKey( new Integer(height) ))
		{
			tMap = heightMap.get( new Integer(height) );
		}else
		{
			tMap = new LinkedHashSet<Token>();
			heightMap.put( new Integer(height) , tMap);
		}
		tMap.add(t);
		
		// * Check if this is the highest node yet.
		topHeight = Math.max(topHeight, height);
	}	
	
	/**
	 * check if the subsequence starting with first, of length length is significant
	 * along this sequence.
	 */
	private boolean isSignificant(int first, int length, List<Token> sequence, 
															double dropThreshold,
															double significanceThreshold)
	{
		if(first < 1) return false;
		if(first + length > sequence.size()) return false;
		
		// this is wrong
		// sequence = sequence.subList(first-1, first + length);
		
		first = 1;
		int last = sequence.size() - 2;
			
		//int last = length - 1;
		fillN(new PositionList(sequence));
		fillM();
		
		double backSig, forSig;
		boolean significant = false;		
		
		// forall backward drops D(first, i) with i = last to first  
		// 		forall forward drops D(j , last + 1) with j = i - 1 to last
		//			check significance
		
		for(int i = last; i >= first && !significant ; i--)
		{
			for(int j = i-1; j <= last && !significant ; j++)
			{
				backSig = 0.0;
				backSig = significance(n[first-1][i], n[first][i], dropThreshold * m[first][i]);
				forSig  = significance(n[last+1][j], n[last][j], dropThreshold * m[last][j]);
				
				if(backSig < significanceThreshold && forSig < significanceThreshold)
					significant = true;
			}
		}
		
		return significant;
	}

	/**
	 * Fill the matrices required to extract motifs for this sequence 
	 */
	protected boolean fillMatrices(	
			List<Position> sequence,
			double dropThreshold, 
			double significanceThreshold)
	{
		boolean result;
		result = fillN(sequence);
		
		fillM();
		calculateDrops(dropThreshold, significanceThreshold);
		return result;
	}	
	
	/**
	 * Fill the matrix of sentence frequencies. 
	 * 
	 * Note: if the slot is at index 0 or sequence.size()-1 (ie. the first
	 * or the last index), the results are not reliable. 
	 * 
	 * the sequence from a to b (incl. a and b) has n[b][a] paths along it
	 * 
	 * @return true if at least one Token was found for each slot along the 
	 *         sequence. 
	 */
	protected boolean fillN(List<Position> sequence)
	{

		int length = sequence.size();
	
		n = new int[length][];
	
		for(int row = 0; row < length; row++){
			n[row] = new int[length];
			for(int column = 0; column < length; column++){
				n[row][column] = 0;
			}
		}
	
		// from all the nodes in sequence, move forward and tally the n matrix
		// the path we're extracting motifs from (the argument to this method)
		// is the control path
		// The path we're checking against the control path is the current path
	
		int start = 0;
		int end;
		
		Position controlPosition, startPosition;
		
		Node firstNode, currentNode, currentNode2;
	
		Iterator<Node> itPaths;
		Iterator<Position> itForward = sequence.iterator();
		ListIterator<Position> itInner;
		ListIterator<Position> itInner2;
		
		while(itForward.hasNext()){
			
			startPosition = itForward.next();
	
			//for all paths through startToken, follow and check
			//for how long they follow the current path.
			itPaths = startPosition.nodes(true).iterator();
			
			while(itPaths.hasNext()){
				
				end = start;
				
				// the first node of the subpath we're checking against the control path
				firstNode = itPaths.next();
				currentNode = firstNode;
				
				itInner = sequence.listIterator(start);
				
				if(itInner.hasNext()) itInner.next();
	
				// follow currentNode's path until it deviates from the control
				while(true)
				{
					// * If itInner is done, the candidate path has followed the control 
					//   all the way to the end.
					if(!itInner.hasNext())
						break;
					
					// if currentNode has no next node, this is where it ends.
					if(!currentNode.hasNextNode()) 
						break;
	
					// this is the number of the node in the current control path
					end++;
										
					controlPosition = itInner.next();
					currentNode = currentNode.getNextNode();
	
					if(controlPosition.matches(currentNode.getToken()))
					{
						n[end][start]++;					
					}else
					{
						break;
					}
				}
			}
	
			start++;
		}
		
		// do the upper half of the matrix (following paths backwards
		
		// if the sequence consists only of regular positions, we can assume
		// that the matrix is symmetric
		
		// check if we can assume symmetry
		itForward = sequence.iterator();
		boolean symmetric = true;
		while(itForward.hasNext())
			if(! (itForward.next() instanceof MexGraph<?>.RegularPosition))
				symmetric = false;
		if(symmetric)
		{
			for(int row = 0; row < length; row++){
				for(int column = row; column < length; column++){
					n[row][column] = n[column][row];
				}
			}
		}else
		{

			// if not we have to follow all the paths again		
			ListIterator<Position> itBackward = sequence.listIterator(sequence.size());

			while(itBackward.hasPrevious())
			{
				start = itBackward.previousIndex();
				startPosition = itBackward.previous();

				//for all paths through startPosition, follow and check
				//for how long they follow the current path.
				itPaths = startPosition.nodes(false).iterator();

				while(itPaths.hasNext())
				{
					end = start;

					// the first node of the subpath we're checking against the control path
					firstNode = itPaths.next();
					currentNode = firstNode;

					itInner = sequence.listIterator(start+1);

					if(itInner.hasPrevious()) itInner.previous();

					// follow currentNode's path until it deviates from the control
					while(true)
					{
						// if itInner is done, the candidate path has followed the control 
						// all the way to the end.
						if(!itInner.hasPrevious())
							break;

						// if currentNode has no previous node, this is where its path ends.
						if(!currentNode.hasPreviousNode()) 
							break;

						// this is the number of the node in the current control path
						end = itInner.previousIndex();

						controlPosition = itInner.previous();
						currentNode = currentNode.getPreviousNode();

						if(controlPosition.matches(currentNode.getToken()))
						{
							n[end][start]++;					
						}else
						{
							break;
						}
					}
				}
			}
		}
	
		//move once more along the path to tally the diagonal of the matrix
		Iterator<Position> iterator = sequence.iterator();
	
		for(int c = 0; iterator.hasNext(); c++)
		{
			controlPosition = iterator.next();
			n[c][c] = controlPosition.numberOfNodes();			
		}
		
		// check the sequence to see that all the slots found at least
		// one Token
		iterator = sequence.iterator();
		Position position;
		boolean slotsFilled = true;
		while(iterator.hasNext())
		{
			position = iterator.next();
			if(position instanceof MexGraph<?>.SlotPosition)
				if( ((SlotPosition)position).getTokens().size() < 1)
					slotsFilled = false;
		}
		
		return slotsFilled;	
	}
	
	/**
	 * Fill the probability matrix 
	 */
	protected void fillM()
	{
		int length = n.length;
		
		// * Create the probability matrix m based on the matrix of path lengths n --
		
		m = new double[length][];

		for(int row = 0; row < length; row++){
			m[row] = new double[length];
			for(int column = 0; column < length; column++)
			{
				if(row > column)
					m[row][column] = n[row][column] / (double)n[row-1][column];
				else if(row < column)
					m[row][column] = n[row][column] / (double)n[row+1][column];
				else
					m[row][column] = n[row][column] / (double)totalNodes;
			}
		}
	}
	
	/**
	 * Calculate all significant drops in the matrix m
	 */
	protected void calculateDrops(double dropThreshold, double significanceThreshold)
	{
		int length = m.length;
		
		df = new Vector<Segment>();
		db = new Vector<Segment>();

		double sig, strength;
		// * Set the forward drops (only check the lower half of the
		//   matrix (i >= j)).
		for(int i = 0; i < length-1; i++){
			for(int j = 0; j <= i; j++){
				strength = m[i + 1][j] / m[i][j];
				if(strength < dropThreshold)
				{
					sig = significance(n[i + 1][j], n[i][j], dropThreshold * m[i][j]);
					if(sig < significanceThreshold)
						df.add(new Segment(j, i + 1, true, sig, strength));
				}
			}
		}

		// * Set the backward drops (only check the upper half of the
		//   matrix (i <= j))
		for(int i = 1; i < length ; i++){
			for(int j = i; j < length; j++){
				strength = m[i-1][j] / m[i][j];
				if(strength < dropThreshold){
					sig = significance(n[i - 1][j], n[i][j], dropThreshold * m[i][j]);
					if(sig < significanceThreshold)
						db.add(new Segment(j, i - 1, false, sig, strength));
				}
			}
		}
	}
	
	/**
	 * Determines the significance of a certain drop (say Dr(ei ; ej))
	 *
	 * This method returns the p-value that indicates the probability
	 * that Dr(ei ; ej) < threshold if Pr(ei; ej)/Pr(ei; ej-1) <= n.
	 * This value needs to be lower than the significanceThreshold for
	 * the drop to be considered.
	 *
	 * Totalpaths is the number of full paths (ie. ei;ej) and subpaths
	 * is the number of paths up to (but not including) the last node
	 * (ei;ej-1).
	 *
	 * prob is n * Pr(ei;ej-1)
	 */

	protected double significance(int totalPaths, int subPaths, double prob)
	{
		//the binomial coefficient algorithm can't handle
		// top values over 1030, but at those rates, there's isn't any risk of
		// small sample statistics anyway.
		
		// if(subPaths > 1)
		// 	return 0.0;
		
		// we make this a binomial distribution (and translate variable names 
		// for the sake of clarity)
		int n = subPaths;
		double p = prob; 
		
		// we want the cumulative probability of x
		// Note: my june 2006 implementation had totalPaths +1 here instead. 
		// I see no reason why that should be there (it also caused exceptions 
		// if totalPaths == subPaths)
		int x = totalPaths;
		
		double result = 0.0;
		
		for(int i = 0; i <= x; i++)
			result += bc.get(n, i) *  Math.pow(p, i) * Math.pow(1.0 - p, n-i);
				
		return result;
	}
	
	/**
	 * Converts a list of tokens to a list of values
	 * - AtomicTokens are converted to their value
	 * - ParallelTokens are expanded into multiple values 
	 * - SerialTokens are turned into null, probably... don't know yet 
	 */
	protected List<T> toValueList(List<? extends Token> inList)
	{
		List<T> result = new Vector<T>();
		Iterator<? extends Token> it = inList.iterator();
		
		Token token;
		while(it.hasNext())
		{
			token = it.next();
			if(token instanceof MexGraph<?>.AtomicToken)
				result.add( ((AtomicToken)token).getValue() );
		}
		
		return result;	
	}
	
	/**
	 * This class represents one node on the graph.
	 */
	protected abstract class Token implements Serializable
	{

		private Vector<Node> nodes;

		public Token(){
			nodes = new Vector<Node>();
		}
		
		public void addNode(Node n){
			nodes.add(n);
		}
		
		public void removeNode(Node n){
			nodes.remove(n);
		}		

		/**
		 * The nodes of the paths that cross this token.
		 */
		public List<Node> getNodes(){
			return new Vector<Node>(nodes);
		}

		/**
		 * The number of paths that cross this token.
		 */
		public int numberOfPaths()
		{
			return nodes.size();
		}

		public String nodesString(){
			Iterator<Node> it = nodes.iterator();
			StringBuilder result = new StringBuilder();

			boolean first = true;
			while(it.hasNext()){
				if(first)
					first = false;
				else
					result.append(", ");
				result.append(it.next().toComplexString());
			}

			return result.toString();
		}
		
		public abstract String toString(String delim);
		public abstract String toStringSingle();
		/**
		 * The number of Strings this Token represents.
		 */
		public abstract int productivity();
		
		/**
		 * The distance to this token's deepest descendant
		 */
		public abstract int height();		
		
		// equals is defined by Object.equals. In other words,
		// two tokens are equal if they are the same object
	}
	
	/**
	 * This class represents and atomic token. Ie. a node in the graph that
	 * represents a single value.
	 *  
	 * Behavior is undefined is the value is changed after this token
	 * is constructed around it
	 */
	protected class AtomicToken extends Token
	{
		private T value;
		private int hash; 
		
		public AtomicToken(T value)
		{
			super();
			this.value = value;
			this.hash = value.hashCode();
		}
		
		public T getValue()
		{
			return value;
		}
		
		public String toString()
		{
			return value.toString();
		}
		
		public int hashCode()
		{
			return hash;
		}
		
		public String toString(String delim)
		{
			return value.toString();
		}
		
		public String toStringSingle()
		{
			return value.toString();
		}
		
		public int productivity()
		{
			return 1;
		}
		
		public int height()
		{
			return 1;
		}
	}
	
	/**
	 * This class represents a marker token, which can be used to delimit 
	 * sentence. It contains no T value or other tokens.
	 *
	 * @author Peter
	 */
	protected class MarkerToken extends Token
	{
		private String label;
		
		public MarkerToken(String label)
		{
			super();
			this.label = label;			
		}
		
		public String getLabel()
		{
			return label;
		}
		
		public String toString()
		{
			return label;
		}
		
		public int hashCode()
		{
			return label.hashCode();
		}
		
		public String toString(String delim)
		{
			return label;
		}
		
		public String toStringSingle()
		{
			return label;
		}
		
		public boolean equals(Object o)
		{
			if(o instanceof MexGraph<?>.MarkerToken)
				if(this.label.equals(((MarkerToken)o).label))
					return true;

			return false;
		}
		
		public int productivity()
		{
			return 1;
		}
		
		public int height()
		{
			return 1;
		}
	}	
	
	/**
	 * This class represents a parallel token. Ie. a node in the graph that
	 * unites a sequences of nodes under it.
	 * 
	 * The list tokens is used directly. It should not be modified after the
	 * token is created.
	 */
	protected class SerialToken extends Token
	{
		private String label;
		private List<Token> tokens;
		private int hash;
		private int height = 1;
		private int productivity = 1;
		
		public SerialToken(List<Token> tokens)
		{
			super();
			serialTokens++;
			this.label = "s" + serialTokens;
			this.tokens = tokens;
			hash = label.hashCode();
			
			Iterator<Token> it = tokens.iterator();
			Token t;
			while(it.hasNext())
			{
				t = it.next();
				height = Math.max(height, t.height());
				productivity *= t.productivity();				
			}
			height++;
		}
		
		public List<Token> getTokens()
		{
			return tokens;
		}
		
		public String getLabel()
		{
			return label;
		}
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(label).append("[");
			Iterator<Token> it = tokens.iterator();
			boolean first = true;
			while(it.hasNext())
			{
				if(first) first = false;
				else sb.append(", ");
				sb.append(it.next().toStringSingle());
			}
			sb.append("]");
			return sb.toString();
		}
		
		public int hashCode()
		{
			return hash;
		}
		
		public String toString(String delim)
		{
			StringBuilder sb = new StringBuilder();
			Iterator<Token> it = tokens.iterator();
			while(it.hasNext())
				sb.append(it.next().toString(delim)).append(delim);
					
			return sb.toString();
		}
		
		public String toStringSingle()
		{
			return label;
		}
		
		public int productivity()
		{
			return productivity;

		}
		public int height()
		{
			return height;
		}
	}	
	
	/**
	 * This class represents a parallel token.
	 *   
	 */
	protected class ParallelToken extends Token{
		
		private Set<Token> members;
		private String label;
		private int hash;
		// a collection of al nodes that cross this token
		// or one of its members
		private Vector<Node> allNodes;
		
		private int height = 1;
		private int productivity = 0;		
		
		public ParallelToken(Set<Token> members){
			this.members = members;
			parallelTokens++;
			label = "p" + parallelTokens;
			hash = label.hashCode();
			
			Iterator<Token> it = members.iterator();
			Token t;
			while(it.hasNext())
			{
				t = it.next();
				height = Math.max(height, t.height());
				productivity += t.productivity();				
			}
			height ++;
		}
		
		public Set<Token> getMembers()
		{
			return members;
		}
		
		public boolean isMember(Token t)
		{
			return members.contains(t);
		}
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(label).append("{");
			Iterator<Token> it = members.iterator();
			boolean first = true;
			while(it.hasNext())
			{
				if(first) first = false;
				else sb.append("| ");
				sb.append(it.next().toStringSingle());
			}
			sb.append("}");
			return sb.toString();
		}
		
		public String toString(String delim)
		{
			StringBuilder sb = new StringBuilder();

			Iterator<Token> it = members.iterator();
			sb.append(label+"{");
			boolean first = true;
			while(it.hasNext())
			{
				if(first) first = false;
				else sb.append(delim);
				sb.append(it.next().toString(delim));
			}
			sb.append("}");
			return sb.toString();
		}
		
		public int hashCode()
		{
			return hash;
		}
		
		/**
		 * Calculates the overlap between a set of tokens and the
		 * tokens that are members of this parallel token
		 * 
		 * @param set
		 * @return The overlap between the 
		 */
		public double overlap(Set<? extends Token> set)
		{
			int o = Functions.setOverlap(this.members, set);
			return ((double)o)/((double)members.size());
		}
		
		/**
		 * All nodes crossing this token or one of it's members.
		 * Once the value is requested once, it's cached for optimization, so be sure 
		 * the node won't be modified again.
		 * 
		 * @return
		 */
		public Vector<Node> allNodes()
		{
			if(allNodes != null)
				return allNodes;
			
			Iterator<Token> it = members.iterator();
			allNodes = new Vector<Node>();			
			while(it.hasNext())
				allNodes.addAll( it.next().getNodes() );
			
			// Should we add these too? The algorithm description isn't clear on this
			// maybe run some tests? (It doesn't seem to make much of a difference)
			allNodes.addAll(this.getNodes());
			
			return allNodes;
		}
		public String toStringSingle()
		{
			return label;
		}	
		
		public int productivity()
		{
			return productivity;
		}
		
		public int height()
		{
			return height;
		}		
	}
	
	/**
	 * A class to represent paths along the graph.
	 */

	protected class Path extends AbstractList<Token> implements List<Token>, Serializable{

		
		private int cacheModCount = modCount - 1;
		private MexGraph.Node[] cache;
		
		private Node firstNode;
		private Node lastNode;
		private int length = 0;
		
		public Path(){			
		}
		
		public int size()
		{
			return length;			
		}
		
		public Token get(int i)
		{
			return getNode(i).getToken();
		}
		
		public Node getNode(int i)
		{
			if(i < 0) 			throw new ArrayIndexOutOfBoundsException(i);
			if(i > length-1)	throw new ArrayIndexOutOfBoundsException(i);
			
			if(cacheModCount != modCount)
			{
				cache = new MexGraph.Node[length];
				cacheModCount = modCount;				
			}

			if(cache[i] != null)
				return (Node)cache[i];
			if(i > 0 && cache[i-1] != null)
				return ((Node)cache[i-1]).getNextNode();
			if(i < length -1 && cache[i+1] != null)
				return ((Node)cache[i+1]).getPreviousNode();

				
			Node current;
						
			if(length/2 > i)
			{
				current = firstNode;
				for(int j = 0; j < i; j ++)
					current = current.getNextNode(); 			
			}else
			{
				current = lastNode;
				for(int j = length-1; j > i; j--)
					current = current.getPreviousNode();
			}
			
			cache[i] = current;
			return current;
		}
		
		public boolean add(Token token){
			Node n = new Node(token, this);
			
			if(firstNode == null){
				firstNode = n;
				lastNode = n;
			}else{
				lastNode.setNextNode(n);
				n.setPreviousNode(lastNode);

				lastNode = n;
			}
			
			length++;
			modCount++;
			
			return true;
		}
		
		/**
		 * @param firstNode The first Node to be replaced
		 * @param lastNode  The last node to be replaced
		 * @param length    The number of nodes that are replaced
		 * @param token     The token for the new node that replaces them
		 */
		
		public void replaceRange(Node firstNode, Node lastNode, int length, Token token)
		{
			if(! firstNode.getPath().equals(this))
				throw new IllegalArgumentException("firstNode not part of this path (this path:"+this+", firstNode's path:"+firstNode.getPath()+")");
			if(! lastNode.getPath().equals(this))
				throw new IllegalArgumentException("lastNode not part of this path (this path:"+this+", lastNode's path:"+lastNode.getPath()+")");
			
			Node newNode = new Node(token, this);
			
			this.length++;
			
			// wire up the previous node to the new one 
			if(firstNode.hasPreviousNode())
			{
				firstNode.getPreviousNode().setNextNode(newNode);
				newNode.setPreviousNode(firstNode.getPreviousNode());
			}else
			{
				this.firstNode = newNode;
			}
			
			// wire up the new node to the first one after the range
			if(lastNode.hasNextNode())
			{
				lastNode.getNextNode().setPreviousNode(newNode);
				newNode.setNextNode(lastNode.getNextNode());
			}else
			{
				this.lastNode = newNode;
			}
			
			// delete the intermediate nodes
			Node current = firstNode,
				 next = firstNode;
			while(current.hasNextNode())
			{
				if(newNode.hasNextNode() && current.equals(newNode.getNextNode()))
					break;					
						
				next = current.getNextNode();
				current.clear();
				current = next;
			}
			this.length = this.length - length;
			
			totalNodes -= length - 1; 
				
			modCount++;	
		}

		public int length(){
			return length;
		}
		
		public Iterator<Token> iterator()
		{
			return new PathIterator(firstNode, 0);
		}

		public ListIterator<Token> listIterator()
		{
			return new PathIterator(firstNode, 0);
		}

		public ListIterator<Token> listIterator(int i)
		{
			try
			{
				return new PathIterator(getNode(i), i);
			}catch(Exception e)
			{
				System.out.println(i);
				System.out.println(this);
				e.printStackTrace();
				System.exit(9);
			}
			return null;
		}
		
		public ListIterator<Token> listIterator(Node n)
		{
			return new PathIterator(n);
		}
		
		public int indexOf(Node n)
		{
			int i = 0;
			Node current = firstNode;
			boolean found = false;
			while(true)
			{
				if(current.equals(n))
				{
					found = true;
					break;
				}
				if(!current.hasNextNode())
					break;
				
				i++;
				current = current.getNextNode();				
			}
			
			if(found)
				return i;
			else
				return -1;
		}

		public String toString(){
			StringBuffer r = new StringBuffer();
			Node node = firstNode;

			r.append(length + ": ");
			
			int i = 0;
			
			while(node.hasNextNode()){				
				r.append(node + "(" + i + "), ");
				i++;
				node = node.getNextNode();
			}
			
			r.append(node + "(" + i + ")");
				
			return r.toString();
		}

		public Node getFirstNode(){
			return firstNode;
		}

		public Node getLastNode(){
			return lastNode;
		}
		
		public void incrementMod()
		{
			modCount++;
		}
		
		public int productivity()
		{
			Iterator<Token> it = this.iterator();
			int result = 1;
			while(it.hasNext())
				result *= it.next().productivity();
			return result;
		}				
		
		private class PathIterator implements ListIterator<Token>
		{
			// the paths modCount at the creation of this iterator
			private int myModCount;
			// the nodes on either side of the cursor. Null if the cursor is at
			// the end
			private Node left, right, result;
			private int index = -1;
			
			public PathIterator(Node n, int i)
			{
				this(n);
				index = i;
			}
						
			public PathIterator(Node n)
			{
				if(length != 0)
				{
					right = n;
				
					if(right.hasPreviousNode())
						left = right.getPreviousNode();
				}
			
				myModCount = modCount;
			}
			
			private void checkModCount()
			{
				if(modCount != myModCount) throw new ConcurrentModificationException(); 				
			}
			
			public boolean hasNext(){
				checkModCount();
				return right != null;
			}
			public boolean hasPrevious(){
				checkModCount();
				return left != null;
			}
			
			public Token next()
			{
				checkModCount();
				
				if(!hasNext())
					throw new NoSuchElementException();
				
				result = right;
				left = right;
				if(right.hasNextNode())
					right = right.getNextNode();
				else
					right = null;

				if(index != -1) index++;
				return result.getToken();
			}
			
			public Token previous()
			{
				checkModCount();
				if(!hasPrevious())
					throw new NoSuchElementException();
				
				result = left;
				right = left;
				if(left.hasPreviousNode())
					left = left.getPreviousNode();
				else
					left = null;
				
				if(index != -1) index--;
				return result.getToken();
			}
			
			public int nextIndex(){
				checkModCount();
				
				if(index == -1)
					if(right == null)
						index = length;
					else
						index = indexOf(right);
				
				return index; 
			}
			public int previousIndex(){	
				checkModCount();

				if(index == -1)
					if(right == null)
						index = length;
					else
						index = indexOf(right);
			
				return index - 1; 
			} 
			
			public void remove(){ 
				checkModCount();
				throw new UnsupportedOperationException(); }
			public void set(Token t){
				checkModCount();
				throw new UnsupportedOperationException(); }
			public void add(Token t){
				checkModCount();
				throw new UnsupportedOperationException(); }
		}		
	}

	/**
	 * A class to represent nodes (which make up paths).
	 */
	protected class Node implements Serializable{
		
		private Path path;
		private Token token;
		private Node nextNode;
		private Node previousNode;
		// private boolean motifBeginning = false;
		// private boolean motifEnd = false;
	
		public Node(Token token, Path path)
		{
			if(token == null)
				throw new IllegalArgumentException("Cannot instantiate node with null Token");
			if(token == null)
				throw new IllegalArgumentException("Cannot instantiate node with null Path");
			this.path = path;
			this.token = token;
			totalNodes++;
			
			path.incrementMod();
			
			token.addNode(this);
		}
		
		public Path getPath()
		{
			return path;
		}
		
		public Token getToken(){
			return token;
		}	
	
		public void setNextNode(Node n){
			nextNode = n;
			path.incrementMod();
		}
	
		public Node getNextNode(){
			return nextNode;
		}
	
		public void setPreviousNode(Node n){
			previousNode = n;
			path.incrementMod();
		}
	
		public Node getPreviousNode(){
			return previousNode;
		}
	
		public boolean hasNextNode(){
			return !(nextNode == null);
		}
	
		public boolean hasPreviousNode(){
			return !(previousNode == null);
		}
	
		public void clearNextNode(){
			nextNode = null;
			path.incrementMod();
		}
	
		public void clearPreviousNode(){
			previousNode = null;
			path.incrementMod();
		}
		
		/**
		 * Clear all information in this node (if a node is deleted, this 
		 * should be called to clear all object references for the gc)
		 */
		public void clear()
		{
			token.removeNode(this);
			path.incrementMod();
			
			path = null;
			token = null;
			nextNode = null;
			previousNode = null;
		}

		public String toString(){
			return token.toString();
		}

		public String toComplexString(){
			return "[" + previousNode + "-" + token.toString() +  "-" + nextNode + "]";
		}
	}
	
	/**
	 * This class is used internally to store segments of a sequence
	 * that represent a significant backward or forward drop
	 *
	 */
	protected class Segment implements Serializable{
		int start, end;
		boolean forward;
		double significance, strength;

		public Segment(int s, int e, boolean f, double si, double st){
			start = s;
			end = e;
			forward = f;
			significance = si;
			strength = st;
		}

		public int getStart(){
			return start;
		}

		public int getEnd(){
			return end;
		}
		public boolean isForward(){
			return forward;
		}
		public double getSignificance(){
			return significance;
		}
		public double getStrength(){
			return strength;
		}
		
		public String toString()
		{
			return "[" +start + ", "+ end + ", " + significance + ", " +  strength + "]";
		}
	}
	
	/**
	 * This class represents one element of a generalized search path.
	 * 
	 * Each position can represent a single Token, a slot or an 
	 * equivalence class. Each path in the graph is checked against
	 * a generalized search path (a list of Positions), checking the tokens 
	 * of the path against the Position objects. A RegularPosition object
	 * macthes a token if the RegularPosition represents the Token, an 
	 * EquivPosition matches a token if the EquivPosition represents a 
	 * ParallelToken of which the token is a member. A SlotPosition always 
	 * matches a Token.  
	 */
	protected abstract class Position
	{
		/**
		 * Returns whether this Token matches the Position.
		 * 
		 * This is used to determine which parts of a list 
		 * of positions are significant patterns. 
		 */
		public abstract boolean matches(Token token);
		
		/**
		 * Returns whether this token fits the position
		 * 
		 * This is used to determine whether a series of tokens on a path 
		 * should be replaced by a SerialToken based on this list of Positions.  
		 */
		public abstract boolean fits(Token token);
		
		/**
		 */
		public abstract int numberOfNodes();
		
		/**
		 * Returns all pathnodes that can be used to follow paths to the 
		 * 'next' Position (forwards if forward is true, backwards otherwise).  
		 * 
		 * For RegularPosition and EquivPosition, this just returns all nodex 
		 * that cross the represented tokens (and ignores the parameters). Since 
		 * a SlotPosition should technically return all node sin the graph, this 
		 * returns all nodes that are guaranteed to lead in to the next position.
		 */
		public abstract Collection<Node> nodes(boolean forward);
		
		/**
		 * This is used by SlotPosition to maintain a list of Tokens occuring in 
		 * this place. Other Position objects ignore it.
		 * @param t
		 */
		public abstract void addToken(Token t);
		
		/**
		 * return the token (existing or newly created, that should be wired into 
		 * the graph, if the search path (of which this Position is a part) gets 
		 * accepted.
		 */
		public abstract Token getToken();
		
		/**
		 * @return Whether the Position returns a new token, or an already existing token.
		 */
		public abstract boolean newToken();
		
		/**
		 * @return The number of sequences that this position represents
		 */
		public abstract int productivity(); 
	}
	
	protected class RegularPosition extends Position
	{
		private Token t;
		public RegularPosition(Token t)
		{
			this.t = t;
		}
		
		public boolean matches(Token tc)
		{
			return t.equals(tc);
		}

		public boolean fits(Token tc)
		{
			return t.equals(tc);
		}
		
		public int numberOfNodes()
		{
			return t.getNodes().size();
		}
		
		public Collection<Node> nodes(boolean forward)
		{
			return t.getNodes();
		}
		
		public void addToken(Token t){}	
		
		public Token getToken()
		{
			return t;
		}
		
		public boolean newToken()
		{
			return false;
		}
		
		public String toString()
		{
			return "r|" + t.toString();			
		}
		
		public int productivity()
		{
			return t.productivity();
		}
	}

	protected class EquivPosition extends Position
	{
		private Set<Token> members;
		// a collection of al nodes thsat cross this token
		// or one of its members
		private Vector<Node> allNodes;
		private double overlap;
		private Set<Token> slotTokens;
		private ParallelToken pt, newPt;
		
		
		public EquivPosition(ParallelToken t, double overlap, Set<Token> slotTokens)
		{
			if(overlap > 1.0) throw new IllegalArgumentException("Overlap cannot be > 1.0");
			
			this.members = t.getMembers();
			this.slotTokens = slotTokens;
			this.overlap = overlap;
			this.pt = t;
		}
		
		public EquivPosition(Set<Token> members)
		{
System.out.print("!");
			this.members = members;
		}
		
		public boolean matches(Token tc)
		{
			return members.contains(tc);
		}
		
		public boolean fits(Token t)
		{
			if(newPt == null)
				getToken();
			return newPt.isMember(t);
		}
		
		public int numberOfNodes()
		{
			nodes(true);
			return allNodes.size();
		}
		
		public Collection<Node> nodes(boolean forward)
		{
			Iterator<Token> it = members.iterator();
			allNodes = new Vector<Node>();			
			while(it.hasNext())
				allNodes.addAll( it.next().getNodes() );
				
			return allNodes;
		}
		
		public void addToken(Token t){}
		
		public Token getToken()
		{
			if(newPt != null)
				return newPt;
			
			 // if perfect overlap (> 0.99 used instead of == 1.0 because of fl. point errors)
			if(overlap > 0.99)  
			{
				newPt = pt;
				return pt;
			}else
			{
				Set<Token> newMembers = new LinkedHashSet<Token>(members);
				newMembers.retainAll(slotTokens);
System.out.println("* Creating new parallel token:");				
System.out.println("  slotTokens: " + slotTokens);
System.out.println("  E(j) members: " + members);
				newPt = new ParallelToken(newMembers);
System.out.println("  " + newPt);				
 				return newPt;
			}
		}
		
		public boolean newToken()
		{
			return (overlap != 1.0);
		}
		
		public String toString()
		{
			return "e|" + members;
		}
		
		public int productivity()
		{
			Iterator<Token> it = members.iterator();
			int result = 0;
			while(it.hasNext())
				result += it.next().productivity();
			return result;			
		}		
	}

	protected class SlotPosition extends Position
	{		
		Position previous;
		Position next;
		
		//all tokens that can occur at this slot position
		Set<Token> slotTokens = new LinkedHashSet<Token>();
		
		public SlotPosition(Position previous, Position next)
		{
			this.previous = previous;
			this.next = next;
		}

		public boolean matches(Token tc)
		{
			return true;
		}

		public boolean fits(Token tc)
		{
			return slotTokens.contains(tc);
		}
		
		public int numberOfNodes()
		{
			if(previous == null && next == null)
				return 0;
			if(previous == null)
				return next.numberOfNodes();
			if(next == null)
				return previous.numberOfNodes();

			return Math.max(previous.numberOfNodes(), next.numberOfNodes());
		}

		public Collection<Node> nodes(boolean forward)
		{
			Position follows;
			if(forward)	follows = next;
			else        follows = previous;
			
			Set<Node> nodes;
			if(follows == null)
			{
				nodes =  Collections.emptySet();
			}else
			{						
				Collection<Node> nextNodes = follows.nodes(forward);
				nodes = new LinkedHashSet<Node>();

				Iterator<Node> it = nextNodes.iterator();
				Node node;
				while(it.hasNext())
				{
					node = it.next();
					if(forward) node = node.getPreviousNode();
					else        node = node.getNextNode();

					if(node != null)
						nodes.add(node);
				}
			}
			Log.logln("  % returning nodes for slot: " + nodes.size() + " nodes returned ("+previous+","+next+")");
			return nodes;
		}
		
		// adds a token to this position's set of tokens
		public void addToken(Token t)
		{
			slotTokens.add(t);
		}
		
		public Set<Token> getTokens()
		{
			return slotTokens;
		}
		
		public Token getToken()
		{
			Token result = null;
			if(slotTokens.size() == 0)
				result = null;				
			else if(slotTokens.size() > 1)
			{
				// try to find an existing paralleltoken that overlaps this one exactly
				Iterator<ParallelToken> it = ptokens.iterator();
				ParallelToken t;
				while(it.hasNext() && result == null)
				{
					t = it.next();
					if(t.members.equals(this.slotTokens))
						result = t;					
				}
				
				if(result == null)
					result = new ParallelToken(slotTokens);				
			}else 
			{
				// return the first (and only) token
				result = slotTokens.iterator().next();
			}
			
			return result;
		}
		
		public boolean newToken()
		{
			if(slotTokens.size() > 1)
				return true;
			return false;
		}
		
		public String toString()
		{
			return "s|" + slotTokens;
		}
		
		public int productivity()
		{
			if(slotTokens == null)
				return 0;
			
			Iterator<Token> it = slotTokens.iterator();
			int result = 0;
			while(it.hasNext())
				result += it.next().productivity();
			return result;			
		}
	}
	
	/** 
	 * Wrapper class to translate a list of Tokens to a list of RegularPositions
	 */ 
	protected class PositionList extends AbstractList<Position>
	{
		private List<Token> master; 		
		public PositionList(List<Token> master)
		{
			this.master = master;
		}
		
		public Position get(int i)
		{
			return new RegularPosition(master.get(i));
		}
		
		public int size()
		{
			return master.size();
		}
	}
	
	protected class Motif implements Comparable<Motif>{

		// the sequence of tokens
		private List<Position> sequence;
		
		private double significanceStart;
		private double significanceEnd;
		private double significance;		
		private double strengthStart;
		private double strengthEnd;
		private int productivity;
		
		public Motif(	List<Position> sequence, 
				double significanceStart,
				double significanceEnd,
				double strengthStart,
				double strengthEnd)
		{
			this.sequence          = sequence;
			this.significanceStart = significanceStart; 
			this.significanceEnd   = significanceEnd;
			this.significance      = Math.min(significanceEnd, significanceStart);
			this.strengthStart     = strengthStart; 
			this.strengthEnd       = strengthEnd; 

			Iterator<Position> it = sequence.iterator();
			productivity = 1;
			while(it.hasNext())
				productivity *= it.next().productivity();
		}	
		
		public List<Position> getSequence(){
			return sequence;
		}	

		public double getSignificanceStart(){
			return significanceStart;
		}

		public double getSignificanceEnd(){
			return significanceEnd;
		}

		public double getStrengthStart(){
			return strengthStart;
		}

		public double getStrengthEnd(){
			return strengthEnd;
		}

		public int length(){
			return sequence.size();
		}

		public String toString(){
			StringBuilder r = new StringBuilder();
			Iterator<Position> it = sequence.iterator();
			
			boolean first = true;
			while(it.hasNext()){
				if(first)
					first = false;
				else
					r.append(", ");
				r.append(it.next());			
			}
			
			r.append(" :" + Math.min(significanceStart, significanceEnd));
			r.append(" :" + productivity);
			
			return r.toString();
		}

		public String toDelimitedString(String delimiter){
			StringBuilder r = new StringBuilder();
			Iterator<Position> it = sequence.iterator();

			boolean first = true;
			while(it.hasNext()){
				if(first)
					first = false;
				else
					r.append(delimiter);
				r.append(it.next());
			}

			return r.toString();
		}

		public boolean equals(Object o){
			if(o instanceof MexGraph<?>.Motif)
			{
				Motif m = (Motif)o;
				return m.sequence.equals(sequence);
			} else
			{
				return false;
			}
		}

		/**
		 * This comparison assumes that smaller motifs are better. If
		 * the significances are different, the motif with the smaller 
		 * significance is smaller, if the significances are equal, the 
		 * motif with the highest productivity is smaller.
		 */
		public int compareTo(Motif m){
			if(this.significance == m.significance)
			{
				if (this.productivity > m.productivity)
					return -1;
				else if (this.productivity < m.productivity)
					return 1;
				else 
					return 0;
			}else if (this.significance < m.significance)
				return -1;
			else 
				return 1;
		}
	}	
	
	private class GraphCorpus 
		extends AbstractCorpus<String>
		implements SequenceCorpus<String>
	{
		private int lastModCount;
		
		public GraphCorpus()
		{
			lastModCount = modCount;			
		}
		
		public SequenceIterator<String> iterator()
		{
			return new GraphCorpusIterator();
		}
		
		private class GraphCorpusIterator
			extends AbstractCorpusIterator<String>
			implements SequenceIterator<String>
		{
			private Iterator<? extends List<Token>> sentenceIt;
			private Iterator<Token> tokenIt;
			
			private int lastSentence;
			private int currentSentence;
			
	
			public GraphCorpusIterator()
			{
				checkCoMod();
				
				sentenceIt = paths.iterator();
				if(sentenceIt.hasNext())
					tokenIt = getIt(sentenceIt.next());
				
				currentSentence = 0;
				
				lastSentence = paths.size() - 1;
			}
	
			public boolean hasNext()
			{
				checkCoMod();
				
				if(tokenIt == null)
					return false;
				
				if(tokenIt.hasNext())
					return currentSentence <= lastSentence;
				
				return currentSentence < lastSentence;
			}
	
			public String next()
			{
				checkCoMod();
				
				if(tokenIt == null)
					throw new NoSuchElementException();
	
				while(hasNext())
				{
					if(tokenIt.hasNext())
						return tokenIt.next().toString("");
					if(sentenceIt.hasNext())
					{
						tokenIt = getIt(sentenceIt.next());
						currentSentence++;
					}
				}
				
				throw new NoSuchElementException();
			}
	
			public boolean atSequenceEnd()
			{
				checkCoMod();
				
				return ! tokenIt.hasNext();
			}
	
			private void checkCoMod()
			{
				if(modCount != lastModCount)
					throw new ConcurrentModificationException();
			}
			
			/**
			 * Strips the first and last token of a path 
			 */
			private Iterator<Token> getIt(List<Token> in)
			{
				return in.subList(1, in.size() - 1).iterator();
			}
		}
	}
}
