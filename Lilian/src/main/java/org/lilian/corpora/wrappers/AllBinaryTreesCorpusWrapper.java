package org.lilian.corpora.wrappers;

import java.io.*;
import java.util.*;

import org.lilian.util.*;
import org.lilian.util.trees.Tree;
import org.lilian.corpora.*;

/**
 *
 * TODO:
 * <ul>
 * 	<li/> 	Allow the user to define a separate rootSymbol ("S") for the generated
 * 			trees	
 * </ul>
 *
 * @author Stephan Schroevers
 */
public class AllBinaryTreesCorpusWrapper<T> 
	extends CorpusWrapper<Tree<T>, T>
{
	private static final int BUFFER_SIZE = 2;
	private static final int EXP_MAX_SENTENCE_SIZE = 100;
	protected T filler = null;	

	private static ArrayList<Long> catalanNumbers =
	    new ArrayList<Long>(EXP_MAX_SENTENCE_SIZE);
	
	protected SequenceCorpus<T> master;

	public AllBinaryTreesCorpusWrapper(SequenceCorpus<T> master, T filler) throws IOException
	{
		super(master);
		this.master = master; 

		assert (BUFFER_SIZE > 1);

		this.filler = filler;
	}
	
	@Override
	public Iterator<Tree<T>> iterator() {
		return new ABTIterator();
	}

	public class ABTIterator
		extends WrapperIterator
	{
		protected LinkedList<ArrayList<T>> sentences = new LinkedList<ArrayList<T>>();
		protected ArrayList<Tree<T>> trees = new ArrayList<Tree<T>>();
		
		protected SequenceIterator<T> masterIterator;
		
		public ABTIterator()
		{
			super();
			masterIterator = master.iterator();
			
			for (int i = 0; i < BUFFER_SIZE && fetchSentence(); i++);
		}

		@Override
		public Tree<T> next()
		{
			if (this.trees.size() == 0)				
				generateTrees();
			
	
			return trees.remove(this.trees.size() - 1);
		}

		@Override
		public boolean hasNext()
		{
			return trees.size() > 0 || sentences.size() > 0;
		}

		private boolean fetchSentence()
		{
			if (! masterIterator.hasNext()) {
				return false;
			}
	
			sentences.addLast(new ArrayList<T>());
	
			do {
				sentences.getLast().add(masterIterator.next());
			} while (!masterIterator.atSequenceEnd());
	
			return true;
		}
	
		private boolean generateTrees()
		{
			if (!this.fetchSentence() && this.sentences.size() == 0) {
				return false;
			}
	
			for (TmpTree tree : generateTrees(this.sentences.getFirst(), 0,
	                                          this.sentences.removeFirst().size())) {
				this.trees.add(tree.toRegTree());
			}
	
			return true;
		}

		private ArrayList<TmpTree> generateTrees(List<T> sentence, int i, int j)
		{
			assert (i < j);
	
			TmpTree root = null;
			ArrayList<TmpTree> subtrees =
			    new ArrayList<TmpTree>((int)catalanNumber(j - i - 1));
	
			if (i + 1 == j) {
				subtrees.add(new TmpTree(sentence.get(i)));
				return subtrees;
			}
	
			for (int k = i + 1; k < j; k++) {
				for (TmpTree left : generateTrees(sentence, i, k)) {
					for (TmpTree right : generateTrees(sentence, k, j)) {
						root = new TmpTree(filler);
						root.addChildren(left, right);
	
						subtrees.add(root);
					}
				}
			}
	
			return subtrees;
		}

		private class TmpTree
		{
			public final T value;
			private TmpTree left;
			private TmpTree right;
	
			private TmpTree(T value)
			{
				this.value = value;
			}
	
			public void addChildren(TmpTree left, TmpTree right)
			{
				this.left = left;
				this.right = right;
			}
	
			public Tree<T> toRegTree()
			{
				Tree<T> tree = new Tree<T>(this.value);
				this.toRegTree(tree.getRoot());
				return tree;
			}
	
			public void toRegTree(Tree<T>.Node node)
			{
				assert (this.value == node.getValue());
	
				List<Tree<T>.Node> children = null;
	
				if (this.left != null) {
					assert (this.right != null);
	
					node.addChild(this.left.value);
					node.addChild(this.right.value);
	
					children = node.getChildren();
					this.left.toRegTree(children.get(0));
					this.right.toRegTree(children.get(1));
				}
			}
		}
	}
	private static long catalanNumber(int n)
	{
		long prev, cur;

		assert (n >= 0);

		if (catalanNumbers.size() > n) {
			return catalanNumbers.get(n);
		}

		if (n == 0) {
			catalanNumbers.add(1L);
			return 1L;
		}

		prev = catalanNumber(n - 1);
		cur = 4 * prev - (6 * prev / (n + 1));

		catalanNumbers.add(cur);
		return cur;
	}
	
}
