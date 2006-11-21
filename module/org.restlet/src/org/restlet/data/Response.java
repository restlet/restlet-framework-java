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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.restlet.resource.Representation;
import org.restlet.resource.Resource;

/**
 * Generic response sent by server connectors. It is then received by client connectors. Responses 
 * are uniform across all types of connectors, protocols and components.
 * @see org.restlet.data.Request
 * @see org.restlet.Restlet
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Response extends Message
{
	/** The authentication request sent by an origin server to a client. */
	private ChallengeRequest challengeRequest;

	/** The cookie settings provided by the server. */
	private List<CookieSetting> cookieSettings;

	/** The set of dimensions on which the response entity may vary. */
	private Set<Dimension> dimensions;

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
		this.dimensions = null;
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
	 * Returns the set of selecting dimensions on which the response entity may vary. If some server-side 
	 * content negotiation is done, this set should be properly updated, other it can be left empty. 
	 * @return The set of dimensions on which the response entity may vary.
	 */
	public Set<Dimension> getDimensions()
	{
		if (this.dimensions == null) this.dimensions = new HashSet<Dimension>();
		return this.dimensions;
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
	 * @deprecated Use other setEntity method.
	 */
	@Deprecated
	public void setEntity(Resource resource, Language fallbackLanguage)
	{
		setEntity(resource);
	}

	/**
	 * Sets the entity representation. If the request conditions are matched, the status is set to 
	 * REDIRECTION_NOT_MODIFIED, otherwise the entity is set.
	 * @param entity The entity representation.
	 */
	public void setEntity(Representation entity)
	{
		if (getRequest().getConditions().isModified(entity))
		{
			// Send the representation as the response entity
			setStatus(Status.SUCCESS_OK);
			super.setEntity(entity);
		}
		else
		{
			// Indicates to the client that he already has the best representation 
			setStatus(Status.REDIRECTION_NOT_MODIFIED);
		}
	}

	/**
	 * Sets the entity with the preferred representation of a resource, according to the client preferences.
	 * <br/> If no representation is found, sets the status to "Not found".<br/>
	 * If no acceptable representation is available, sets the status to "Not acceptable".<br/>
	 * @param resource The resource for which the best representation needs to be set.
	 * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
	 */
	public void setEntity(Resource resource)
	{
		List<Representation> variants = resource.getVariants();

		if ((variants == null) || (variants.size() < 1))
		{
			// Resource not found
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
		else
		{
			// Set the variants' resource
			for (Representation variant : variants)
			{
				variant.setResource(resource);
			}

			// Compute the preferred variant
			Representation preferredVariant = getRequest().getClientInfo()
					.getPreferredVariant(variants);

			if (preferredVariant == null)
			{
				// No variant was found matching the client preferences
				setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
			}
			else
			{
				setEntity(preferredVariant);
			}
		}
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
	 * Sets the reference that the client should follow for redirections or resource creations.
	 * @param redirectUri The redirection URI.
	 */
	public void setRedirectRef(String redirectUri)
	{
		setRedirectRef(new Reference(getRequest().getBaseRef(), redirectUri).getTargetRef());
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
