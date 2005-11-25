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

import org.restlet.AbstractRestlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.component.RestletContainer;
import org.restlet.data.MediaType;
import org.restlet.data.Statuses;

import com.noelios.restlet.data.FileRepresentation;

/**
 * Restlet supported by a single file.
 */
public class FileRestlet extends AbstractRestlet
{
   /** The file's path. */
   private String filePath;

   /** The file's media type. */
   private MediaType mediaType;

   /**
    * Constructor.
    * @param container The parent container.
    * @param filePath The file's path.
    * @param mediaType The file's media type.
    */
   public FileRestlet(RestletContainer container, String filePath, MediaType mediaType)
   {
      super(container);
      this.filePath = filePath;
      this.mediaType = mediaType;
   }

   /**
    * Returns the file's media type.
    * @return The file's media type.
    */
   public MediaType getMediaType()
   {
      return mediaType;
   }

   /**
    * Returns the file's path.
    * @return The file's path.
    */
   public String getPath()
   {
      return filePath;
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "File Restlet";
   }

   /**
    * Handles a REST call.
    * @param call The call to handle.
    */
   public void handle(RestletCall call) throws RestletException
   {
      if(call.getPath(0, false).equals(""))
      {
         call.setOutput(new FileRepresentation(getPath(), getMediaType()));
      }
      else
      {
         call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }
   }
}
