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

/**
 * Generic request sent by client connectors. It is then received by server connectors and processed 
 * by handlers implementing UniformInterface. This request can also be processed by a chain of 
 * handlers, on the client or server sides. Requests are uniform across all types of connectors, 
 * protocols and components.
 * @see org.restlet.data.Response
 * @see org.restlet.UniformInterface
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Request extends Message
{
	/** The authentication response sent by a client to an origin server. */
	private ChallengeResponse challengeResponse;

	/** The base reference. */
	private Reference baseRef;

	/** The client data. */
	private ClientInfo clientInfo;

	/** The condition data. */
	private Conditions conditions;

	/** Indicates if the call came over a confidential channel. */
	private boolean confidential;

	/** The cookies provided by the client. */
	private List<Cookie> cookies;

	/** The method. */
	private Method method;

	/** The protocol. */
	private Protocol protocol;

	/** The referrer reference. */
	private Reference referrerRef;

	/** The resource reference. */
	private Reference resourceRef;

	/**
	 * Constructor.
	 */
	public Request()
	{
		this.confidential = false;
	}

	/**
	 * Constructor.
	 * @param method The call's method.
	 * @param resourceRef The resource reference.
	 */
	public Request(Method method, Reference resourceRef)
	{
		this(method, resourceRef, null);
	}

	/**
	 * Constructor.
	 * @param method The call's method.
	 * @param resourceRef The resource reference.
	 * @param entity The entity.
	 */
	public Request(Method method, Reference resourceRef, Representation entity)
	{
		super(entity);
		setMethod(method);
		setResourceRef(resourceRef);
	}

	/**
	 * Constructor.
	 * @param method The call's method.
	 * @param resourceUri The resource URI.
	 */
	public Request(Method method, String resourceUri)
	{
		this(method, new Reference(resourceUri));
	}

	/**
	 * Constructor.
	 * @param method The call's method.
	 * @param resourceUri The resource URI.
	 * @param entity The entity.
	 */
	public Request(Method method, String resourceUri, Representation entity)
	{
		this(method, new Reference(resourceUri), entity);
	}

	/**
	 * Returns the base reference.
	 * @return The base reference.
	 */
	public Reference getBaseRef()
	{
		return this.baseRef;
	}

	/**
	 * Returns the authentication response sent by a client to an origin server.
	 * @return The authentication response sent by a client to an origin server.
	 */
	public ChallengeResponse getChallengeResponse()
	{
		return this.challengeResponse;
	}

	/**
	 * Returns the client-specific information.
	 * @return The client-specific information.
	 */
	public ClientInfo getClientInfo()
	{
		if (this.clientInfo == null) this.clientInfo = new ClientInfo();
		return this.clientInfo;
	}

	/**
	 * Returns the conditions applying to this call.
	 * @return The conditions applying to this call.
	 */
	public Conditions getConditions()
	{
		if (this.conditions == null) this.conditions = new Conditions();
		return this.conditions;
	}

	/**
	 * Returns the cookies provided by the client.
	 * @return The cookies provided by the client.
	 */
	public List<Cookie> getCookies()
	{
		if (this.cookies == null) this.cookies = new ArrayList<Cookie>();
		return this.cookies;
	}

	/**
	 * Returns the method.
	 * @return The method.
	 */
	public Method getMethod()
	{
		return this.method;
	}

	/**
	 * Returns the protocol. It can either indicate the protocol used by a server connector to receive 
	 * or the one that must be used to send. If the protocol is not specified when sending a request, 
	 * the implementation will attempt to guess it by looking at a scheme protocol associated with the 
	 * target resource reference. 
	 * @return The protocol or null if not available.
	 */
	public Protocol getProtocol()
	{
		return this.protocol;
	}

	/**
	 * Returns the referrer reference if available.
	 * @return The referrer reference.
	 */
	public Reference getReferrerRef()
	{
		return this.referrerRef;
	}

	/**
	 * Returns the resource path relative to the context's base reference.
	 * @return The relative resource path .
	 */
	public String getRelativePart()
	{
		if (getBaseRef() != null)
		{
			return getResourceRef().toString(false, false).substring(
					getBaseRef().toString().length());
		}
		else
		{
			return getResourceRef().toString(false, false);
		}
	}

	/**
	 * Returns the resource reference relative to the context's base reference.
	 * @return The relative resource reference.
	 */
	public Reference getRelativeRef()
	{
		return getResourceRef().getRelativeRef(getBaseRef());
	}

	/**
	 * Returns the reference of the target resource.
	 * @return The reference of the target resource.
	 */
	public Reference getResourceRef()
	{
		return this.resourceRef;
	}

	/**
	 * Indicates if the call came over a confidential channel
	 * such as an SSL-secured connection.
	 * @return True if the call came over a confidential channel.
	 */
	public boolean isConfidential()
	{
		return this.confidential;
	}

	/**
	 * Indicates if a content is available and can be sent. Several conditions must be met: the method 
	 * must allow the sending of content, the content must exists and have some available data.
	 * @return True if a content is available and can be sent.
	 */
	public boolean isEntityAvailable()
	{
		if (getMethod().equals(Method.GET) || getMethod().equals(Method.HEAD)
				|| getMethod().equals(Method.DELETE))
		{
			return false;
		}
		else
		{
			return super.isEntityAvailable();
		}
	}

	/**
	 * Sets the base reference that will serve to compute relative resource references.
	 * @param baseUri The base absolute URI.
	 */
	public void setBaseRef(String baseUri)
	{
		setBaseRef(new Reference(baseUri));
	}

	/**
	 * Sets the base reference that will serve to compute relative resource references.
	 * @param baseRef The base reference.
	 */
	public void setBaseRef(Reference baseRef)
	{
		if (getResourceRef() == null)
		{
			throw new IllegalArgumentException(
					"You must specify a resource reference before setting a base reference");
		}
		else if ((baseRef != null) && !baseRef.isParent(getResourceRef()))
		{
			new IllegalArgumentException(
					"You must specify a base reference that is a parent of the resource reference");
		}

		this.baseRef = baseRef;
	}

	/**
	 * Sets the authentication response sent by a client to an origin server.
	 * @param response The authentication response sent by a client to an origin server.
	 */
	public void setChallengeResponse(ChallengeResponse response)
	{
		this.challengeResponse = response;
	}

	/**
	 * Indicates if the call came over a confidential channel
	 * such as an SSL-secured connection.
	 * @param confidential True if the call came over a confidential channel.
	 */
	public void setConfidential(boolean confidential)
	{
		this.confidential = confidential;
	}

	/**
	 * Sets the method called.
	 * @param method The method called.
	 */
	public void setMethod(Method method)
	{
		this.method = method;
	}

	/**
	 * Sets the protocol used by the call. It can either indicate the protocol used by a server connector
	 * to receive the call or the one that must be used to send the call. If the protocol is not specified 
	 * when sending a call, the implementation will attempt to guess it by looking at a scheme protocol 
	 * associated with the target resource reference. 
	 * @param protocol The protocol to set.
	 */
	public void setProtocol(Protocol protocol)
	{
		this.protocol = protocol;
	}

	/**
	 * Sets the referrer reference if available using an URI string.
	 * @param referrerUri The referrer URI.
	 */
	public void setReferrerRef(String referrerUri)
	{
		setReferrerRef(new Reference(referrerUri));
	}

	/**
	 * Sets the referrer reference if available.
	 * @param referrerRef The referrer reference.
	 */
	public void setReferrerRef(Reference referrerRef)
	{
		this.referrerRef = referrerRef;
	}

	/**
	 * Sets the target resource reference using an URI string. Note that the URI can be either
	 * absolute or relative to the context's base reference.
	 * @param resourceUri The resource URI.
	 */
	public void setResourceRef(String resourceUri)
	{
		setResourceRef(new Reference(getBaseRef(), resourceUri));
	}

	/**
	 * Sets the target resource reference. If the reference is relative, it will be resolved as an
	 * absolute reference. Also, the context's base reference will be reset. Finally, the reference
	 * will be normalized to ensure a consistent handling of the call.
	 * @param resourceRef The resource reference.
	 */
	public void setResourceRef(Reference resourceRef)
	{
		if ((resourceRef != null) && resourceRef.isRelative()
				&& (resourceRef.getBaseRef() != null))
		{
			this.resourceRef = resourceRef.getTargetRef();
		}
		else
		{
			this.resourceRef = resourceRef.normalize();
		}

		// Reset the context's base reference
		setBaseRef((Reference) null);
	}

}
