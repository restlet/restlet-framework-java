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
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientData;
import org.restlet.data.ConditionData;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Representation;

/**
 * Wrapper used to enrich a request with additional state or logic.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperRequest extends Request
{
	/** Wrapped request. */
	private Request wrappedRequest;

	/**
	 * Constructor.
	 * @param wrappedCall The wrapped call.
	 */
	public WrapperRequest(Request wrappedCall)
	{
		this.wrappedRequest = wrappedCall;
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
	 * Returns a modifiable attributes map that can be used by developers to save information relative
	 * to the current request. This is an easier alternative to the creation of a wrapper around the whole 
	 * request.
	 * @return The modifiable attributes map.
	 */
	public Map<String, Object> getAttributes()
	{
		return getWrappedRequest().getAttributes();
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
	 * Returns the client specific data.
	 * @return The client specific data.
	 */
	public ClientData getClient()
	{
		return getWrappedRequest().getClient();
	}

	/**
	 * Returns the condition data applying to this call.
	 * @return The condition data applying to this call.
	 */
	public ConditionData getCondition()
	{
		return getWrappedRequest().getCondition();
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
	 * Returns the representation provided by the client.
	 * @return The representation provided by the client.
	 */
	public Representation getInput()
	{
		return getWrappedRequest().getInput();
	}

	/**
	 * Returns the representation provided by the client as a form.<br/>
	 * Note that this triggers the parsing of the input representation.<br/>
	 * This method and the associated getInput method can only be invoked once.
	 * @return The input form provided by the client.
	 */
	public Form getInputAsForm()
	{
		return getWrappedRequest().getInputAsForm();
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
	 * Returns the protocol used by the call. It can either indicate the protocol used by a server connector
	 * to receive the call or the one that must be used to send the call. If the protocol is not specified 
	 * when sending a call, the implementation will attempt to guess it by looking at a scheme protocol 
	 * associated with the target resource reference. 
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
	 * Indicates if an input representation is available and can be sent to a client.
	 * Several conditions must be met: the method must allow the sending of input representations,
	 * the input representation must exists and has some available content.
	 * @return True if an input representation is available and can be sent to a client.
	 */
	public boolean isInputAvailable()
	{
		return getWrappedRequest().isInputAvailable();
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
	 * Sets the representation provided by the client.
	 * @param input The representation provided by the client.
	 */
	public void setInput(Representation input)
	{
		getWrappedRequest().setInput(input);
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
