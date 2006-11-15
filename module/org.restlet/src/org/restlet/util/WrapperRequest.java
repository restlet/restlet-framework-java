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

package org.restlet.util;

import java.util.List;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;

/**
 * Request wrapper. Useful for application developer who need to enrich the request
 * with application related properties and behavior.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperRequest extends Request
{
	/** The wrapped request. */
	private Request wrappedRequest;

	/**
	 * Constructor.
	 * @param wrappedRequest The wrapped request.
	 */
	public WrapperRequest(Request wrappedRequest)
	{
		this.wrappedRequest = wrappedRequest;
	}

	/**
	 * Returns the wrapped request.
	 * @return The wrapped request.
	 */
	protected Request getWrappedRequest()
	{
		return this.wrappedRequest;
	}

	/**
	 * Returns the base reference.
	 * @return The base reference.
	 */
	public Reference getBaseRef()
	{
		return getWrappedRequest().getBaseRef();
	}

	/**
	 * Returns the authentication response sent by a client to an origin server.
	 * @return The authentication response sent by a client to an origin server.
	 */
	public ChallengeResponse getChallengeResponse()
	{
		return getWrappedRequest().getChallengeResponse();
	}

	/**
	 * Returns the client-specific information.
	 * @return The client-specific information.
	 */
	public ClientInfo getClientInfo()
	{
		return getWrappedRequest().getClientInfo();
	}

	/**
	 * Returns the conditions applying to this call.
	 * @return The conditions applying to this call.
	 */
	public Conditions getConditions()
	{
		return getWrappedRequest().getConditions();
	}

	/**
	 * Returns the cookies provided by the client.
	 * @return The cookies provided by the client.
	 */
	public List<Cookie> getCookies()
	{
		return getWrappedRequest().getCookies();
	}

	/**
	 * Returns the method.
	 * @return The method.
	 */
	public Method getMethod()
	{
		return getWrappedRequest().getMethod();
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
		return getWrappedRequest().getProtocol();
	}

	/**
	 * Returns the referrer reference if available.
	 * @return The referrer reference.
	 */
	public Reference getReferrerRef()
	{
		return getWrappedRequest().getReferrerRef();
	}

	/**
	 * Returns the resource path relative to the context's base reference.
	 * @return The relative resource path .
	 */
	public String getRelativePart()
	{
		return getWrappedRequest().getRelativePart();
	}

	/**
	 * Returns the resource reference relative to the context's base reference.
	 * @return The relative resource reference.
	 */
	public Reference getRelativeRef()
	{
		return getWrappedRequest().getRelativeRef();
	}

	/**
	 * Returns the reference of the target resource.
	 * @return The reference of the target resource.
	 */
	public Reference getResourceRef()
	{
		return getWrappedRequest().getResourceRef();
	}

	/**
	 * Indicates if the call came over a confidential channel
	 * such as an SSL-secured connection.
	 * @return True if the call came over a confidential channel.
	 */
	public boolean isConfidential()
	{
		return getWrappedRequest().isConfidential();
	}

	/**
	 * Indicates if a content is available and can be sent. Several conditions must be met: the method 
	 * must allow the sending of content, the content must exists and have some available data.
	 * @return True if a content is available and can be sent.
	 */
	public boolean isEntityAvailable()
	{
		return getWrappedRequest().isEntityAvailable();
	}

	/**
	 * Sets the base reference that will serve to compute relative resource references.
	 * @param baseUri The base absolute URI.
	 */
	public void setBaseRef(String baseUri)
	{
		getWrappedRequest().setBaseRef(baseUri);
	}

	/**
	 * Sets the base reference that will serve to compute relative resource references.
	 * @param baseRef The base reference.
	 */
	public void setBaseRef(Reference baseRef)
	{
		getWrappedRequest().setBaseRef(baseRef);
	}

	/**
	 * Sets the authentication response sent by a client to an origin server.
	 * @param response The authentication response sent by a client to an origin server.
	 */
	public void setChallengeResponse(ChallengeResponse response)
	{
		getWrappedRequest().setChallengeResponse(response);
	}

	/**
	 * Indicates if the call came over a confidential channel
	 * such as an SSL-secured connection.
	 * @param confidential True if the call came over a confidential channel.
	 */
	public void setConfidential(boolean confidential)
	{
		getWrappedRequest().setConfidential(confidential);
	}

	/**
	 * Sets the method called.
	 * @param method The method called.
	 */
	public void setMethod(Method method)
	{
		getWrappedRequest().setMethod(method);
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
		getWrappedRequest().setProtocol(protocol);
	}

	/**
	 * Sets the referrer reference if available using an URI string.
	 * @param referrerUri The referrer URI.
	 */
	public void setReferrerRef(String referrerUri)
	{
		getWrappedRequest().setReferrerRef(referrerUri);
	}

	/**
	 * Sets the referrer reference if available.
	 * @param referrerRef The referrer reference.
	 */
	public void setReferrerRef(Reference referrerRef)
	{
		getWrappedRequest().setReferrerRef(referrerRef);
	}

	/**
	 * Sets the target resource reference using an URI string. Note that the URI can be either
	 * absolute or relative to the context's base reference.
	 * @param resourceUri The resource URI.
	 */
	public void setResourceRef(String resourceUri)
	{
		getWrappedRequest().setResourceRef(resourceUri);
	}

	/**
	 * Sets the target resource reference. If the reference is relative, it will be resolved as an
	 * absolute reference. Also, the context's base reference will be reset. Finally, the reference
	 * will be normalized to ensure a consistent handling of the call.
	 * @param resourceRef The resource reference.
	 */
	public void setResourceRef(Reference resourceRef)
	{
		getWrappedRequest().setResourceRef(resourceRef);
	}

}
