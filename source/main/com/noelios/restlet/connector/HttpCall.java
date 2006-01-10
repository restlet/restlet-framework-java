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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.CharacterSets;
import org.restlet.data.Conditions;
import org.restlet.data.CookieSetting;
import org.restlet.data.Cookies;
import org.restlet.data.Languages;
import org.restlet.data.MediaTypes;
import org.restlet.data.Method;
import org.restlet.data.Methods;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;
import org.restlet.data.Security;
import org.restlet.data.Tag;

import com.noelios.restlet.UniformCallImpl;
import com.noelios.restlet.data.ChallengeResponseImpl;
import com.noelios.restlet.data.ChallengeSchemeImpl;
import com.noelios.restlet.data.ConditionsImpl;
import com.noelios.restlet.data.CookiesImpl;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.MediaTypeImpl;
import com.noelios.restlet.data.MethodImpl;
import com.noelios.restlet.data.PreferenceImpl;
import com.noelios.restlet.data.PreferenceReaderImpl;
import com.noelios.restlet.data.ReferenceImpl;
import com.noelios.restlet.data.SecurityImpl;
import com.noelios.restlet.data.TagImpl;

/**
 * Base class for HTTP based uniform calls.
 */
public abstract class HttpCall extends UniformCallImpl
{
   public static final int STATUS_SUCCESS_CREATED = 201;
   public static final int STATUS_REDIRECTION_MULTIPLE_CHOICES = 300;
   public static final int STATUS_REDIRECTION_MOVED_PERMANENTLY = 301;
   public static final int STATUS_REDIRECTION_FOUND = 302;
   public static final int STATUS_REDIRECTION_SEE_OTHER = 303;
   public static final int STATUS_REDIRECTION_TEMPORARY_REDIRECT = 307;
   public static final int STATUS_CLIENT_ERROR_UNAUTHORIZED = 401;

   public static final String HEADER_ACCEPT = "Accept";
   public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
   public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
   public static final String HEADER_AUTHORIZATION = "Authorization";
   public static final String HEADER_COOKIE = "Cookie";
   public static final String HEADER_CONTENT_LENGTH = "Content-Length";
   public static final String HEADER_CONTENT_TYPE = "Content-Type";
   public static final String HEADER_ETAG = "ETag";
   public static final String HEADER_EXPIRES = "Expires";
   public static final String HEADER_IF_MATCH = "If-Match";
   public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
   public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
   public static final String HEADER_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
   public static final String HEADER_LAST_MODIFIED = "Last-Modified";
   public static final String HEADER_LOCATION = "Location";
   public static final String HEADER_REFERRER = "Referer";
   public static final String HEADER_USER_AGENT = "User-Agent";
   public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

   /**
    * Initialization the call by extracting information from the underlying request object.
    */
   protected void init()
   {
      setCharacterSetPrefs(extractCharacterSetPrefs());
      setClientAddress(extractClientAddress());
      setClientName(extractClientName());
      setCookies(extractCookies());
      setInput(extractInput());
      setLanguagePrefs(extractLanguagePrefs());
      setMediaTypePrefs(extractMediaTypePrefs());
      setMethod(extractMethod());
      setReferrerRef(extractReferrer());
      setResourceRef(extractResource());
      setSecurity(extractSecurity());
      setConditions(extractConditions());
   }

