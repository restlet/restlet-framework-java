/*
 * Copyright 2005 Jérôme LOUVEL
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
    * Returns the character set preferences of the user agent.
    * @return The character set preferences of the user agent.
    */
   public List<Preference> getCharacterSetPrefs();

   /**
    * Returns the client's IP address.
    * @return The client's IP address.
    */
   public String getClientAddress();

   /**
    * Returns the client's name (ex: user agent name).
    * @return The client's name.
    */
   public String getClientName();

   /**
    * Returns the cookies sent by the user agent.
    * @return The cookies sent by the user agent.
    */
   public Cookies getCookies();

   /**
    * Returns the list of cookies to be set in the user agent. Cookie settings can be browsed, added or
    * removed.
    * @return The list of cookies to be set in the user agent.
    */
   public List<CookieSetting> getCookieSettings();

   /**
    * Returns the representation received from the user agent.
    * @return The representation received from the user agent.
    */
   public Representation getInput();

   /**
    * Returns the language preferences of the user agent.
    * @return The language preferences of the user agent.
    */
   public List<Preference> getLanguagePrefs();

   /**
    * Returns the media type preferences of the user agent.
    * @return The media type preferences of the user agent.
    */
   public List<Preference> getMediaTypePrefs();

   /**
    * Returns the method called.
    * @return The method called.
    */
   public Method getMethod();

   /**
    * Returns the representation to send to the user agent
    * @return The representation to send to the user agent
    */
   public Representation getOutput();

   /**
    * Returns the referrer reference if available.<br/>
    * This reference shouldn't be modified during the call handling.
    * @return The referrer reference.
    */
   public Reference getReferrerRef();

   /**
    * Returns the resource's reference.<br/>
    * This reference shouldn't be modified during the call handling, exceptio for redirect rewritings.
    * @return The resource's reference.
    */
   public Reference getResourceRef();

   /**
    * Returns the result status.
    * @return The result status.
    */
   public Status getStatus();

   /**
    * Sets the best representation of a given resource according to the user agent preferences. If no
    * representation is found, sets the status to "Not found". If no acceptable representation is available,
    * sets the status to "Not acceptable".
    * @param resource The resource for which the best representation needs to be set.
    * @throws RestletException
    */
   public void setBestOutput(Resource resource) throws RestletException;

   /**
    * Sets the character set preferences of the user agent.
    * @param prefs The character set preferences of the user agent.
    */
   public void setCharacterSetPrefs(List<Preference> prefs);

   /**
    * Sets the client's IP address.
    * @param address The client's IP address.
    */
   public void setClientAddress(String address);

   /**
    * Sets the client's name (ex: user agent name).
    * @param name The client's name.
    */
   public void setClientName(String name);

   /**
    * Sets the cookies sent by the user agent.
    * @param cookies The cookies sent by the user agent.
    */
   public void setCookies(Cookies cookies);

   /**
    * Sets the representation received from the user agent.
    * @param input The representation received from the user agent.
    */
   public void setInput(Representation input);

   /**
    * Sets the language preferences of the user agent.
    * @param prefs The language preferences of the user agent.
    */
   public void setLanguagePrefs(List<Preference> prefs);

   /**
    * Sets the media type preferences of the user agent.
    * @param prefs The media type preferences of the user agent.
    */
   public void setMediaTypePrefs(List<Preference> prefs);

   /**
    * Sets the method called.
    * @param method The method called.
    */
   public void setMethod(Method method);

   /**
    * Sets the representation to send to the user agent.
    * @param output The representation to send to the user agent.
    */
   public void setOutput(Representation output);

   /**
    * Sets the referrer reference if available.<br/>
    * This reference shouldn't be modified during the call handling.
    * @param referrerRef The referrer reference.
    */
   public void setReferrerRef(Reference referrerRef);

   /**
    * Sets the resource's reference.<br/>
    * This reference shouldn't be modified during the call handling, except for redirection rewriting.
    * @param resourceRef The resource's reference.
    */
   public void setResourceRef(Reference resourceRef);

   /**
    * Sets the result status.
    * @param status The result status to set.
    */
   public void setStatus(Status status);

   /**
    * Asks the user agent to redirect itself to the given URI. Modifies the result output and status
    * properties.
    * @param targetURI The target URI.
    */
   public void setTemporaryRedirect(String targetURI);

}
