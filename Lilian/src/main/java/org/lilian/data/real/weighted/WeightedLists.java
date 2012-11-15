package org.lilian.data.real.weighted;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.lilian.Global;
import org.lilian.util.Series;

public class WeightedLists
{

	public static <T> Weighted<T> empty()
	{
		return new WeightedList<T>(
				new ArrayList<T>(), new ArrayList<Double>());
	}	
	
	public static <T> Weighted<T> combine(List<T> items, List<Double> weights)
	{
		return new WeightedList<T>(items, weights);
	}
	
	private static class WeightedList<T> implements Weighted<T>, Serializable
	{
		private static final long serialVersionUID = 8940089843209082110L;
		private List<T> master;
		private List<Double> weights;
		
		private double sum;

		public WeightedList(List<T> items, List<Double> weights)
		{
			if(items.size() != weights.size())
				throw new IllegalArgumentException("Item and weight list must have same length. Lengths were: " + items.size() + " and " + weights.size());
			
			this.master = items;
			this.weights = weights;
			
			sum = .0;
			for(double w : weights)
				sum += w;
			
		}		
		
		@Override
		public int size()
		{
			return master.size();
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}

		@Override
		public boolean contains(Object object)
		{
			return master.contains(object);
		}

		@Override
		public Iterator<T> iterator()
		{
			return master.iterator();
		}

		@Override
		public Object[] toArray()
		{
			return master.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a)
		{
			return master.toArray(a);
		}

		@Override
		public boolean add(T e)
		{
			return add(e, 1.0);
		}

		@Override
		public boolean remove(Object o)
		{
			int i = indexOf(o);
			
			if(i == -1)
				return false;
			
			master.remove(i);
			sum -= weights.remove(i);
			
			return true;
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			return master.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends T> c)
		{
			boolean result = false;;
			for(T t : c)
			{
				result = true;
			 	add(t, 1.0);
			}
			
			return result;
		}

		@Override
		public boolean addAll(int index, Collection<? extends T> c)
		{
			boolean result = false;;
			int i = 0;
			for(T t : c)
			{
			 	add(index+i, t, 1.0);
			 	
			 	i++;
				result = true;
			}
			
			return result;		
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			boolean changed = false;
			for(Object o : c)
				changed |= remove(o);
			
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			boolean modified = false;
			for(T item : this)
				if(! master.contains(item))
				{		
					remove(item);
					modified = true;
				}
			
			return modified;
		}

		@Override
		public void clear()
		{
			master.clear();
			weights.clear();
			sum = 0;
		}

		@Override
		public T get(int index)
		{
			return master.get(index);
		}

		@Override
		public T set(int index, T element)
		{
			return master.set(index, element);
		}

		@Override
		public void add(int index, T element)
		{
			add(index, element, 1.0);
		}

		@Override
		public T remove(int index)
		{
			sum -= weights.remove(index);
			return master.remove(index);
		}

		@Override
		public int indexOf(Object o)
		{
			return master.indexOf(o);
		}

		@Override
		public int lastIndexOf(Object o)
		{
			return master.lastIndexOf(o);
		}

		@Override
		public ListIterator<T> listIterator()
		{
			return master.listIterator();
		}

		@Override
		public ListIterator<T> listIterator(int index)
		{
			return listIterator(index);
		}

		@Override
		public List<T> subList(int from, int to)
		{
			return master.subList(from, to);
		}

		@Override
		public double weight(int i)
		{
			return weights.get(i);
		}

		@Override
		public double probability(int i)
		{
			if(sum == 0.0)
				return 0.0;
			
			return weight(i) / sum();
		}

		@Override
		public T choose()
		{
			// * select random element
			double draw = Global.random.nextDouble();
			double total = 0.0;
			int elem = 0;
			for(int i : Series.series(size()))
			{
				total += probability(i);
				if(total > draw)
					break;
				
				elem ++;
			}
			
			// * account for floating point problems
			if(elem >= size())
				elem = size() - 1;

			return get(elem);
		}

		@Override
		public double sum()
		{
			return sum;
		}

		@Override
		public boolean add(T item, double weight)
		{
			master.add(item);
			weights.add(weight);
			sum += weight;
			
			return true;
		}

		@Override
		public boolean add(int index, T item, double weight)
		{
			master.add(index, item);
			weights.add(index, weight);
			sum += weight;
			
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends T> c, double weight)
		{
			for(T t : c)
				add(t, weight);
			
			return !c.isEmpty();
		}

		@Override
		public boolean addAll(int index, Collection<? extends T> c,
				double weight)
		{
			for(T t : c)
				add(index++, t, weight);
			
			return !c.isEmpty();
		}

		@Override
		public T set(int i, T item, double weight)
		{
			double old = weights.set(i, weight);
			sum = sum - old + weight;
			
			return master.set(i, item);
		}

		@Override
		public Weighted<T> subWeighted(int from, int to)
		{
			return combine(master.subList(from, to), weights.subList(from, to));
		}
		
		
		public String toString()
		{
			String res = "[";
			
			
			for(int i : Series.series(size()))
				res += (i==0 ? ", " : "") + get(i) + "("+probability(i)+")"; 
				
			return res+ "]";
		}
		
	}
}