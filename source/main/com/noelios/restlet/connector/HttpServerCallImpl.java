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

package com.noelios.restlet.connector;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Manager;
import org.restlet.UniformCall;
import org.restlet.connector.HttpCall;
import org.restlet.connector.HttpServerCall;
import org.restlet.data.CharacterSetPref;
import org.restlet.data.CharacterSets;
import org.restlet.data.ConditionData;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Encoding;
import org.restlet.data.EncodingPref;
import org.restlet.data.Encodings;
import org.restlet.data.Language;
import org.restlet.data.LanguagePref;
import org.restlet.data.Languages;
import org.restlet.data.MediaType;
import org.restlet.data.MediaTypePref;
import org.restlet.data.MediaTypes;
import org.restlet.data.Methods;
import org.restlet.data.Parameter;
import org.restlet.data.PreferenceData;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;
import org.restlet.data.SecurityData;
import org.restlet.data.Statuses;
import org.restlet.data.Tag;

import com.noelios.restlet.Engine;
import com.noelios.restlet.UniformCallImpl;
import com.noelios.restlet.data.CharacterSetPrefImpl;
import com.noelios.restlet.data.ConditionDataImpl;
import com.noelios.restlet.data.EncodingPrefImpl;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.LanguagePrefImpl;
import com.noelios.restlet.data.MediaTypePrefImpl;
import com.noelios.restlet.data.MethodImpl;
import com.noelios.restlet.data.PreferenceDataImpl;
import com.noelios.restlet.data.ReadableRepresentation;
import com.noelios.restlet.data.ReferenceImpl;
import com.noelios.restlet.data.SecurityDataImpl;
import com.noelios.restlet.data.TagImpl;
import com.noelios.restlet.util.CookieReader;
import com.noelios.restlet.util.CookieUtils;
import com.noelios.restlet.util.DateUtils;
import com.noelios.restlet.util.PreferenceReader;
import com.noelios.restlet.util.SecurityUtils;

/**
 * Implementation of a server call for the HTTP protocol.
 */
