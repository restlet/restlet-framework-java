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