   /**
    * Reply to the call by sending a response back to the client.
    */
   public void reply()
   {
      try
      {
         // Set the status code in the response
         if(getStatus() != null)
         {
            setResponseStatus(getStatus().getHttpCode(), getStatus().getDescription());
   
            switch(getStatus().getHttpCode())
            {
               case STATUS_SUCCESS_CREATED:
               case STATUS_REDIRECTION_MULTIPLE_CHOICES:
               case STATUS_REDIRECTION_MOVED_PERMANENTLY:
               case STATUS_REDIRECTION_FOUND:
               case STATUS_REDIRECTION_SEE_OTHER:
               case STATUS_REDIRECTION_TEMPORARY_REDIRECT:
                  // Extract the redirection URI from the call output
                  if((getOutput() != null)
                        && (getOutput().getMetadata().getMediaType().equals(MediaTypes.TEXT_URI)))
                  {
                     setResponseHeader(HEADER_LOCATION, getOutput().toString());
                     setOutput(null);
                  }
               break;
               
               case STATUS_CLIENT_ERROR_UNAUTHORIZED:
                  if((getSecurity() != null) && (getSecurity().getChallengeRequest() != null))
                  {
                     ChallengeRequest challenge = getSecurity().getChallengeRequest();
                     setResponseHeader(HEADER_WWW_AUTHENTICATE, challenge.getScheme().getTechnicalName() + " realm=\"" + challenge.getRealm() + '"');
                  }
               break;
            }
         }
         
         // Set cookies
         for(Iterator iter = getCookieSettings().iterator(); iter.hasNext();)
         {
            setResponseCookie((CookieSetting)iter.next());
         }
   
         // If an output was set during the call, copy it to the output stream;
         if(getOutput() != null)
         {
            RepresentationMetadata meta = getOutput().getMetadata();
   
            if(meta.getMediaType() != null)
            {
               StringBuilder contentType = new StringBuilder(meta.getMediaType().getName());
   
               if(meta.getCharacterSet() != null)
               {
                  // Specify the character set parameter
                  contentType.append("; charset=").append(meta.getCharacterSet().getName());
               }
   
               setResponseHeader(HEADER_CONTENT_TYPE, contentType.toString());
            }
   
            if(meta.getExpirationDate() != null)
            {
               setResponseDateHeader(HEADER_EXPIRES, meta.getExpirationDate().getTime());
            }
   
            if(meta.getModificationDate() != null)
            {
               setResponseDateHeader(HEADER_LAST_MODIFIED, meta.getModificationDate().getTime());
            }
   
            if(meta.getTag() != null)
            {
               setResponseHeader(HEADER_ETAG, meta.getTag().getName());
            }
   
            if(getOutput().getSize() != -1)
            {
               setResponseHeader(HEADER_CONTENT_LENGTH, Long.toString(getOutput().getSize()));
            }
   
            // Send the output to the client
            getOutput().write(getResponseStream());
         }
      }
      catch(IOException ioe)
      {
      }
   }
   
   /**
    * Returns a request date header value.
    * @param name The name of the header.
    * @return A header value.
    */
   public abstract long getRequestDateHeader(String name);
   
   /**
    * Returns a request date header value.
    * @param name The name of the header.
    * @return A header value.
    */
   public abstract String getRequestHeader(String name);
   
   /**
    * Gets the request stream.
    * @return The request stream.
    */
   protected abstract InputStream getRequestStream();
   
   /**
    * Sets a response cookie.
    * @param cookie The cookie setting.
    */
   public abstract void setResponseCookie(CookieSetting cookie);
   
   /**
    * Sets a response header value.
    * @param name The name of the header.
    * @param value The value of the header.
    */
   public abstract void setResponseHeader(String name, String value);
   
   /**
    * Sets a response date header value.
    * @param name The name of the header.
    * @param date The value of the header.
    */
   public abstract void setResponseDateHeader(String name, long date);

   /**
    * Sets the response's status code.
    * @param code The response's status code.
    * @param description The status code description.
    */
   public abstract void setResponseStatus(int code, String description);
   
   /**
    * Gets the response stream.
    * @return The response stream.
    * @throws IOException 
    */
   protected abstract OutputStream getResponseStream() throws IOException;
   
   /**
    * Extracts the resource URI.
    * @return The resource URI.
    */
   protected abstract String extractResourceURI();
   
   /**
    * Indicates if the confidentiality is ensured.
    * @return True if the confidentiality is ensured. 
    */
   protected abstract boolean extractConfidentiality();
   
