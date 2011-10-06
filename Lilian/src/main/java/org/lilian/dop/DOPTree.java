package org.lilian.dop;

import java.util.*;

// IMPORTANT: If this class is ever made non-final, then the method
// equals(DOPTree) must be updated!

/**
 * This class models a syntax tree that can be used for Data Oriented Parsing.
 *
 * Specifically, <code>DOPTree</code> instances allow easy iteration over all
 * non-trivial subtrees.
 *
 * @author Stephan Schroevers
 */
public final class DOPTree<T> implements Iterable<DOPTree<T>>
{
	/**
	 * Expected maximum number of children for each node. Used to reduce the
	 * memory footprint of the program.
	 */
	public static final int EXP_MAX_CHILDREN = 4;

	/**
	 * The root node of the tree.
	 */
	public T root = null;
	/**
	 * Left-to-right list of the children under the root node.
	 */
	public List<DOPTree<T>> children = null;

	/**
	 * Constructs a tree consisting of the single given node.
	 *
	 * @param  root  the root node
	 */
	public DOPTree(final T root)
	{
		this(root, new ArrayList<DOPTree<T>>(EXP_MAX_CHILDREN));
	}

	/**
	 * Constructs a tree with the given node as its root, and any given trees
	 * as the root node's children.
	 *
	 * @param  root      the root node
	 * @param  children  child nodes of the given root node
	 */
	public DOPTree(final T root, final List<DOPTree<T>> children)
	{
		this.root = root;
		this.children = children;
	}

	/**
	 * Indicates whether this tree starts with some other tree.
	 *
	 * A tree <code>a</code> starts with a tree <code>b</code> iff they have
	 * identical roots <em>and either</em> <code>b</code> consists of just one
	 * node, <em>or</em> <code>a</code>'s and <code>b</code>'s root node have
	 * equally many children, such that the <code>i</code>th child of
	 * <code>a</code>'s root node starts with the <code>i</code>th child of
	 * <code>b</code>'s root node, for all <code>i</code>.
	 *
	 * @param  other  the tree of of which to test that is constitutes the
	 *                start of this tree
	 * @return        <code>true</code> if this tree starts with
	 *                <code>other</code>, <code>false</code> otherwise
	 */
	public boolean startsWith(final DOPTree<T> other)
	{
		return this.startsWith(other, null);
	}

