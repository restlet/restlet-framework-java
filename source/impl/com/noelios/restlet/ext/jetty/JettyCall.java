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

package com.noelios.restlet.ext.jetty;

import java.util.ArrayList;
import java.util.List;

import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.restlet.data.CharacterSets;
import org.restlet.data.Cookies;
import org.restlet.data.MediaTypes;
import org.restlet.data.Method;
import org.restlet.data.Methods;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Representation;

import com.noelios.restlet.UniformCallImpl;
import com.noelios.restlet.data.MediaTypeImpl;
import com.noelios.restlet.data.MethodImpl;
import com.noelios.restlet.data.PreferenceImpl;
import com.noelios.restlet.data.PreferenceReaderImpl;
import com.noelios.restlet.data.ReferenceImpl;
import com.noelios.restlet.data.InputRepresentation;

/**
 * Call that is used by the Jetty HTTP server connector.
 */
public class JettyCall extends UniformCallImpl
{
   /**
    * Constructor.
    * @param request		The Jetty HTTP request.
    * @param response	The Jetty HTTP response.
    */
   public JettyCall(HttpRequest request, HttpResponse response)
   {
      super(getReferrer(request), request.getField("User-Agent"), getMediaPrefs(request), 
            getCharacterSetPrefs(request), getLanguagePrefs(request), getMethod(request), 
            getResource(request), getCookies(request), getInput(request));
   }

   /**
    * Extracts the call's referrer from the HTTP request.
    * @param request The Jetty HTTP request.
    * @return        The call's referrer.
    */
   private static Reference getReferrer(HttpRequest request)
   {
      String referrer = request.getField("Referer");
      
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
    * Extracts the call's resource from the HTTP request.
    * @param request The Jetty HTTP request.
    * @return        The call's resource.
    */
   private static Reference getResource(HttpRequest request)
   {
      String resource = request.getRootURL() + request.getURI().toString();
      
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
    * @param request	The Jetty HTTP request.
    * @return 			The call's method.
    */
   private static Method getMethod(HttpRequest request)
   {
      String method = request.getMethod();
      if (method.equals(HttpRequest.__DELETE))
         return Methods.DELETE;
      else if (method.equals(HttpRequest.__GET))
         return Methods.GET;
      else if (method.equals(HttpRequest.__POST))
         return Methods.POST;
      else if (method.equals(HttpRequest.__PUT))
         return Methods.PUT;
      else
         return new MethodImpl(method);
   }

   /**
    * Extracts the call's input representation from the HTTP request.
    * @param request	The Jetty HTTP request.
    * @return 			The call's input representation.
    */
   private static Representation getInput(HttpRequest request)
   {
      return new InputRepresentation(request.getInputStream(), new MediaTypeImpl(request.getContentType()));
   }

   /**
    * Extracts the call's media preferences from the HTTP request.
    * @param request	The Jetty HTTP request.
    * @return 			The call's media preferences.
    */
   private static List<Preference> getMediaPrefs(HttpRequest request)
   {
      List<Preference> result = null;
      String accept = request.getField(HttpFields.__Accept);

      if (accept != null)
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
    * @param request	The Jetty HTTP request.
    * @return 			The call's character set preferences.
    */
   private static List<Preference> getCharacterSetPrefs(HttpRequest request)
   {
      // Implementation according to
      // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.2
      List<Preference> result = null;
      String acceptCharset = request.getField(HttpFields.__AcceptCharset);

      if (acceptCharset != null)
      {
         if (acceptCharset.length() == 0)
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
    * @param request	The Jetty HTTP request.
    * @return 			The call's language preferences.
    */
   private static List<Preference> getLanguagePrefs(HttpRequest request)
   {
      List<Preference> result = null;
      String acceptLanguage = request.getField(HttpFields.__AcceptLanguage);

      if (acceptLanguage != null)
      {
         PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_LANGUAGE, acceptLanguage);
         result = pr.readPreferences();
      }

      return result;
   }

   /**
    * Extracts the call's cookies from the HTTP request.
    * @param request	The Jetty HTTP request.
    * @return 			The call's cookies.
    */
   private static Cookies getCookies(HttpRequest request)
   {
      Cookies result = null;

      if (request.getField(HttpFields.__Cookie) != null)
      {
         result = new com.noelios.restlet.data.CookiesImpl(request.getField(HttpFields.__Cookie));
      }

      return result;
   }

}

