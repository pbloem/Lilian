package org.lilian.graphs.subdue;

import static org.lilian.util.Series.series;

import org.lilian.graphs.MapUTGraph;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.UTNode;

/**
 * A wrapping represents a context within which the labels of a given graph are
 * wrapped into token objects, so that another graph can be made in which nodes 
 * with the original labels can exist together with nodes with new labels 
 * (ie. symbol nodes).  
 * 
 * 
 * @author Peter
 *
 */
public class Wrapping<L, T>
{
	
	private static int nextSymbolTokenID = 0;
	private static int nextSymbolTagTokenID = 0;
	
	/**
	 * Produces a graph with the same structure as the input, but with wrapper 
	 * objects as labels/tags that refer back to the original labels/tags 
	 * (but not the original nodes). 
	 * 
	 * @param graph
	 * @return
	 */
	public MapUTGraph<Token, TagToken> wrap(UTGraph<L, T> graph)
	{
		MapUTGraph<Token, TagToken> wrapped = new MapUTGraph<Token, TagToken>();
		
		for(UTNode<L, T> nodeIn : graph.nodes())
			wrapped.add(new LabelToken(nodeIn.label()));
		
		for(int i : series(graph.size()))
			for(int j : series(i, graph.size()))
			{
				UTNode<L, T> ni = graph.nodes().get(i), nj = graph.nodes().get(j);
				for(T tag : graph.tags())
				{
					if(ni.connected(nj, tag))
						wrapped.nodes().get(i).connect(wrapped.nodes().get(j), 
								new LabelTagToken(tag));
				}
			}
		
		return wrapped;
	}
	
	public int numSymbols()
	{
		return nextSymbolTokenID;
	}
	
	public int numTagSymbols()
	{
		return nextSymbolTagTokenID;
	}
	
	public Token symbol()
	{
		return new SymbolToken();
	}
	
	public Token token(L label)
	{
		return new LabelToken(label);
	} 	
	
	public TagToken tag(T tag)
	{
		return new LabelTagToken(tag);
	} 
	
	public TagToken tag(T tag, int firstAnnotation)
	{
		return new LabelTagToken(tag, firstAnnotation);
	}
	
	public TagToken tag(T tag, int firstAnnotation, int secondAnnotation)
	{
		return new LabelTagToken(tag, firstAnnotation, secondAnnotation);
	}	

	/**
	 * A token is a node label in a substructure. It can represent a labeled 
	 * node in the graph or a variable node.
	 * 
	 * @author Peter
	 *
	 */
	public interface Token {
		
	}
	
	public interface TagToken {
		
		public Integer firstAnnotation();
		
		public Integer secondAnnotation();
	}
	
	public class LabelToken implements Token 
	{
		L label;
	
		public LabelToken(L label)
		{
			this.label = label;
		}
		
		public L label()
		{
			return label;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LabelToken other = (LabelToken) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (label == null)
			{
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			return true;
		}

		private Wrapping<L, T> getOuterType()
		{
			return Wrapping.this;
		}
		
		public String toString()
		{
			return label+"";
		}
	}
	
	public class LabelTagToken implements TagToken 
	{
		T tag;
		Integer firstAnnotation = null, secondAnnotation = null; 

		public LabelTagToken(T tag)
		{
			this.tag = tag;
		} 
		
		public LabelTagToken(T tag, int firstAnnotation)
		{
			this.tag = tag;
			this.firstAnnotation = firstAnnotation;
		}
		
		public LabelTagToken(T tag, int firstAnnotation, int secondAnnotation)
		{
			this.tag = tag;
			this.firstAnnotation = firstAnnotation;
			this.secondAnnotation = secondAnnotation;
		}	
		
		public T tag()
		{
			return tag;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((tag == null) ? 0 : tag.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LabelTagToken other = (LabelTagToken) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (tag == null)
			{
				if (other.tag != null)
					return false;
			} else if (!tag.equals(other.tag))
				return false;
			return true;
		}

		private Wrapping<L, T> getOuterType()
		{
			return Wrapping.this;
		}
		
		public String toString()
		{
			return tag + "";
		}

		@Override
		public Integer firstAnnotation()
		{
			return firstAnnotation;
		}

		@Override
		public Integer secondAnnotation()
		{
			return secondAnnotation;
		}
	}
	
	/**
	 * A token that does not represent a label in the original graph 
	 * 
	 * @author Peter
	 *
	 */
	public class SymbolToken implements Token {
		private int id;
		
		public SymbolToken()
		{
			id = nextSymbolTokenID;
			nextSymbolTokenID++;
		}

		@Override
		public int hashCode()
		{
			return id;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SymbolToken other = (SymbolToken) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (id != other.id)
				return false;
			return true;
		}

		private Wrapping getOuterType()
		{
			return Wrapping.this;
		}
		
		public String toString()
		{
			return "%"+id;
		}
	}

	/**
	 * A tagtoken that does not represent a tag in the original graph
	 *  
	 * @author Peter
	 *
	 */
	public class SymbolTagToken implements TagToken {
		private int id;
		
		public SymbolTagToken()
		{
			id = nextSymbolTagTokenID;
			nextSymbolTagTokenID++;
		}

		@Override
		public int hashCode()
		{
			return id;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SymbolTagToken other = (SymbolTagToken) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (id != other.id)
				return false;
			return true;
		}

		private Wrapping<L, T> getOuterType()
		{
			return Wrapping.this;
		}
		
		public String toString()
		{
			return "%"+id;
		}

		@Override
		public Integer firstAnnotation()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Integer secondAnnotation()
		{
			// TODO Auto-generated method stub
			return null;
		}
	}
}
