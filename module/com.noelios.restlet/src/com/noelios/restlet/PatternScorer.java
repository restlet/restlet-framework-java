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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.Scorer;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.noelios.restlet.util.ReferenceTemplate;

/**
 * Router scorer based on a URI pattern. Note that the matching is case sensitive unless some inline modifiers
 * were used in the pattern using the "(?i)" inline flag.
 * @see java.util.regex.Pattern
 * @see <a href="http://javaalmanac.com/egs/java.util.regex/pkg.html">Java Almanac on the Regex package</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class PatternScorer extends Scorer
{
	/** The URI pattern. */
	private Pattern pattern;

	/** The list of variable names found in the pattern. */
	private List<String> variables;

	/**
	 * Constructor.
	 * @param router The parent router.
	 * @param pattern The URI pattern.
	 * @param target The target Restlet.
	 */
	public PatternScorer(Router router, String pattern, Restlet target)
	{
		super(router, target);
		this.variables = new ArrayList<String>();
		this.pattern = compile(pattern);
	}

	/**
	 * Compiles the URI pattern into a Regex pattern.
	 * @param uriPattern The URI pattern.
	 * @return The Regex pattern.
	 */
	protected Pattern compile(String uriPattern)
	{
		ReferenceTemplate rt = new ReferenceTemplate(uriPattern, getVariables());
		String regex = rt.toRegex();
		return Pattern.compile(regex);
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
	 * Returns the URI variables.
	 * @return The URI variables.
	 */
	public List<String> getVariables()
	{
		return this.variables;
	}

	/**
	 * Returns the score for a given call (between 0 and 1.0).
	 * @param request The request to score.
	 * @param response The response to score.
	 * @return The score for a given call (between 0 and 1.0).
	 */
	public float score(Request request, Response response)
	{
		float result = 0F;
		String remainingRef = request.getRelativePart();
		Matcher matcher = getPattern().matcher(remainingRef);
		boolean matched = matcher.lookingAt();

		if (matched)
		{
			float totalLength = remainingRef.length();

			if (totalLength > 0.0F)
			{
				float matchedLength = matcher.end();
				result = getRouter().getRequiredScore()
						+ (1.0F - getRouter().getRequiredScore())
						* (matchedLength / totalLength);
			}
			else
			{
				result = 1.0F;
			}
		}

		if (getLogger().isLoggable(Level.FINER))
		{
			getLogger().finer(
					"Call score for the \"" + getPattern().toString() + "\" URI pattern: "
							+ result);
		}

		return result;
	}

	/**
	 * Allows filtering before processing by the next Restlet. Set the base reference. 
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void beforeHandle(Request request, Response response)
	{
		String remainingRef = request.getRelativePart();
		Matcher matcher = getPattern().matcher(remainingRef);
		boolean matched = matcher.lookingAt();

		if (getLogger().isLoggable(Level.FINER))
		{
			getLogger().finer(
					"Attempting to match this pattern: " + getPattern().toString() + " >> "
							+ matched);
		}

		if (matched)
		{
			// Update the attributes with the variables value
			String attributeName = null;
			String attributeValue = null;
			for (int i = 0; i < getVariables().size(); i++)
			{
				attributeName = getVariables().get(i);
				attributeValue = matcher.group(i + 1);
				request.getAttributes().put(attributeName, attributeValue);
			}

			// Updates the context
			String matchedPart = remainingRef.substring(0, matcher.end());
			Reference baseRef = request.getBaseRef();

			if (baseRef == null)
			{
				baseRef = new Reference(matchedPart);
			}
			else
			{
				baseRef = new Reference(baseRef.toString(false, false) + matchedPart);
			}

			request.setBaseRef(baseRef);

			if (getLogger().isLoggable(Level.FINE))
			{
				getLogger().fine("New base URI: " + request.getBaseRef());
				getLogger().fine("New relative part: " + request.getRelativePart());
			}

			if (getLogger().isLoggable(Level.FINE))
			{
				getLogger().fine("Delegating the call to the target Restlet");
			}
		}
		else
		{
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}
}
