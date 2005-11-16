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

package com.noelios.restlet.data;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.restlet.RestletException;
import org.restlet.data.MediaTypeEnum;

/**
 * Object instance representation.
 * Based on Java object serialization.
 */
public class ObjectRepresentation extends OutputRepresentation
{
   /** The represented object. */
   private Object object;

   /**
    * Constructor;
    * @param object The represented object.
    */
   public ObjectRepresentation(Object object)
   {
      super(MediaTypeEnum.APPLICATION_JAVA_OBJECT);
      this.object = object;
   }

   /**
    * Writes the datum as a stream of bytes.
    * @param outputStream The stream to use when writing.
    */
   public void write(OutputStream outputStream) throws RestletException
   {
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(outputStream);
         oos.writeObject(getObject());
         oos.close();
      }
      catch (IOException ioe)
      {
         throw new RestletException("Unable to recreate Java object", ioe);
      }
   }

   /**
    * Returns the represented object.
    * @return The represented object.
    */
   public Object getObject() throws RestletException
   {
      return this.object;
   }

}




