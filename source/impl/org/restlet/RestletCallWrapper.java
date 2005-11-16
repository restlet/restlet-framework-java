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

package org.restlet;

import java.util.ArrayList;
import java.util.List;

/**
 * Restlet call wrapper.
 * Useful for application developer who need to enrich the call with application related things.
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 */
public class RestletCallWrapper extends UniformCallWrapper implements RestletCall
{
   /** The list of paths. */
   List<String> paths;

   /**
    * Constructor.
    * @param wrappedCall The wrapped call
    */
   public RestletCallWrapper(UniformCall wrappedCall)
   {
      super(wrappedCall);
      this.paths = new ArrayList<String>();
      
      // Set the absolute resource path as the initial path in the list.
      getPaths().add(0, getResourceUri().toString(false, false));
   }

   /**
    * Constructor.
    * @param wrappedCall The wrapped call
    */
   public RestletCallWrapper(RestletCall wrappedCall)
   {
      super(wrappedCall);
      this.paths = wrappedCall.getPaths();
   }

   /**
    * Returns one of the paths in the list.
    * The first path is the resource path relatively to the current restlet.
    * The second path is the current reslet path relatively to the parent restlet.
    * All the hierarchy of restlet paths is also available depending on the restlet tree.
    * @param index   Index of the path in the list.
    * @param strip   Indicates if leading and ending slashes should be stripped.
    * @return        The path at the given index.
    */
   public String getPath(int index, boolean strip)
   {
      if(strip)
      {
         return strip(getPaths().get(index));
      }
      else
      {
         return getPaths().get(index);
      }
   }

   /**
    * Returns the list of restlets paths.  
    * The list is sorted according to the handlers hierarchy.
    * @return The list of restlets paths.
    */
   public List<String> getPaths()
   {
      return this.paths;
   }

   /**
    * Strip the slashing from both ends of the source string.
    * @param source  The source string to strip.
    * @return        The stripped string.
    */
   public static String strip(String source)
   {
      return strip(source, '/', true, true);      
   }
   
   /**
    * Strip a delimiter character from a source string. 
    * @param source     The source string to strip.
    * @param delimiter  The character to remove.
    * @param start      Indicates if start of source should be stripped.
    * @param end        Indicates if end of source should be stripped.
    * @return           The stripped source string.
    */
   public static String strip(String source, char delimiter, boolean start, boolean end)
   {
      int beginIndex = 0;
      int endIndex = source.length();
      boolean stripping = true;

      // Strip beginning
      while(stripping && (beginIndex < endIndex))
      {
         if(source.charAt(beginIndex) == delimiter) 
         {
            beginIndex++;
         }
         else
         {
            stripping = false;
         }
      }

      // Strip end
      stripping = true;
      while(stripping && (beginIndex < endIndex - 1))
      {
         if(source.charAt(endIndex - 1) == delimiter) 
         {
            endIndex--;
         }
         else
         {
            stripping = false;
         }
      }
      
      return source.substring(beginIndex, endIndex);
   }

}




