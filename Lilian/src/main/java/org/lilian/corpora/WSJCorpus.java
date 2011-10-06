package org.lilian.corpora;

import java.util.*;
import java.io.*; 

import org.lilian.util.*;
import org.lilian.util.trees.Tree;
import org.lilian.corpora.*;

/**
 * Reads tree files in WSJ format.
 * 
 * NOTE: not to specification (if one exists), and not robust at all.
 * 
 */
public class WSJCorpus
	extends AbstractCorpus<Tree<String>>
{
	private static final String EXTENSION = "mrg";
	
	/* Every substring of this string is considered punctuation */
	private static final String PUNCTUATION = ":;?.,!``''$%---\"";
		
	private ArrayList<File> files;
	private boolean removePunctuation = true;	
	
	/**
	 * Creates a TreeCorpus based on a text file.
	 *
	 * @param input The text file to read trees from.
	 * @param addTop Whether to wrap sentences with a "top" rule.
	 */
	 public WSJCorpus(	File inputFile, 
			 			final boolean checkExtension, 
			 			boolean removePunctuation) 
	 {
		 this.removePunctuation = removePunctuation;		 
		 
		 if(inputFile.isDirectory())
		 {
			 File[] filesArray = inputFile.listFiles(
					new FileFilter(){
						public boolean accept(File file) {
							 return ! checkExtension ||  file.getName().endsWith(EXTENSION);
					} } );
			 
			 
			 files = new ArrayList<File>(filesArray.length);
			 for(File file : filesArray)
				 files.add(file);
		 } else
		 {
			 files = new ArrayList<File>(1);
			 files.add(inputFile);
		 }
	 }

	@Override
	public Iterator<Tree<String>> iterator() {
		return new WSJCorpusIterator();
	}	 
	
	public class WSJCorpusIterator 
		extends AbstractCorpusIterator<Tree<String>>
	{
		private int currentFile;
		
		private BufferedReader in;
	
		private int buffermax = 5;
		private ArrayList<Tree<String>> buffer;
	
		private boolean endOfFilesReached = false;
		
		public WSJCorpusIterator()
		{
			currentFile = 0;
			
			try {
				in = new BufferedReader(new FileReader( files.get(currentFile) ));
			} catch(IOException e) {
				throw new RuntimeException(e);}
	
			buffer = new ArrayList<Tree<String>>();
	
			endOfFilesReached = false;
			
			try {
				fillBuffer(); }
			catch (IOException e) {
				throw new RuntimeException(e); }
		}
	
		/**
		 * Returns the next tree in the corpus.
		 *
		 * @throws NoSuchElementException
		 */
		public Tree<String> next()
		{
			while(buffer.size() < buffermax && !endOfFilesReached)
				try { 
					fillBuffer(); }
				catch (IOException e) {
					throw new RuntimeException(e); }
	
			if(buffer.size() == 0)
				throw new NoSuchElementException("No More sentences in corpus");
	
			Tree<String> result = buffer.remove(0);
	
			return result;
		}
	
		/**
		 * Returns whether the corpus can return more tokens
		 */
		public boolean hasNext()
		{
			while(buffer.size() < buffermax && !endOfFilesReached)
				try { 
					fillBuffer(); }
				catch (IOException e) {
					throw new RuntimeException(e); }
			
			return !(endOfFilesReached && buffer.size() == 0);
		}
	
		/**
		 * Parses a tree from the corpus and adds it to the buffer.
		 *
		 * If there are no lines left in the corpus, it simply returns.
		 * Note that it adds the "top -> XP" rule last, signifying the end of the sentence
		 */
		private void fillBuffer() throws IOException
		{
			if(endOfFilesReached)
				return;
	
			Tree<String> tree = readTree(in);
			
			if(tree != null)
			{
				if(removePunctuation) 
					removePunctuation(tree);			
				buffer.add(tree);
			} else
			{
				if(currentFile == files.size() - 1)
					endOfFilesReached = true;
				else 
					in = new BufferedReader(new FileReader(files.get(++currentFile)));
			}
		}
	}
	
	public String toString()
	{
		return "WSJCorpus based on files: " + files;
	}
	
	/**
	 * Parses a tree from a string
	 */
	public static Tree<String> parseTree(String in)
	{
		Tree<String> tree = null;
		try{
			tree = readTree(new StringReader(in));
		} catch (IOException e)
		{
			//* StringReader won't throw an IOException
			e.printStackTrace();
			System.exit(1);
		}
		
		return tree;
	}
	
	/**
	 * Read a tree from the provided reader
	 * 
	 * @returns A tree, parsed from the reader's output. null if no tree was 
	 * 	found.  
	 * @throws 	RuntimeException If the beginning of a tree was found (ie. an 
	 * 			opening	parenthesis), but it could not be parsed
	 **/
	public static Tree<String> readTree(Reader in)
	throws IOException
	{
		Tree<String> tree = new Tree<String>(null);
		
		//* Read past the opening parenthesis
		int character = in.read();
		while((char)character != '(' && character != -1) character = in.read();
		
		if(character == -1)
			return null;
		
		readSymbol(tree.getRoot(), in);
		postprocess(tree.getRoot());
		
		Tree<String> newTree = new Tree<String>(null); 
		removeEmptyNodes(tree.getRoot(), newTree.getRoot());
		
		return newTree;
	}
	
	/**
	 * Reads the current symbol from the Reader, and puts it into the 
	 * provided treenode. creates children as necessary.
	 * 
	 * The reader should be just past the '(' character.
	 */
	private static void readSymbol(Tree<String>.Node node, Reader in)
		throws IOException
	{
		int character = in.read();
		StringBuilder symbol = new StringBuilder();
		while(true)
		{
			if(character == -1)			throw new RuntimeException("Parsing Error");
			if((char) character == ')')	break;
			if((char) character == '(')	break;
			
			symbol.append((char) character);
			
			character = in.read();
		}
		
		node.setValue(symbol.toString());
		
		while(true)
		{
			if(character == -1)			throw new RuntimeException("Parsing Error");
			if((char)character == '(')
				readSymbol(node.addChild(null), in);
			
			if((char)character == ')')
				return;
			
			character = in.read();
		}
	}
	
	private static void postprocess(Tree<String>.Node node)
	{
		String value = node.getValue();
		value = value.trim();
		
		if(node.isLeaf())
		{
			//* split around white space (with a maximum of two tokens)
			String[] split = value.split("[\\s]*\\s", 2);
			
			String parent = split[0];
			String child  = split.length > 1 ? split[1] : null;
			
			if(child != null)
				postprocess(node.addChild(child));
			else 
				parent = parent.toLowerCase();
			
			node.setValue(parent.trim());
		}else
		{
			node.setValue(value);
			for(Tree<String>.Node child : node.getChildren())
				postprocess(child);
		}
	}
	
	public static void removePunctuation(Tree<String> tree)
	{
		while(removeFirstPunctuation(tree.getRoot()))
		; //* Not a bug 
	}

	/**
	 * Removes the first punctuation node found. returns true if a node was 
	 * removed.
	 */
	private static boolean removeFirstPunctuation(Tree<String>.Node node)
	{
		if( PUNCTUATION.indexOf(node.getValue()) != -1 || 
				node.getValue().startsWith("*") ||
				(node.getValue().startsWith("-") && ! node.isLeaf()))
		{
			removeNode(node);
			return true;
		}
		
		for(Tree<String>.Node child : node.getChildren())
			if(removeFirstPunctuation(child)) return true;
		
		return false;
	}
	
	private static void removeEmptyNodes(Tree<String>.Node nodeIn, Tree<String>.Node nodeOut)
	{
		if (nodeIn.getValue() != null && ! nodeIn.getValue().trim().equals(""))
		{
//System.out.println("a _" +nodeIn.getValue()+ "_");
			nodeOut.setValue(nodeIn.getValue());
			for (Tree<String>.Node childIn : nodeIn.getChildren())
			{
				Tree<String>.Node childOut = nodeOut.addChild(null);
				removeEmptyNodes(childIn, childOut);
			}
		} else if (nodeIn.isRoot())
		{
//System.out.println("b_" +nodeIn.getValue()+ "_");
			if(nodeIn.getChildren().size() > 1)
				throw new RuntimeException("Cannot remove empty root-node. It has more than one child.");
			if(nodeIn.getChildren().size() == 0)
				throw new RuntimeException("Cannot remove empty root-node. It has no children.");
			
			removeEmptyNodes( nodeIn.getChildren().get(0), nodeOut);
		} else
		{
//System.out.println("c_" +nodeIn.getValue()+ "_");
			
			boolean first = true;
			Tree<String>.Node parent = nodeOut.getParent();
			for(Tree<String>.Node childIn : nodeIn.getChildren())
			{
				if(first)
				{
					removeEmptyNodes(childIn, nodeOut);
					first = false;
				} else
				{
					Tree<String>.Node childOut = parent.addChild(null);
					removeEmptyNodes(childIn, childOut);
				}
			}
		}
	}
	
	/**
	 * Removes a node from its tree, and any parent of it, so long as that 
	 * parent has no other children.
	 */
	private static void removeNode(Tree<String>.Node node)
	{
		Tree<String>.Node parent = node.getParent();
		node.remove();
		while(parent.getChildren().size() == 0 && ! parent.isRoot())
		{
			node = parent;
			parent = node.getParent();
			node.remove();
		}
	}	
}