   /**
    * Extracts the requested HTTP method name.
    * @return The requested HTTP method name.
    */
   protected abstract String extractMethodName();
   
   /**
    * Extracts the client IP address.
    * @return The client IP address.
    */
   protected abstract String extractClientAddress();

   /**
    * Extracts the call's referrer from the HTTP header.
    * @return The call's referrer.
    */
   protected Reference extractReferrer()
   {
      String referrer = getRequestHeader(HEADER_REFERRER);

      if(referrer != null)
      {
         return new ReferenceImpl(referrer);
      }
      else
      {
         return null;
      }
   }

   /**
    * Extracts the client name from the HTTP header.
    * @return The client name .
    */
   protected String extractClientName()
   {
      return getRequestHeader(HEADER_USER_AGENT);
   }

   /**
    * Extracts the call's resource from the HTTP request.
    * @return The call's resource.
    */
   protected Reference extractResource()
   {
      String resource = extractResourceURI();

      if(resource != null)
      {
         return new ReferenceImpl(resource);
      }
      else
      {
         return null;
      }
   }

   /**
    * Extracts the call's method from the HTTP request.
    * @return The call's method.
    */
   protected Method extractMethod()
   {
      String method = extractMethodName();
      if(method == null) return null;
      else if(method.equals(Methods.GET.getName())) return Methods.GET;
      else if(method.equals(Methods.POST.getName())) return Methods.POST;
      else if(method.equals(Methods.HEAD.getName())) return Methods.HEAD;
      else if(method.equals(Methods.OPTIONS.getName())) return Methods.OPTIONS;
      else if(method.equals(Methods.PUT.getName())) return Methods.PUT;
      else if(method.equals(Methods.DELETE.getName())) return Methods.DELETE;
      else if(method.equals(Methods.CONNECT.getName())) return Methods.CONNECT;
      else if(method.equals(Methods.COPY.getName())) return Methods.COPY;
      else if(method.equals(Methods.LOCK.getName())) return Methods.LOCK;
      else if(method.equals(Methods.MKCOL.getName())) return Methods.MKCOL;
      else if(method.equals(Methods.MOVE.getName())) return Methods.MOVE;
      else if(method.equals(Methods.PROPFIND.getName())) return Methods.PROPFIND;
      else if(method.equals(Methods.PROPPATCH.getName())) return Methods.PROPPATCH;
      else if(method.equals(Methods.TRACE.getName())) return Methods.TRACE;
      else if(method.equals(Methods.UNLOCK.getName())) return Methods.UNLOCK;
      else return new MethodImpl(method);
   }

   /**
    * Extracts the call's input representation from the HTTP request.
    * @return The call's input representation.
    */
   protected Representation extractInput()
   {
      return new InputRepresentation(getRequestStream(),
           new MediaTypeImpl(getRequestHeader(HEADER_CONTENT_TYPE)));
   }

   /**
    * Extracts the call's media preferences from the HTTP request.
    * @return The call's media preferences.
    */
   protected List<Preference> extractMediaTypePrefs()
   {
      List<Preference> result = null;
      String accept = getRequestHeader(HEADER_ACCEPT);

      if(accept != null)
      {
         PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_MEDIA_TYPE, accept);
         result = pr.readPreferences();
      }
      else
      {
         result = new ArrayList<Preference>();
         result.add(new PreferenceImpl(MediaTypes.ALL));
      }

