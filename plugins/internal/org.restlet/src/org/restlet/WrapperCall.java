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

import java.util.List;
import java.util.Map;

import org.restlet.data.ClientData;
import org.restlet.data.ConditionData;
import org.restlet.data.ContextData;
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
 * Wrapper for Restlet Call instances. Useful for application developer who need to enrich the call with
 * some additional state or logic.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class WrapperCall extends Call
{
   /** Wrapped call. */
   protected Call wrappedCall;

   /**
    * Constructor.
    * @param wrappedCall The wrapped call.
    */
   public WrapperCall(Call wrappedCall)
   {
      this.wrappedCall = wrappedCall;
   }

	/**
	 * Returns a modifiable attributes map that can be used by developers to save information relative
	 * to the current call. This is an easier alternative to the creation of a wrapper around the whole call.
	 * @return The modifiable attributes map.
	 */
   public Map<String, Object> getAttributes()
   {
      return getWrappedCall().getAttributes();
   }

	/**
	 * Returns the client specific data.
	 * @return The client specific data.
	 */
	public ClientData getClient()
	{
      return getWrappedCall().getClient();
	}

	/**
	 * Returns the condition data applying to this call.
	 * @return The condition data applying to this call.
	 */
   public ConditionData getCondition()
   {
      return getWrappedCall().getCondition();
   }

	/**
	 * Returns the context data of the current Restlet applying to this call.
	 * @return The context data applying to this call.
	 */
	public ContextData getContext()
	{
      return getWrappedCall().getContext();
	}

   /**
	 * Returns the cookies provided by the client.
	 * @return The cookies provided by the client.
    */
   public List<Cookie> getCookies()
   {
      return getWrappedCall().getCookies();
   }

   /**
	 * Returns the cookie settings provided by the server.
	 * @return The cookie settings provided by the server.
    */
   public List<CookieSetting> getCookieSettings()
   {
      return getWrappedCall().getCookieSettings();
   }

   /**
    * Returns the representation provided by the client.
    * @return The representation provided by the client.
    */
   public Representation getInput()
   {
      return getWrappedCall().getInput();
   }

   /**
	 * Returns the representation provided by the client as a form.<br/>
	 * Note that this triggers the parsing of the input representation.<br/>
	 * This method and the associated getInput method can only be invoked once.
	 * @return The input form provided by the client.
    */
   public Form getInputAsForm()
   {
      return getWrappedCall().getInputAsForm();
   }

   /**
	 * Returns the method.
	 * @return The method.
    */
   public Method getMethod()
   {
      return getWrappedCall().getMethod();
   }

   /**
    * Returns the representation provided by the server.
    * @return The representation provided by the server.
    */
   public Representation getOutput()
   {
      return getWrappedCall().getOutput();
   }

   /**
	 * Returns the reference that the client should follow for redirections or resource creations.
	 * @return The redirection reference.
    */
   public Reference getRedirectRef()
   {
      return getWrappedCall().getRedirectRef();
   }

   /**
    * Returns the referrer reference if available.
    * @return The referrer reference.
    */
   public Reference getReferrerRef()
   {
      return getWrappedCall().getReferrerRef();
   }

   /**
	 * Returns the reference of the target resource.
	 * @return The reference of the target resource.
    */
   public Reference getResourceRef()
   {
      return getWrappedCall().getResourceRef();
   }

   /**
    * Returns the security data related to this call.
    * @return The security data related to this call.
    */
   public SecurityData getSecurity()
   {
      return getWrappedCall().getSecurity();
   }

   /**
    * Returns the server specific data.
    * @return The server specific data.
    */
   public ServerData getServer()
   {
      return getWrappedCall().getServer();
   }

   /**
    * Returns the call status.
    * @return The call status.
    */
   public Status getStatus()
   {
      return getWrappedCall().getStatus();
   }

   /**
    * Returns the wrapped call.
    * @return The wrapped call
    */
   protected Call getWrappedCall()
   {
      return this.wrappedCall;
   }

   /**
    * Sets the representation provided by the client.
    * @param input The representation provided by the client.
    */
   public void setInput(Representation input)
   {
      getWrappedCall().setInput(input);
   }

   /**
    * Sets the method called.
    * @param method The method called.
    */
   public void setMethod(Method method)
   {
      getWrappedCall().setMethod(method);
   }

	/**
	 * Sets the representation provided by the server.
	 * @param output The representation provided by the server.
	 */
   public void setOutput(Representation output)
   {
      getWrappedCall().setOutput(output);
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
      getWrappedCall().setOutput(resource, fallbackLanguage);
   }

   /**
	 * Sets the reference that the client should follow for redirections or resource creations.
	 * @param redirectUri The redirection URI.
    */
   public void setRedirectRef(String redirectUri)
   {
      getWrappedCall().setRedirectRef(redirectUri);
   }

   /**
	 * Sets the reference that the client should follow for redirections or resource creations.
	 * @param redirectRef The redirection reference.
    */
   public void setRedirectRef(Reference redirectRef)
   {
      getWrappedCall().setRedirectRef(redirectRef);
   }

   /**
    * Sets the referrer reference if available using an URI string.
    * @param referrerUri The referrer URI.
    */
   public void setReferrerRef(String referrerUri)
   {
      getWrappedCall().setReferrerRef(referrerUri);
   }

   /**
    * Sets the referrer reference if available.
    * @param referrerRef The referrer reference.
    */
   public void setReferrerRef(Reference referrerRef)
   {
      getWrappedCall().setReferrerRef(referrerRef);
   }

	/**
	 * Sets the target resource reference using an URI string. Note that the URI can be either
	 * absolute or relative to the context's base reference.
	 * @param resourceUri The resource URI.
	 */
   public void setResourceRef(String resourceUri)
   {
      getWrappedCall().setResourceRef(resourceUri);
   }

   /**
	 * Sets the target resource reference. If the reference is relative, it will be resolved as an
	 * absolute reference. Also, the context's base reference will be reset. Finally, the reference
	 * will be normalized to ensure a consistent handling of the call.
	 * @param resourceRef The resource reference.
    */
   public void setResourceRef(Reference resourceRef)
   {
      getWrappedCall().setResourceRef(resourceRef);
   }

   /**
    * Sets the call status.
    * @param status The call status to set.
    */
   public void setStatus(Status status)
   {
      getWrappedCall().setStatus(status);
   }

}
