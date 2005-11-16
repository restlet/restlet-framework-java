/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
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
   // ------------------------------
   // Methods related to the request
   // ------------------------------

   /**
    * Returns the referrer reference if available.
    * This reference shouldn't be modified during the call handling.
    * @return The referrer reference.
    */
   public Reference getReferrerUri();

   /**
    * Returns the user agent name.
    * @return The user agent name.
    */
   public String getUserAgentName();

   /**
    * Returns the media type preferences of the user agent.
    * @return The media type preferences of the user agent.
    */
   public List<Preference> getMediaTypePrefs();

   /**
    * Returns the character set preferences of the user agent.
    * @return The character set preferences of the user agent.
    */
   public List<Preference> getCharacterSetPrefs();

   /**
    * Returns the language preferences of the user agent.
    * @return The language preferences of the user agent.
    */
   public List<Preference> getLanguagePrefs();

   /**
    * Returns the method called.
    * @return The method called.
    */
   public Method getMethod();

   /**
    * Returns the resource's reference.
    * This reference shouldn't be modified during the call handling.
    * @return The resource's reference.
    */
   public Reference getResourceUri();

   /**
    * Returns the cookies sent by the user agent.
    * @return The cookies sent by the user agent.
    */
   public Cookies getCookies();

   /**
    * Returns the representation received from the user agent.
    * @return The representation received from the user agent.
    */
   public Representation getInput();

   // -------------------------------
   // Methods related to the response
   // -------------------------------

   /**
    * Returns the result status.
    * @return The result status.
    */
   public Status getStatus();

   /**
    * Sets the result status.
    * @param status 	The result status to set.
    */
   public void setStatus(Status status);

   /**
    * Returns the representation to send to the user agent
    * @return The representation to send to the user agent
    */
   public Representation getOutput();

   /**
    * Sets the representation to send to the user agent.
    * @param output The representation to send to the user agent.
    */
   public void setOutput(Representation output);

   /**
    * Sets the best representation of a given resource according to the user agent preferences.
    * If no representation is found, sets the status to "Not found".
    * If no acceptable representation is available, sets the status to "Not acceptable".
    * @param resource The resource for which the best representation needs to be set.
    */
   public void setBestOutput(Resource resource) throws RestletException;

   /**
    * Returns the list of cookies to be set in the user agent.
    * Cookie settings can be browsed, added or removed.
    * @return The list of cookies to be set in the user agent.
    */
   public List<CookieSetting> getCookieSettings();

   /**
    * Asks the user agent to redirect itself to the given URI.
    * Modifies the result output and status properties.
    * @param targetURI The target URI.
    */
   public void setTemporaryRedirect(String targetURI);

}