	/**
	 * Indicates whether this tree starts with some other tree.
	 *
	 * A tree <code>a</code> starts with a tree <code>b</code> iff they have
	 * identical roots <em>and either</em> <code>b</code> consists of just one
	 * node, <em>or</em> <code>a</code>'s and <code>b</code>'s root node have
	 * equally many children, such that the <code>i</code>th child of
	 * <code>a</code>'s root node starts with the <code>i</code>th child of
	 * <code>b</code>'s root node, for all <code>i</code>.
	 *
	 * If the given tree <code>other</code> does not coincide with this tree,
	 * then the subtrees of this tree that are rooted at the leaves of
	 * <code>other</code> are returned in the list <code>remaining</code>, if
	 * it is not <code>null</code>.
	 *
	 * @param  other      the tree of of which to test that is constitutes the
	 *                    start of this tree
	 * @param  remaining  list to be filled with subtrees of this tree which
	 *                    were not matched by <code>other</code>, if not
	 *                    <code>null</code>
	 * @return            <code>true</code> if this tree starts with
	 *                    <code>other</code>, <code>false</code> otherwise
	 */
	public boolean startsWith(final DOPTree<T> other,
	                          final List<DOPTree<T>> remaining)
	{
		final int childAmt = other.children.size();

		/* Check whether the roots are identical. */
		if (!this.root.equals(other.root)) {
			return false;
		}

		/*
		 * If the other tree consists of just a root node, then it is a subtree
		 * of this tree.
		 */
		if (childAmt == 0) {
			/*
			 * If the other tree does not fully overlap with this tree, then
			 * register the non-overlapping subtree.
			 */
			if (remaining != null && this.children.size() != 0) {
				remaining.add(this);
			}

			return true;
		}

		/*
		 * If the other tree's root node has a positive but different number of
		 * children, then it cannot be a subtree in the sense of DOP.
		 */
		if (childAmt != this.children.size()) {
			return false;
		}

		/* Recursively test that the children match up. */
		for (int i = 0; i < childAmt; i++) {
			if (!this.children.get(i).startsWith(other.children.get(i), remaining)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Indicates whether some object is equal to this tree.
	 *
	 * Two trees are equal iff they have an identical (equal) root node with
	 * equally many children, such that the <code>i</code>th child of the first
	 * tree's root node is equal to the <code>i</code>th child of the second
	 * tree's root node, for all <code>i</code>.
	 *
	 * @return  <code>true</code> if <code>obj</code> is identical to this
	 *          tree, <code>false</code> otherwise
	 */
	@Override
	public boolean equals(final Object obj)
	{
		final int childAmt;
		final DOPTree other;

		/* If the object is of the wrong type, do not even consider it. */
		if (!(obj instanceof DOPTree)) {
			return false;
		}

		other = (DOPTree)obj;
		childAmt = other.children.size();

		/* Check whether the roots are identical. */
		if (!this.root.equals(other.root) || childAmt != this.children.size()) {
			return false;
		}

		/* Recursively test whether the children are identical. */
		for (int i = 0; i < childAmt; i++) {
			if (!this.children.get(i).equals(other.children.get(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns a hash code value for this tree.
	 *
	 * @return  a hash code calculated over this tree's root node and all its
	 *          children
	 */
	@Override
	public int hashCode()
	{
		int hash = 217 + this.root.hashCode();

		for (DOPTree<T> child : this.children) {
			hash = hash * 31 + child.hashCode();
		}

		return hash;
	}

	/**
	 * Returns an iterator over all subtrees of this tree that contain two or
	 * more nodes.
	 *
	 * @return  an iterator over all non-trivial subtrees of this tree
	 */
	@Override
	public Iterator<DOPTree<T>> iterator()
	{
		return iterator(false);
	}

	/**
	 * Returns an iterator over subtrees of this tree.
	 *
	 * If <code>topTreesOnly</code> is <code>true</code>, then the root node of
	 * this tree will be the <em>first</em> subtree returned by the iterator,
	 * followed by all proper subtrees that are rooted by this tree's root
	 * node. No other subtrees are yielded.
	 *
	 * If <code>topTreesOnly</code> is <code>false</code>, then <em>all</em>
	 * proper subtrees will be returned.
	 *
	 * @param  topTreesOnly  indicate what kind of subtrees to iterate over
	 * @return               an iterator over subtrees of this tree
	 */
	public Iterator<DOPTree<T>> iterator(final boolean topTreesOnly)
	{
		return new TreeIterator<T>(this, topTreesOnly);
	}

	/**
	 * Returns a string representation of the tree.
	 *
	 * @return  a string representation of the tree
	 */
	@Override
	public String toString()
	{
		final int childAmt = this.children.size();
		String res = "(" + this.root + ", [";

		/* Add first child, if present. */
		if (childAmt > 0) {
			res += this.children.get(0);
		}

		/* Add remaining children, if any. */
		for (int i = 1; i < childAmt; i++) {
			res += "," + this.children.get(i);
		}

		return res + "])";
	}

	/**
	 * Iterator over subtrees of a given tree.
	 *
	 * This iterator supports two modes:
	 * <ul>
	 *   <li>
	 *     Iteration over all proper subtrees (subtrees containing two or more
	 *     nodes).
	 *   </li>
	 *   <li>
	 *     Iteration over all proper subtres that are rooted at the tree's root
	 *     node <em>and</em> the single trivial subtree which consists of just
	 *     the tree's root node.
	 *   </li>
	 * </ul>
	 */
	private final class TreeIterator<T> implements Iterator<DOPTree<T>>
	{
		/**
		 * Indicates which set of subtrees to iterate over.
		 */
		private final boolean topTreesOnly;
		/**
		 * The tree of which the subtrees must be returned.
		 */
		private final DOPTree<T> tree;
		/**
		 * Iterator over the children of this tree's root node.
		 */
		private final Iterator<DOPTree<T>> childrenIter;
		/**
		 * Iterator over all possible combinations of subtrees of the trees
		 * rooted by the children of this tree's root node.
		 */
		private final Iterator<List<DOPTree<T>>> childrenCombIter;

		/**
		 * The next subtree to return.
		 */
		private DOPTree<T> nextTree = null;
		/**
		 * Iterator over subtrees of one of the trees rooted by one of the
		 * children of this tree's root node.
		 */
		private Iterator<DOPTree<T>> childIter = null;

		/**
		 * Creates an iterator over all non-trivial subtrees of the given tree.
		 *
		 * @param  tree  the tree over which to iterate
		 */
		private TreeIterator(final DOPTree<T> tree)
		{
			this(tree, false);
		}

		/**
		 * Creates an iterator over subtrees of the given tree.
		 *
		 * If <code>topTreesOnly</code> is <code>true</code>, then the root
		 * node of this tree will be the <em>first</em> subtree returned by the
		 * iterator, followed by all proper subtrees that are rooted by this
		 * tree's root node. No other subtrees are yielded.
		 *
		 * If <code>topTreesOnly</code> is <code>false</code>, then
		 * <em>all</em> proper subtrees will be returned.
		 *
		 * @param  tree          the tree over which to iterate
		 * @param  topTreesOnly  indicate what kind of subtrees to iterate over
		 */
		private TreeIterator(final DOPTree<T> tree, final boolean topTreesOnly)
		{
			this.tree = tree;
			this.childrenIter = tree.children.iterator();
			this.childrenCombIter = new CombinationIterator<T>(tree.children);
			this.topTreesOnly = topTreesOnly;

			/*
			 * The subtree consisting of just the tree's root node is the only
			 * trivial subtree that may be returned. Schedule it now, and be
			 * done with it.
			 */
			if (topTreesOnly) {
				this.nextTree = new DOPTree<T>(tree.root);
			}
		}

		/**
		 * Tells whether the iteration has more subtrees.
		 *
		 * @return  <code>false</code> if a call to {@link #next()} would yield
		 *          a {@link java.util.NoSuchElementException},
		 *          <code>true</code> otherwise
		 */
		@Override
		public boolean hasNext()
		{
			/*
			 * It may be that the constructor, or a previous call to hasNext()
			 * already constructed the next subtree.
			 */
			if (this.nextTree != null) {
				return true;
			}

			try {
				/*
				 * If creation of the next subtree succeeds, then store it,
				 * since we still need to return it through next().
				 */
				this.nextTree = next();
			}
			catch (NoSuchElementException ex) {
				return false;
			}

			return true;
		}

		/**
		 * Returns the next subtree in the iteration.
		 *
		 * @return  the next subtree
		 * @throws  java.util.NoSuchElementException
		 *              if no subtrees remain
		 */
		@Override
		public DOPTree<T> next()
		{
			DOPTree<T> res = this.nextTree;

			/*
			 * It may be that the constructor, or a call to hasNext() already
			 * constructed the next subtree.
			 */
			if (res != null) {
				this.nextTree = null;
				return res;
			}

			if (!this.topTreesOnly) {
				/*
				 * First, iterate over all subtrees of the trees rooted by one
				 * of the children of this tree's root.
				 */
				if (this.childIter != null && this.childIter.hasNext()) {
					return this.childIter.next();
				}

				/* Try to advance to the next child of this tree's root. */
				if (this.childrenIter.hasNext()) {
					this.childIter = this.childrenIter.next().iterator(false);

					return this.next();
				}
			}

			/*
			 * Now that all subtrees of the trees rooted by one of the children
			 * of this tree's root are iterated, we start iterating subtrees
			 * rooted by this tree's root.
			 */
			if (!this.childrenCombIter.hasNext()) {
				throw new NoSuchElementException();
			}

			return new DOPTree<T>(this.tree.root, this.childrenCombIter.next());
		}

		/**
		 * Throws an {@link java.lang.UnsupportedOperationException}.
		 *
		 * @throws  java.lang.UnsupportedOperationException
		 */
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * Iterator over subtree combinations.
		 *
		 * More precisely, given a list of trees of size <code>n</code>, this
		 * iterator returns all lists of trees of size <code>n</code> such that
		 * the tree in position <code>i</code> is a subtree of the tree that
		 * was in position <code>i</code> in the original list, for all
		 * <code>i</code>, with the additional restriction that only subtrees
		 * rooted at the root of the original tree are considered.
		 */
		private final class CombinationIterator<T>
		    implements Iterator<List<DOPTree<T>>>
		{
			/**
			 * Iterator over the subtrees of the first tree in the list.
			 */
			private final Iterator<DOPTree<T>> firstTreeIter;
			/**
			 * List of trees, minus the first one.
			 */
			private final List<DOPTree<T>> remTrees;

			/**
			 * Iterator over the combinations of subtrees, where the first tree
			 * in the list is excluded.
			 */
			private CombinationIterator<T> remTreesIter = null;
			/**
			 * Subtree of the first tree that is currently used to create
			 * combinations.
			 */
			private DOPTree<T> curFirstElem = null;

			/**
			 * Creates an iterator over certain combinations of subtrees of the
			 * trees in the given list.
			 *
			 * For a more elaborate explaination, consult the {@link
			 * CombinationIterator class description}.
			 *
			 * @param  trees  the list of trees for which to generate the
			 *                subtree combinations
			 */
			private CombinationIterator(final List<DOPTree<T>> trees)
			{
				final int noTrees = trees.size();

				this.firstTreeIter = (noTrees > 0)
				                         ? trees.get(0).iterator(true) : null;
				this.remTrees = (noTrees > 1)
				                    ? trees.subList(1, noTrees) : null;
			}

			/**
			 * Tells whether the iteration has more subtree combinations.
			 *
			 * @return  <code>false</code> if a call to {@link #next()} would
			 *          yield a {@link java.util.NoSuchElementException},
			 *          <code>true</code> otherwise
			 */
			@Override
			public boolean hasNext()
			{
				return (this.remTreesIter != null && this.remTreesIter.hasNext()) ||
				       (this.firstTreeIter != null && this.firstTreeIter.hasNext());
			}

			/**
			 * Returns the next combination of subtrees in the iteration.
			 *
			 * @return  the next combination of subtrees
			 * @throws  java.util.NoSuchElementException
			 *              if no combination of subtrees remains
			 */
			@Override
			public List<DOPTree<T>> next()
			{
				List<DOPTree<T>> nextComb = null;

				/*
				 * If all combinations of subtrees in place 2 and higher in the
				 * list have been exhausted, then it is time to move on to the
				 * next subtree of the first tree in the list, after which it
				 * can be combined with all combinations for the remaining
				 * trees.
				 */
				if (this.remTreesIter == null || !this.remTreesIter.hasNext()) {
					if (this.firstTreeIter == null || !this.firstTreeIter.hasNext()) {
						throw new NoSuchElementException();
					}

					this.curFirstElem = this.firstTreeIter.next();

					/*
					 * It may be that there is only one tree over which to
					 * iterate.
					 */
					if (this.remTrees != null) {
						this.remTreesIter = new CombinationIterator<T>(this.remTrees);
					}
				}

				/*
				 * Get the next combination of subtrees for the second and
				 * further trees in the list.
				 */
				nextComb = (this.remTreesIter != null)
				               ? this.remTreesIter.next()
				               : new ArrayList<DOPTree<T>>(EXP_MAX_CHILDREN);
				/* Combine with a subtree from the first tree in the list. */
				nextComb.add(0, this.curFirstElem);

				return nextComb;
			}

			/**
			 * Throws an {@link java.lang.UnsupportedOperationException}.
			 *
			 * @throws  java.lang.UnsupportedOperationException
			 */
			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		}
	}
}
