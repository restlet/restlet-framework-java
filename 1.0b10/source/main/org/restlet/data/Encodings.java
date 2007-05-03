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

package org.restlet.data;

/**
 * Enumeration of common encodings.
 */
public enum Encodings implements Encoding
{
   /** All encodings acceptable. */
   ALL,

   /** The GNU Zip encoding. */
   GZIP,

   /** The common Unix file compression. */
   COMPRESS,

   /** The zlib format defined by RFC 1950 and 1951. */
   DEFLATE,

   /** The default (identity) encoding. */
   IDENTITY;

   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
    */
   public String getName()
   {
      String result = null;

      switch(this)
      {
         case ALL:
            result = "*";
            break;
         case GZIP:
            result = "gzip";
            break;
         case COMPRESS:
            result = "compress";
            break;
         case DEFLATE:
            result = "deflate";
            break;
         case IDENTITY:
            result = "identity";
            break;
      }

      return result;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      String result = null;

      switch(this)
      {
         case ALL:
            result = "All encodings";
            break;
         case GZIP:
            result = "GZip compression";
            break;
         case COMPRESS:
            result = "Common Unix compression";
            break;
         case DEFLATE:
            result = "Deflate compression using the zlib format";
            break;
         case IDENTITY:
            result = "The default encoding with no transformation";
            break;
      }

      return result;
   }

   /**
    * Indicates if the encoding is equal to a given one.
    * @param encoding The encoding to compare to.
    * @return True if the encoding is equal to a given one.
    */
   public boolean equals(Encoding encoding)
   {
      return getName().equalsIgnoreCase(encoding.getName());
   }
   
}
