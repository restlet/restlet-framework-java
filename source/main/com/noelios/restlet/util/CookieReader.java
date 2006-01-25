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

package com.noelios.restlet.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.restlet.data.Cookie;
import org.restlet.data.Parameter;

import com.noelios.restlet.data.CookieImpl;

/**
 * Cookie header reader.
 */
public class CookieReader extends HeaderReader
{
   private static final String NAME_VERSION = "$Version";
   private static final String NAME_PATH = "$Path";
   private static final String NAME_DOMAIN = "$Domain";

   /**
    * The cached pair. Used by the readPair() method.
    */
   protected Parameter cachedPair;

   /** The global cookie specification version. */
   protected int globalVersion;

   /**
    * Constructor.
    * @param header The header to read.
    */
   public CookieReader(String header)
   {
      super(header);
      this.cachedPair = null;
      this.globalVersion = -1;
   }

   /**
    * Reads the next cookie available or null.
    * @return The next cookie available or null.
    */
   public Cookie readNextCookie() throws IOException
   {
      Cookie result = null;
      Parameter pair = readPair();

      if(this.globalVersion == -1)
      {
         // Cookies version not yet detected
         if(pair.getName().equalsIgnoreCase(NAME_VERSION))
         {
            if(pair.getValue() != null)
            {
               this.globalVersion = Integer.parseInt(pair.getValue());
            }
            else
            {
               throw new IOException(
                     "Empty cookies version attribute detected. Please check your cookie header");
            }
         }
         else
         {
            // Set the default version for old Netscape cookies
            this.globalVersion = 0;
         }
      }

      while((pair != null) && (pair.getName().charAt(0) == '$'))
      {
         // Unexpected special attribute
         // Silently ignore it as it may have been introduced by new
         // specifications
         pair = readPair();
      }

      if(pair != null)
      {
         // Set the cookie name and value
         result = new CookieImpl(this.globalVersion, pair.getName(), pair.getValue());
         pair = readPair();
      }

      while((pair != null) && (pair.getName().charAt(0) == '$'))
      {
         if(pair.getName().equalsIgnoreCase(NAME_PATH))
         {
            result.setPath(pair.getValue());
         }
         else if(pair.getName().equalsIgnoreCase(NAME_DOMAIN))
         {
            result.setDomain(pair.getValue());
         }
         else
         {
            // Unexpected special attribute
            // Silently ignore it as it may have been introduced by new
            // specifications
         }

         pair = readPair();
      }

      if(pair != null)
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
    * @throws IOException
    */
   private Parameter readPair() throws IOException
   {
      Parameter result = null;

      if(cachedPair != null)
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
            while((result == null) && (nextChar != -1))
            {
               nextChar = read();

               if(readingName)
               {
                  if((HeaderUtils.isSpace(nextChar)) && (nameBuffer.length() == 0))
                  {
                     // Skip spaces
                  }
                  else if((nextChar == -1) || (nextChar == ';') || (nextChar == ','))
                  {
                     if(nameBuffer.length() > 0)
                     {
                        // End of pair with no value
                        result = createParameter(nameBuffer, null);
                     }
                     else if(nextChar == -1)
                     {
                        // Do nothing return null preference
                     }
                     else
                     {
                        throw new IOException("Empty cookie name detected. Please check your cookies");
                     }
                  }
                  else if(nextChar == '=')
                  {
                     readingName = false;
                     readingValue = true;
                  }
                  else if(HeaderUtils.isTokenChar(nextChar))
                  {
                     nameBuffer.append((char)nextChar);
                  }
                  else
                  {
                     throw new IOException(
                           "Separator and control characters are not allowed within a token. Please check your cookie header");
                  }
               }
               else if(readingValue)
               {
                  if((HeaderUtils.isSpace(nextChar)) && (valueBuffer.length() == 0))
                  {
                     // Skip spaces
                  }
                  else if((nextChar == -1) || (nextChar == ';') || (nextChar == ','))
                  {
                     // End of pair
                     result = createParameter(nameBuffer, valueBuffer);
                  }
                  else if((nextChar == '"') && (valueBuffer.length() == 0))
                  {
                     valueBuffer.append(readQuotedString());
                  }
                  else if(HeaderUtils.isTokenChar(nextChar))
                  {
                     valueBuffer.append((char)nextChar);
                  }
                  else
                  {
                     throw new IOException(
                           "Separator and control characters are not allowed within a token. Please check your cookie header");
                  }
               }
            }
         }
         catch(UnsupportedEncodingException uee)
         {
            throw new IOException("Unsupported encoding. Please contact the administrator");
         }
      }

      return result;
   }

}
