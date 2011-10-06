package org.lilian.util.graphs;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A basic implementation of the {@link Graph} interface
 * @author peter
 *
 * @param <L>
 */
public class BaseGraph<L> extends AbstractList<BaseGraph<L>.BaseNode> 
	implements Graph<L, BaseGraph<L>.BaseNode>
{

	public class BaseNode implements Node<L, BaseNode>
	{
		private List<BaseNode> neighbours = new ArrayList<BaseNode>();
		private L label;

		@Override
		public List<BaseNode> neighbours()
		{
			return Collections.unmodifiableList(neighbours);
		}

		@Override
		public BaseNode neighbour(L label)
		{
			for(BaseNode node: neighbours)
				if(node.equals(label))
					return node;
			
			return null;			 
		}

		@Override
		public L label()
		{
			return label;
		}

		@Override
		public List<BaseNode> neighbours(L label)
		{
			List<BaseNode> result = new ArrayList<BaseNode>();
			for(BaseNode node: neighbours)
				if(node.equals(label))
					result.add(node);
			
			return result;
			
		}

		@Override
		public void connect(BaseNode other)
		{
			// TODO Auto-generated method stub
			
		}
	}

	@Override
	public BaseNode node(L label)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseNode get(int index)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<BaseNode> nodes(L label)
	{
		// TODO Auto-generated method stub
		return null;
	}


}
