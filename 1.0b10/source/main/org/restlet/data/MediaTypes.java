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

import java.util.List;

/**
 * The data format of a representation.
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 */
public enum MediaTypes implements MediaType
{
   ALL,

   APPLICATION_ALL, APPLICATION_ATOM_XML, APPLICATION_HTTP_COOKIES, APPLICATION_JAVA_OBJECT,
   APPLICATION_PDF, APPLICATION_WORD, APPLICATION_WWW_FORM, APPLICATION_XHTML_XML, 
   APPLICATION_XML, APPLICATION_ZIP,

   IMAGE_ALL, IMAGE_GIF, IMAGE_ICON, IMAGE_PNG,

   TEXT_ALL, TEXT_CSS, TEXT_HTML, TEXT_PLAIN, TEXT_URI_LIST, TEXT_XML;

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
            result = "*/*";
            break;
         case APPLICATION_ALL:
            result = "application/*";
            break;
         case APPLICATION_ATOM_XML:
            result = "application/atom+xml";
            break;
         case APPLICATION_HTTP_COOKIES:
            result = "application/x-http-cookies"; // Guessed!
            break;
         case APPLICATION_JAVA_OBJECT:
            result = "application/x-java-serialized-object";
            break;
         case APPLICATION_PDF:
            result = "application/pdf";
            break;
         case APPLICATION_WORD:
            result = "application/msword";
            break;
         case APPLICATION_WWW_FORM:
            result = "application/x-www-form-urlencoded";
            break;
         case APPLICATION_XHTML_XML:
            result = "application/xhtml+xml";
            break;
         case APPLICATION_XML:
            result = "application/xml";
            break;
         case APPLICATION_ZIP:
            result = "application/zip";
            break;
         case IMAGE_ALL:
            result = "image/*";
            break;
         case IMAGE_GIF:
            result = "image/gif";
            break;
         case IMAGE_ICON:
            result = "image/x-icon";
            break;
         case IMAGE_PNG:
            result = "image/png";
            break;
         case TEXT_ALL:
            result = "text/*";
            break;
         case TEXT_CSS:
            result = "text/css";
            break;
         case TEXT_HTML:
            result = "text/html";
            break;
         case TEXT_PLAIN:
            result = "text/plain";
            break;
         case TEXT_URI_LIST:
            result = "text/uri-list";
            break;
         case TEXT_XML:
            result = "text/xml";
            break;
      }

      return result;
   }

   /**
    * Returns the main type.
    * @return The main type.
    */
   public String getMainType()
   {
      return getName().substring(0, getName().indexOf('/'));
   }

   /**
    * Returns the sub-type.
    * @return The sub-type.
    */
   public String getSubType()
   {
      int separator = getName().indexOf(';');

      if(separator == -1)
      {
         return getName().substring(getName().indexOf('/') + 1);
      }
      else
      {
         return getName().substring(getName().indexOf('/') + 1, separator);
      }
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
            result = "All media";
            break;
         case APPLICATION_ALL:
            result = "All application documents";
            break;
         case APPLICATION_ATOM_XML:
            result = "Atom syndication documents";
            break;
         case APPLICATION_HTTP_COOKIES:
            result = "HTTP cookies"; // Guessed!
            break;
         case APPLICATION_JAVA_OBJECT:
            result = "Java serialized object";
            break;
         case APPLICATION_PDF:
            result = "Adobe PDF document";
            break;
         case APPLICATION_WORD:
            result = "Microsoft Word document";
            break;
         case APPLICATION_WWW_FORM:
            result = "Web form (URL encoded)";
            break;
         case APPLICATION_XHTML_XML:
            result = "XHTML/XML application document";
            break;
         case APPLICATION_XML:
            result = "XML application document";
            break;
         case APPLICATION_ZIP:
            result = "ZIP archive";
            break;
         case IMAGE_ALL:
            result = "All images";
            break;
         case IMAGE_GIF:
            result = "GIF image";
            break;
         case IMAGE_ICON:
            result = "Favicon image";
            break;
         case IMAGE_PNG:
            result = "PNG image";
            break;
         case TEXT_ALL:
            result = "All texts";
            break;
         case TEXT_CSS:
            result = "CSS stylesheet";
            break;
         case TEXT_HTML:
            result = "HTML text document";
            break;
         case TEXT_PLAIN:
            result = "Plain text";
            break;
         case TEXT_URI_LIST:
            result = "List of Uniform Resource Identifiers";
            break;
         case TEXT_XML:
            result = "XML text";
            break;
      }

      return result;
   }

   /**
    * Returns the list of parameters.
    * @return The list of parameters.
    */
   public List<Parameter> getParameters()
   {
      return null;
   }

   /**
    * Returns the value of a parameter with a given name.
    * @param name The name of the parameter to return.
    * @return The value of the parameter with a given name.
    */
   public String getParameterValue(String name)
   {
      return null;
   }

   /**
    * Returns the media type name.
    * @return The media type name.
    */
   public String toString()
   {
      return getName();
   }

}
