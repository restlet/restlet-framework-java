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

package com.noelios.restlet.data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.restlet.data.Parameter;

/**
 * Abstract header reader.
 */
public abstract class HeaderReaderImpl extends BufferedInputStream
{
   /**
    * Constructor.
    * @param headerInputStream The header stream to read.
    */
   public HeaderReaderImpl(InputStream headerInputStream)
   {
      super(headerInputStream);
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
            if(isText(nextChar))
            {
               buffer.append((char)nextChar);
               quotedPair = false;
            }
            else
            {
               throw new IOException("Invalid character detected in quoted string. Please check your value");
            }
         }
         else if(isDoubleQuote(nextChar))
         {
            // End of quoted string
            done = true;
         }
         else if(nextChar == '\\')
         {
            // Begin of quoted pair (escape sequence)
            quotedPair = true;
         }
         else if(isText(nextChar))
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
         return new ParameterImpl(name.toString().toLowerCase(), value.toString());
      }
      else
      {
         return new ParameterImpl(name.toString().toLowerCase(), null);
      }
   }

   /**
    * Indicates if the given character is in ASCII range.
    * @param character The character to test.
    * @return True if the given character is in ASCII range.
    */
   public static boolean isAsciiChar(int character)
   {
      return (character >= 0) && (character <= 127);
   }

   /**
    * Indicates if the given character is upper case (A-Z).
    * @param character The character to test.
    * @return True if the given character is upper case (A-Z).
    */
   public static boolean isUpperCase(int character)
   {
      return (character >= 'A') && (character <= 'Z');
   }

   /**
    * Indicates if the given character is lower case (a-z).
    * @param character The character to test.
    * @return True if the given character is lower case (a-z).
    */
   public static boolean isLowerCase(int character)
   {
      return (character >= 'a') && (character <= 'z');
   }

   /**
    * Indicates if the given character is alphabetical (a-z or A-Z).
    * @param character The character to test.
    * @return True if the given character is alphabetical (a-z or A-Z).
    */
   public static boolean isAlpha(int character)
   {
      return isUpperCase(character) || isLowerCase(character);
   }

   /**
    * Indicates if the given character is a digit (0-9).
    * @param character The character to test.
    * @return True if the given character is a digit (0-9).
    */
   public static boolean isDigit(int character)
   {
      return (character >= '0') && (character <= '9');
   }

   /**
    * Indicates if the given character is a control character.
    * @param character The character to test.
    * @return True if the given character is a control character.
    */
   public static boolean isControlChar(int character)
   {
      return ((character >= 0) && (character <= 31)) || (character == 127);
   }

   /**
    * Indicates if the given character is a carriage return.
    * @param character The character to test.
    * @return True if the given character is a carriage return.
    */
   public static boolean isCarriageReturn(int character)
   {
      return (character == 13);
   }

   /**
    * Indicates if the given character is a line feed.
    * @param character The character to test.
    * @return True if the given character is a line feed.
    */
   public static boolean isLineFeed(int character)
   {
      return (character == 10);
   }

   /**
    * Indicates if the given character is a space.
    * @param character The character to test.
    * @return True if the given character is a space.
    */
   public static boolean isSpace(int character)
   {
      return (character == 32);
   }

   /**
    * Indicates if the given character is an horizontal tab.
    * @param character The character to test.
    * @return True if the given character is an horizontal tab.
    */
   public static boolean isHorizontalTab(int character)
   {
      return (character == 9);
   }

   /**
    * Indicates if the given character is a double quote.
    * @param character The character to test.
    * @return True if the given character is a double quote.
    */
   public static boolean isDoubleQuote(int character)
   {
      return (character == 34);
   }

   /**
    * Indicates if the given character is textual (ASCII and not a control character).
    * @param character The character to test.
    * @return True if the given character is textual (ASCII and not a control character).
    */
   public static boolean isText(int character)
   {
      return isAsciiChar(character) && !isControlChar(character);
   }

   /**
    * Indicates if the given character is a separator.
    * @param character The character to test.
    * @return True if the given character is a separator.
    */
   public static boolean isSeparator(int character)
   {
      switch(character)
      {
         case '(':
         case ')':
         case '<':
         case '>':
         case '@':
         case ',':
         case ';':
         case ':':
         case '\\':
         case '"':
         case '/':
         case '[':
         case ']':
         case '?':
         case '=':
         case '{':
         case '}':
         case ' ':
         case '\t':
            return true;

         default:
            return false;
      }
   }

   /**
    * Indicates if the given character is a token character (text and not a separator).
    * @param character The character to test.
    * @return True if the given character is a token character (text and not a separator).
    */
   public static boolean isTokenChar(int character)
   {
      return isText(character) && !isSeparator(character);
   }

}
