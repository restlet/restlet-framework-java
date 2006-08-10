/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.restlet.data.Parameter;

/**
 * Modifiable list of scorers with some helper methods. Note that this class implements the java.util.List
 * interface using the Scorer interface as the template type. This allows you to use an instance of this class
 * as any other java.util.List, in particular all the helper methods in java.util.Collections.<br/>
 * <br/>
 * Note that structural changes to this list are synchronized. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 * @see java.util.Collections
 * @see java.util.List
 */
public class ScorerList implements List<Scorer>
{
	/** The parent router. */
	protected Router router;
	
	/** The delegate list. */
	protected List<Scorer> delegate;
	
	/** The index of the last scorer used in the round robin mode. */ 
	protected int lastIndex;

	/**
	 * Constructor.
	 * @param router The parent router.
	 */
	public ScorerList(Router router)
	{
		this(router, null);
	}

	/**
	 * Constructor.
	 * @param router The parent router.
	 * @param initialCapacity The initial list capacity.
	 */
	public ScorerList(Router router, int initialCapacity)
	{
		this(router, new ArrayList<Scorer>(initialCapacity));
	}
	
	/**
	 * Constructor.
	 * @param delegate The delegate list.
	 */
	public ScorerList(Router router, List<Scorer> delegate)
	{
		this.delegate = delegate;
		this.router = router;
      this.lastIndex = -1;
	}

	/**
	 * Returns the delegate list.
	 * @return The delegate list.
	 */
	protected List<Scorer> getDelegate()
	{
		if(this.delegate == null)
		{
			this.delegate = new ArrayList<Scorer>();
		}
		
		return this.delegate;
	}
	
	/**
	 * Creates then adds a scorer at the end of the list.
    * Adds a target option based on an URI pattern at the end of the list of options. 
    * @param pattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
	 * @return True (as per the general contract of the Collection.add method).
	 */
	public boolean add(String pattern, Restlet target)
	{
		return add(Factory.getInstance().createScorer(this.router, pattern, target));
	}

   /**
    * Creates then adds a scorer based on an URI pattern at a specific position.
    * @param pattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
    * @param target The target instance to attach.
    * @param index The insertion position in the list of attachments.
    * @see java.util.regex.Pattern
    */
   public void add(String pattern, Restlet target, int index)
   {
   	add(index, Factory.getInstance().createScorer(this.router, pattern, target));
   }

	/**
	 * Adds a scorer at the end of the list.
	 * @return True (as per the general contract of the Collection.add method).
	 */
	public synchronized boolean add(Scorer scorer)
	{
		return getDelegate().add(scorer);
	}

	/**
	 * Inserts the specified scorer at the specified position in this list.
	 * @param index The insertion position.
	 * @param scorer The scorer to insert.
	 */
	public synchronized void add(int index, Scorer scorer)
	{
		getDelegate().add(index, scorer);
	}

	/**
	 * Appends all of the scorers in the specified collection to the end of this list.
	 * @param scorers The collection of scorers to append.
	 */
	public synchronized boolean addAll(Collection<? extends Scorer> scorers)
	{
		return getDelegate().addAll(scorers);
	}

	/**
	 * Inserts all of the scorers in the specified collection into this list at the specified position.
	 * @param index The insertion position.
	 * @param scorers The collection of scorers to insert.
	 */
	public synchronized boolean addAll(int index, Collection<? extends Scorer> scorers)
	{
		return getDelegate().addAll(index, scorers);
	}

	/**
	 * Removes all of the scorers from this list.
	 */
	public synchronized void clear()
	{
		if(this.delegate != null)
		{
			getDelegate().clear();
			this.delegate = null;
		}
	}

