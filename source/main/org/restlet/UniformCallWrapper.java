/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

import org.restlet.connector.ConnectorCall;
import org.restlet.data.*;

/**
 * Uniform call wrapper.<br/>
 * Useful for application developer who need to enrich the call with application related things.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 */
public class UniformCallWrapper implements UniformCall
{
   /** Wrapped call. */
   protected UniformCall wrappedCall;

   /**
    * Constructor.
    * @param wrappedCall The wrapped call
    */
   public UniformCallWrapper(UniformCall wrappedCall)
   {
      this.wrappedCall = wrappedCall;
   }

   /**
    * Returns the best variant representation for a given resource according the the client preferences.
    * @param resource The resource for which the best representation needs to be set.
    * @param fallbackLanguage The language to use if no preference matches.
    * @return The best variant representation. 
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   public RepresentationMetadata getBestVariant(Resource resource, Language fallbackLanguage)
   {
      return getWrappedCall().getBestVariant(resource, fallbackLanguage);
   }

   /**
    * Returns the client IP address.
    * @return The client IP address.
    */
   public String getClientAddress()
   {
      return getWrappedCall().getClientAddress();
   }

   /**
    * Returns the client name (ex: user agent name).
    * @return The client name.
    */
   public String getClientName()
   {
      return getWrappedCall().getClientName();
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
    * Returns the low-level connector call.
    * @return The low-level connector call.
    */
   public ConnectorCall getConnectorCall()
   {
      return getWrappedCall().getConnectorCall();
   }

   /**
    * Returns the cookies provided by the client to the server.
    * @return The cookies provided by the client to the server.
    */
   public List<Cookie> getCookies()
   {
      return getWrappedCall().getCookies();
   }

   /**
    * Returns the cookies provided by the server to the client.
    * @return The cookies provided by the server to the client.
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
    * This method and the associated getInput method should be invoked only once. 
    * @return The input form provided by the client.
    */
   public Form getInputAsForm()
   {
      return getWrappedCall().getInputAsForm();
   }

   /**
    * Returns the call method.
    * @return The call method.
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
    * Returns the preference data of the client.
    * @return The preference data of the client.
    */
   public PreferenceData getPreference()
   {
      return getWrappedCall().getPreference();
   }

   /**
    * Returns the reference for redirections or resource creations.
    * @return The redirection reference.
    */
   public Reference getRedirectionRef()
   {
      return getWrappedCall().getRedirectionRef();
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
    * Returns the list of substrings matched in the current resource path.
    * @return The list of substrings matched.
    * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Matcher.html#group(int)">Matcher.group()</a>
    */
   public List<String> getResourceMatches()
   {
      return getWrappedCall().getResourceMatches();
   }

   /**
    * Returns a path in the list of resource paths.<br/>
    * The first path is the resource path relatively to the current Maplet.<br/>
    * The second path is the current Maplet path relatively to the parent Maplet.<br/> 
    * All the list of remaining Maplet paths is also available.
    * @param index Index of the path in the list.
    * @param strip Indicates if leading and ending slashes should be stripped.
    * @return The path at the given index.
    */
   public String getResourcePath(int index, boolean strip)
   {
      return getWrappedCall().getResourcePath(index, strip);
   }

   /**
    * Returns the list of paths dividing the initial resource path.<br/>
    * The list is sorted according to the Maplets hierarchy.
    * @return The list of paths.
    */
   public List<String> getResourcePaths()
   {
      return getWrappedCall().getResourcePaths();
   }

   /**
    * Returns the resource reference.
    * @return The resource reference.
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
    * Returns the server IP address.
    * @return The server IP address.
    */
   public String getServerAddress()
   {
      return getWrappedCall().getServerAddress();
   }

   /**
    * Returns the server name (ex: web server name).
    * @return The server name.
    */
   public String getServerName()
   {
      return getWrappedCall().getServerName();
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
   protected UniformCall getWrappedCall()
   {
      return this.wrappedCall;
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
      getWrappedCall().setBestOutput(resource, fallbackLanguage);
   }

   /**
    * Sets the client IP address.
    * @param address The client IP address.
    */
   public void setClientAddress(String address)
   {
      getWrappedCall().setClientAddress(address);
   }

   /**
    * Sets the client name (ex: user agent name).
    * @param name The client name.
    */
   public void setClientName(String name)
   {
      getWrappedCall().setClientName(name);
   }

   /**
    * Sets the low-level connector call.
    * @param call The low-level connector call.
    */
   public void setConnectorCall(ConnectorCall call)
   {
      getWrappedCall().setConnectorCall(call);
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
    * Sets the reference for redirections or resource creations.
    * @param redirectionRef The redirection reference.
    */
   public void setRedirectionRef(Reference redirectionRef)
   {
      getWrappedCall().setRedirectionRef(redirectionRef);
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
    * Sets the resource reference.
    * @param resourceRef The resource reference.
    */
   public void setResourceRef(Reference resourceRef)
   {
      getWrappedCall().setResourceRef(resourceRef);
   }

   /**
    * Sets the server IP address.
    * @param address The server IP address.
    */
   public void setServerAddress(String address)
   {
      getWrappedCall().setServerAddress(address);
   }

   /**
    * Sets the server name (ex: web server name).
    * @param name The server name.
    */
   public void setServerName(String name)
   {
      getWrappedCall().setServerName(name);
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
