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

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Representation;

/**
 * Generic request sent by client connectors. It is then received by server connectors and processed 
 * by handlers implementing UniformInterface. This request can also be processed by a chain of 
 * handlers, on the client or server sides. Requests are uniform across all types of connectors, 
 * protocols and components.
 * @see org.restlet.Response
 * @see org.restlet.UniformInterface
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Request
{
	/** The modifiable attributes map. */
	private Map<String, Object> attributes;

	/** The authentication response sent by a client to an origin server. */
	private ChallengeResponse challengeResponse;

	/** The base reference. */
	private Reference baseRef;

	/** The client data. */
	private ClientInfo client;

	/** The condition data. */
	private Conditions conditions;

	/** Indicates if the call came over a confidential channel. */
	private boolean confidential;

	/** The cookies provided by the client. */
	private List<Cookie> cookies;

	/** The representation provided by the client. */
	private Representation input;

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
	 * @param input The input representation to send.
	 */
	public Request(Method method, Reference resourceRef, Representation input)
	{
		this();
		setMethod(method);
		setResourceRef(resourceRef);
		setInput(input);
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
	 * @param input The input representation to send.
	 */
	public Request(Method method, String resourceUri, Representation input)
	{
		this(method, new Reference(resourceUri), input);
	}

	/**
	 * Returns a modifiable attributes map that can be used by developers to save information relative
	 * to the current request. This is an easier alternative to the creation of a wrapper around the whole 
	 * request.<br/>
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
	 * 		<td>Server HTTP connectors must provide all the request headers exactly as they were received
	 * from the client. When invoking client HTTP connectors, developers can also set this attribute to 
	 * specify <b>non-standard</b> HTTP headers that should be added to the request sent to a server.</td>
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
	 * Returns the client specific data.
	 * @return The client specific data.
	 */
	public ClientInfo getClient()
	{
		if (this.client == null) this.client = new ClientInfo();
		return this.client;
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
	 * Returns the representation provided by the client.
	 * @return The representation provided by the client.
	 */
	public Representation getInput()
	{
		return this.input;
	}

	/**
	 * Returns the representation provided by the client as a form.<br/>
	 * Note that this triggers the parsing of the input representation.<br/>
	 * This method and the associated getInput method can only be invoked once.
	 * @return The input form provided by the client.
	 */
	public Form getInputAsForm()
	{
		return new Form(getInput());
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
	 * Returns the protocol used by the call. It can either indicate the protocol used by a server connector
	 * to receive the call or the one that must be used to send the call. If the protocol is not specified 
	 * when sending a call, the implementation will attempt to guess it by looking at a scheme protocol 
	 * associated with the target resource reference. 
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
		if(getBaseRef() != null)
		{
			return getResourceRef().toString(false, false).substring(getBaseRef().toString().length());
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
	 * Indicates if an input representation is available and can be sent to a client.
	 * Several conditions must be met: the method must allow the sending of input representations,
	 * the input representation must exists and has some available content.
	 * @return True if an input representation is available and can be sent to a client.
	 */
	public boolean isInputAvailable()
	{
		boolean result = true;

		if (getMethod().equals(Method.GET) || getMethod().equals(Method.HEAD)
				|| getMethod().equals(Method.DELETE))
		{
			result = false;
		}
		else
		{
			result = (getInput() != null) && getInput().isAvailable() && (getInput().getSize() > 0);
		}

		return result;
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
	   if(getResourceRef() == null)
	   {
	      throw new IllegalArgumentException("You must specify a resource reference before setting a base reference");
	   }
	   else if((baseRef != null) && !baseRef.isParent(getResourceRef()))
	   {
	   	new IllegalArgumentException("You must specify a base reference that is a parent of the resource reference");
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
	 * Sets the representation provided by the client.
	 * @param input The representation provided by the client.
	 */
	public void setInput(Representation input)
	{
		this.input = input;
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
		if((resourceRef != null) && resourceRef.isRelative() && (resourceRef.getBaseRef() != null))
		{
			this.resourceRef = resourceRef.getTargetRef();
		}
		else
		{
			this.resourceRef = resourceRef.normalize();
		}
		
		// Reset the context's base reference
		setBaseRef((Reference)null);
	}
	
}
