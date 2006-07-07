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

import org.restlet.component.Component;
import org.restlet.data.Statuses;

/**
 * Default Router that can directly be used.
 * @see <a href="http://www.restlet.org/tutorial#part11">Tutorial: Routers and hierarchical URIs</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DefaultRouter extends AbstractHandler implements Router
{
	/** The modifiable list of target options. */
	protected List<Scorer> options;
	
	/** The routing mode. */
	protected RouterMode mode;
	
	/** The minimum score required to have a match. */
	protected float requiredScore;
	
	/** The maximum number of attempts if no attachment could be matched on the first attempt. */
	protected int maxAttempts;
	
	/** The delay (in milliseconds) before a new attempt. */
	protected long retryDelay;
	
	/** The index of the last attachment used in the round robin mode. */ 
	protected int lastIndex;
	
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
      this.options = null;
      this.mode = RouterMode.BEST_MATCH;
      this.requiredScore = 0.5F;
      this.maxAttempts = 1;
      this.retryDelay = 500L;
      this.lastIndex = -1;
   }

	/**
	 * Finds the next Restlet if available.
	 * @param call The current call.
	 * @return The next Restlet if available or null.
	 */
	public Restlet findNext(Call call)
	{
		Scorer result = null;
		
		if(this.options != null)
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
				Scorer att;
				switch(getMode())
				{
					case BEST_MATCH:
						float bestScore = 0F;
						float score;
						for(Scorer current : getOptions())
						{
							score = current.score(call);

							if((score > bestScore) && (score >= getRequiredScore()))
							{
								bestScore = score;
								result = current;
							}
						}
					break;
					
					case FIRST_MATCH:
						for(int j = 0; (result == null) && (j < getOptions().size()); j++)
						{
							att = getOptions().get(j);
							if(att.score(call) >= getRequiredScore()) result = att;
						}
					break;
					
					case LAST_MATCH:
						for(int j = (getOptions().size() - 1); (result == null) && (j >= 0); j--)
						{
							att = getOptions().get(j);
							if(att.score(call) >= getRequiredScore()) result = att;
						}
					break;
					
					case RANDOM:
						int j = new Random().nextInt(getOptions().size());
						att = getOptions().get(j);
						if(att.score(call) >= getRequiredScore()) result = att;
					break;
					
					case ROUND_ROBIN:
						synchronized(this)
						{
							// Compute the next index
							lastIndex++;
							if(lastIndex >= getOptions().size())
							{
								lastIndex = 0;
							}
							
							// Starting from the next index, find the first matching target
							for(int k = lastIndex; (result == null) && (k < getOptions().size()); k++)
							{
								att = getOptions().get(k);
								if(att.score(call) >= getRequiredScore()) result = att;
							}
						}						
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
	 * Returns the modifiable list of options.
	 * @return The modifiable list of options.
	 */
	public List<Scorer> getOptions()
	{
      if(this.options == null) this.options = new ArrayList<Scorer>();
      return this.options;
	}

   /**
    * Adds a target option based on an URI pattern at the end of the list of options. 
    * @param pattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
    * @param target The target instance to attach.
    * @see java.util.regex.Pattern
    */
   public void addOption(String pattern, Restlet target)
   {
   	getOptions().add(Factory.getInstance().createScorer(this, pattern, target));
   }

   /**
    * Adds a target option based on an URI pattern at a specific position.
    * @param pattern The URI pattern used to map calls (see {@link java.util.regex.Pattern} for the syntax).
    * @param target The target instance to attach.
    * @param index The insertion position in the list of attachments.
    * @see java.util.regex.Pattern
    */
   public void addOption(String pattern, Restlet target, int index)
   {
   	getOptions().add(index, Factory.getInstance().createScorer(this, pattern, target));
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
