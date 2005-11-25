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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.restlet.RestletException;

/**
 * Object instance reader. Based on Java object serialization.
 */
public class ObjectReader extends BufferedInputStream
{
   /**
    * Constructor.
    * @param objectStream The object stream to deserialize.
    * @throws RestletException
    */
   public ObjectReader(InputStream objectStream) throws RestletException
   {
      super(objectStream);
   }

   /**
    * Returns the representation as a Java object.
    * @return The representation as a Java object.
    * @throws RestletException
    */
   public Object readObject() throws RestletException
   {
      Object result = null;

      try
      {
         ObjectInputStream ois = new ObjectInputStream(this);
         result = ois.readObject();
         ois.close();
      }
      catch(IOException ioe)
      {
         throw new RestletException("Unable to recreate Java object", ioe);
      }
      catch(ClassNotFoundException cnfe)
      {
         throw new RestletException("Unable to recreate Java object", cnfe);
      }

      return result;
   }

}
