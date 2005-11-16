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

package com.noelios.restlet.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.restlet.RestletException;
import org.restlet.data.Cookie;
import org.restlet.data.CookiesReader;
import org.restlet.data.Parameter;

/**
 * Default cookies reader implementation.
 */
public class CookiesReaderImpl extends HeaderReaderImpl implements CookiesReader
{
   private static final String NAME_VERSION = "$Version";
   private static final String NAME_PATH    = "$Path";
   private static final String NAME_DOMAIN  = "$Domain";

   /**
    * The cached pair.
    * Used by the readPair() method.
    */
   protected Parameter cachedPair;

   /** The global cookie specification version. */
   protected int globalVersion;

   /**
    * Constructor.
    * @param headerInputStream The cookies header stream.
    */
   public CookiesReaderImpl(InputStream headerInputStream)
   {
      super(headerInputStream);
      this.cachedPair = null;
      this.globalVersion = -1;
   }

   /**
    * Reads the cookies whose name is a key in the given map.
    * If a matching cookie is found, its value is put in the map.
    * @param cookies The cookies map controlling the reading.
    */
   public void readCookies(Map<String, Cookie> cookies) throws RestletException
   {
      Cookie cookie = readCookie();

      while (cookie != null)
      {
         if (cookies.containsKey(cookie.getName()))
         {
            cookies.put(cookie.getName(), cookie);
         }

         cookie = readCookie();
      }

      try
      {
         close();
      }
      catch (IOException ioe)
      {
         throw new RestletException("Error while closing the input stream", ioe);
      }
   }

   /**
    * Reads the next cookie available or null.
    * @return The next cookie available or null.
    */
   public Cookie readCookie() throws RestletException
   {
      Cookie result = null;
      Parameter pair = readPair();

      if (this.globalVersion == -1)
      {
         // Cookies version not yet detected
         if (pair.getName().equalsIgnoreCase(NAME_VERSION))
         {
            if (pair.getValue() != null)
            {
               this.globalVersion = Integer.parseInt(pair.getValue());
            }
            else
            {
               throw new RestletException("Empty cookies version attribute detected", "Please check your cookie header");
            }
         }
         else
         {
            // Set the default version for old Netscape cookies
            this.globalVersion = 0;
         }
      }

      while ((pair != null) && (pair.getName().charAt(0) == '$'))
      {
         // Unexpected special attribute
         // Silently ignore it as it may have been introduced by new specifications
         pair = readPair();
      }

      if (pair != null)
      {
         // Set the cookie name and value
         result = new CookieImpl(this.globalVersion, pair.getName(), pair.getValue());
         pair = readPair();
      }

      while ((pair != null) && (pair.getName().charAt(0) == '$'))
      {
         if (pair.getName().equalsIgnoreCase(NAME_PATH))
         {
            result.setPath(pair.getValue());
         }
         else if (pair.getName().equalsIgnoreCase(NAME_DOMAIN))
         {
            result.setDomain(pair.getValue());
         }
         else
         {
            // Unexpected special attribute
            // Silently ignore it as it may have been introduced by new specifications
         }

         pair = readPair();
      }

      if (pair != null)
      {
         // We started to read the next cookie
         // So let's put it back into the stream
         this.cachedPair = pair;
      }

      return result;
   }

   /**
    * Reads the next pair as a parameter.
    * @return The next pair as a parameter.
    */
   private Parameter readPair() throws RestletException
   {
      Parameter result = null;

      if (cachedPair != null)
      {
         result = cachedPair;
      }
      else
      {
         try
         {
            boolean readingName = true;
            boolean readingValue = false;
            StringBuilder nameBuffer = new StringBuilder();
            StringBuilder valueBuffer = new StringBuilder();

            int nextChar = 0;
            while ((result == null) && (nextChar != -1))
            {
               nextChar = read();

               if (readingName)
               {
                  if ((isSpace(nextChar)) && (nameBuffer.length() == 0))
                  {
                     // Skip spaces
                  }
                  else if ((nextChar == -1) || (nextChar == ';') || (nextChar == ','))
                  {
                     if (nameBuffer.length() > 0)
                     {
                        // End of pair with no value
                        result = createParameter(nameBuffer, null);
                     }
                     else if (nextChar == -1)
                     {
                        // Do nothing return null preference
                     }
                     else
                     {
                        throw new RestletException("Empty cookie name detected", "Please check your cookies");
                     }
                  }
                  else if (nextChar == '=')
                  {
                     readingName = false;
                     readingValue = true;
                  }
                  else if (isTokenChar(nextChar))
                  {
                     nameBuffer.append((char)nextChar);
                  }
                  else
                  {
                     throw new RestletException("Separator and control characters are not allowed within a token",
                         "Please check your cookie header");
                  }
               }
               else if (readingValue)
               {
                  if ((isSpace(nextChar)) && (valueBuffer.length() == 0))
                  {
                     // Skip spaces
                  }
                  else if ((nextChar == -1) || (nextChar == ';') || (nextChar == ','))
                  {
                     // End of pair
                     result = createParameter(nameBuffer, valueBuffer);
                  }
                  else if ((nextChar == '"') && (valueBuffer.length() == 0))
                  {
                     valueBuffer.append(readQuotedString());
                  }
                  else if (isTokenChar(nextChar))
                  {
                     valueBuffer.append((char)nextChar);
                  }
                  else
                  {
                     throw new RestletException("Separator and control characters are not allowed within a token",
                         "Please check your cookie header");
                  }
               }
            }
         }
         catch (UnsupportedEncodingException uee)
         {
            throw new RestletException("Unsupported encoding", "Please contact the administrator");
         }
         catch (IOException ioe)
         {
            throw new RestletException("Unexpected I/O exception", "Please contact the administrator");
         }
      }

      return result;
   }

}

