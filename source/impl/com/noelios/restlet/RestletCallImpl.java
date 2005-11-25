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

      // Set the absolute resource path as the initial path in the list.
      getPaths().add(0, getResourceUri().toString(false, false));
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
         return StringUtils.strip(getPaths().get(0), '/');
      }
      else
      {
         return getPaths().get(0);
      }
   }

}
