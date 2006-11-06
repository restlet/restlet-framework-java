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

package org.restlet.util;

import java.util.List;

import org.restlet.Restlet;
import org.restlet.Scorer;
import org.restlet.data.Request;
import org.restlet.data.Response;

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
public interface ScorerList extends List<Scorer>
{
	/**
	 * Creates then adds a scorer at the end of the list.
	 * Adds a target option based on an URI pattern at the end of the list of options. 
	 * @param uriPattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
	 * @param target The target Restlet to attach.
	 * @see java.util.regex.Pattern
	 * @return True (as per the general contract of the Collection.add method).
	 */
	public boolean add(String uriPattern, Restlet target);

	/**
	 * Creates then adds a scorer based on an URI pattern at a specific position.
	 * @param uriPattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
	 * @param target The target Restlet to attach.
	 * @param index The insertion position in the list of attachments.
	 * @see java.util.regex.Pattern
	 */
	public void add(String uriPattern, Restlet target, int index);

	/**
	 * Returns the best scorer match for a given call.
    * @param request The request to score.
    * @param response The response to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return The best scorer match or null.
	 */
	public Scorer getBest(Request request, Response response, float requiredScore);

	/**
	 * Returns the first scorer match for a given call.
    * @param request The request to score.
    * @param response The response to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return The first scorer match or null.
	 */
	public Scorer getFirst(Request request, Response response, float requiredScore);

	/**
	 * Returns the last scorer match for a given call.
    * @param request The request to score.
    * @param response The response to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return The last scorer match or null.
	 */
	public Scorer getLast(Request request, Response response, float requiredScore);

	/**
	 * Returns a next scorer match in a round robin mode for a given call.
    * @param request The request to score.
    * @param response The response to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return A next scorer or null.
	 */
	public Scorer getNext(Request request, Response response, float requiredScore);

	/**
	 * Returns a random scorer match for a given call.
    * @param request The request to score.
    * @param response The response to score.
	 * @param requiredScore The minimum score required to have a match. 
	 * @return A random scorer or null.
	 */
	public Scorer getRandom(Request request, Response response, float requiredScore);
	
	/**
	 * Removes all scorers routing to a given target.
	 * @param target The target Restlet to detach.
	 */
	public void removeAll(Restlet target);

	/**
	 * Returns a view of the portion of this list between the specified fromIndex, 
	 * inclusive, and toIndex, exclusive.
	 * @param fromIndex The start position.
	 * @param toIndex The end position (exclusive).
	 * @return The sub-list.
	 */
	public ScorerList subList(int fromIndex, int toIndex);
}
