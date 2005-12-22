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

package com.noelios.restlet;

import java.util.ArrayList;
import java.util.List;

import org.restlet.RestletCall;
import org.restlet.UniformCall;
import org.restlet.UniformCallWrapper;

import com.noelios.restlet.util.StringUtils;

/**
 * Default restlet call implementation.
 */
public class RestletCallImpl extends UniformCallWrapper implements RestletCall
{
   /** The list of matches. */
   List<String> matches;

   /** The list of paths. */
   List<String> paths;
   
   /**
    * Constructor.
    * @param call The inform call to wrap.
    */
   public RestletCallImpl(UniformCall call)
   {
      super(call);

      // Creates the list of paths
      this.paths = new ArrayList<String>();

      // Creates the list of matches
      this.matches = new ArrayList<String>();

      // Set the absolute resource path as the initial path in the list.
      getPaths().add(0, getResourceRef().toString(false, false));
   }

   /**
    * Returns the list of substring matched in the current restlet's path.
    * @return The list of substring matched.
    * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Matcher.html#group(int)">Matcher.group()</a>
    */
   public List<String> getMatches()
   {
      return this.matches;
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
         return StringUtils.strip(getPaths().get(index), '/');
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

}
