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

package com.noelios.restlet.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.Scorer;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.spi.Factory;
import org.restlet.spi.ScorerList;
import org.restlet.util.WrapperList;

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
public class DefaultScorerList extends WrapperList<Scorer> implements ScorerList 
{
	/** The parent router. */
	private Router router;

	/** The index of the last scorer used in the round robin mode. */
	private int lastIndex;

	/**
	 * Constructor.
	 * @param router The parent router.
	 */
	public DefaultScorerList(Router router)
	{
		this(router, null);
	}

	/**
	 * Constructor.
	 * @param router The parent router.
	 * @param initialCapacity The initial list capacity.
	 */
	public DefaultScorerList(Router router, int initialCapacity)
	{
		this(router, new ArrayList<Scorer>(initialCapacity));
	}

	/**
	 * Constructor.
	 * @param delegate The delegate list.
	 */
	public DefaultScorerList(Router router, List<Scorer> delegate)
	{
		super(delegate);
		this.router = router;
		this.lastIndex = -1;
	}

	/**
	 * Creates then adds a scorer at the end of the list.
	 * Adds a target option based on an URI pattern at the end of the list of options. 
	 * @param uriPattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
	 * @param target The target Restlet to attach.
	 * @see java.util.regex.Pattern
	 * @return True (as per the general contract of the Collection.add method).
	 */
	public boolean add(String uriPattern, Restlet target)
	{
		return add(Factory.getInstance().createScorer(this.router, uriPattern, target));
	}

	/**
	 * Creates then adds a scorer based on an URI pattern at a specific position.
	 * @param uriPattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
	 * @param target The target Restlet to attach.
	 * @param index The insertion position in the list of attachments.
	 * @see java.util.regex.Pattern
	 */
	public void add(String uriPattern, Restlet target, int index)
	{
		add(index, Factory.getInstance().createScorer(this.router, uriPattern, target));
	}

	/**
	 * Returns the best scorer match for a given call.
    * @param request The request to score.
    * @param response The response to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return The best scorer match or null.
	 */
	public synchronized Scorer getBest(Request request, Response response, float requiredScore)
	{
		Scorer result = null;
		float bestScore = 0F;
		float score;
		for (Scorer current : this)
		{
			score = current.score(request, response);

			if ((score > bestScore) && (score >= requiredScore))
			{
				bestScore = score;
				result = current;
			}
		}

		return result;
	}

	/**
	 * Returns the first scorer match for a given call.
    * @param request The request to score.
    * @param response The response to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return The first scorer match or null.
	 */
	public synchronized Scorer getFirst(Request request, Response response, float requiredScore)
	{
		for (Scorer current : this)
		{
			if (current.score(request, response) >= requiredScore) return current;
		}

		// No match found
		return null;
	}

	/**
	 * Returns the last scorer match for a given call.
    * @param request The request to score.
    * @param response The response to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return The last scorer match or null.
	 */
	public synchronized Scorer getLast(Request request, Response response, float requiredScore)
	{
		for (int j = (size() - 1); (j >= 0); j--)
		{
			if (get(j).score(request, response) >= requiredScore) return get(j);
		}

		// No match found
		return null;
	}

	/**
	 * Returns a next scorer match in a round robin mode for a given call.
    * @param request The request to score.
    * @param response The response to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return A next scorer or null.
	 */
	public synchronized Scorer getNext(Request request, Response response, float requiredScore)
	{
		for (int initialIndex = lastIndex++; initialIndex != lastIndex; lastIndex++)
		{
			if (lastIndex == size())
			{
				lastIndex = 0;
			}

			if (get(lastIndex).score(request, response) >= requiredScore) return get(lastIndex);
		}

		// No match found
		return null;
	}

	/**
	 * Returns a random scorer match for a given call.
    * @param request The request to score.
    * @param response The response to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return A random scorer or null.
	 */
	public synchronized Scorer getRandom(Request request, Response response, float requiredScore)
	{
		int j = new Random().nextInt(size());
		if (get(j).score(request, response) >= requiredScore) return get(j);

		for (int initialIndex = j++; initialIndex != j; j++)
		{
			if (j == size())
			{
				j = 0;
			}

			if (get(j).score(request, response) >= requiredScore) return get(j);
		}

		// No match found
		return null;
	}
	
	/**
	 * Removes all scorers routing to a given target.
	 * @param target The target Restlet to detach.
	 */
	public void removeAll(Restlet target)
	{
		for(int i = size() - 1; i >= 0; i--)
		{
			if(get(i).getNext() == target) remove(i); 
		}
	}

	/**
	 * Returns a view of the portion of this list between the specified fromIndex, 
	 * inclusive, and toIndex, exclusive.
	 * @param fromIndex The start position.
	 * @param toIndex The end position (exclusive).
	 * @return The sub-list.
	 */
	public synchronized DefaultScorerList subList(int fromIndex, int toIndex)
	{
		return new DefaultScorerList(this.router, getDelegate().subList(fromIndex, toIndex));
	}
}
