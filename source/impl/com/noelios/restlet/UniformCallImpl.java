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

package com.noelios.restlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.restlet.Resource;
import org.restlet.RestletException;
import org.restlet.UniformCall;
import org.restlet.data.*;

import com.noelios.restlet.data.StringRepresentation;

/**
 * Default call implementation.
 */
public class UniformCallImpl implements UniformCall
{
   /** The referrer reference. */
   protected Reference referrerUri;

   /** The user agent name. */
   protected String userAgentName;

   /** The media preferences of the user agent. */
   protected List<Preference> mediaPrefs;

   /** The character set preferences of the user agent. */
   protected List<Preference> characterSetPrefs;

   /** The language preferences of the user agent. */
   protected List<Preference> languagePrefs;

   /** The method type. */
   protected Method method;

   /** The resource reference. */
   protected Reference resourceUri;

   /** The existing cookies of the user agent. */
   protected Cookies cookies;

   /** The representation received from the user agent. */
   protected Representation input;

   /** The status. */
   protected Status status;

   /** The representation to send to the user agent. */
   protected Representation output;

   /**
    * The list of cookies to be set in the user agent.
    * @see org.restlet.data.CookieSetting
    */
   protected List<CookieSetting> cookieSettings;

   /**
    * Constructor.
    * @param referrer The referrer reference.
    * @param userAgent The user agent.
    * @param mediaPrefs The media preferences of the user agent.
    * @param characterSetPrefs The character set preferences of the user agent.
    * @param languagePrefs The language preferences of the user agent.
    * @param method The method type.
    * @param resource The resource reference.
    * @param cookies The cookies sent by the user agent.
    * @param input The content received in the request.
    */
   public UniformCallImpl(Reference referrer, String userAgent, List<Preference> mediaPrefs,
         List<Preference> characterSetPrefs, List<Preference> languagePrefs, Method method,
         Reference resource, Cookies cookies, Representation input)
   {
      this.referrerUri = referrer;
      this.userAgentName = userAgent;
      this.mediaPrefs = mediaPrefs;
      this.characterSetPrefs = characterSetPrefs;
      this.languagePrefs = languagePrefs;
      this.method = method;
      this.resourceUri = resource;
      this.cookies = cookies;
      this.input = input;

      this.status = null;
      this.output = null;
      this.cookieSettings = null;
   }

   /**
    * Constructor.
    */
   public UniformCallImpl()
   {
      //
   }

   // ------------------------------
   // Methods related to the request
   // ------------------------------

   /**
    * Returns the referrer reference if available. This reference shouldn't be modified during the call
    * handling.
    * @return The referrer reference.
    */
   public Reference getReferrerUri()
   {
      return this.referrerUri;
   }

   /**
    * Sets the referrer reference if available. This reference shouldn't be modified during the call handling.
    * @param referrerUri The referrer reference.
    */
   public void setReferrerUri(Reference referrerUri)
   {
      this.referrerUri = referrerUri;
   }

   /**
    * Sets the referrer reference.
    * @param referrer The referrer reference.
    */
   public void setReferrer(Reference referrer)
   {
      this.referrerUri = referrer;
   }

   /**
    * Returns the user agent name.
    * @return The user agent name.
    */
   public String getUserAgentName()
   {
      return this.userAgentName;
   }

   /**
    * Sets the user agent name.
    * @param name The user agent name.
    */
   public void setUserAgentName(String name)
   {
      this.userAgentName = name;
   }

   /**
    * Returns the media type preferences of the user agent.
    * @return The media type preferences of the user agent.
    */
   public List<Preference> getMediaTypePrefs()
   {
      return this.mediaPrefs;
   }

   /**
    * Sets the media type preferences of the user agent.
    * @param prefs The media type preferences of the user agent.
    */
   public void setMediaTypePrefs(List<Preference> prefs)
   {
      this.mediaPrefs = prefs;
   }

