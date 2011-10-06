package org.lilian.corpora.wrappers;

import java.util.*;
import java.io.*;

import org.lilian.corpora.*;
import org.lilian.util.*;
import org.lilian.util.trees.Tree;

/**
 * This class converts a treecorpus to Chomsky Normal Form (CNF). 
 * 
 * In CNF, each node has precisely two children, with the exception of the 
 * parent nodes of the terminals (the POS-nodes), who have just one child.
 * 
 * A node with more than two children is converted to CNF by taking all its
 * but the leftmost, and making them children under a new temporary node, with a
 * unique symbol. Afterwards, this unique symbol can then remover, moving its 
 * children one node up to recreate the original tree.
 * 
 * NOTES & TODO:
 * <ul>
 * <li />	Create a second FromCNFWrapper that reconstructs sentences from this
 * 			kind of corpus.
 * <li />	For non-POS nodes with a single child, this node is removed. This 
 * 			operation cannot be undone perfectly. There may be a better way to 
 * 			do this, but there may also not be.
 * <li />	The TempConstituents are not guaranteed unique across sessions. If 
 * 			the JVM is restarted, then the number is restarted as well. 
 * </ul>
 */

public class ToCNF<T> extends 
	CorpusWrapper<Tree<ToCNF.Token<T>>, Tree<T>> 
{
	private static int tempTokens = 0;
	private boolean collapseUnary = true;
	
	public ToCNF(Corpus<Tree<T>> master)
	{
		super(master);		
	}
	
	public ToCNF(Corpus<Tree<T>> master, boolean collapseUnary)
	{
		super(master);		
		this.collapseUnary = collapseUnary;
	}
	
	public Iterator<Tree<Token<T>>> iterator()
	{
		return new TCNFIterator();
	}
	
	private class TCNFIterator
		extends WrapperIterator
	{
	
		public Tree<ToCNF.Token<T>> next()

		{
			return toCNF(masterIterator.next(), collapseUnary);
		}
		
	}
	
	public static abstract class Token<T>
	{
	}
	
	public static class RegularToken<T> extends Token<T>
	{
		T value;

		public RegularToken(T value)
		{
			this.value = value;
		}

		public T getValue()
		{
			return value;
		}

		public int hashCode()
		{
			return value.hashCode();
		}

		public String toString()
		{
			return value.toString();
		}

		public boolean equals(Object o)
		{
			return (o instanceof ToCNF.RegularToken) &&
			       ((RegularToken)o).value.equals(this.value);
		}
	}
	
	public static class TempToken<T> extends Token<T>
	{
		int number;
		String stringRep;

		public TempToken()
		{
			number = ++tempTokens;
			stringRep = "_X" + number;
		}

		public int hashCode()
		{
			return stringRep.hashCode();
		}

		public String toString()
		{
			return stringRep;
		}

		public boolean equals(Object o)
		{
			return (o instanceof ToCNF.TempToken) &&
			       ((TempToken)o).stringRep.equals(this.stringRep);
		}		
	}
	
	/**
	 * Converts a regular tree to a tree in Chomsky Normal Form containing
	 * Tokens rather than plain T's.
	 * 
	 * @param tree
	 * @param collapseUnary Whether to collapse unary production rules
	 */
	public static <T> Tree<ToCNF.Token<T>> toCNF(
			Tree<T> tree, 
			boolean collapseUnary)
	{
		Tree<Token<T>> result = new Tree<Token<T>>(
			new RegularToken<T>(tree.getRoot().getValue()));

		toCNF(result.getRoot(), tree.getRoot().getChildren(), collapseUnary);

		return result;
	}

	/**
	 * The list Children contains the children to be added to the node out in 
	 * CNF.
	 *  
	 * @param out
	 * @param children
	 * @param collapseUnary
	 */
	private static <T> void toCNF(Tree<Token<T>>.Node out, 
			List<Tree<T>.Node> children, 
			boolean collapseUnary)
	{
		/* No children: end the recursion. */
		if (children == null || children.size() == 0)
			return;

		//* Single child. 
		if (children.size() == 1) {
			if (children.get(0).isLeaf())
			{
				out.addChild(new RegularToken<T>(children.get(0).getValue()));
				return;
			} else if(collapseUnary) //* unary production rule, skip the node
			{
				toCNF(out, children.get(0).getChildren(), collapseUnary);
			} else
			{
				Tree<Token<T>>.Node child = out.addChild(
						new RegularToken<T>(children.get(0).getValue()));
				
				toCNF(child, children.get(0).getChildren(), collapseUnary);				
			}
		} else if (children.size() == 2)
		{
			Tree<Token<T>>.Node child1 = out.addChild(
				new RegularToken<T>(children.get(0).getValue()));
			Tree<Token<T>>.Node child2 = out.addChild(
				new RegularToken<T>(children.get(1).getValue()));

			toCNF(child1, children.get(0).getChildren(), collapseUnary);
			toCNF(child2, children.get(1).getChildren(), collapseUnary);
		} else /* more than two children, we need temporary constituents */
		{
			/* We take the first child normally */
			Tree<Token<T>>.Node child1 = out.addChild(
				new RegularToken<T>(children.get(0).getValue()));
			/* For the rest, we create a new temp constituent */
			TempToken<T> t = new TempToken<T>();
			Tree<Token<T>>.Node child2 = out.addChild(t);

			toCNF(child1, children.get(0).getChildren(), collapseUnary);
			toCNF(child2, children.subList(1, children.size()), collapseUnary);
		}
	}

	/**
	 * Takes a parse tree and returns the tree with temp constituents removed, and
	 * the constituent objects turned into basic tokens.
	 *
	 * @param in	A CNF parse tree (ie. a tree with temporary constituents)
	 * @return A Tree with the Tokens unwrapped and TempTokens removed (collapsed)
	 */
	public static <T> Tree<T> reconstruct(Tree<Token<T>> in)
	{
		RegularToken<T> inRoot = null;

		if (in.getRoot().getValue() instanceof TempToken)
			throw new IllegalArgumentException("Tree cannot have TempToken for root");
		else if (in.getRoot().getValue() instanceof ToCNF.RegularToken)
			inRoot = (RegularToken<T>) in.getRoot().getValue();
		else
			throw new IllegalArgumentException("Tree has unknown kind of token as root");
			
		Tree<T> out = new Tree<T>(inRoot.getValue());

		for (Tree<Token<T>>.Node child : in.getRoot().getChildren())
			reconstruct(child, out.getRoot());

		return out;
	}

	/**
	 * Takes a treenode from a tree of constituents, and adds the token from the
	 * constituent to a new treenode, which is a child of the parameter out.
	 *
	 * If the treenode in contains a temporary constituent (which doesn't
	 * represent a T), then the temporary constituent is folded up and
	 * removed.
	 */
	private static <T> void reconstruct(Tree<Token<T>>.Node in, Tree<T>.Node out)
	{
		List<Tree<Token<T>>.Node> inChildren = in.getChildren();

		Tree<T>.Node outChild;

		Token<T> token = in.getValue();

		if (token instanceof ToCNF.TempToken)
		{
			/* If the current out node is a temp constituent, add its children to
			 * the current node of the regular tree */
			for (Tree<Token<T>>.Node child : inChildren)
				reconstruct(child, out);
		} else
		{
			/* Create a new node in the out tree and recurse with that.
			 */
			outChild = out.addChild( ((RegularToken<T>)token).getValue());
			for(Tree<Token<T>>.Node child : inChildren)
				reconstruct(child, outChild);
		}
	}
}
