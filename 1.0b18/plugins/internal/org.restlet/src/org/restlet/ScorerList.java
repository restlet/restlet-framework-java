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
import java.util.List;
import java.util.Random;

import org.restlet.data.WrapperList;
import org.restlet.spi.Factory;

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
public class ScorerList extends WrapperList<Scorer>
{
	/** The parent router. */
	private Router router;
	
	/** The index of the last scorer used in the round robin mode. */ 
	private int lastIndex;

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
		super(delegate);
		this.router = router;
      this.lastIndex = -1;
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
}
