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

package com.noelios.restlet;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.AbstractScorer;
import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Reference;
import org.restlet.data.Status;

/**
 * Router handler based on a URI pattern. Note that the matching is case sensitive unless some inline modifiers
 * were used in the pattern using the "(?i)" inline flag.
 * @see java.util.regex.Pattern
 * @see <a href="http://javaalmanac.com/egs/java.util.regex/pkg.html">Java Almanac on the Regex package</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class PatternScorer extends AbstractScorer
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(PatternScorer.class.getCanonicalName());

   /** The URI pattern. */
   Pattern pattern;

   /**
    * Constructor.
    * @param router The parent router.
    * @param pattern The URI pattern.
    * @param target The Restlet target.
    */
   public PatternScorer(Router router, String pattern, Restlet target)
   {
      super(router, target);
      this.pattern = Pattern.compile(pattern);
   }

   /**
    * Returns the URI pattern.
    * @return The URI pattern.
    */
   public Pattern getPattern()
   {
      return this.pattern;
   }
	
	/**
	 * Returns the score for a given call (between 0 and 1.0).
	 * @param call The call to score.
	 * @return The score for a given call (between 0 and 1.0).
	 */
	public float score(Call call)
	{
		float result = 0F;
		String remainingRef = getRemainingRef(call);
		Matcher matcher = getPattern().matcher(remainingRef);
      boolean matched = matcher.lookingAt();

      if(matched)
      {
      	float totalLength = remainingRef.length();
      	
      	if(totalLength > 0.0F)
      	{
      		float matchedLength = matcher.end();
      		result = getRouter().getRequiredScore() + (1.0F - getRouter().getRequiredScore()) * (matchedLength/totalLength);
      	}
      	else
      	{
      		result = 1.0F;
      	}
      }
      
      if(logger.isLoggable(Level.FINER))
      {
      	logger.finer("Scoring this pattern: " + getPattern().toString() + " >> " + result);
      }

      return result;
	}
	
	/**
	 * Handles the call.
	 * @param call The call to handle.
	 */
	public void handle(Call call)
	{
		String remainingRef = getRemainingRef(call);
		Matcher matcher = getPattern().matcher(remainingRef);
      boolean matched = matcher.lookingAt();
         
      if(logger.isLoggable(Level.FINER))
      {
      	logger.finer("Attempting to match this pattern: " + getPattern().toString() + " >> " + matched);
      }

      if(matched)
      {
	      // Updates the context
	      String matchedPart = remainingRef.substring(0, matcher.end());
	      Reference baseRef = call.getContext().getBaseRef();
	
	      if(baseRef == null)
	      {
	      	baseRef = new Reference(matchedPart);
	      }
	      else
	      {
	      	baseRef = new Reference(baseRef.toString(false, false) + matchedPart);
	      }
	      
      	call.getContext().setBaseRef(baseRef);
	
	      if(logger.isLoggable(Level.FINE))
	      {
	      	logger.fine("New base URI: " + call.getContext().getBaseRef());
	      	logger.fine("New relative path: " + call.getContext().getRelativePath());
	      }
	
	      if(logger.isLoggable(Level.FINE))
	      {
	      	logger.fine("Delegating the call to the target Restlet");
	      }
	
	      // Invoke the call restlet
	      super.handle(call);
	   }
      else
      {
      	call.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      }
	}   

	/**
	 * Returns the remaining reference following the base reference.
	 * @param call The call to handle.
	 * @return The remaining reference following the base reference.
	 */
	protected String getRemainingRef(Call call)
	{
		if(call.getContext().getBaseRef() != null)
		{
			return call.getResourceRef().toString().substring(call.getContext().getBaseRef().toString().length());
		}
		else
		{
			return call.getResourceRef().toString();
		}
	}
}
