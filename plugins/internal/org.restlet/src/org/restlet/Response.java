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
import java.util.Map;
import java.util.TreeMap;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.CookieSetting;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.Resource;
import org.restlet.data.ServerData;
import org.restlet.data.Status;
import org.restlet.spi.Factory;

/**
 * Generic response sent by server connectors. It is then received by client connectors. Responses 
 * are uniform across all types of connectors, protocols and components.
 * @see org.restlet.Request
 * @see org.restlet.UniformInterface
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Response
{
	/** The modifiable attributes map. */
	private Map<String, Object> attributes;

	/** The list of methods allowed on the requested resource. */ 
	private List<Method> allowedMethods;
	
   /** The authentication request sent by an origin server to a client. */
	private ChallengeRequest challengeRequest;

	/** The cookie settings provided by the server. */
	private List<CookieSetting> cookieSettings;
	
	/** The representation provided by the server. */
	private Representation output;

	/** The redirection reference. */
	private Reference redirectRef;

	/** The request associated to this response. */
	private Request request;
	
	/** The server data. */
	private ServerData server;

	/** The status. */
	private Status status;

	/**
	 * Constructor.
	 * @param request The request associated to this response.
	 */
	public Response(Request request)
	{
		this.request = request;
	}
	
	/**
	 * Returns the list of methods allowed on the requested resource.
	 * @return The list of allowed methods.
	 */
	public List<Method> getAllowedMethods()
	{
		if(this.allowedMethods == null)
		{
			this.allowedMethods = new ArrayList<Method>();
		}
		
		return this.allowedMethods;
	}

	/**
	 * Returns a modifiable attributes map that can be used by developers to save information relative
	 * to the current response. This is an easier alternative to the creation of a wrapper around the whole 
	 * response.<br/>
	 * <br/>
	 * In addition, this map is a shared space between the developer, the Restlet implementation and the
	 * connectors used. In this case, it is used to exchange information that is not uniform 
	 * across all protocols and couldn't therefore be directly included in the API. For this purpose, 
	 * all attribute names starting with "org.restlet" are reserved. Currently the following attributes 
	 * are used:
	 * <table>
	 * 	<tr>
	 * 		<th>Attribute name</th>
	 * 		<th>Class name</th>
	 * 		<th>Description</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>org.restlet.http.headers</td>
	 * 		<td>org.restlet.data.ParameterList</td>
	 * 		<td>Client HTTP connectors must provide all the response headers exactly as they were received
	 * from the server. When replying to server HTTP connectors, developers can also set this attribute to 
	 * specify <b>non-standard</b> HTTP headers that should be added to the response sent to a client.</td>
	 * 	</tr>
	 *	</table>
	 * Adding standard HTTP headers is forbidden because it could conflict with the connector's internal 
	 * behavior, limit portability or prevent future optimizations.</td>
	 * @return The modifiable attributes map.
	 */
	public Map<String, Object> getAttributes()
	{
		if (attributes == null)
		{
			attributes = new TreeMap<String, Object>();
		}

		return attributes;
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
	 * Returns the representation provided by the server.
	 * @return The representation provided by the server.
	 */
	public Representation getOutput()
	{
		return this.output;
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
	 * Returns the request associated to this response.
	 * @return The request associated to this response.
	 */
	public Request getRequest()
	{
		return request;
	}

	/**
	 * Returns the server specific data.
	 * @return The server specific data.
	 */
	public ServerData getServer()
	{
		if (this.server == null) this.server = new ServerData();
		return this.server;
	}

	/**
	 * Returns the call status.
	 * @return The call status.
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
	 * Sets the representation provided by the server.
	 * @param output The representation provided by the server.
	 */
	public void setOutput(Representation output)
	{
		this.output = output;
	}

	/**
	 * Sets a textual representation provided by the server.
    * @param value The represented string.
    * @param mediaType The representation's media type.
	 */
	public void setOutput(String value, MediaType mediaType)
	{
		setOutput(Factory.getInstance().createRepresentation(value, mediaType));
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
		Factory.getInstance().setOutput(getRequest(), this, resource, fallbackLanguage);
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
	 * Sets the request associated to this response.
	 * @param request The request associated to this response.
	 */
	public void setRequest(Request request)
	{
		this.request = request;
	}

	/**
	 * Sets the call status.
	 * @param status The call status to set.
	 */
	public void setStatus(Status status)
	{
		this.status = status;
	}

}
