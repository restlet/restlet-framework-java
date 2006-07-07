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

import org.restlet.component.Component;
import org.restlet.data.ScorerList;
import org.restlet.data.Statuses;

/**
 * Default Router that can directly be used.
 * @see <a href="http://www.restlet.org/tutorial#part11">Tutorial: Routers and hierarchical URIs</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DefaultRouter extends AbstractHandler implements Router
{
	/** The modifiable list of scorers. */
	protected ScorerList scorers;
	
	/** The routing mode. */
	protected RouterMode mode;
	
	/** The minimum score required to have a match. */
	protected float requiredScore;
	
	/** The maximum number of attempts if no attachment could be matched on the first attempt. */
	protected int maxAttempts;
	
	/** The delay (in milliseconds) before a new attempt. */
	protected long retryDelay;
	
   /**
    * Constructor.
    */
   public DefaultRouter()
   {
      this(null);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    */
	public DefaultRouter(Component owner)
   {
      super(owner);
      this.scorers = null;
      this.mode = RouterMode.BEST;
      this.requiredScore = 0.5F;
      this.maxAttempts = 1;
      this.retryDelay = 500L;
   }

	/**
	 * Finds the next Restlet if available.
	 * @param call The current call.
	 * @return The next Restlet if available or null.
	 */
	public Restlet findNext(Call call)
	{
		Scorer result = null;
		
		if(this.scorers != null)
		{
			for(int i = 0; (result == null) && (i < getMaxAttempts()); i++)
			{
				if(i > 0)
				{
					// Before attempting another time, let's
					// sleep during the "retryDelay" set.
					try
					{
						Thread.sleep(getRetryDelay());
					}
					catch (InterruptedException e)
					{
					}
				}
				
				// Select the routing mode
				switch(getMode())
				{
					case BEST:
						result = getScorers().getBest(call, this.requiredScore);
					break;
					
					case FIRST:
						result = getScorers().getFirst(call, this.requiredScore);
					break;
					
					case LAST:
						result = getScorers().getLast(call, this.requiredScore);
					break;
					
					case NEXT:
						result = getScorers().getNext(call, this.requiredScore);
					break;
					
					case RANDOM:
						result = getScorers().getRandom(call, this.requiredScore);
					break;
					
					case CUSTOM:
						result = customFind(call);
					break;
				}
			}
		}		
		
		if(result == null)
		{
			// No routing option could be matched
			call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
		}
		
		return result;
	}
	
	/**
	 * Returns the matched scorer according to a custom algorithm. To use in combination of the RouterMode.CUSTOM 
	 * enumeration. The default implementation (to be overriden), returns null. 
	 * @param call The current call.
	 * @return The matched scorer if available or null.
	 */
	protected Scorer customFind(Call call)
	{
		return null;
	}
	
	/**
	 * Returns the modifiable list of scorers.
	 * @return The modifiable list of scorers.
	 */
	public ScorerList getScorers()
	{
      if(this.scorers == null) this.scorers = new ScorerList(this);
      return this.scorers;
	}

	/**
	 * Returns the routing mode.
	 * @return The routing mode.
	 */
	public RouterMode getMode()
	{
		return this.mode;
	}
	
	/**
	 * Sets the routing mode.
	 * @param mode The routing mode.
	 */
	public void setMode(RouterMode mode)
	{
		this.mode = mode;
	}
	
	/**
	 * Returns the minimum score required to have a match.
	 * @return The minimum score required to have a match.
	 */
	public float getRequiredScore()
	{
		return this.requiredScore;
	}
	
	/**
	 * Sets the score required to have a match.
	 * @param score The score required to have a match.
	 */
	public void setRequiredScore(float score)
	{
		this.requiredScore = score;
	}

	/**
	 * Returns the maximum number of attempts if no attachment could be matched on the first attempt.
	 * This is useful when the attachment scoring is dynamic and therefore could change on a retry.
	 * @return The maximum number of attempts if no attachment could be matched on the first attempt.
	 */
	public int getMaxAttempts()
	{
		return this.maxAttempts;
	}
	
	/**
	 * Sets the maximum number of attempts if no attachment could be matched on the first attempt.
	 * This is useful when the attachment scoring is dynamic and therefore could change on a retry.
	 * @param maxAttempts The maximum number of attempts. 
	 */
	public void setMaxAttempts(int maxAttempts)
	{
		this.maxAttempts = maxAttempts;
	}

	/**
	 * Returns the delay (in seconds) before a new attempt.
	 * @return The delay (in seconds) before a new attempt.
	 */
	public long getRetryDelay()
	{
		return this.retryDelay;
	}
	
	/**
	 * Sets the delay (in seconds) before a new attempt.
	 * @param retryDelay The delay (in seconds) before a new attempt.
	 */
	public void setRetryDelay(long retryDelay)
	{
		this.retryDelay = retryDelay;
	}
	
}
