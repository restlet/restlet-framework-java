/*
 * Copyright 2005-2006 Jerome LOUVEL
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

import org.restlet.data.Parameter;

/**
 * HTTP header reader.
 */
public class HeaderReader
{
   /** The header to read. */
   protected String header;
   
   /** The current read index (or -1 if not reading anymore). */
   protected int index;
   
   /**
    * Constructor.
    * @param header The header to read.
    */
   public HeaderReader(String header)
   {
      this.header = header;
      this.index = ((header == null) || (header.length() == 0)) ? -1 : 0;
   }

   /**
    * Reads the next character.
    * @return The next character.
    */
   public int read()
   {
      int result = -1;
      
      if(index != -1)
      {
         result = this.header.charAt(index++);
         if(index >= this.header.length()) index = -1;
      }
      
      return result;
   }
   
   /**
    * Reads the next quoted string.
    * @return The next quoted string.
    * @throws IOException
    */
   protected String readQuotedString() throws IOException
   {
      StringBuilder sb = new StringBuilder();
      appendQuotedString(sb);
      return sb.toString();
   }

   /**
    * Appends the next quoted string.
    * @param buffer The buffer to append.
    * @throws IOException
    */
   protected void appendQuotedString(Appendable buffer) throws IOException
   {
      boolean done = false;
      boolean quotedPair = false;
      int nextChar = 0;

      while((!done) && (nextChar != -1))
      {
         nextChar = read();

         if(quotedPair)
         {
            // End of quoted pair (escape sequence)
            if(HeaderUtils.isText(nextChar))
            {
               buffer.append((char)nextChar);
               quotedPair = false;
            }
            else
            {
               throw new IOException("Invalid character detected in quoted string. Please check your value");
            }
         }
         else if(HeaderUtils.isDoubleQuote(nextChar))
         {
            // End of quoted string
            done = true;
         }
         else if(nextChar == '\\')
         {
            // Begin of quoted pair (escape sequence)
            quotedPair = true;
         }
         else if(HeaderUtils.isText(nextChar))
         {
            buffer.append((char)nextChar);
         }
         else
         {
            throw new IOException("Invalid character detected in quoted string. Please check your value");
         }
      }
   }

   /**
    * Creates a parameter.
    * @param name The parameter name buffer.
    * @param value The parameter value buffer (can be null).
    * @return The created parameter.
    * @throws IOException
    */
   public static Parameter createParameter(CharSequence name, CharSequence value) throws IOException
   {
      if(value != null)
      {
         return new Parameter(name.toString(), value.toString());
      }
      else
      {
         return new Parameter(name.toString(), null);
      }
   }

}
