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


/**
 * Router of calls to one of several target Restlet options. Each Restlet option is represented by a scorer 
 * that can compute an affinity score for each call depending on various criteria. Some add() methods in the 
 * modifiable ScorerList instance returned by getScorers() allow the creation of scorers based on URI path 
 * patterns matching the beginning of a the resource path in the current context (see Call.getContextPath() 
 * and getResourcePath() methods).<br/>
 * <br/>
 * In addition, several routing modes are supported, implementing various algorithms like:
 * <ul>
 * <li>Best match (default)</li>
 * <li>First match</li>
 * <li>Last match</li>
 * <li>Random match</li>
 * <li>Round robin</li>
 * <li>Custom</li>
 * </ul>
 * <br/>
 * Note that for scorers using URI patterns will update the call paths during the routing if they are selected.
 * If you are handling hierarchical paths, remember to directly attach the child routers to their parent router
 * instead of the top level Restlet container. Also, remember to manually handle the path separator characters 
 * in your path patterns otherwise the delegation will not work as expected.<br/>
 * <br/>
 * Finally, you can modify the scorers list while handling incoming calls as the delegation code is ensured to 
 * be thread-safe.
 * @see <a href="http://www.restlet.org/tutorial#part11">Tutorial: Routers and hierarchical URIs</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface Router extends Handler
{
	/** Enumeration of available router modes. */
	public enum Mode
	{
		/**
		 * Each call will be routed to the scorer with the best score, if the required score is reached.
		 */
		BEST,
		
		/**
		 * Each call is routed to the first scorer if the required score is reached. If the required score 
		 * is not reached, then the scorer is skipped and the next one is considered. 
		 */
		FIRST,
		
		/**
		 * Each call will be routed to the last scorer if the required score is reached. If the required score 
		 * is not reached, then the scorer is skipped and the previous one is considered. 
		 */
		LAST,
		
		/**
		 * Each call is be routed to the next scorer target if the required score is reached. The next scorer is 
		 * relative to the previous call routed (round robin mode). If the required score is not reached, then the
		 * scorer is skipped and the next one is considered. If the last scorer is reached, the first scorer will 
		 * be considered.  
		 */
		NEXT,
		
		/**
		 * Each call will be randomly routed to one of the scorers that reached the required score. If the random 
		 * scorer selected is not a match then the immediate next scorer is evaluated until one matching scorer is 
		 * found. If we get back to the inital random scorer selected with no match, then we return null.
		 */
		RANDOM,
		
		/**
		 * Each call will be routed according to a custom mode.
		 */
		CUSTOM;	
	}
	
	/**
	 * Returns the routing mode.
	 * @return The routing mode.
	 */
	public Mode getMode();
	
	/**
	 * Returns the modifiable list of scorers.
	 * @return The modifiable list of scorers.
	 */
	public ScorerList getScorers();

	/**
	 * Sets the routing mode.
	 * @param mode The routing mode.
	 */
	public void setMode(Mode mode);
	
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
