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

package org.restlet.data;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Resource;
import org.restlet.spi.Factory;

/**
 * Generic response sent by server connectors. It is then received by client connectors. Responses 
 * are uniform across all types of connectors, protocols and components.
 * @see org.restlet.data.Request
 * @see org.restlet.Restlet
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Response extends Message
{
	/** The authentication request sent by an origin server to a client. */
	private ChallengeRequest challengeRequest;

	/** The cookie settings provided by the server. */
	private List<CookieSetting> cookieSettings;

	/** The redirection reference. */
	private Reference redirectRef;

	/** The associated request. */
	private Request request;

	/** The server-specific information. */
	private ServerInfo serverInfo;

	/** The status. */
	private Status status;

	/**
	 * Constructor.
	 * @param request The request associated to this response.
	 */
	public Response(Request request)
	{
		this.challengeRequest = null;
		this.cookieSettings = null;
		this.redirectRef = null;
		this.request = request;
		this.serverInfo = null;
		this.status = Status.SUCCESS_OK;
	}

	/**
	 * Returns the authentication request sent by an origin server to a client.
	 * @return The authentication request sent by an origin server to a client.
	 */
	public ChallengeRequest getChallengeRequest()
	{
		return this.challengeRequest;
	}

	/**
	 * Returns the cookie settings provided by the server.
	 * @return The cookie settings provided by the server.
	 */
	public List<CookieSetting> getCookieSettings()
	{
		if (this.cookieSettings == null)
			this.cookieSettings = new ArrayList<CookieSetting>();
		return this.cookieSettings;
	}

	/**
	 * Returns the reference that the client should follow for redirections or resource creations.
	 * @return The redirection reference.
	 */
	public Reference getRedirectRef()
	{
		return this.redirectRef;
	}

	/**
	 * Returns the associated request
	 * @return The associated request
	 */
	public Request getRequest()
	{
		return request;
	}

	/**
	 * Returns the server-specific information.
	 * @return The server-specific information.
	 */
	public ServerInfo getServerInfo()
	{
		if (this.serverInfo == null) this.serverInfo = new ServerInfo();
		return this.serverInfo;
	}

	/**
	 * Returns the status.
	 * @return The status.
	 */
	public Status getStatus()
	{
		return this.status;
	}

	/**
	 * Sets the authentication request sent by an origin server to a client.
	 * @param request The authentication request sent by an origin server to a client.
	 */
	public void setChallengeRequest(ChallengeRequest request)
	{
		this.challengeRequest = request;
	}

	/**
	 * Sets the entity with the best representation of a resource, according to the client preferences.
	 * <br/> If no representation is found, sets the status to "Not found".<br/>
	 * If no acceptable representation is available, sets the status to "Not acceptable".<br/>
	 * @param resource The resource for which the best representation needs to be set.
	 * @param fallbackLanguage The language to use if no preference matches.
	 * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
	 */
	public void setEntity(Resource resource, Language fallbackLanguage)
	{
		Factory.getInstance().setResponseEntity(getRequest(), this, resource, fallbackLanguage);
	}

	/**
	 * Sets the reference that the client should follow for redirections or resource creations.
	 * @param redirectUri The redirection URI.
	 */
	public void setRedirectRef(String redirectUri)
	{
		setRedirectRef(new Reference(getRequest().getBaseRef(), redirectUri).getTargetRef());
	}

	/**
	 * Sets the reference that the client should follow for redirections or resource creations.
	 * @param redirectRef The redirection reference.
	 */
	public void setRedirectRef(Reference redirectRef)
	{
		this.redirectRef = redirectRef;
	}

	/**
	 * Sets the associated request.
	 * @param request The associated request
	 */
	public void setRequest(Request request)
	{
		this.request = request;
	}

	/**
	 * Sets the status.
	 * @param status The status to set.
	 */
	public void setStatus(Status status)
	{
		this.status = status;
	}

	/**
	 * Sets the status.
	 * @param status The status to set.
	 * @param message The status message.
	 */
	public void setStatus(Status status, String message)
	{
		setStatus(new Status(status, message));
	}

}
