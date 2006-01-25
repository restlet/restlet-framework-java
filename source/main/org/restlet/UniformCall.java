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

import org.restlet.data.*;

/**
 * Represents a call handled via the uniform interface.
 * @see org.restlet.UniformInterface
 */
public interface UniformCall
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
    * Returns the cookies provided by the client.
    * @return The cookies provided by the client.
    */
   public List<Cookie> getCookies();

   /**
    * Returns the cookies provided to the client.
    * @return The cookies provided to the client.
    */
   public List<CookieSetting> getCookieSettings();

   /**
    * Returns the representation provided by the client.
    * @return The representation provided by the client.
    */
   public Representation getInput();

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
    * @return The redirect reference.
    */
   public Reference getRedirectRef();

   /**
    * Returns the referrer reference if available.
    * @return The referrer reference.
    */
   public Reference getReferrerRef();

   /**
    * Returns the list of substrings matched in the current resource path.
    * @return The list of substrings matched.
    * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Matcher.html#group(int)">Matcher.group()</a>
    */
   public List<String> getResourceMatches();

   /**
    * Returns a path in the list of resource paths.<br/>
    * The first path is the resource path relatively to the current maplet.<br/>
    * The second path is the current maplet path relatively to the parent maplet.<br/> 
    * All the list of remaining maplet paths is also available.
    * @param index Index of the path in the list.
    * @param strip Indicates if leading and ending slashes should be stripped.
    * @return The path at the given index.
    */
   public String getResourcePath(int index, boolean strip);

   /**
    * Returns the list of paths dividing the initial resource path.<br/>
    * The list is sorted according to the maplets hierarchy.
    * @return The list of paths.
    */
   public List<String> getResourcePaths();

   /**
    * Returns the resource reference.
    * @return The resource reference.
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
    * Returns the server status.
    * @return The server status.
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
    * Sets the client name (ex: user agent name).
    * @param name The client name.
    */
   public void setClientName(String name);

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
    * @param redirectRef The redirect reference.
    */
   public void setRedirectRef(Reference redirectRef);

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
    * Sets the server status.
    * @param status The server status to set.
    */
   public void setStatus(Status status);

}
