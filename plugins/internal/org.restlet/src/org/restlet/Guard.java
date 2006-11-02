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

import java.util.Map;
import java.util.TreeMap;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Filter guarding the access to an attached Restlet. 
 * @see <a href="http://www.restlet.org/tutorial#part09">Tutorial: Guarding access to sensitive resources</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Guard extends Filter
{
	/** Map of authorizations (login/password combinations). */
	private Map<String, String> authorizations;

	/** The authentication scheme. */
	private ChallengeScheme scheme;

	/** The authentication realm. */
	private String realm;

	/**
	 * @deprecated Please use the new constructor, authentication now done at the connector level.
	 */
	@Deprecated
	public Guard(Context context, boolean authenticate, ChallengeScheme scheme, String realm, boolean authorize)
	{
	}
		
	/**
	 * Constructor.
	 * @param context The context.
	 * @param scheme The authentication scheme to use. 
	 * @param realm The authentication realm.
	 */
	public Guard(Context context, ChallengeScheme scheme, String realm)
	{
		super(context);
		this.authorizations = null;

		if ((scheme == null))
		{
			throw new IllegalArgumentException(
					"Please specify a challenge scheme. Use the 'None' challenge if no authentication is required.");
		}
		else
		{
			this.scheme = scheme;
			this.realm = realm;
		}
	}

	/**
	 * Handles the call by distributing it to the next Restlet. 
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void doHandle(Request request, Response response)
	{
		int result = authorize(request);

		if (result == 1)
		{
			accept(request, response);
		}
		else
		{
			reject(response, (result == 0));
		}
	}

	/**
	 * Returns the map of authorizations (identifier/secret combinations).
	 * @return The map of authorizations (identifier/secret combinations).
	 */
	public Map<String, String> getAuthorizations()
	{
		if (this.authorizations == null)
			this.authorizations = new TreeMap<String, String>();
		return this.authorizations;
	}

	/**
	 * Indicates if the call is authorized to pass through the Guard Filter.
	 * At this point the caller should be authenticated and the security data should contain a valid login,
	 * password and optionnaly a role name.<br/>
	 * The application should take care of the authorization logic, based on custom criteria such as
	 * checking whether the current user has the proper role or access rights.<br/>
	 * By default, a call is authorized if the call's login/password couple is available in the map of authorizations. 
	 * Of course, this method is meant to be overriden by subclasses requiring a custom authorization mechanism.
	 * @param request The request to authorize.
	 * @return -1 if the given credentials were invalid, 0 if no credentials were found and 1 otherwise.
	 */
	protected int authorize(Request request)
	{
		int result = 0;
		String identifier = request.getChallengeResponse().getIdentifier();
		String secret = request.getChallengeResponse().getSecret();

		if ((identifier != null) && (secret != null))
		{
			if (secret.equals(getAuthorizations().get(identifier)))
			{
				result = 1;
			}
			else
			{
				result = -1;
			}
		}

		return result;
	}

	/**
	 * Accepts the call.
	 * By default, invokes the attached Restlet.
	 * @param request The request to accept.
	 * @param response The response to accept.
	 */
	protected void accept(Request request, Response response)
	{
		// Invoke the chained Restlet
		super.doHandle(request, response);
	}

	/**
	 * Rejects the call call due to a failed authentication or authorization.
	 * This can be overriden to change the defaut behavior, for example to display an error page.
	 * By default, if authentication is required, the challenge method is invoked, otherwise the 
	 * call status is set to CLIENT_ERROR_FORBIDDEN.
	 * @param response The reject response.
	 * @param challenge Indicates if the client should be challenged.
	 */
	protected void reject(Response response, boolean challenge)
	{
		if (challenge)
		{
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			response.setChallengeRequest(new ChallengeRequest(this.scheme, this.realm));
		}
		else
		{
			response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
		}
	}

}
