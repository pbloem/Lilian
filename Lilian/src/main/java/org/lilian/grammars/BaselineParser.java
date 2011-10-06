package org.lilian.grammars;

import java.util.*;
import java.io.*;

import org.lilian.grammars.*;
import org.lilian.util.*;
import org.lilian.util.trees.Tree;
import org.lilian.util.trees.Tree.Node;
import org.lilian.*;

/**
 * A parser that always constructs a simple kind of parse tree.
 * 
 * The available options are left branching, right branching, and random 
 * branching (different for every time parse() is called).
 * 
 * 
 */
public class BaselineParser<T> implements Parser<T>
{
	public enum Mode {LEFT_BRANCHING, RIGHT_BRANCHING, RANDOM}
	
	private Mode mode = Mode.LEFT_BRANCHING;
	private T tempSymbol;
	
	public BaselineParser(T tempSymbol)
	{
		this.tempSymbol = tempSymbol;
	}
	
	public BaselineParser(Mode mode, T tempSymbol)
	{
		this(tempSymbol);
		this.mode = mode;
	}
	
	public Parse<T> parse(Collection<? extends T> sentence)
	{
		return new BaselineParse(sentence);
	}
	
	private class BaselineParse implements Parse<T>
	{
		private int n;
		private Tree<T> parse;
		
		private Pair<Tree<T>, Double> pair;
		private ArrayList<Pair<Tree<T>, Double>> parses;
		
		public BaselineParse(Collection<? extends T> sentenceIn)
		{
		
			LinkedList<T> sentence = new LinkedList<T>();
			sentence.addAll(sentenceIn);
					
			n = sentence.size();					
			
			//* Create the structure of the tree
			
			parse = new Tree<T>(tempSymbol);
			int leavesSize = 1;
			while(leavesSize < n)
			{
				branchNode(parse.getRoot());
				leavesSize++;
			}
			
			//* Add the sentence tokens to the leaf nodes
			
			LinkedList<Tree<T>.Node> leafNodes = new LinkedList<Tree<T>.Node>();
			Iterator<Tree<T>.Node> it = parse.iterator();
			while(it.hasNext())
			{
				Tree<T>.Node node = it.next();
				if(node.isLeaf())
					leafNodes.add(node);
			}
			
			Iterator<T> tokenIt = sentence.iterator();
			Iterator<Tree<T>.Node> nodeIt = leafNodes.iterator();
			while(tokenIt.hasNext())
			{
				nodeIt.next().setValue(tokenIt.next());
			}
			
			pair = new Pair<Tree<T>, Double>(parse, 1.0);
			parses = new ArrayList<Pair<Tree<T>, Double>>(1);
			parses.add(pair);
		}

		/** 
		 * Branches a node in this subtree 
		 * 
		 */
		private void branchNode(Tree<T>.Node node)
		{
			if(node.isLeaf())
			{
				node.addChild(tempSymbol);
				node.addChild(tempSymbol);
			} else
			{
				switch (mode) {
				case LEFT_BRANCHING:
					branchNode(node.getChildren().get(0));
					break;
				case RIGHT_BRANCHING:
					branchNode(node.getChildren().get(1));					
					break;
				case RANDOM:
					double draw = Global.random.nextDouble();
					if(draw < 0.5) 	branchNode(node.getChildren().get(0));
					else 			branchNode(node.getChildren().get(1));
					break;
				}
			}
		}

		@Override
		public Collection<Pair<Tree<T>, Double>> allParses()
		{
			return parses;			
		}

		@Override
		public Pair<Tree<T>, Double> bestParse()
		{
			return pair;
		}

		@Override
		public boolean isMember()
		{
			return true;
		}

		@Override
		public void write(File directory, String base) throws IOException
		{
		}
		
	}
	
	public static void main(String[] args)
	{
		BaselineParser<String> b = new BaselineParser<String>(Mode.RANDOM, "Y");  
		
		System.out.println(b.parse(Functions.sentence("A B C D")).bestParse().getFirst());
		System.out.println(b.parse(Functions.sentence("A B C D")).bestParse().getFirst());
		System.out.println(b.parse(Functions.sentence("A B C D")).bestParse().getFirst());
		System.out.println(b.parse(Functions.sentence("A B C D")).bestParse().getFirst());		
	}
}
