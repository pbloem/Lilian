package org.lilian.corpora;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.lilian.corpora.*;
import org.lilian.util.*;
import org.lilian.util.trees.Tree;

/**
 * Reads a treebank from a text file, and returns an encountered tree every time
 * next() is called.
 * 
 * FIXME: Empty input file causes exception
 * 
 */

public class OVISCorpus
	extends AbstractCorpus<Tree<String>>
{
	private File inputFile;
	private boolean addTop;	
	
	/**
	 * Creates a TreeCorpus based on a text file.
	 *
	 * @param input The text file to read trees from.
	 * @param addTop Whether to wrap sentences with a "top" rule.
	 */
	public OVISCorpus(File inputFile, boolean addTop) throws IOException
	{
		 this.inputFile = inputFile;
		 this.addTop = addTop;
	}	
	
	public OVISIterator iterator()
	{
		return new OVISIterator();
	}

	public class OVISIterator 
		extends AbstractCorpusIterator<Tree<String>>
	{
		private BufferedReader in;
	
		private int buffermax = 5;
		private Vector<Tree<String>> buffer;
	
		private boolean endOfFileReached;
		private boolean atSentenceEnd;
	
		private int treesRead;
	
		public OVISIterator()
		{
			try {
				in = new BufferedReader(new FileReader(inputFile));
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
	
			buffer = new Vector<Tree<String>>();
	
			endOfFileReached = false;
			atSentenceEnd  = false;
	
			treesRead = 0;
		}
	
		/**
		 * Returns the next tree in the corpus.
		 *
		 * @throws NoSuchElementException
		 */
		public Tree<String> next()
		{
			while(buffer.size() < buffermax && !endOfFileReached)
				fillBuffer();
	
			if(buffer.size() == 0 && endOfFileReached)
				throw new NoSuchElementException("No more sentences in corpus");
	
			Tree<String> result = buffer.remove(0);
	
			treesRead++;
			atSentenceEnd = true;
	
			return result;
		}
	
		/**
		 * Returns whether the corpus can return more tokens
		 */
		public boolean hasNext()
		{
			return !(endOfFileReached && buffer.size() == 0);
		}
	
		/**
		 * Returns whether the last rule returned was the last for that sentence.
		 * Always returns false for this corpus.
		 */
		public boolean atSentenceEnd()
		{
			return atSentenceEnd;
		}

		/**
		 * Returns the number of tokens read so far
		 */
		public long tokensRead()
		{
			return treesRead;
		}
	
		/**
		 * Parses a line from the corpus and adds the tree to the buffer.
		 *
		 * If there are no lines left in the corpus, it simply returns.
		 * Note that it adds the "top -> XP" rule last, signifying the end of the sentence
		 */
		private void fillBuffer()
		{
			try {
				if(endOfFileReached)
					return;
		
				String line = in.readLine();
		
				if(line == null)
				{
					endOfFileReached = true;
					return;
				}
		
				if(line.length() > 0)
					buffer.add(OVISCorpus.parseTree(line, addTop));
		
				// reset the variables to unusable values
				tree = null;
				index = -1;
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public String toString()
	{
		return "OVIS syntax treecorpus " + (addTop ? "with" : "without") + " topSymbol added, based on file: " + inputFile;
	}
	
	private static int index;
	private static String tree;
	
	/** 
	 * Temporary solution
	 */
	public static final String TOP_LABEL = "TOP";

	/**
	 * This is a recursive function that is used to parse the string
	 * representation of the tree
	 *
	 * As this method is entered, index is at the start of a child of
	 * node in the string representation in the tree.
	 *
	 */
	private static void parseNode(Tree<String>.Node parent)
	{
		StringBuilder label = new StringBuilder(5);
		Tree<String>.Node thisNode;

		boolean first = true;

		//skip past the '('
		index++;

		// read into label until ',' is encountered (ignoring whitespace)
		while(tree.charAt(index) != ',')
		{
			if(!Character.isWhitespace(tree.charAt(index)))
				label.append(tree.charAt(index));
			index++;
		}

		// create this node as a child of parent
		thisNode = parent.addChild(label.toString());

		// read until '[' is encountered
		while(tree.charAt(index) != '[')
			index++;

		// read the list of children...
		while(tree.charAt(index) != ']')
		{
			// ... read until '('...
			while( (tree.charAt(index) != '(') && tree.charAt(index) != ']')
				index++;

			if(tree.charAt(index) == '(')
			{
				// ...call parseNode() for each (which will keep incrementing i++)...
				parseNode(thisNode);
			}
		}

		// increment index to skip past the ']'
		index++;
	}	
	
	/**
	 * Parses a String representation of a tree.
	 *
	 */
	public static Tree<String> parseTree(String strTree, boolean addTop)
	{
		tree = strTree;
		index = 0;

		StringBuilder label = new StringBuilder(5);

		//skip past the '('
		index++;

		// read into label until ',' is encountered (ignoring whitespace)
		while(tree.charAt(index) != ',')
		{
			if(!Character.isWhitespace(tree.charAt(index)))
				label.append(tree.charAt(index));
			index++;
		}

		// create the tree
		Tree<String> result;
		Tree<String>.Node top, root;
		if(addTop){
			result = new Tree<String>(TOP_LABEL);
			top = result.getRoot();
			root = top.addChild(label.toString());
		}
		else
		{
		 	result = new Tree<String>(label.toString());
		 	root = result.getRoot();
		}


		// read until '[' is encountered
		while(tree.charAt(index) != '[')
			index++;

		// read the list of children...
		while(tree.charAt(index) != ']')
		{
			// ... read until '('...
			while( (tree.charAt(index) != '(') && tree.charAt(index) != ']')
				index++;

			if(tree.charAt(index) == '(')
			{
				// ...call parseNode() for each (which will keep incrementing i++)...
				parseNode(root);
			}
		}

		return result;
	}	
}
