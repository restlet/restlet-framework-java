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

import org.restlet.connector.ConnectorCall;
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
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.impl.CallImpl");

   /** The modifiable attributes map. */
   protected Map<String, Object> attributes;

   /** The condition data. */
   protected ConditionData condition;

   /** The low-level connector call. */
   protected ConnectorCall connectorCall;

   /** The current cookies of the client. */
   protected List<Cookie> cookies;

   /** The cookies to set in the client. */
   protected List<CookieSetting> cookieSettings;

   /** The context path. */
   protected String contextPath;

   /** The representation provided by the client. */
   protected Representation input;

   /** The call method. */
   protected Method method;

   /** The representation provided by the server. */
   protected Representation output;

   /** The client data. */
   protected ClientData client;

   /** The redirection reference. */
   protected Reference redirectRef;

   /** The referrer reference. */
   protected Reference referrerRef;

   /** The resource reference. */
   protected Reference resourceRef;

   /** The security data. */
   protected SecurityData security;

   /** The server data. */
   protected ServerData server;

   /** The server status. */
   protected Status status;
   
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
    * @see org.restlet.WrapperCall
    */
   public Map<String, Object> getAttributes()
   {
      if(attributes == null)
      {
         attributes = new TreeMap<String, Object>();
      }

      return attributes;
   }

   /**
    * Returns the condition data applying to this call.
    * @return The condition data applying to this call.
    */
   public ConditionData getCondition()
   {
      if(this.condition == null) this.condition = new ConditionData();
      return this.condition;
   }

   /**
    * Returns the absolute context path, preceeding the relative resource path in the resource reference.
    * @return The absolute context path.
    */
   public String getContextPath()
   {
      return this.contextPath;
   }

   /**
    * Returns the base reference.
    * @return The base reference.
    */
   public Reference getBaseRef()
   {
      return new Reference(getContextPath());
   }

   /**
    * Returns the cookies provided by the client.
    * @return The cookies provided by the client.
    */
   public List<Cookie> getCookies()
   {
      if(this.cookies == null) this.cookies = new ArrayList<Cookie>();
      return this.cookies;
   }

   /**
    * Returns the cookies provided by the server.
    * @return The cookies provided by the server to the client.
    */
   public List<CookieSetting> getCookieSettings()
   {
      if(this.cookieSettings == null) this.cookieSettings = new ArrayList<CookieSetting>();
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
    * This method and the associated getInput method should be invoked only once.
    * @return The input form provided by the client.
    */
   public Form getInputAsForm()
   {
      try
      {
         return new Form(getInput());
      }
      catch(IOException e)
      {
         return null;
      }
   }

   /**
    * Returns the call method.
    * @return The call method.
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
    * Returns the reference that the client should follow for redirections or creation of new resources.
    * @return The redirection reference.
    */
   public Reference getRedirectRef()
   {
      return this.redirectRef;
   }

   /**
    * Returns the client specific data.
    * @return The client specific data.
    */
   public ClientData getClient()
   {
      if(this.client == null) this.client = new ClientData();
      return this.client;
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
    * Returns the relative resource path, following the absolute Restlet path in the resource reference.
    * @return The relative resource path.
    */
   public String getResourcePath()
   {
      if(getContextPath() == null)
      {
         return this.resourceRef.toString(false, false);
      }
      else
      {
         String resourceURI = this.resourceRef.toString(false, false);
         int length = getContextPath().length();

         if(logger.isLoggable(Level.FINE))
         {
            logger.fine("Resource URI: " + resourceURI);
            logger.fine("Context path: " + getContextPath());
            logger.fine("Context path length: " + length);
         }

         return resourceURI.substring(length);
      }
   }

   /**
    * Returns the absolute resource reference.
    * @return The absolute resource reference.
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
      if(this.security == null) this.security = new SecurityData();
      return this.security;
   }

   /**
    * Returns the server specific data.
    * @return The server specific data.
    */
   public ServerData getServer()
   {
      if(this.server == null) this.server = new ServerData();
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
    * Sets the best representation of a given resource according to the client preferences.<br/>
    * If no representation is found, sets the status to "Not found".<br/>
    * If no acceptable representation is available, sets the status to "Not acceptable".<br/>
    * @param resource The resource for which the best representation needs to be set.
    * @param fallbackLanguage The language to use if no preference matches.
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   public void setBestOutput(Resource resource, Language fallbackLanguage)
   {
   	Factory.getInstance().setBestOutput(this, resource, fallbackLanguage);
   }

   /**
    * Sets the absolute context path, preceeding the relative resource path in the resource reference.
    * @param contextPath The absolute context path.
    */
   public void setContextPath(String contextPath)
   {
      if(this.resourceRef == null)
      {
         logger.warning("You must specify a resource reference before you can set a context path");
      }
      else if((contextPath != null) && (!this.resourceRef.toString(false, false).startsWith(contextPath)))
      {
         logger.warning("The specified context path doesn't match the beginning of the resource reference: " + contextPath);
      }

      this.contextPath = contextPath;
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
    * Sets the reference that the client should follow for redirections or creation of new resources.
    * @param redirectRef The redirection reference.
    */
   public void setRedirectRef(Reference redirectRef)
   {
      this.redirectRef = redirectRef;
   }

   /**
    * Sets the reference that the client should follow for redirections or creation of new resources.
    * @param redirectUri The redirection URI.
    */
   public void setRedirectRef(String redirectUri)
   {
      setRedirectRef(new Reference(getBaseRef(), redirectUri).getTargetRef());
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
    * Sets the referrer reference if available using an URI string.
    * @param referrerUri The referrer URI.
    */
   public void setReferrerRef(String referrerUri)
   {
      setReferrerRef(new Reference(referrerUri));
   }

   /**
    * Sets the resource reference.<br/>
    * Also reset the current restlet path and matches.
    * @param resourceRef The resource reference.
    */
   public void setResourceRef(Reference resourceRef)
   {
      this.resourceRef = resourceRef;

      // Reset the current context path
      setContextPath(null);
   }

   /**
    * Sets the resource reference using an URI string. Note that the resource URI can be either
    * absolute or relative to the current context reference.
    * @param resourceUri The resource URI.
    */
   public void setResourceRef(String resourceUri)
   {
      setResourceRef(new Reference(getBaseRef(), resourceUri).getTargetRef());
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
