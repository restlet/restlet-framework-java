/*
 * Copyright 2005-2006 Jerome LOUVEL
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
 * Represents a uniform call handled by a Restlet.
 * @see org.restlet.Restlet
 */
public interface RestletCall
{
   /**
    * Returns the best variant representation for a given resource according the the client preferences.
    * @param resource The resource for which the best representation needs to be set.
    * @param fallbackLanguage The language to use if no preference matches.
    * @return The best variant representation. 
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   public RepresentationMetadata getBestVariant(Resource resource, Language fallbackLanguage);

   /**
    * Returns the client IP address.
    * @return The client IP address.
    */
   public String getClientAddress();

   /**
    * Returns the list of client IP addresses.<br/>
    * The first address is the one of the immediate client component as returned by the getClientAdress() method and
    * the last address should correspond to the origin client (frequently a user agent). 
    * This is useful when the user agent is separated from the origin server by a chain of intermediary components.<br/>
    * This list of addresses is based on headers such as the "X-Forwarded-For" header supported by popular proxies and caches.<br/>
    * However, this information is only safe for intermediary components within your local network.<br/>
    * Other addresses could easily be changed by setting a fake header and should never be trusted for serious security checks.  
    * @return The client IP addresses.
    */
   public List<String> getClientAddresses();

   /**
    * Returns the client name (ex: user agent name).
    * @return The client name.
    */
   public String getClientName();

   /**
    * Returns the condition data applying to this call.
    * @return The condition data applying to this call.
    */
   public ConditionData getCondition();
   
   /**
    * Returns the low-level connector call.<br/>
    * This should be only used in exceptional cases where access to non-standard headers is necessary for example.
    * @return The low-level connector call.
    */
   public ConnectorCall getConnectorCall();

   /**
    * Returns the list of substrings matched in the current context path.
    * @return The list of substrings matched.
    * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Matcher.html#group(int)">Matcher.group()</a>
    */
   public List<String> getContextMatches();

   /**
    * Returns the absolute context path, preceeding the relative resource path in the resource reference.
    * @return The absolute context path.
    */
   public String getContextPath();

   /**
    * Returns the context path as a reference.
    * @return The context path as a reference.
    */
   public Reference getContextRef();
   
   /**
    * Returns the cookies provided by the client to the server.
    * @return The cookies provided by the client to the server.
    */
   public List<Cookie> getCookies();

   /**
    * Returns the cookies provided by the server to the client.
    * @return The cookies provided by the server to the client.
    */
   public List<CookieSetting> getCookieSettings();

   /**
    * Returns the representation provided by the client.
    * @return The representation provided by the client.
    */
   public Representation getInput();

   /**
    * Returns the representation provided by the client as a form.<br/>
    * Note that this triggers the parsing of the input representation.<br/>
    * This method and the associated getInput method should be invoked only once. 
    * @return The input form provided by the client.
    */
   public Form getInputAsForm();

   /**
    * Returns the call method.
    * @return The call method.
    */
   public Method getMethod();

   /**
    * Returns the representation provided by the server.
    * @return The representation provided by the server.
    */
   public Representation getOutput();

   /**
    * Returns the preference data of the client.
    * @return The preference data of the client.
    */
   public PreferenceData getPreference();
   
   /**
    * Returns the reference for redirections or resource creations.
    * @return The redirection reference.
    */
   public Reference getRedirectionRef();

   /**
    * Returns the referrer reference if available.
    * @return The referrer reference.
    */
   public Reference getReferrerRef();

   /**
    * Returns the relative resource path, following the absolute Restlet path in the resource reference.
    * @return The relative resource path.
    */
   public String getResourcePath();

   /**
    * Returns the absolute resource reference.
    * @return The absolute resource reference.
    */
   public Reference getResourceRef();

   /**
    * Returns the security data related to this call.
    * @return The security data related to this call.
    */
   public SecurityData getSecurity();

   /**
    * Returns the server IP address.
    * @return The server IP address.
    */
   public String getServerAddress();

   /**
    * Returns the server name (ex: web server name).
    * @return The server name.
    */
   public String getServerName();

   /**
    * Returns the call status.
    * @return The call status.
    */
   public Status getStatus();

   /**
    * Sets the best representation of a given resource according to the client preferences.<br/> 
    * If no representation is found, sets the status to "Not found".<br/>
    * If no acceptable representation is available, sets the status to "Not acceptable".<br/>
    * @param resource The resource for which the best representation needs to be set.
    * @param fallbackLanguage The language to use if no preference matches.
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   public void setBestOutput(Resource resource, Language fallbackLanguage);

   /**
    * Sets the client IP address.
    * @param address The client IP address.
    */
   public void setClientAddress(String address);

   /**
    * Sets the list of client IP addresses.  
    * @param addresses The list of client IP addresses.
    */
   public void setClientAddresses(List<String> addresses);

   /**
    * Sets the client name (ex: user agent name).
    * @param name The client name.
    */
   public void setClientName(String name);

   /**
    * Sets the low-level connector call.
    * @param call The low-level connector call.
    */
   public void setConnectorCall(ConnectorCall call);

   /**
    * Sets the absolute context path, preceeding the relative resource path in the resource reference.
    * @param contextPath The absolute context path.
    */
   public void setContextPath(String contextPath);

   /**
    * Sets the representation provided by the client.
    * @param input The representation provided by the client.
    */
   public void setInput(Representation input);

   /**
    * Sets the method called.
    * @param method The method called.
    */
   public void setMethod(Method method);

   /**
    * Sets the representation provided by the server.
    * @param output The representation provided by the server.
    */
   public void setOutput(Representation output);

   /**
    * Sets the reference for redirections or resource creations.
    * @param redirectionRef The redirection reference.
    */
   public void setRedirectionRef(Reference redirectionRef);

   /**
    * Sets the referrer reference if available.
    * @param referrerRef The referrer reference.
    */
   public void setReferrerRef(Reference referrerRef);

   /**
    * Sets the resource reference.
    * @param resourceRef The resource reference.
    */
   public void setResourceRef(Reference resourceRef);

   /**
    * Sets the resource reference using an URI string.
    * @param resourceUri The resource URI.
    */
   public void setResourceRef(String resourceUri);

   /**
    * Sets the server IP address.
    * @param address The server IP address.
    */
   public void setServerAddress(String address);

   /**
    * Sets the server name (ex: web server name).
    * @param name The server name.
    */
   public void setServerName(String name);

   /**
    * Sets the call status.
    * @param status The call status to set.
    */
   public void setStatus(Status status);

}
