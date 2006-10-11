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
import org.restlet.Resource;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.CookieSetting;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;

/**
 * Wrapper used to enrich a response with additional state or logic.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperResponse extends Response
{
	/** Wrapped request. */
	private Response wrappedResponse;

	/**
	 * Constructor.
	 * @param wrappedResponse The wrapped response.
	 */
	public WrapperResponse(Response wrappedResponse)
	{
		super(null);
		this.wrappedResponse = wrappedResponse;
	}

	/**
	 * Returns the wrapped response.
	 * @return The wrapped response.
	 */
	protected Response getWrappedResponse()
	{
		return this.wrappedResponse;
	}

	/**
	 * Returns the list of methods allowed on the requested resource.
	 * @return The list of allowed methods.
	 */
	public List<Method> getAllowedMethods()
	{
		return getWrappedResponse().getAllowedMethods();
	}

	/**
	 * Returns a modifiable attributes map that can be used by developers to save information relative
	 * to the current response. This is an easier alternative to the creation of a wrapper around the whole 
	 * response.
	 * @return The modifiable attributes map.
	 */
	public Map<String, Object> getAttributes()
	{
		return getWrappedResponse().getAttributes();
	}

   /**
    * Returns the authentication request sent by an origin server to a client.
    * @return The authentication request sent by an origin server to a client.
    */
   public ChallengeRequest getChallengeRequest()
   {
      return getWrappedResponse().getChallengeRequest();
   }

	/**
	 * Returns the cookie settings provided by the server.
	 * @return The cookie settings provided by the server.
	 */
	public List<CookieSetting> getCookieSettings()
	{
		return getWrappedResponse().getCookieSettings();
	}

	/**
	 * Returns the representation provided by the server.
	 * @return The representation provided by the server.
	 */
	public Representation getOutput()
	{
		return getWrappedResponse().getOutput();
	}
	
	/**
	 * Returns the reference that the client should follow for redirections or resource creations.
	 * @return The redirection reference.
	 */
	public Reference getRedirectRef()
	{
		return getWrappedResponse().getRedirectRef();
	}

	/**
	 * Returns the request associated to this response.
	 * @return The request associated to this response.
	 */
	public Request getRequest()
	{
		return getWrappedResponse().getRequest();
	}

	/**
	 * Returns the server specific data.
	 * @return The server specific data.
	 */
	public ServerInfo getServer()
	{
		return getWrappedResponse().getServer();
	}

	/**
	 * Returns the call status.
	 * @return The call status.
	 */
	public Status getStatus()
	{
		return getWrappedResponse().getStatus();
	}

   /**
    * Sets the authentication request sent by an origin server to a client.
    * @param request The authentication request sent by an origin server to a client.
    */
   public void setChallengeRequest(ChallengeRequest request)
   {
   	getWrappedResponse().setChallengeRequest(request);
   }

	/**
	 * Sets the representation provided by the server.
	 * @param output The representation provided by the server.
	 */
	public void setOutput(Representation output)
	{
		getWrappedResponse().setOutput(output);
	}

	/**
	 * Sets a textual representation provided by the server.
    * @param value The represented string.
    * @param mediaType The representation's media type.
	 */
	public void setOutput(String value, MediaType mediaType)
	{
		getWrappedResponse().setOutput(value, mediaType);
	}

	/**
	 * Sets the best output representation of a given resource according to the client preferences.<br/>
	 * If no representation is found, sets the status to "Not found".<br/>
	 * If no acceptable representation is available, sets the status to "Not acceptable".<br/>
	 * @param resource The resource for which the best representation needs to be set.
	 * @param fallbackLanguage The language to use if no preference matches.
	 * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
	 */
	public void setOutput(Resource resource, Language fallbackLanguage)
	{
		getWrappedResponse().setOutput(resource, fallbackLanguage);
	}

	/**
	 * Sets the reference that the client should follow for redirections or resource creations.
	 * @param redirectUri The redirection URI.
	 */
	public void setRedirectRef(String redirectUri)
	{
		getWrappedResponse().setRedirectRef(redirectUri);
	}

	/**
	 * Sets the reference that the client should follow for redirections or resource creations.
	 * @param redirectRef The redirection reference.
	 */
	public void setRedirectRef(Reference redirectRef)
	{
		getWrappedResponse().setRedirectRef(redirectRef);
	}

	/**
	 * Sets the request associated to this response.
	 * @param request The request associated to this response.
	 */
	public void setRequest(Request request)
	{
		getWrappedResponse().setRequest(request);
	}

	/**
	 * Sets the call status.
	 * @param status The call status to set.
	 */
	public void setStatus(Status status)
	{
		getWrappedResponse().setStatus(status);
	}

}
