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

package com.noelios.restlet;

import org.restlet.AbstractHandler;
import org.restlet.UniformCall;
import org.restlet.component.RestletContainer;
import org.restlet.data.MediaType;
import org.restlet.data.Statuses;

import com.noelios.restlet.data.FileRepresentation;

/**
 * Restlet supported by a single file.
 */
public class FileRestlet extends AbstractHandler
{
   /** The file's path. */
   private String filePath;

   /** The file's media type. */
   private MediaType mediaType;
   
   /** Indicates the time to live for a file representation before it expires (in seconds; default to 10 minutes). */
   protected int timeToLive;

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
      this.timeToLive = 600;
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
    * Returns the time to live for a file representation before it expires (in seconds).
    * @return The time to live for a file representation before it expires (in seconds).
    */
   public int getTimeToLive()
   {
      return this.timeToLive;
   }

   /**
    * Sets the time to live for a file representation before it expires (in seconds).
    * @param ttl The time to live for a file representation before it expires (in seconds).
    */
   public void setTimeToLive(int ttl)
   {
      this.timeToLive = ttl;
   }

   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(UniformCall call)
   {
      if(call.getResourcePath().equals(""))
      {
         call.setOutput(new FileRepresentation(getPath(), getMediaType(), getTimeToLive()));
      }
      else
      {
         call.setStatus(Statuses.CLIENT_ERROR_NOT_FOUND);
      }
   }
}
