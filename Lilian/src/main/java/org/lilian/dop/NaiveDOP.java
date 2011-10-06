package org.lilian.dop;

import java.io.*;
import java.util.*;

import org.lilian.corpora.*;
import org.lilian.util.*;
import org.lilian.util.trees.DOPTree;
import org.lilian.util.trees.Tree;

/**
 * A naive implementation of the DOP-1 model which requires a humongous amount
 * of memory.
 *
 * @author Stephan Schroevers
 */
public class NaiveDOP<T>
{
	/**
	 * Dictionary that keeps track of the roots of all subtrees and their
	 * counts.
	 */
	protected final Map<T, Double> roots = new HashMap<T, Double>();
	/**
	 * Dictionary that keeps track of all subtrees and their counts.
	 */
	protected final Map<DOPTree<T>, Double> subtrees = new HashMap<DOPTree<T>, Double>();

	/**
	 * Creates an empty DOP-1 model.
	 *
	 * Note that the resulting <code>NaiveDOP</code> instance must still be
	 * trained, for otherwise it would assign probability 0 to every tree.
	 */
	public NaiveDOP()
	{
	}

	/**
	 * Creates a DOP-1 model that is trained on the given corpus.
	 *
	 * @param   corpus  collection of trees
	 */
	public NaiveDOP(final Corpus<Tree<T>> corpus)
	{
		this.add(corpus);
	}

	/**
	 * Uses the subtrees in the given corpus to update the DOP-1 model.
	 *
	 * @param   corpus  collection of trees
	 */
	public void add(final Corpus<Tree<T>> corpus) 
	{
		for(Tree<T> tree : corpus)
			this.add(tree);
	}

	// FIXME: Eventually DOPTree and Tree must be combined, then either of the two methods below can go

	/**
	 * Uses the subtrees of the given tree to update the DOP-1 model.
	 *
	 * @param  tree  tree from which to extract the subtrees
	 */
	public void add(final Tree<T> tree)
	{
		this.add(convertTree(tree.getRoot()));
	}

	/**
	 * Extracts the subtrees of the given tree to update the DOP-1 model.
	 *
	 * @param  tree  tree from which to extract the subtrees
	 */
	public void add(final DOPTree<T> tree)
	{
		/*
		 * Iterate over all subtrees, and store them, one by one. A count of
		 * the subtrees as well as their root nodes must be kept.
		 */
		for (DOPTree<T> sub : tree) {
			this.roots.put(sub.root, this.roots.containsKey(sub.root)
			                             ? this.roots.get(sub.root) + 1 : 1);
			this.subtrees.put(sub, this.subtrees.containsKey(sub)
			                           ? this.subtrees.get(sub) + 1 : 1);
		}
	}

	/**
	 * Calculates the DOP-1 probability of the given tree.
	 *
	 * @param  tree  the tree whose probability must be calculated
	 * @return       the given tree's probability
	 */
	public double probability(final DOPTree<T> tree)
	{
		/* Accumulated probability of the tree.*/
		double totalProb = 0.0;
		/* Probability for a specific subtree. */
		double prob = 0.0;
		/* List to store unmatched parts of the tree. */
		List<DOPTree<T>> remaining;

		/* Try to match each known subtree with the given tree. */
		for (DOPTree<T> subtree : this.subtrees.keySet()) {
			remaining = new ArrayList<DOPTree<T>>();

			if (tree.startsWith(subtree, remaining)) {
				/*
				 * Calculate the probability that this specific subtree occurs.
				 */
				prob = this.subtrees.get(subtree) /
				           this.roots.get(subtree.root);

				/*
				 * Multiply by the DOP-1 probabilities of the parts of the tree
				 * that are not matched by the current subtree.
				 */
				for (DOPTree<T> remSubTree : remaining) {
					prob *= this.probability(remSubTree);
				}

				totalProb += prob;
			}
		}

		return totalProb;
	}

	// FIXME: Eventually DOPTree and Tree must be combined, then convertTree() can go

	/**
	 * Converts a tree modelled by {@link org.lilian.util.trees.Tree.Node} to
	 * a {@link DOPTree}.
	 *
	 * @param  node  root node of the tree that must be converted
	 * @return       a <code>DOPTree</code> that is isomorphic to the tree
	 *               rooted by <code>node</code>
	 */
	private DOPTree<T> convertTree(final Tree<T>.Node node)
	{
		final DOPTree<T> tree = new DOPTree<T>(node.getValue());

		/* Recursively convert all child nodes. */
		for (Tree<T>.Node child : node.getChildren()) {
			tree.children.add(this.convertTree(child));
		}

		return tree;
	}

}
