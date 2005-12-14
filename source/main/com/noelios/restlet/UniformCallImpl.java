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

package com.noelios.restlet;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

   /** The client's name. */
   protected String clientName;

   /** The client's IP address. */
   protected String clientAddress;

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
    * @param clientName The client's name (ex: user agent name).
    * @param mediaPrefs The media preferences of the user agent.
    * @param characterSetPrefs The character set preferences of the user agent.
    * @param languagePrefs The language preferences of the user agent.
    * @param method The method type.
    * @param resource The resource reference.
    * @param cookies The cookies sent by the user agent.
    * @param input The content received in the request.
    */
   public UniformCallImpl(Reference referrer, String clientName, List<Preference> mediaPrefs,
         List<Preference> characterSetPrefs, List<Preference> languagePrefs, Method method,
         Reference resource, Cookies cookies, Representation input)
   {
      this.referrerUri = referrer;
      this.clientName = clientName;

      try
      {
         this.clientAddress = InetAddress.getLocalHost().getHostAddress();
      }
      catch(UnknownHostException e)
      {
         this.clientAddress = null;
      }
      
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
   public String getClientName()
   {
      return this.clientName;
   }

   /**
    * Sets the user agent name.
    * @param name The user agent name.
    */
   public void setClientName(String name)
   {
      this.clientName = name;
   }

   /**
    * Returns the client's IP address.
    * @return The client's IP address.
    */
   public String getClientAddress()
   {
      return this.clientAddress;  
   }

   /**
    * Sets the client's IP address.
    * @param address The client's IP address.
    */
   public void setClientAddress(String address)
   {
      this.clientAddress = address;
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
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a> 
    */
   public void setBestOutput(Resource resource) throws RestletException
   {
      Parameter currentParam = null;
      boolean compatiblePref = false;
      boolean compatibleLanguage = false;

      RepresentationMetadata currentVariant = null;
      Language currentLanguage = null;
      MediaType currentMediaType = null;

      RepresentationMetadata bestVariant = null;
      float bestQuality = 0;

      Preference currentPref = null;
      Preference bestLanguagePref = null;
      Preference bestMediaTypePref = null;

      float currentScore = 0;
      float bestLanguageScore = 0;
      float bestMediaTypeScore = 0;

      // For each media type supported by this resource
      List<RepresentationMetadata> variants = resource.getVariantsMetadata();
      if(variants == null)
      {
         setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }
      else
      {
         // For each available variant, we will compute the negotiation score
         // which is dependant on the language score and on the media type score
         for(Iterator iter1 = variants.iterator(); iter1.hasNext();)
         {
            currentVariant = (RepresentationMetadata)iter1.next();

            // For each language preference defined in the call
            // Calculate the score and remember the best scoring preference
            for(Iterator iter2 = getLanguagePrefs().iterator(); (currentVariant.getLanguage() != null) && iter2.hasNext();)
            {
               currentPref = (Preference)iter2.next();
               currentLanguage = (Language)currentPref.getMetadata();
               compatiblePref = true;
               currentScore = 0;

               // 1) Compare the main tag
               if(currentVariant.getLanguage().getMainTag().equals(currentLanguage.getMainTag()))
               {
                  currentScore += 100;
               }
               else if(!currentLanguage.getMainTag().equals("*"))
               {
                  compatiblePref = false;
               }
               else if(currentLanguage.getSubTag() != null)
               {
                  // Only "*" is an acceptable language range
                  compatiblePref = false;
               }
               else
               {
                  // The valid "*" range has the lowest valid score
                  currentScore++;
               }

               if(compatiblePref)
               {
                  // 2) Compare the sub tags
                  if((currentLanguage.getSubTag() == null) || (currentVariant.getLanguage().getSubTag() == null))
                  {
                     if(currentVariant.getLanguage().getSubTag() == currentLanguage.getSubTag())
                     {
                        currentScore += 10;
                     }
                     else
                     {
                        // Don't change the score
                     }
                  }
                  else if(currentLanguage.getSubTag().equals(currentVariant.getLanguage().getSubTag()))
                  {
                     currentScore += 10;
                  }
                  else
                  {
                     // SubTags are different
                     compatiblePref = false;
                  }

                  // 3) Do we have a better preference?
                  // currentScore *= currentPref.getQuality();
                  if(compatiblePref && ((bestLanguagePref == null) || (currentScore > bestLanguageScore)))
                  {
                     bestLanguagePref = currentPref;
                     bestLanguageScore = currentScore;
                  }
               }
            }
            
            // If the variant has a language set, do we have a compatible preference?
            compatibleLanguage = (currentVariant.getLanguage() == null) || (bestLanguagePref != null); 
               
            // For each media range preference defined in the call
            // Calculate the score and remember the best scoring preference
            for(Iterator iter2 = getMediaTypePrefs().iterator(); compatibleLanguage && iter2.hasNext();)
            {
               currentPref = (Preference)iter2.next();
               currentMediaType = (MediaType)currentPref.getMetadata();
               compatiblePref = true;
               currentScore = 0;

               // 1) Compare the main types
               if(currentMediaType.getMainType().equals(currentVariant.getMediaType().getMainType()))
               {
                  currentScore += 1000;
               }
               else if(!currentMediaType.getMainType().equals("*"))
               {
                  compatiblePref = false;
               }
               else if(!currentMediaType.getSubType().equals("*"))
               {
                  // Ranges such as "*/html" are not supported
                  // Only "*/*" is acceptable in this case
                  compatiblePref = false;
               }

               if(compatiblePref)
               {
                  // 2) Compare the sub types
                  if(currentVariant.getMediaType().getSubType().equals(currentMediaType.getSubType()))
                  {
                     currentScore += 100;
                  }
                  else if(!currentMediaType.getSubType().equals("*"))
                  {
                     // Subtype are different
                     compatiblePref = false;
                  }

                  if(compatiblePref && (currentVariant.getMediaType().getParameters() != null))
                  {
                     // 3) Compare the parameters
                     // If current media type is compatible with the current
                     // media range then the parameters need to be checked too
                     for(Iterator iter3 = currentVariant.getMediaType().getParameters().iterator(); iter3
                           .hasNext();)
                     {
                        currentParam = (Parameter)iter3.next();

                        if(isParameterFound(currentParam, currentMediaType))
                        {
                           currentScore++;
                        }
                     }
                  }

                  // 3) Do we have a better preference?
                  // currentScore *= currentPref.getQuality();
                  if(compatiblePref && ((bestMediaTypePref == null) || (currentScore > bestMediaTypeScore)))
                  {
                     bestMediaTypePref = currentPref;
                     bestMediaTypeScore = currentScore;
                  }
               }
            }

            // Do we have a compatible media type?
            if(bestMediaTypePref != null)
            {
               // So, let's conclude on the current variant, its quality
               float currentQuality = bestMediaTypePref.getQuality();
               if(bestLanguagePref != null)
               {
                  currentQuality += (bestLanguagePref.getQuality() * 10F);
               }
            
               if(bestVariant == null)
               {
                  bestVariant = currentVariant;
                  bestQuality = currentQuality;
               }
               else if(currentQuality > bestQuality)
               {
                  bestVariant = currentVariant;
                  bestQuality = currentQuality;
               }
               
               // Reset the preference variables
               bestLanguagePref = null;
               bestLanguageScore = 0;
               bestMediaTypePref = null;
               bestMediaTypeScore = 0;
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