public abstract class HttpServerCallImpl extends UniformCallImpl implements HttpServerCall
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.connector.HttpServerCallImpl");
   
   /** The response headers. */
   protected List<Parameter> responseHeaders;

   /**
    * Converts to an uniform call.
    * @return An equivalent uniform call.
    */
   public UniformCall toUniform()
   {
      // Set the properties
      setClientAddress(getRequestAddress());
      setServerAddress(getResponseAddress());
      setServerName(Engine.VERSION_HEADER);
      setStatus(Statuses.SUCCESS_OK);

      // Set the method
      String method = getRequestMethod();
      if(method != null)
      {
         if(method.equals(Methods.GET.getName())) setMethod(Methods.GET);
         else if(method.equals(Methods.POST.getName())) setMethod(Methods.POST);
         else if(method.equals(Methods.HEAD.getName())) setMethod(Methods.HEAD);
         else if(method.equals(Methods.OPTIONS.getName())) setMethod(Methods.OPTIONS);
         else if(method.equals(Methods.PUT.getName())) setMethod(Methods.PUT);
         else if(method.equals(Methods.DELETE.getName())) setMethod(Methods.DELETE);
         else if(method.equals(Methods.CONNECT.getName())) setMethod(Methods.CONNECT);
         else if(method.equals(Methods.COPY.getName())) setMethod(Methods.COPY);
         else if(method.equals(Methods.LOCK.getName())) setMethod(Methods.LOCK);
         else if(method.equals(Methods.MKCOL.getName())) setMethod(Methods.MKCOL);
         else if(method.equals(Methods.MOVE.getName())) setMethod(Methods.MOVE);
         else if(method.equals(Methods.PROPFIND.getName())) setMethod(Methods.PROPFIND);
         else if(method.equals(Methods.PROPPATCH.getName())) setMethod(Methods.PROPPATCH);
         else if(method.equals(Methods.TRACE.getName())) setMethod(Methods.TRACE);
         else if(method.equals(Methods.UNLOCK.getName())) setMethod(Methods.UNLOCK);
         else setMethod(new MethodImpl(method));
      }

      // Set the resource reference
      String resource = getRequestUri();
      if(resource != null)
      {
         setResourceRef(new ReferenceImpl(resource));

         // Set the absolute resource path as the initial path in the list.
         getResourcePaths().add(0, resource);
      }

      return this;
   }
   
   /**
    * Commits after synchronization with an uniform call.
    * @param call The call to synchronize with.
    */
   public void commitFrom(UniformCall call)
   {
      try
      {
         // Add the cookie settings
         List<CookieSetting> cookies = call.getCookieSettings();
         for(int i = 0; i < cookies.size(); i++)
         {
            addResponseHeader(HttpCall.HEADER_SET_COOKIE, CookieUtils.format(cookies.get(i)));
         }
         
         // Set the redirection URI
         if(call.getRedirectRef() != null)
         {
            addResponseHeader(HEADER_LOCATION, call.getRedirectRef().toString());
         }

         // Set the security data
         if(call.getSecurity().getChallengeRequest() != null)
         {
            addResponseHeader(HEADER_WWW_AUTHENTICATE, SecurityUtils.format(call.getSecurity().getChallengeRequest()));
         }

         // Set the server name again
         addResponseHeader(HEADER_SERVER, call.getServerName());
         
         // Set the status code in the response
         if(call.getStatus() != null)
         {
            setResponseStatus(call.getStatus().getHttpCode(), call.getStatus().getDescription());
         }
   
         // If an output was set during the call, copy it to the output stream;
         if(call.getOutput() != null)
         {
            RepresentationMetadata meta = call.getOutput().getMetadata();
   
            if(meta.getExpirationDate() != null)
            {
               addResponseHeader(HEADER_EXPIRES, formatDate(meta.getExpirationDate(), false));
            }
            
            if((meta.getEncoding() != null) && (!meta.getEncoding().equals(Encodings.IDENTITY)))
            {
               addResponseHeader(HEADER_CONTENT_ENCODING, meta.getEncoding().getName());
            }
            
            if(meta.getLanguage() != null)
            {
               addResponseHeader(HEADER_CONTENT_LANGUAGE, meta.getLanguage().getName());
            }
            
            if(meta.getMediaType() != null)
            {
               StringBuilder contentType = new StringBuilder(meta.getMediaType().getName());
   
               if(meta.getCharacterSet() != null)
               {
                  // Specify the character set parameter
                  contentType.append("; charset=").append(meta.getCharacterSet().getName());
               }
   
               addResponseHeader(HEADER_CONTENT_TYPE, contentType.toString());
            }
   
            if(meta.getModificationDate() != null)
            {
               addResponseHeader(HEADER_LAST_MODIFIED, formatDate(meta.getModificationDate(), false));
            }
   
            if(meta.getTag() != null)
            {
               addResponseHeader(HEADER_ETAG, meta.getTag().getName());
            }
            
            if(call.getOutput().getSize() != -1)
            {
               addResponseHeader(HEADER_CONTENT_LENGTH, Long.toString(call.getOutput().getSize()));
            }
   
            // Commit the headers
            commitResponseHeaders();
            
            // Send the output to the client
            call.getOutput().write(getResponseStream());
         }
         else
         {
            // Only commit the headers
            commitResponseHeaders();
         }
      }
      catch(Exception e)
      {
         logger.log(Level.INFO, "Exception intercepted", e);
         setResponseStatus(500, "An unexpected exception occured");
      }
   }
   
   /**
    * Parses a date string.
    * @param date The date string to parse.
    * @param cookie Indicates if the date is in the cookie format.
    * @return The parsed date.
    */
   public Date parseDate(String date, boolean cookie)
   {
      if(cookie)
      {
         return DateUtils.parse(date, DateUtils.FORMAT_RFC_1036);
      }
      else
      {
         return DateUtils.parse(date, DateUtils.FORMAT_RFC_1123);
      }
   }
   
   /**
    * Formats a date as a header string.
    * @param date The date to format.
    * @param cookie Indicates if the date should be in the cookie format.
    * @return The formatted date.
    */
   public String formatDate(Date date, boolean cookie)
   {
      if(cookie)
      {
         return DateUtils.format(date, DateUtils.FORMAT_RFC_1036);
      }
      else
      {
         return DateUtils.format(date, DateUtils.FORMAT_RFC_1123);
      }
   }
   
   /**
    * Returns the list of response headers.
    * @return The list of response headers.
    */
   public List<Parameter> getResponseHeaders()
   {
      if(this.responseHeaders == null)
      {
         this.responseHeaders = new ArrayList<Parameter>();
      }
      
      return this.responseHeaders;
   }

   /**
    * Adds a response header.
    * @param name The header's name.
    * @param value The header's value.
    */
   public void addResponseHeader(String name, String value)
   {
      getResponseHeaders().add(Manager.createParameter(name, value));
   }

   /**
    * Returns the client name.
    * @return The client name.
    */
   public String getClientName()
   {
      if(this.clientName == null)
      {
         // Extract the header values
         Parameter header;
         for(Iterator<Parameter> iter = getRequestHeaders().iterator(); iter.hasNext(); )
         {
            header = iter.next();
            
            if(header.getName().equalsIgnoreCase(HEADER_USER_AGENT))
            {
               this.clientName = header.getValue();
            }
         }
      }
      
      return this.clientName;
   }

   /**
    * Returns the condition data applying to this call.
    * @return The condition data applying to this call.
    */
   public ConditionData getCondition()
   {
      if(this.condition == null) 
      {
         this.condition = new ConditionDataImpl();

         // Extract the header values
         Date ifModifiedSince = null;
         Date ifUnmodifiedSince = null;
         String ifMatchHeader = null;
         String ifNoneMatchHeader = null;
         Parameter header;
         for(Iterator<Parameter> iter = getRequestHeaders().iterator(); iter.hasNext(); )
         {
            header = iter.next();
            
            if(header.getName().equalsIgnoreCase(HEADER_IF_MATCH))
            {
               ifMatchHeader = header.getValue();
            }
            else if(header.getName().equalsIgnoreCase(HEADER_IF_MODIFIED_SINCE))
            {
               ifModifiedSince = parseDate(header.getValue(), false);
            }
            else if(header.getName().equalsIgnoreCase(HEADER_IF_NONE_MATCH))
            {
               ifNoneMatchHeader = header.getValue();
            }
            else if(header.getName().equalsIgnoreCase(HEADER_IF_UNMODIFIED_SINCE))
            {
               ifUnmodifiedSince = parseDate(header.getValue(), false);
            }
         }
         
         // Set the If-Modified-Since date
         if((ifModifiedSince != null) && (ifModifiedSince.getTime() != -1))
         {
            getCondition().setModifiedSince(ifModifiedSince);
         }

         // Set the If-Unmodified-Since date
         if((ifUnmodifiedSince != null) && (ifUnmodifiedSince.getTime() != -1))
         {
            getCondition().setUnmodifiedSince(ifUnmodifiedSince);
         }

         // Set the If-Match tags
         List<Tag> match = null;
         Tag current = null;
         if(ifMatchHeader != null)
         {
            try
            {
               String[] tags = ifMatchHeader.split(",");
               for (int i = 0; i < tags.length; i++)
               {
                  current = new TagImpl(tags[i]);
               
                  // Is it the first tag?
                  if(match == null) 
                  {
                     match = new ArrayList<Tag>();
                     getCondition().setMatch(match);
                  }
                  
                  // Add the new tag
                  match.add(current);
               }
            }
            catch(Exception e)
            {
               logger.log(Level.WARNING, "Unable to process the if-match header: " + ifNoneMatchHeader, e);
            }
         }

         // Set the If-None-Match tags
         List<Tag> noneMatch = null;
         if(ifNoneMatchHeader != null)
         {
            try
            {
               String[] tags = ifNoneMatchHeader.split(",");
               for (int i = 0; i < tags.length; i++)
               {
                  current = new TagImpl(tags[i]);
                  
                  // Is it the first tag?
                  if(noneMatch == null) 
                  {
                     noneMatch = new ArrayList<Tag>();
                     getCondition().setNoneMatch(noneMatch);
                  }
                  
                  noneMatch.add(current);
               }
            }
            catch(Exception e)
            {
               logger.log(Level.WARNING, "Unable to process the if-none-match header: " + ifNoneMatchHeader, e);
            }
         }
      }

      return this.condition;
   }

   /**
    * Returns the cookies provided by the client.
    * @return The cookies provided by the client.
    */
   public List<Cookie> getCookies()
   {
      if(this.cookies == null) 
      {
         this.cookies = new ArrayList<Cookie>();

         // Extract the header values
         Parameter header;
         for(Iterator<Parameter> iter = getRequestHeaders().iterator(); iter.hasNext(); )
         {
            header = iter.next();
            if(header.getName().equalsIgnoreCase(HEADER_COOKIE))
            {
               try
               {
                  CookieReader cr = new CookieReader(header.getValue());
                  Cookie current = cr.readCookie();
                  while(current != null)
                  {
                     this.cookies.add(current);
                     current = cr.readCookie();
                  }
               }
               catch(Exception e)
               {
                  logger.log(Level.WARNING, "An exception occured during cookies parsing. Header: " + header.getValue(), e);
               }
            }
         }
      }
      
      return this.cookies;
   }

   /**
    * Returns the representation provided by the client.
    * @return The representation provided by the client.
    */
   public Representation getInput()
   {
      if((this.input == null) && ((getRequestStream() != null) || getRequestChannel() != null))
      {
         // Extract the header values
         Encoding contentEncoding = null;
         Language contentLanguage = null;
         MediaType contentType = null;
         Parameter header;
         for(Iterator<Parameter> iter = getRequestHeaders().iterator(); iter.hasNext(); )
         {
            header = iter.next();
            if(header.getName().equalsIgnoreCase(HEADER_CONTENT_ENCODING))
            {
               contentEncoding = Manager.createEncoding(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(HEADER_CONTENT_LANGUAGE))
            {
               contentLanguage = Manager.createLanguage(header.getValue());
            }
            else if(header.getName().equalsIgnoreCase(HEADER_CONTENT_TYPE))
            {
               contentType = Manager.createMediaType(header.getValue());
            }
         }

         if(getRequestStream() != null)
         {
            this.input = new InputRepresentation(getRequestStream(), contentType);
         }
         else if(getRequestChannel() != null)
         {
            this.input = new ReadableRepresentation(getRequestChannel(), contentType);
         }
         
         this.input.getMetadata().setEncoding(contentEncoding);
         this.input.getMetadata().setLanguage(contentLanguage);
      }
      
      return this.input;
   }

   /**
    * Returns the preference data of the client.
    * @return The preference data of the client.
    */
   public PreferenceData getPreference()
   {
      if(this.preference == null) 
      {
         this.preference = new PreferenceDataImpl();

         // Extract the header values
         String acceptCharset = null;
         String acceptEncoding = null;
         String acceptLanguage = null;
         String acceptMediaType = null;
         Parameter header;
         for(Iterator<Parameter> iter = getRequestHeaders().iterator(); iter.hasNext(); )
         {
            header = iter.next();

            if(header.getName().equalsIgnoreCase(HEADER_ACCEPT))
            {
               if(acceptMediaType == null)
               {
                  acceptMediaType = header.getValue();
               }
               else
               {
                  acceptMediaType = acceptMediaType + ", " + header.getValue();
               }
            }
            else if(header.getName().equalsIgnoreCase(HEADER_ACCEPT_CHARSET))
            {
               if(acceptCharset == null)
               {
                  acceptCharset = header.getValue();
               }
               else
               {
                  acceptCharset = acceptCharset + ", " + header.getValue();
               }
            }
            else if(header.getName().equalsIgnoreCase(HEADER_ACCEPT_ENCODING))
            {
               if(acceptEncoding == null)
               {
                  acceptEncoding = header.getValue();
               }
               else
               {
                  acceptEncoding = acceptEncoding + ", " + header.getValue();
               }
            }
            else if(header.getName().equalsIgnoreCase(HEADER_ACCEPT_LANGUAGE))
            {
               if(acceptLanguage == null)
               {
                  acceptLanguage = header.getValue();
               }
               else
               {
                  acceptLanguage = acceptLanguage + ", " + header.getValue();
               }
            }
         }         

         if(acceptCharset != null)
         {
            // Implementation according to
            // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.2
            if(acceptCharset.length() == 0)
            {
               this.preference.getCharacterSets().add(new CharacterSetPrefImpl(CharacterSets.ISO_8859_1));
            }
            else
            {
               try
               {
                  PreferenceReader pr = new PreferenceReader(PreferenceReader.TYPE_CHARACTER_SET, acceptCharset);
                  CharacterSetPref currentPref = (CharacterSetPref)pr.readPreference();
                  while(currentPref != null)
                  {
                     this.preference.getCharacterSets().add(currentPref);
                     currentPref = (CharacterSetPref)pr.readPreference();
                  }
               }
               catch(Exception e)
               {
                  logger.log(Level.WARNING, "An exception occured during character set preferences parsing. Header: " + acceptCharset, e);
               }
            }
         }
         else
         {
            this.preference.getCharacterSets().add(new CharacterSetPrefImpl(CharacterSets.ALL));
         }

         if(acceptEncoding != null)
         {
            try
            {
               PreferenceReader pr = new PreferenceReader(PreferenceReader.TYPE_ENCODING, acceptEncoding);
               EncodingPref currentPref = (EncodingPref)pr.readPreference();
               while(currentPref != null)
               {
                  this.preference.getEncodings().add(currentPref);
                  currentPref = (EncodingPref)pr.readPreference();
               }
            }
            catch(Exception e)
            {
               logger.log(Level.WARNING, "An exception occured during encoding preferences parsing. Header: " + acceptEncoding, e);
            }
         }
         else
         {
            this.preference.getEncodings().add(new EncodingPrefImpl(Encodings.ALL));
         }

         if(acceptLanguage != null)
         {
            try
            {
               PreferenceReader pr = new PreferenceReader(PreferenceReader.TYPE_LANGUAGE, acceptLanguage);
               LanguagePref currentPref = (LanguagePref)pr.readPreference();
               while(currentPref != null)
               {
                  this.preference.getLanguages().add(currentPref);
                  currentPref = (LanguagePref)pr.readPreference();
               }
            }
            catch(Exception e)
            {
               logger.log(Level.WARNING, "An exception occured during language preferences parsing. Header: " + acceptLanguage, e);
            }
         }
         else
         {
            this.preference.getLanguages().add(new LanguagePrefImpl(Languages.ALL));
         }

         if(acceptMediaType != null)
         {
            try
            {
               PreferenceReader pr = new PreferenceReader(PreferenceReader.TYPE_MEDIA_TYPE, acceptMediaType);
               MediaTypePref currentPref = (MediaTypePref)pr.readPreference();
               while(currentPref != null)
               {
                  this.preference.getMediaTypes().add(currentPref);
                  currentPref = (MediaTypePref)pr.readPreference();
               }
            }
            catch(Exception e)
            {
               logger.log(Level.WARNING, "An exception occured during media type preferences parsing. Header: " + acceptMediaType, e);
            }
         }
         else
         {
            this.preference.getMediaTypes().add(new MediaTypePrefImpl(MediaTypes.ALL));
         }
      }
      
      return this.preference;
   }

   /**
    * Returns the referrer reference if available.
    * @return The referrer reference.
    */
   public Reference getReferrerRef()
   {
      if(this.referrerRef == null)
      {
         // Extract the header values
         Parameter header;
         for(Iterator<Parameter> iter = getRequestHeaders().iterator(); (this.referrerRef == null) && iter.hasNext(); )
         {
            header = iter.next();
            
            if(header.getName().equalsIgnoreCase(HEADER_REFERRER))
            {
               this.referrerRef = new ReferenceImpl(header.getValue());
            }
         }
      }
      
      return this.referrerRef;
   }

   /**
    * Returns the security data related to this call.
    * @return The security data related to this call.
    */
   public SecurityData getSecurity()
   {
      if(this.security == null) 
      {
         this.security = new SecurityDataImpl();

         if(isConfidential()) 
         {
            getSecurity().setConfidential(isConfidential());
         }
         else
         {
            // We don't want to autocreate the security data just for this information
            // Because that will by the default value of this property if read by someone.
         }

         // Extract the header values
         String authorization = null;
         Parameter header;
         for(Iterator<Parameter> iter = getRequestHeaders().iterator(); (authorization == null) && iter.hasNext(); )
         {
            header = iter.next();
            
            if(header.getName().equalsIgnoreCase(HEADER_AUTHORIZATION))
            {
               authorization = header.getValue();
            }
         }

         // Set the challenge response
         getSecurity().setChallengeResponse(SecurityUtils.parseResponse(authorization));
      }

      return this.security;
   }
   
}
