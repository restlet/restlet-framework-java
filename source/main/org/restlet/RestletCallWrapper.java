/*
 * Copyright 2005 Jérôme LOUVEL
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

package org.restlet;

import java.util.List;

/**
 * Restlet call wrapper. Useful for application developer who need to enrich the call with application related
 * things.
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
   public RestletCallWrapper(RestletCall wrappedCall)
   {
      super(wrappedCall);
      this.paths = wrappedCall.getPaths();
   }

   /**
    * Returns one of the paths in the list. The first path is the resource path relatively to the current
    * restlet. The second path is the current reslet path relatively to the parent restlet. All the hierarchy
    * of restlet paths is also available depending on the restlet tree.
    * @param index Index of the path in the list.
    * @param strip Indicates if leading and ending slashes should be stripped.
    * @return The path at the given index.
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
    * Returns the list of restlets paths. The list is sorted according to the handlers hierarchy.
    * @return The list of restlets paths.
    */
   public List<String> getPaths()
   {
      return this.paths;
   }

   /**
    * Strip the slashing from both ends of the source string.
    * @param source The source string to strip.
    * @return The stripped string.
    */
   public static String strip(String source)
   {
      return strip(source, '/', true, true);
   }

   /**
    * Strip a delimiter character from a source string.
    * @param source The source string to strip.
    * @param delimiter The character to remove.
    * @param start Indicates if start of source should be stripped.
    * @param end Indicates if end of source should be stripped.
    * @return The stripped source string.
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
