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

package org.restlet.data;

import java.util.List;

/**
 * The data format of a representation.
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 */
public enum MediaTypes implements MediaType
{
   ALL, APPLICATION_ALL, APPLICATION_HTTP_COOKIES, APPLICATION_JAVA_OBJECT, APPLICATION_WWW_FORM, APPLICATION_XHTML_XML, APPLICATION_XML, IMAGE_ALL, IMAGE_GIF, IMAGE_ICON, IMAGE_PNG, TEXT_ALL, TEXT_CSS, TEXT_HTML, TEXT_URI, TEXT_XML;

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
         case APPLICATION_HTTP_COOKIES:
            result = "application/x-http-cookies"; // Guessed!
            break;
         case APPLICATION_JAVA_OBJECT:
            result = "application/x-java-serialized-object";
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
         case TEXT_URI:
            result = "text/uri";
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
   public String getSubtype()
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
         case APPLICATION_HTTP_COOKIES:
            result = "HTTP cookies"; // Guessed!
            break;
         case APPLICATION_JAVA_OBJECT:
            result = "Java serialized object";
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
         case TEXT_URI:
            result = "Uniform Resource Identifier";
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

}
