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

package com.noelios.restlet.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Resource;
import org.restlet.Call;
import org.restlet.connector.ConnectorCall;
import org.restlet.data.ConditionData;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.LanguagePref;
import org.restlet.data.MediaType;
import org.restlet.data.MediaTypePref;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.PreferenceData;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;
import org.restlet.data.SecurityData;
import org.restlet.data.Status;
import org.restlet.data.Statuses;

import com.noelios.restlet.util.DateUtils;

/**
 * Implementation of an uniform call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class CallImpl implements Call
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.impl.CallImpl");

   /** The modifiable map of attributes. */
   protected Map<String, Object> attributes;

   /** The client IP addresses. */
   protected List<String> clientAddresses;

   /** The client name. */
   protected String clientName;

   /** The condition data. */
   protected ConditionData condition;

   /** The low-level connector call. */
   protected ConnectorCall connectorCall;
   
   /** The current cookies of the client. */
   protected List<Cookie> cookies;

   /** The cookies to set in the client. */
   protected List<CookieSetting> cookieSettings;

   /** The list of substrings matched in the context path. */
   protected List<String> contextMatches;
   
   /** The context path. */
   protected String contextPath;

   /** The representation provided by the client. */
   protected Representation input;

   /** The call method. */
   protected Method method;

   /** The representation provided by the server. */
   protected Representation output;

   /** The preference data. */
   protected PreferenceData preference;
   
   /** The redirection reference. */
   protected Reference redirectionRef;
   
   /** The referrer reference. */
   protected Reference referrerRef;
   
   /** The resource reference. */
   protected Reference resourceRef;

   /** The security data. */
   protected SecurityData security;

   /** The server IP address. */
   protected String serverAddress;

   /** The server name. */
   protected String serverName;

   /** The server status. */
   protected Status status;

	/**
	 * Returns a map of attributes that can be used by developer to save information relative
	 * to the current call. This is a quicker alternative to the creation of wrapper class.
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
    * Returns the best variant representation for a given resource according the the client preferences.
    * @param resource The resource for which the best representation needs to be set.
    * @param fallbackLanguage The language to use if no preference matches.
    * @return The best variant representation. 
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   public RepresentationMetadata getBestVariant(Resource resource, Language fallbackLanguage)
   {
      return getBestVariant(resource.getVariantsMetadata(), fallbackLanguage);
   }

   /**
    * Returns the best variant representation for a given resource according the the client preferences.
    * @param variants The list of variants to compare.
    * @param fallbackLanguage The language to use if no preference matches.
    * @return The best variant representation. 
    * @see <a href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache content negotiation algorithm</a>
    */
   protected RepresentationMetadata getBestVariant(List<RepresentationMetadata> variants, Language fallbackLanguage)
   {
      if(variants == null)
      {
         return null;
      }
      else
      {
         Parameter currentParam = null;
         Language currentLanguage = null;
         MediaType currentMediaType = null;

         boolean compatiblePref = false;
         boolean compatibleLanguage = false;
         boolean compatibleMediaType = false;

         RepresentationMetadata currentVariant = null;
         RepresentationMetadata bestVariant = null;

         LanguagePref currentLanguagePref = null;
         LanguagePref bestLanguagePref = null;
         MediaTypePref currentMediaTypePref = null;
         MediaTypePref bestMediaTypePref = null;

         float bestQuality = 0;
         float currentScore = 0;
         float bestLanguageScore = 0;
         float bestMediaTypeScore = 0;

         // For each available variant, we will compute the negotiation score
         // which is dependant on the language score and on the media type score
         for(Iterator iter1 = variants.iterator(); iter1.hasNext();)
         {
            currentVariant = (RepresentationMetadata)iter1.next();

            // For each language preference defined in the call
            // Calculate the score and remember the best scoring preference
            for(Iterator<LanguagePref> iter2 = getPreference().getLanguages().iterator(); (currentVariant.getLanguage() != null) && iter2.hasNext();)
            {
               currentLanguagePref = iter2.next();
               currentLanguage = currentLanguagePref.getLanguage();
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
                     bestLanguagePref = currentLanguagePref;
                     bestLanguageScore = currentScore;
                  }
               }
            }

            // Are the preferences compatible with the current variant language?
            compatibleLanguage = (currentVariant.getLanguage() == null) || 
                                 (bestLanguagePref != null) || 
                                 (currentVariant.getLanguage().equals(fallbackLanguage));

            // For each media range preference defined in the call
            // Calculate the score and remember the best scoring preference
            for(Iterator<MediaTypePref> iter2 = getPreference().getMediaTypes().iterator(); compatibleLanguage && iter2.hasNext();)
            {
               currentMediaTypePref = iter2.next();
               currentMediaType = currentMediaTypePref.getMediaType();
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
                     bestMediaTypePref = currentMediaTypePref;
                     bestMediaTypeScore = currentScore;
                  }
               }
            }

            // Are the preferences compatible with the current media type?
            compatibleMediaType = (currentVariant.getMediaType() == null) || 
                                  (bestMediaTypePref != null); 

            if(compatibleLanguage && compatibleMediaType)
            {
               // Do we have a compatible media type?
               float currentQuality = 0;
               if(bestLanguagePref != null)
               {
                  currentQuality += (bestLanguagePref.getQuality() * 10F);
               }
               else if((currentVariant.getLanguage() != null) && currentVariant.getLanguage().equals(fallbackLanguage))
               {
                  currentQuality += 0.1F * 10F;
               }
   
               if(bestMediaTypePref != null)
               {
                  // So, let's conclude on the current variant, its quality
                  currentQuality += bestMediaTypePref.getQuality();
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
            }

            // Reset the preference variables
            bestLanguagePref = null;
            bestLanguageScore = 0;
            bestMediaTypePref = null;
            bestMediaTypeScore = 0;
         }

         return bestVariant;
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
    * Returns the client IP address.
    * @return The client IP address.
    */
   public String getClientAddress()
   {
      return getClientAddresses().get(0);
   }

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
   public List<String> getClientAddresses()
   {
   	if(this.clientAddresses == null)
   	{
   		this.clientAddresses = new ArrayList<String>();
   		
   		// Add a null value for the immediate client address
   		// This can be changed by subclasses like HttpServerRestletCall
   		this.clientAddresses.add(null);
   	}
   	
      return this.clientAddresses;
   }

   /**
    * Returns the client name.
    * @return The client name.
    */
   public String getClientName()
   {
      return this.clientName;
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
    * Returns the low-level connector call.
    * @return The low-level connector call.
    */
   public ConnectorCall getConnectorCall()
   {
      if(this.connectorCall == null) this.connectorCall = new ConnectorCallImpl();
      return this.connectorCall;
   }

   /**
    * Returns the list of substrings matched in the current context path.
    * @return The list of substrings matched.
    * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Matcher.html#group(int)">Matcher.group()</a>
    */
   public List<String> getContextMatches()
   {
      if(this.contextMatches == null) this.contextMatches = new ArrayList<String>();
      return this.contextMatches;
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
    * Returns the context path as a reference.
    * @return The context path as a reference.
    */
   public Reference getContextRef()
   {
      return new Reference(getContextPath());
   }

   /**
    * Returns the cookies provided by the client to the server.
    * @return The cookies provided by the client to the server.
    */
   public List<Cookie> getCookies()
   {
      if(this.cookies == null) this.cookies = new ArrayList<Cookie>();
      return this.cookies;
   }

   /**
    * Returns the cookies provided by the server to the client.
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
    * Returns the preference data of the client.
    * @return The preference data of the client.
    */
   public PreferenceData getPreference()
   {
      if(this.preference == null) this.preference = new PreferenceData();
      return this.preference;
   }

   /**
    * Returns the reference for redirections or resource creations.
    * @return The redirection reference.
    */
   public Reference getRedirectionRef()
   {
      return this.redirectionRef;
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
    * Returns the server IP address.
    * @return The server IP address.
    */
   public String getServerAddress()
   {
      return this.serverAddress;
   }

   /**
    * Returns the server name (ex: web server name).
    * @return The server name.
    */
   public String getServerName()
   {
      return this.serverName;
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
      List<RepresentationMetadata> variants = resource.getVariantsMetadata();

      if((variants == null) || (variants.size() < 1))
      {
         // Resource not found
         setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }
      else
      {
         // Compute the best variant
         RepresentationMetadata bestVariant = getBestVariant(variants, fallbackLanguage);
         
         if(bestVariant == null)
         {
            // No variant was found matching the call preferences
            setStatus(Statuses.CLIENT_ERROR_NOT_ACCEPTABLE);
         }
         else
         {
            // Was the representation modified since the last client call?
            Date modifiedSince = (getCondition() == null) ? null : getCondition().getModifiedSince();
            if((modifiedSince == null) || DateUtils.after(modifiedSince, bestVariant.getModificationDate()))
            {
               // Yes, set the best representation as the call output
               setOutput(resource.getRepresentation(bestVariant));
               setStatus(Statuses.SUCCESS_OK);
            }
            else
            {
               // No, indicates it to the client
               setStatus(Statuses.REDIRECTION_NOT_MODIFIED);
            }
         }
      }
   }
   
   /**
    * Sets the client IP address.
    * @param address The client IP address.
    */
   public void setClientAddress(String address)
   {
     	getClientAddresses().set(0, address);
   }

   /**
    * Sets the client name.
    * @param name The client name.
    */
   public void setClientName(String name)
   {
      this.clientName = name;
   }

   /**
    * Sets the low-level connector call.
    * @param call The low-level connector call.
    */
   public void setConnectorCall(ConnectorCall call)
   {
      this.connectorCall = call;
   }

   /**
    * Sets the absolute context path, preceeding the relative resource path in the resource reference.
    * @param contextPath The absolute context path.
    */
   public void setContextPath(String contextPath)
   {
      if((contextPath != null) && (!this.resourceRef.toString(false, false).startsWith(contextPath)))
      {
         logger.warning("Context path doesn't match the start of the resource URI: " + contextPath);
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
    * Sets the reference for redirections or resource creations.
    * @param redirectionRef The redirection reference.
    */
   public void setRedirectionRef(Reference redirectionRef)
   {
      this.redirectionRef = redirectionRef;
   }

   /**
    * Sets the reference for redirections or resource creations using an URI string. Note that
    * the redirection URI can be either absolute or relative to the current context reference.
    * @param redirectionUri The redirection URI.
    */
   public void setRedirectionRef(String redirectionUri)
   {
   	setRedirectionRef(new Reference(getContextRef(), redirectionUri).getTargetRef());
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
      
      // Reset the current restlet
      setContextPath(null);
      getContextMatches().clear();
   }

   /**
    * Sets the resource reference using an URI string. Note that the resource URI can be either 
    * absolute or relative to the current context reference.
    * @param resourceUri The resource URI.
    */
   public void setResourceRef(String resourceUri)
   {
   	setResourceRef(new Reference(getContextRef(), resourceUri).getTargetRef());
   }

   /**
    * Sets the server IP address.
    * @param address The server IP address.
    */
   public void setServerAddress(String address)
   {
      this.serverAddress = address;
   }

   /**
    * Sets the server name (ex: web server name).
    * @param name The server name.
    */
   public void setServerName(String name)
   {
      this.serverName = name;
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