	/**
	 * Returns true if this list contains the specified element.
	 * @param element The element to find.
	 * @return True if this list contains the specified element.
	 */
	public synchronized boolean contains(Object element)
	{
		if(this.delegate != null)
		{
			return this.delegate.contains(element);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns true if this list contains all of the elements of the specified collection.
	 * @param elements The collection of elements to find.
	 * @return True if this list contains all of the elements of the specified collection.
	 */
	public synchronized boolean containsAll(Collection<?> elements)
	{
		if(this.delegate != null)
		{
			return this.delegate.containsAll(elements);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns the scorer at the specified position in this list.
	 * @param index The scorer position.
	 * @return The scorer at the specified position in this list.
	 */
	public synchronized Scorer get(int index)
	{
		if(this.delegate != null)
		{
			return this.delegate.get(index);
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Returns the best scorer match for a given call.
	 * @param call The call to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return The best scorer match or null.
	 */
	public synchronized Scorer getBest(Call call, float requiredScore)
	{
		Scorer result = null;
		float bestScore = 0F;
		float score;
		for(Scorer current : this)
		{
			score = current.score(call);

			if((score > bestScore) && (score >= requiredScore))
			{
				bestScore = score;
				result = current;
			}
		}
		
		return result;
	}

	/**
	 * Returns the first scorer match for a given call.
	 * @param call The call to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return The first scorer match or null.
	 */
	public synchronized Scorer getFirst(Call call, float requiredScore)
	{
		for(Scorer current : this)
		{
			if(current.score(call) >= requiredScore) return current;
		}
		
		// No match found
		return null;
	}

	/**
	 * Returns the last scorer match for a given call.
	 * @param call The call to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return The last scorer match or null.
	 */
	public synchronized Scorer getLast(Call call, float requiredScore)
	{
		for(int j = (size() - 1); (j >= 0); j--)
		{
			if(get(j).score(call) >= requiredScore) return get(j);
		}
		
		// No match found
		return null;
	}

	/**
	 * Returns a next scorer match in a round robin mode for a given call.
	 * @param call The call to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return A next scorer or null.
	 */
	public synchronized Scorer getNext(Call call, float requiredScore)
	{
		for(int initialIndex = lastIndex++; initialIndex != lastIndex; lastIndex++)
		{
			if(lastIndex == size())
			{
				lastIndex = 0;
			}
			
			if(get(lastIndex).score(call) >= requiredScore) return get(lastIndex);
		}

		// No match found
		return null;
	}

	/**
	 * Returns a random scorer match for a given call.
	 * @param call The call to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return A random scorer or null.
	 */
	public synchronized Scorer getRandom(Call call, float requiredScore)
	{
		int j = new Random().nextInt(size());
		if(get(j).score(call) >= requiredScore) return get(j);
		
		for(int initialIndex = j++; initialIndex != j; j++)
		{
			if(j == size())
			{
				j = 0;
			}
			
			if(get(j).score(call) >= requiredScore) return get(j);
		}

		// No match found
		return null;
	}
	
   /**
    * Returns the index in this list of the first occurrence of the specified element, 
    * or -1 if this list does not contain this element.
    * @param element The element to find.
    * @return The index of the first occurrence.
    */
	public synchronized int indexOf(Object element)
	{
		if(this.delegate != null)
		{
			return this.delegate.indexOf(element);
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Returns true if this list contains no elements.
	 */
	public synchronized boolean isEmpty()
	{
		if(this.delegate != null)
		{
			return this.delegate.isEmpty();
		}
		else
		{
			return true;
		}
	}

	/**
	 * Returns an iterator over the scorers in this list in proper sequence.
	 * @return An iterator over the scorers in this list in proper sequence.
	 */
	public synchronized Iterator<Scorer> iterator()
	{
		return getDelegate().iterator();
	}

	/**
	 * Returns the index in this list of the last occurrence of the specified element, 
	 * or -1 if this list does not contain this element.
	 */
	public synchronized int lastIndexOf(Object element)
	{
		if(this.delegate != null)
		{
			return this.delegate.lastIndexOf(element);
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Returns a list iterator of the scorers in this list (in proper sequence).
	 * @return A list iterator of the scorers in this list (in proper sequence).
	 */
	public synchronized ListIterator<Scorer> listIterator()
	{
		return getDelegate().listIterator();
	}

	/**
	 * Returns a list iterator of the scorers in this list (in proper sequence), starting at the 
	 * specified position in this list.
	 * @param index The starting position.
	 */
	public synchronized ListIterator<Scorer> listIterator(int index)
	{
		return getDelegate().listIterator(index);
	}

	/**
	 * Removes the first occurrence in this list of the specified element.
	 * @return True if the list was changed.
	 */
	public synchronized boolean remove(Object element)
	{
		if(this.delegate != null)
		{
			return this.delegate.remove(element);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Removes the scorer at the specified position in this list.
	 * @return The removed scorer.
	 */
	public synchronized Scorer remove(int index)
	{
		if(this.delegate != null)
		{
			return this.delegate.remove(index);
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Removes from this list all the elements that are contained in the specified collection.
	 * @param elements The collection of element to remove.
	 * @return True if the list changed.
	 */
	public synchronized boolean removeAll(Collection<?> elements)
	{
		if(this.delegate != null)
		{
			return this.delegate.removeAll(elements);
		}
		else
		{
			return false;
		}
	}

	/**
	 * RemovesRetains only the elements in this list that are contained in the specified collection.
	 * @param elements The collection of element to retain.
	 * @return True if the list changed.
	 */
	public synchronized boolean retainAll(Collection<?> elements)
	{
		if(this.delegate != null)
		{
			return this.delegate.retainAll(elements);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Replaces the scorer at the specified position in this list with the specified scorer.
	 * @param index The position of the scorer to replace.
	 * @param scorer The new scorer.
	 */
	public synchronized Scorer set(int index, Scorer scorer)
	{
		if(this.delegate != null)
		{
			return this.delegate.set(index, scorer);
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}
	
	/**
	 * Returns the number of scorers in this list.
	 * @return The number of scorers in this list.
	 */
	public synchronized int size()
	{
		if(this.delegate != null)
		{
			return this.delegate.size();
		}
		else
		{
			return 0;
		}
	}

	/**
	 * Returns a view of the portion of this list between the specified fromIndex, 
	 * inclusive, and toIndex, exclusive.
	 * @param fromIndex The start position.
	 * @param toIndex The end position (exclusive).
	 * @return The sub-list.
	 */
	public synchronized ScorerList subList(int fromIndex, int toIndex)
	{
		return new ScorerList(this.router, getDelegate().subList(fromIndex, toIndex));
	}

	/**
	 * Returns an array containing all of the elements in this list in proper sequence.
	 * @return An array containing all of the elements in this list in proper sequence.
	 */
	public synchronized Parameter[] toArray()
	{
		return (Parameter[])getDelegate().toArray();
	}

	/**
	 * Returns an array containing all of the elements in this list in proper sequence; 
	 * the runtime type of the returned array is that of the specified array.
	 * @param a The sample array.
	 */
	public synchronized <T> T[] toArray(T[] a)
	{
		return getDelegate().toArray(a);
	}
	
}
