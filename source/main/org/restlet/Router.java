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

import java.util.List;

/**
 * Router of calls to target Restlets. Internally, the targets are handled using attachments objects.
 * Some methods allow the creation of attachments based on URI path patterns matching the beginning of a the
 * resource path in the current context (see Call.getContextPath() method).<br/>
 * Note that during the delegation, the call paths are automatically modified. 
 * If you are handling hierarchical paths, remember to directly attach the child routers to their parent router
 * instead of the top level Restlet container. Also, remember to manually handle the path separator characters 
 * in your path patterns otherwise the delegation will not work as expected.<br/>
 * Also note that you can attach and detach targets while handling incoming calls as the delegation code 
 * is ensured to be thread-safe and atomic.
 * @see <a href="http://www.restlet.org/tutorial#part11">Tutorial: Routers and hierarchical URIs</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface Router extends Handler
{
	/**
	 * Returns the modifiable list of options.
	 * @return The modifiable list of options.
	 */
	public List<Scorer> getOptions();
	
   /**
    * Attaches a target instance shared by all calls. 
    * @param pattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pattern, Restlet target);

   /**
    * Attaches at a specific a target instance shared by all calls.
    * @param pattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
    * @param target The target instance to attach.
    * @param index The insertion position in the list of attachments.
    * @see java.util.regex.Pattern
    */
   public void attach(String pattern, Restlet target, int index);

   /**
    * Detaches all attachments with a given target Restlet.
    * @param target The target Restlet to detach.
    */
   public void detach(Restlet target);

	/**
	 * Returns the routing mode.
	 * @return The routing mode.
	 */
	public RouterMode getMode();
	
	/**
	 * Sets the routing mode.
	 * @param mode The routing mode.
	 */
	public void setMode(RouterMode mode);
	
	/**
	 * Returns the minimum score required to have a match.
	 * @return The minimum score required to have a match.
	 */
	public float getRequiredScore();
	
	/**
	 * Sets the score required to have a match.
	 * @param score The score required to have a match.
	 */
	public void setRequiredScore(float score);

	/**
	 * Returns the maximum number of attempts if no attachment could be matched on the first attempt.
	 * This is useful when the attachment scoring is dynamic and therefore could change on a retry.
	 * @return The maximum number of attempts if no attachment could be matched on the first attempt.
	 */
	public int getMaxAttempts();
	
	/**
	 * Sets the maximum number of attempts if no attachment could be matched on the first attempt.
	 * This is useful when the attachment scoring is dynamic and therefore could change on a retry.
	 * @param maxAttempts The maximum number of attempts. 
	 */
	public void setMaxAttempts(int maxAttempts);

	/**
	 * Returns the delay (in seconds) before a new attempt.
	 * @return The delay (in seconds) before a new attempt.
	 */
	public long getRetryDelay();
	
	/**
	 * Sets the delay (in seconds) before a new attempt.
	 * @param delay The delay (in seconds) before a new attempt.
	 */
	public void setRetryDelay(long delay);
	
}
