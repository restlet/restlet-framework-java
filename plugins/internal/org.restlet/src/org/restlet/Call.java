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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.ClientData;
import org.restlet.data.ConditionData;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.Resource;
import org.restlet.data.SecurityData;
import org.restlet.data.ServerData;
import org.restlet.data.Status;
import org.restlet.spi.Factory;

/**
 * Uniform call handled by Restlets. Issued by a client to a server and handled by one or more Restlets. Calls are 
 * uniform across all type of connectors (client or server), all types of protocols and component. If you are 
 * familiar with the Servlet API, a Restlet call merges and abstracts the Servlet HTTP request and response, 
 * as well as the HttpUrlConnection class from the JDK.
 * @see org.restlet.Restlet
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Call
{
   /** Error message. */
   private static final String UNABLE_TO_START = "Unable to start the target Restlet";
   
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(Call.class.getCanonicalName());

	/** The modifiable attributes map. */
	private Map<String, Object> attributes;

	/** The base reference. */
	private Reference baseRef;
	
	/** The client data. */
	private ClientData client;

	/** The condition data. */
	private ConditionData condition;

	/** The cookies provided by the client. */
	private List<Cookie> cookies;

	/** The cookie settings provided by the server. */
	private List<CookieSetting> cookieSettings;

	/** The representation provided by the client. */
	private Representation input;

	/** The method. */
	private Method method;

	/** The representation provided by the server. */
	private Representation output;

	/** The redirection reference. */
	private Reference redirectRef;

	/** The referrer reference. */
	private Reference referrerRef;

	/** The resource reference. */
	private Reference resourceRef;

	/** The security data. */
	private SecurityData security;

	/** The server data. */
	private ServerData server;

	/** The status. */
	private Status status;

	/**
	 * Constructor.
	 */
	public Call()
	{
	}

	/**
	 * Constructor.
	 * @param method The call's method.
	 * @param resourceRef The resource reference.
	 */
	public Call(Method method, Reference resourceRef)
	{
		this();
		setMethod(method);
		setResourceRef(resourceRef);
	}

	/**
	 * Constructor.
	 * @param method The call's method.
	 * @param resourceUri The resource URI.
	 */
	public Call(Method method, String resourceUri)
	{
		this();
		setMethod(method);
		setResourceRef(resourceUri);
	}

	/**
	 * Returns a modifiable attributes map that can be used by developers to save information relative
	 * to the current call. This is an easier alternative to the creation of a wrapper around the whole call.
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
	 * Returns the client specific data.
	 * @return The client specific data.
	 */
	public ClientData getClient()
	{
		if (this.client == null) this.client = new ClientData();
		return this.client;
	}

	/**
	 * Returns the condition data applying to this call.
	 * @return The condition data applying to this call.
	 */
	public ConditionData getCondition()
	{
		if (this.condition == null) this.condition = new ConditionData();
		return this.condition;
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
		try
		{
			return new Form(getInput());
		}
		catch (IOException e)
		{
			return null;
		}
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
	 * Returns the security data related to this call.
	 * @return The security data related to this call.
	 */
	public SecurityData getSecurity()
	{
		if (this.security == null) this.security = new SecurityData();
		return this.security;
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
	 * Handles a call with a given target Restlet. 
	 * @param target The target Restlet.
	 */
	public void handle(Restlet target)
	{
   	if(target != null)
   	{
			if(target.isStopped())
			{
				try
				{
					// Start the target Restlet
					target.start();
				}
				catch (Exception e)
				{
					logger.log(Level.WARNING, UNABLE_TO_START, e);
					setStatus(Status.SERVER_ERROR_INTERNAL);
				}
			}
			
			if(target.isStarted())
			{
				// Invoke the target handler
				target.handle(this);
			}
			else
			{
				logger.log(Level.WARNING, UNABLE_TO_START);
				setStatus(Status.SERVER_ERROR_INTERNAL);
			}
   	}
   	else
   	{
   		// No additional Restlet available,
   		// moving up the stack of calls,
   		// applying the post-handle filters.
   	}
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
			result = (getInput() != null) && getInput().isAvailable();
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
	      logger.warning("You must specify a resource reference before setting a base reference");
	   }
	   else if((baseRef != null) && !baseRef.isParent(getResourceRef()))
	   {
	      logger.warning("You must specify a base reference that is a parent of the resource reference");
	   }
	
	   this.baseRef = baseRef;
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
	 * Sets the representation provided by the server.
	 * @param output The representation provided by the server.
	 */
	public void setOutput(Representation output)
	{
		this.output = output;
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
		Factory.getInstance().setOutput(this, resource, fallbackLanguage);
	}

	/**
	 * Sets the reference that the client should follow for redirections or resource creations.
	 * @param redirectUri The redirection URI.
	 */
	public void setRedirectRef(String redirectUri)
	{
		setRedirectRef(new Reference(getBaseRef(), redirectUri).getTargetRef());
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

	/**
	 * Sets the call status.
	 * @param status The call status to set.
	 */
	public void setStatus(Status status)
	{
		this.status = status;
	}
	
}