   /**
    * Returns the character set preferences of the user agent.
    * @return The character set preferences of the user agent.
    */
   public List<Preference> getCharacterSetPrefs()
   {
      return this.characterSetPrefs;
   }

   /**
    * Sets the character set preferences of the user agent.
    * @param prefs The character set preferences of the user agent.
    */
   public void setCharacterSetPrefs(List<Preference> prefs)
   {
      this.characterSetPrefs = prefs;
   }

   /**
    * Returns the language preferences of the user agent.
    * @return The language preferences of the user agent.
    */
   public List<Preference> getLanguagePrefs()
   {
      return this.languagePrefs;
   }

   /**
    * Sets the language preferences of the user agent.
    * @param prefs The language preferences of the user agent.
    */
   public void setLanguagePrefs(List<Preference> prefs)
   {
      this.languagePrefs = prefs;
   }

   /**
    * Returns the method called.
    * @return The method called.
    */
   public Method getMethod()
   {
      return this.method;
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
    * Returns the resource's reference. This reference shouldn't be modified during the call handling.
    * @return The resource's reference.
    */
   public Reference getResourceUri()
   {
      return this.resourceUri;
   }

   /**
    * Sets the resource's reference.
    * @param resourceUri The resource's reference.
    */
   public void setResourceUri(Reference resourceUri)
   {
      this.resourceUri = resourceUri;
   }

   /**
    * Returns the cookies sent by the user agent.
    * @return The cookies sent by the user agent.
    */
   public Cookies getCookies()
   {
      return this.cookies;
   }

   /**
    * Sets the cookies sent by the user agent.
    * @param cookies The cookies sent by the user agent.
    */
   public void setCookies(Cookies cookies)
   {
      this.cookies = cookies;
   }

   /**
    * Returns the representation received from the user agent.
    * @return The representation received from the user agent.
    */
   public Representation getInput()
   {
      return this.input;
   }

   /**
    * Sets the content received in the request. param input The content received in the request.
    */
   public void setInput(Representation input)
   {
      this.input = input;
   }

   // -------------------------------
   // Methods related to the response
   // -------------------------------

   /**
    * Returns the result status.
    * @return The result status.
    */
   public Status getStatus()
   {
      return this.status;
   }

   /**
    * Sets the result status.
    * @param status The result status to set.
    */
   public void setStatus(Status status)
   {
      this.status = status;
   }

   /**
    * Returns the representation to send to the user agent
    * @return The representation to send to the user agent
    */
   public Representation getOutput()
   {
      return this.output;
   }

   /**
    * Sets the representation to send to the user agent.
    * @param output The representation to send to the user agent.
    */
   public void setOutput(Representation output)
   {
      this.output = output;
   }

   /**
    * Sets the best representation of a given resource according to the user agent preferences. If no
    * representation is found, sets the status to "Not found". If no acceptable representation is available,
    * sets the status to "Not acceptable".
    * @param resource The resource for which the best representation needs to be set.
    */
   public void setBestOutput(Resource resource) throws RestletException
   {
      Parameter currentParam = null;
      boolean compatible = false;

      RepresentationMetadata currentVariant = null;
      MediaType currentRange = null;

      RepresentationMetadata bestVariant = null;
      float bestQuality = 0;

      Preference currentPref = null;
      Preference bestPref = null;

      int currentSpecificity = 0;
      int bestSpecificity = 0;

      // For each media type supported by this resource
      List<RepresentationMetadata> variants = resource.getVariantsMetadata();
      if(variants == null)
      {
         setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }
      else
      {
         for(Iterator iter1 = variants.iterator(); iter1.hasNext();)
         {
            currentVariant = (RepresentationMetadata)iter1.next();

            // For each media range preference defined in the call
            // Calculate the specificity score
            for(Iterator iter2 = getMediaTypePrefs().iterator(); iter2.hasNext();)
            {
               compatible = true;
               currentSpecificity = 0;
               currentPref = (Preference)iter2.next();
               currentRange = (MediaType)currentPref.getMetadata();

               // 1) Compare the main types
               if(currentVariant.getMediaType().getMainType().equals(currentRange.getMainType()))
               {
                  currentSpecificity += 1000;
               }
               else if(!currentRange.getMainType().equals("*"))
               {
                  compatible = false;
               }
               else if(!currentRange.getSubtype().equals("*"))
               {
                  // Ranges such as "*/html" are not supported
                  // Only "*/*" is acceptable in this case
                  compatible = false;
               }

               if(compatible)
               {
                  // 2) Compare the sub types
                  if(currentVariant.getMediaType().getSubtype().equals(currentRange.getSubtype()))
                  {
                     currentSpecificity += 100;
                  }
                  else if(!currentRange.getSubtype().equals("*"))
                  {
                     // Subtype are different
                     compatible = false;
                  }

                  if(compatible && (currentVariant.getMediaType().getParameters() != null))
                  {
                     // 3) Compare the parameters
                     // If current media type is compatible with the current
                     // media range
                     // then the parameters need to be checked too
                     for(Iterator iter3 = currentVariant.getMediaType().getParameters().iterator(); iter3
                           .hasNext();)
                     {
                        currentParam = (Parameter)iter3.next();

                        if(isParameterFound(currentParam, currentRange))
                        {
                           currentSpecificity++;
                        }
                     }
                  }

                  // 3) Do we have a better preference?
                  if(compatible && ((bestPref == null) || (currentSpecificity > bestSpecificity)))
                  {
                     bestPref = currentPref;
                  }
               }
            }

            if(bestPref != null)
            {
               if(bestVariant == null)
               {
                  bestVariant = currentVariant;
                  bestQuality = bestPref.getQuality();
               }
               else
               {
                  if(bestPref.getQuality() > bestQuality)
                  {
                     bestVariant = currentVariant;
                     bestQuality = bestPref.getQuality();
                  }
               }
            }
         }

         if(bestVariant == null)
         {
            // No variant was found matchin the call preferences
            setStatus(Statuses.CLIENT_ERROR_NOT_ACCEPTABLE);
         }
         else
         {
            // Set the best representation as the call output
            setOutput(resource.getRepresentation(bestVariant));
         }
      }
   }

   /**
    * Indicates if the searched parameter is specified in the given media range.
    * @param searchedParam The searched parameter.
    * @param mediaRange The media range to inspect.
    * @return True if the searched parameter is specified in the given media range.
    */
   private boolean isParameterFound(Parameter searchedParam, MediaType mediaRange)
   {
      boolean result = false;

      for(Iterator iter = mediaRange.getParameters().iterator(); !result && iter.hasNext();)
      {
         result = searchedParam.equals((Parameter)iter.next());
      }

      return result;
   }

   /**
    * Returns the list of cookies to be set in the user agent. Cookie settings can be browsed, added or
    * removed.
    * @return The list of cookies to be set in the user agent.
    */
   public List<CookieSetting> getCookieSettings()
   {
      if(this.cookieSettings == null)
      {
         this.cookieSettings = new ArrayList<CookieSetting>();
      }

      return this.cookieSettings;
   }

   /**
    * Sets the list of cookies to be set in the user agent. Cookie settings can be browsed, added or removed.
    * @param cookieSettings The list of cookies to be set in the user agent.
    */
   public void setCookieSettings(List<CookieSetting> cookieSettings)
   {
      this.cookieSettings = cookieSettings;
   }

   /**
    * Asks the user agent to redirect itself to the given URI. Modifies the result output and status
    * properties.
    * @param targetURI The target URI.
    */
   public void setTemporaryRedirect(String targetURI)
   {
      setOutput(new StringRepresentation(targetURI, MediaTypes.TEXT_URI));
      setStatus(Statuses.REDIRECTION_MOVED_TEMPORARILY);
   }

}