      return result;
   }

   /**
    * Extracts the call's character set preferences from the HTTP request.
    * @return The call's character set preferences.
    */
   protected List<Preference> extractCharacterSetPrefs()
   {
      // Implementation according to
      // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.2
      List<Preference> result = null;
      String acceptCharset = getRequestHeader(HEADER_ACCEPT_CHARSET);

      if(acceptCharset != null)
      {
         if(acceptCharset.length() == 0)
         {
            result = new ArrayList<Preference>();
            result.add(new PreferenceImpl(CharacterSets.ISO_8859_1));
         }
         else
         {
            PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_CHARACTER_SET,
                  acceptCharset);
            result = pr.readPreferences();
         }
      }
      else
      {
         result = new ArrayList<Preference>();
         result.add(new PreferenceImpl(CharacterSets.ALL));
      }

      return result;
   }

   /**
    * Extracts the call's language preferences from the HTTP request.
    * @return The call's language preferences.
    */
   protected List<Preference> extractLanguagePrefs()
   {
      List<Preference> result = null;
      String acceptLanguage = getRequestHeader(HEADER_ACCEPT_LANGUAGE);

      if(acceptLanguage != null)
      {
         PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_LANGUAGE,
               acceptLanguage);
         result = pr.readPreferences();
      }
      else
      {
         result = new ArrayList<Preference>();
         result.add(new PreferenceImpl(Languages.ALL));
      }

      return result;
   }

   /**
    * Extracts the call's cookies from the HTTP header.
    * @return The call's cookies.
    */
   protected Cookies extractCookies()
   {
      Cookies result = null;
      String cookieHeader = getRequestHeader(HEADER_COOKIE);

      if(cookieHeader != null)
      {
         result = new CookiesImpl(cookieHeader);
      }

      return result;
   }

   /**
    * Extracts the call's security data from the HTTP request.
    * @return The call's security data.
    */
   protected Security extractSecurity()
   {
      Security result = new SecurityImpl();
      result.setConfidential(extractConfidentiality());

      String authorization = getRequestHeader(HEADER_AUTHORIZATION);
      if(authorization != null)
      {
         int space = authorization.indexOf(' ');

         if(space != -1)
         {
            String scheme = authorization.substring(0, space);
            String credentials = authorization.substring(space + 1);
            ChallengeResponse challengeResponse = new ChallengeResponseImpl(new ChallengeSchemeImpl("HTTP_" + scheme, scheme), credentials);
            result.setChallengeResponse(challengeResponse);
         }
      }

      return result;
   }

   /**
    * Extracts the call's conditions from the HTTP request.
    * @return The call's conditions.
    */
   protected Conditions extractConditions()
   {
      Conditions result = new ConditionsImpl();

      // Extract the If-Modified-Since date
      long ifModifiedSince = getRequestDateHeader(HEADER_IF_MODIFIED_SINCE);
      if(ifModifiedSince != -1)
      {
         result.setModifiedSince(new Date(ifModifiedSince));
      }

      // Extract the If-Unmodified-Since date
      long ifUnmodifiedSince = getRequestDateHeader(HEADER_IF_UNMODIFIED_SINCE);
      if(ifUnmodifiedSince != -1)
      {
         result.setUnmodifiedSince(new Date(ifUnmodifiedSince));
      }

      // Extract the If-Match tags
      List<Tag> match = null;
      String matchHeader = getRequestHeader(HEADER_IF_MATCH);
      if(matchHeader != null)
      {
         String[] tags = matchHeader.split(",");
         for (int i = 0; i < tags.length; i++)
         {
            if(match == null) 
            {
               match = new ArrayList<Tag>();
               result.setMatch(match);
            }
            
            match.add(new TagImpl(tags[i]));
         }
      }

      // Extract the If-None-Match tags
      List<Tag> noneMatch = null;
      String noneMatchHeader = getRequestHeader(HEADER_IF_NONE_MATCH);
      if(noneMatchHeader != null)
      {
         String[] tags = noneMatchHeader.split(",");
         for (int i = 0; i < tags.length; i++)
         {
            if(noneMatch == null) 
            {
               noneMatch = new ArrayList<Tag>();
               result.setNoneMatch(noneMatch);
            }
            
            noneMatch.add(new TagImpl(tags[i]));
         }
      }
      
      return result;
   }
   
}
